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
package it.greenvulcano.gvesb.virtual.internal.xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.CallOperation;
import it.greenvulcano.gvesb.virtual.ConnectionException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.InvalidDataException;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.ErrHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * XMLValidationCallOperation class
 *
 * @version 3.0.0 Jun 1, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XMLValidationCallOperation implements CallOperation
{

    private static final org.slf4j.Logger logger               = org.slf4j.LoggerFactory.getLogger(XMLValidationCallOperation.class);

    /**
     * The operation key.
     */
    protected OperationKey      key                  = null;

    /**
     * The default XSD will be used also if a valid XSD is specified into the
     * document to be validated.
     */
    public static final String  FORCE_DEFAULT        = "force-default";

    /**
     * The default XSD will be used only if the document to be validated not
     * contains an XSD declaration or the XSD specified is not found in the XSD
     * repository.
     */
    public static final String  NULL_XSD             = "null-xsd";

    /**
     * XMLSchema validation feature name.
     */
    private static final String JAXP_SCHEMA_SOURCE   = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    /**
     * XMLSchema validation feature name.
     */
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * Used to parse a DOM (with standard or non-standard validation) from a
     * given XML string.
     */
    private DocumentBuilderFactory   docBuilderFactory;

    private String              defaultXSD;

    private boolean             returnDOM;

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws InitializationException
    {
        if (node == null) {
            throw new InitializationException("GVCORE_VCL_XMLVALID_INIT_ERROR", new String[][]{{"node", null}});
        }
        try {
            returnDOM = XMLConfig.getBoolean(node, "@return-dom", false);
            logger.debug("Return DOM: " + returnDOM);

            defaultXSD = XMLConfig.get(node, "@default-xsd");
            if (!PropertiesHandler.isExpanded(defaultXSD)) {
                try {
                    defaultXSD = PropertiesHandler.expand(defaultXSD);
                }
                catch (Exception exc) {
                    logger.warn("Cannot expand placeholder: " + defaultXSD, exc);
                }
            }
            defaultXSD = getXSDPath(defaultXSD);
            logger.debug("Default XSD: " + defaultXSD);

            initDocBuilderFactory();

        }
        catch (Exception e) {
            throw new InitializationException("GVCORE_VCL_XMLVALID_INIT_ERROR", new String[][]{{"node",
                    node.getLocalName()}}, e);
        }
    }

    /**
     * Executes the operation.
     *
     * @see it.greenvulcano.gvesb.virtual.CallOperation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException
    {
        if (gvBuffer == null) {
            return null;
        }
        try {
            GVBuffer gvBufferClone = new GVBuffer(gvBuffer);
            validateXML(gvBufferClone);
            if (returnDOM) {
                return gvBufferClone;
            }
            return gvBuffer;
        }
        catch (Exception e) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                    new String[][]{{"service", gvBuffer.getService()}, {"system", gvBuffer.getSystem()},
                            {"id", gvBuffer.getId().toString()}, {"message", e.getMessage()}}, e);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.Operation#cleanUp()
     */
    public void cleanUp()
    {
        // Do nothing
    }

    /**
     * Called when an operation is discarded from cache.
     */
    public void destroy()
    {
        // Do nothing
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

    /**
     * Return the alias for the given service
     *
     * @param gvBuffer
     *        the input service data
     * @return the configured alias
     */
    public String getServiceAlias(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * Initialize DocumentBuilderFactory.
     *
     * @param defaultXSD
     *        default XSD
     * @param xsdPolicy
     *        XSD policy
     *
     * @throws ParserConfigurationException
     *         on errors
     */
    private void initDocBuilderFactory() throws ParserConfigurationException
    {
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        docBuilderFactory.setValidating(true);
        docBuilderFactory.setIgnoringComments(true);
        docBuilderFactory.setIgnoringElementContentWhitespace(true);

        // Enables XMLSchema validation
        try {
            docBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        }
        catch (IllegalArgumentException exc) {
            // Underlying parser doesn't support Jaxp 1.2
            throw exc;
        }

        if ((defaultXSD != null) && (defaultXSD.trim().length() > 0)) {
            docBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, defaultXSD);
        }
    }

    /**
     * Initialize DocumentBuilderFactory.
     *
     * @param defaultXSD
     *        default XSD
     * @param xsdPolicy
     *        XSD policy
     *
     * @throws ParserConfigurationException
     *         on errors
     */
    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilder theDocBuilder = docBuilderFactory.newDocumentBuilder();
        theDocBuilder.setErrorHandler(new ErrHandler());
        return theDocBuilder;
    }

    /**
     * Validates the XML.
     *
     * @param data
     *        the data
     * @throws IOException
     *         if an error occurs.
     * @throws SAXException
     *         if an error occurs.
     * @throws URISyntaxException
     * @throws XMLUtilsException
     */
    private void validateXML(GVBuffer gvBuffer) throws Exception
    {
        Object object = gvBuffer.getObject();
        if (object == null) {
            throw new SAXException("null document");
        }

        byte[] barr = null;
        if (object instanceof Node) {
            barr = XMLUtils.serializeDOMToByteArray_S((Node) object);
        }
        else {
            if (object instanceof byte[]) {
                barr = (byte[]) object;
            }
            else {
                barr = object.toString().getBytes();
            }
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(barr);
        InputSource xmlSource = new InputSource(bais);
        object = getDocumentBuilder().parse(xmlSource);
        gvBuffer.setObject(object);
    }

    private String getXSDPath(String xsdFile) throws Exception
    {
        if (xsdFile == null) {
            return null;
        }
        String xsdResource = "xsds/" + xsdFile;

        logger.debug("looking in classpath for " + xsdResource);

        URL url = XMLValidationCallOperation.class.getClassLoader().getResource(xsdResource);

        if (url == null) {
            xsdResource =  PropertiesHandler.expand(xsdFile);
            logger.debug("looking in filesystem for " + xsdResource);

            File xsd = new File(xsdResource);
            if (xsd.exists() && xsd.isFile()) {
                return xsd.toURI().toString();
            }
        }
        else {
            return url.toURI().toString();
        }

        throw new IOException("XSD " + defaultXSD + " not found");
    }
}
