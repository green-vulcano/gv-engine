/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.ws.wsdl;

import javax.xml.namespace.QName;

import com.ibm.wsdl.Constants;


/**
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public class JMSBindingImpl implements JMSBinding
{
    public static final QName  Q_ELEM_JMS_BINDING        = new QName("http://www.w3.org/2010/soapjms/",
                                                                 Constants.ELEM_BINDING);
    public static final String NS_URI_JMS                = "http://www.w3.org/2010/soapjms/";
    public static final String JNDICONTEXTPARAMETER      = "jndiContextParameter";
    public static final String JNDICONNECTIONFACTORYNAME = "jndiConnectionFactoryName";
    public static final String JNDIINITIALCONTEXTFACTORY = "jndiInitialContextFactory";
    public static final String JNDIURL                   = "jndiURL";
    public static final String DELIVERYMODE              = null;
    public static final String PRIORITY                  = "deliveryMode";
    public static final String TIMETOLIVE                = "timeToLive";
    public static final String REPLYTONAME               = "replyToName";
    public static final String TOPICREPLYTONAME          = "topicReplyToName";
    protected QName            elementType               = Q_ELEM_JMS_BINDING;
    protected Boolean          required                  = null;
    protected String           style                     = null;
    protected String           transportURI              = null;
    private String             jndiContextParameter      = null;
    private String             jndiConnectionFactoryName = null;
    private String             jndiInitialContextFactory = null;
    private String             jndiURL                   = null;
    private String             deliveryMode              = null;
    private String             priority                  = null;
    private String             timeToLive                = null;
    private String             replyToName               = null;
    private String             topicReplyToName          = null;

    public static final long   serialVersionUID          = 1;

    /**
     * Set the type of this extensibility element.
     * 
     * @param elementType
     *        the type
     */
    @Override
    public void setElementType(QName elementType)
    {
        this.elementType = elementType;
    }

    /**
     * Get the type of this extensibility element.
     * 
     * @return the extensibility element's type
     */
    @Override
    public QName getElementType()
    {
        return elementType;
    }

    /**
     * Set whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    @Override
    public void setRequired(Boolean required)
    {
        this.required = required;
    }

    /**
     * Get whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    @Override
    public Boolean getRequired()
    {
        return required;
    }

    /**
     * Set the style for this SOAP binding.
     * 
     * @param style
     *        the desired style
     */
    @Override
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * Get the style for this SOAP binding.
     */
    @Override
    public String getStyle()
    {
        return style;
    }

    /**
     * Set the SOAP transport URI to be used for communicating
     * with this binding.
     * 
     * @param transportURI
     *        the URI describing the transport
     *        to be used
     */
    @Override
    public void setTransportURI(String transportURI)
    {
        this.transportURI = transportURI;
    }

    /**
     * Get the transport URI to be used with this binding.
     * 
     * @return the transport URI to be used
     */
    @Override
    public String getTransportURI()
    {
        return transportURI;
    }

    @Override
    public String toString()
    {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("JMSBinding (" + elementType + "):");
        strBuf.append("\nrequired=" + required);

        if (transportURI != null) {
            strBuf.append("\ntransportURI=" + transportURI);
        }

        if (style != null) {
            strBuf.append("\nstyle=" + style);
        }

        return strBuf.toString();
    }

    @Override
    public String getJndiContextParameter()
    {
        return jndiContextParameter;
    }

    @Override
    public void setJndiContextParameter(String jndiContextParameter)
    {
        this.jndiContextParameter = jndiContextParameter;
    }

    @Override
    public String getJndiConnectionFactoryName()
    {
        return jndiConnectionFactoryName;
    }

    @Override
    public void setJndiConnectionFactoryName(String jndiConnectionFactoryName)
    {
        this.jndiConnectionFactoryName = jndiConnectionFactoryName;
    }

    @Override
    public String getJndiInitialContextFactory()
    {
        return jndiInitialContextFactory;
    }

    @Override
    public void setJndiInitialContextFactory(String jndiInitialContextFactory)
    {
        this.jndiInitialContextFactory = jndiInitialContextFactory;

    }

    @Override
    public String getJndiURL()
    {
        return jndiURL;
    }

    @Override
    public void setJndiURL(String jndiURL)
    {
        this.jndiURL = jndiURL;
    }

    @Override
    public String getDeliveryMode()
    {
        return deliveryMode;
    }

    @Override
    public void setDeliveryMode(String deliveryMode)
    {
        this.deliveryMode = deliveryMode;
    }

    @Override
    public String getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    @Override
    public String getTimeToLive()
    {
        return timeToLive;
    }

    @Override
    public void setTimeToLive(String timeToLive)
    {
        this.timeToLive = timeToLive;
    }

    @Override
    public String getReplyToName()
    {
        return replyToName;
    }

    @Override
    public void setReplyToName(String replyToName)
    {
        this.replyToName = replyToName;
    }

    @Override
    public String getTopicReplyToName()
    {
        return topicReplyToName;
    }

    @Override
    public void setTopicReplyToName(String topicReplyToName)
    {
        this.topicReplyToName = topicReplyToName;
    }
}