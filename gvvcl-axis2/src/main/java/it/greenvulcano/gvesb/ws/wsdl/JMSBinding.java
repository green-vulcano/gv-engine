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

import javax.wsdl.extensions.ExtensibilityElement;

/**
 * @version 3.2.0 Feb 10, 2012
 * @author GreenVulcano Developer Team
 */
public interface JMSBinding extends ExtensibilityElement, java.io.Serializable
{
    /**
     * Set the style for this SOAP binding.
     * 
     * @param style
     *        the desired style
     */
    public void setStyle(String style);

    /**
     * Get the style for this SOAP binding.
     */
    public String getStyle();

    /**
     * Set the SOAP transport URI to be used for communicating
     * with this binding.
     * 
     * @param transportURI
     *        the URI describing the transport
     *        to be used
     */
    public void setTransportURI(String transportURI);

    /**
     * Get the transport URI to be used with this binding.
     * 
     * @return the transport URI to be used
     */
    public String getTransportURI();

    public String getJndiContextParameter();

    public void setJndiContextParameter(String jndiContextParameter);

    public String getJndiConnectionFactoryName();

    public void setJndiConnectionFactoryName(String jndiConnectionFactoryName);

    public String getJndiInitialContextFactory();

    public void setJndiInitialContextFactory(String jndiInitialContextFactory);

    public String getJndiURL();

    public void setJndiURL(String jndiURL);

    public String getDeliveryMode();

    public void setDeliveryMode(String deliveryMode);

    public String getPriority();

    public void setPriority(String priority);

    public String getTimeToLive();

    public void setTimeToLive(String timeToLive);

    public String getTopicReplyToName();

    public void setTopicReplyToName(String topicReplyToName);

    public String getReplyToName();

    public void setReplyToName(String replyToName);
}