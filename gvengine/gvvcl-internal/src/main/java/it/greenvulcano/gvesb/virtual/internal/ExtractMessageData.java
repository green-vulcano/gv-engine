/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.internal;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>ExtractMessageData</code> operation, extract data from the message
 * body and put them into properties. This functionality is useful in
 * order to perform content based routing based on message body.
 * <p/>
 * The data can be extracted by XPath expressions (if the body is a valid XML),
 * or by regular expressions (if the body is an ASCII message). It is possible
 * to apply XPath or regular expressions on a portion of the body message.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */

public class ExtractMessageData implements CallOperation
{
  
    /**
     *
     */
    public static final int     XML             = 1;
    /**
     *
     */
    public static final int     ASCII           = 2;

    private OperationKey        key             = null;
    private List<ExtractData>   expressions     = new LinkedList<ExtractData>();
    private int                 messageType     = 0;
    private int                 beginIndex;
    private int                 bufferLength;
    private String              encoding;

    private DocumentBuilder     documentBuilder = null;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        try {
            NodeList list = XMLConfig.getNodeList(node, "extract-data");
            for (int i = 0; i < list.getLength(); ++i) {
                ExtractData extractData = new ExtractData(list.item(i));
                expressions.add(extractData);
            }

            String messageTypeStr = XMLConfig.get(node, "@message-type", "XML");
            if (messageTypeStr.equals("XML")) {
                messageType = XML;

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(false);
                dbf.setNamespaceAware(true);
                documentBuilder = dbf.newDocumentBuilder();
                documentBuilder.setEntityResolver(new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
                    {
                        return new InputSource(new StringReader(""));
                    }
                });
            }
            else if (messageTypeStr.equals("ASCII")) {
                messageType = ASCII;
            }

            beginIndex = XMLConfig.getInteger(node, "@begin-index", 0);
            bufferLength = XMLConfig.getInteger(node, "@length", -1);

            encoding = XMLConfig.get(node, "@encoding", null);
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_EXTRACTDATA_INITIALIZATION_ERROR", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer data) throws ConnectionException, CallException, InvalidDataException
    {
        try {
            byte[] buffer = extractBuffer(data);

            switch (messageType) {
                case XML :
                    processXMLMessage(buffer, data);
                    break;
                case ASCII :
                    processASCIIMessage(buffer, data);
                    break;
            }
            return data;
        }
        catch (Exception exc) {
            throw new InvalidDataException("GVVCL_EXTRACTDATA_PERFORM_ERROR", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#destroy()
     */
    public void destroy()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getServiceAlias(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public String getServiceAlias(GVBuffer data)
    {
        return data.getService();
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#setKey(it.greenvulcano.gvesb.virtual.OperationKey)
     */
    public void setKey(OperationKey key)
    {
        this.key = key;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#getKey()
     */
    public OperationKey getKey()
    {
        return key;
    }

    private byte[] extractBuffer(GVBuffer gvBuffer)
    {
        byte[] data = (byte[]) gvBuffer.getObject();
        int endIndex = data.length;

        if (bufferLength != -1) {
            endIndex = beginIndex + bufferLength;
            if (endIndex > data.length) {
                endIndex = data.length;
            }
        }

        if (endIndex < beginIndex) {
            return new byte[0];
        }

        if ((beginIndex == 0) && (endIndex == data.length)) {
            return data;
        }

        int size = endIndex - beginIndex;
        byte[] ret = new byte[size];

        System.arraycopy(data, beginIndex, ret, 0, size);

        return ret;
    }

    private void processXMLMessage(byte[] buffer, GVBuffer gvBuffer) throws Exception
    {
        XMLUtils xu = null;
        try {
            xu = XMLUtils.getParserInstance();
            Node data = documentBuilder.parse(new ByteArrayInputStream(buffer));

            for (ExtractData extractData : expressions) {
                String val = xu.get(data, extractData.getExpression(), "");
                gvBuffer.setProperty(extractData.getField(), val);
            }
        }
        finally {
            if (xu != null) {
                XMLUtils.releaseParserInstance(xu);
            }
        }
    }

    private void processASCIIMessage(byte[] buffer, GVBuffer gvBuffer) throws Exception
    {
        String message = null;
        if (encoding == null) {
            message = new String(buffer);
        }
        else {
            message = new String(buffer, encoding);
        }

        for (ExtractData extractData : expressions) {
            Pattern pattern = extractData.getPattern();
            if (pattern == null) {
                pattern = Pattern.compile(extractData.getExpression());
                extractData.setPattern(pattern);
            }
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String val = matcher.group(extractData.getMatchingGroup());
                gvBuffer.setProperty(extractData.getField(), val);
            }
            else {
                gvBuffer.setProperty(extractData.getField(), "");
            }
        }
    }
}
