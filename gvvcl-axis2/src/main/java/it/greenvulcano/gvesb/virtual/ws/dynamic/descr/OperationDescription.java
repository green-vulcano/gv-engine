/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.virtual.ws.dynamic.descr;

import it.greenvulcano.gvesb.virtual.ws.dynamic.Constants;

import java.util.Iterator;
import java.util.Map;

import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.xml.namespace.QName;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class OperationDescription implements Description<ParamDescription>
{
    private static final long             serialVersionUID   = 300L;
    private String                        encoding;
    private SOAPHeader                    header;
    private SOAP12Header                  soap12header;
    private QName                         inputMessageQN;
    private QName                         operationQN;
    private QName                         outputMessageQN;
    private Map<String, ParamDescription> headerDescriptions;
    private Map<String, ParamDescription> inputDescriptions;
    private Map<String, ParamDescription> outputDescriptions;
    private String                        style;
    private boolean                       wrapped;
    private String[]                      headerParts;
    private String[]                      inputParts;
    private String[]                      outputParts;
    private String                        soapAction;
    private boolean                       useSOAP12Namespace = false;
    private String                        verb               = null;
    private String                        mediaType;

    /**
     * Creates a new instance of OperationDescription
     */
    public OperationDescription()
    {
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String separator = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        sb.append("$OPERATION: ").append(separator);
        sb.append("OperationName: ").append(operationQN).append(separator);
        sb.append("InputMessageName: ").append(inputMessageQN).append(separator);
        sb.append("OutputMessageName: ").append(outputMessageQN).append(separator);
        sb.append("Style: ").append(style).append(separator);
        sb.append("Encoding: ").append(encoding).append(separator);

        if (wrapped) {
            sb.append("Wrapped: ").append(wrapped).append(separator);
        }
        if (verb != null) {
            sb.append("REST Verb: ").append(verb).append(separator);
        }

        sb.append("$METHOD HEADERS: ");

        if ((headerDescriptions == null) || (headerDescriptions.size() == 0)) {
            sb.append(separator).append("$PARAMETER: VOID");
        }
        else {
            printMap(sb, separator, headerDescriptions);
        }

        sb.append("$METHOD ARGUMENTS: ");

        if ((inputDescriptions == null) || (inputDescriptions.size() == 0)) {
            sb.append(separator).append("$PARAMETER: VOID");
        }
        else {
            printMap(sb, separator, inputDescriptions);
        }

        sb.append(separator);
        sb.append("$METHOD RESULT: ");

        if ((outputDescriptions == null) || (outputDescriptions.size() == 0)) {
            sb.append(separator).append("$PARAMETER: VOID");
        }
        else {
            printMap(sb, separator, outputDescriptions);
        }

        // for formating with the DynamicInvoker document we need a new line
        sb.append(separator).append(Constants.LINE_SEPARATOR);

        return sb.toString();
    }

    private void printMap(StringBuilder sb, String separator, Map<?, ?> map)
    {
        Iterator<?> itr = map.values().iterator();
        while (itr.hasNext()) {
            sb.append(separator).append(itr.next());
        }
    }

    /**
     * @return the encoding used
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @return the input message qualified name
     */
    public QName getInputMessageXmlName()
    {
        return inputMessageQN;
    }

    /**
     * @return the output message qualified name
     */
    public QName getOutputMessageXmlName()
    {
        return outputMessageQN;
    }

    /**
     * @return the header descriptions
     */
    public Map<String, ParamDescription> getHeaderDescriptions()
    {
        return headerDescriptions;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getDescriptions()
     */
    @Override
    public Map<String, ParamDescription> getDescriptions()
    {
        return inputDescriptions;
    }

    /**
     * @return the output description
     */
    public Map<String, ParamDescription> getOutputDescriptions()
    {
        return outputDescriptions;
    }

    /**
     * @return the style
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#getXmlName()
     */
    @Override
    public QName getXmlName()
    {
        return operationQN;
    }

    /**
     * @param encoding
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * @param header
     */
    public void setSOAPHeader(SOAPHeader header)
    {
        this.header = header;
    }

    /**
     * @return the header
     */
    public SOAPHeader getSOAPHeader()
    {
        return header;
    }

    /**
     * @param header
     */
    public void setSOAP12Header(SOAP12Header header)
    {
        this.soap12header = header;
    }

    /**
     * @return the header
     */
    public SOAP12Header getSOAP12Header()
    {
        return soap12header;
    }

    /**
     * @param inputMessageQN
     */
    public void setInputMessageXmlName(QName inputMessageQN)
    {
        this.inputMessageQN = inputMessageQN;
    }

    /**
     * @param outputMessageQN
     */
    public void setOutputMessageXmlName(QName outputMessageQN)
    {
        this.outputMessageQN = outputMessageQN;
    }

    /**
     * @param parameters
     */
    public void setHeaderDescriptions(Map<String, ParamDescription> parameters)
    {
        this.headerDescriptions = parameters;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setDescriptions(java.util.Map)
     */
    @Override
    public void setDescriptions(Map<String, ParamDescription> parameters)
    {
        this.inputDescriptions = parameters;
    }

    /**
     * @param result
     */
    public void setOutputDescriptions(Map<String, ParamDescription> result)
    {
        this.outputDescriptions = result;
    }

    /**
     * @param style
     */
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * @see it.greenvulcano.gvesb.virtual.ws.dynamic.descr.Description#setXmlName(javax.xml.namespace.QName)
     */
    @Override
    public void setXmlName(QName operationName)
    {
        this.operationQN = operationName;
    }

    /**
     * @return if document is wrapped
     */
    public boolean isWrapped()
    {
        return wrapped;
    }

    /**
     * @param wrapped
     */
    public void setWrapped(boolean wrapped)
    {
        this.wrapped = wrapped;
    }

    /**
     * @return the header parts
     */
    public String[] getHeaderParts()
    {
        return headerParts;
    }

    /**
     * @param headerParts
     */
    public void setHeaderPart(String[] headerParts)
    {
        this.headerParts = headerParts;
    }

    /**
     * @return the input parts
     */
    public String[] getInputParts()
    {
        return inputParts;
    }

    /**
     * @param inputParts
     */
    public void setInputPart(String[] inputParts)
    {
        this.inputParts = inputParts;
    }

    /**
     * @return the output parts
     */
    public String[] getOutputParts()
    {
        return outputParts;
    }

    /**
     * @param outputParts
     */
    public void setOutputPart(String[] outputParts)
    {
        this.outputParts = outputParts;
    }

    /**
     * @param soapActionURI
     */
    public void setSOAPAction(String soapActionURI)
    {
        soapAction = soapActionURI;
    }

    /**
     * @return the soap action
     */
    public String getSOAPAction()
    {
        return soapAction;
    }

    /**
     * @param b
     */
    public void setUseSOAP12Namespace(boolean b)
    {
        useSOAP12Namespace = b;
    }

    /**
     * @return if should be used a SOAP12 name space
     */
    public boolean isUseSOAP12Namespace()
    {
        return useSOAP12Namespace;
    }

    public String getVerb()
    {
        return verb;
    }

    public void setVerb(String verb)
    {
        this.verb = verb;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }
}