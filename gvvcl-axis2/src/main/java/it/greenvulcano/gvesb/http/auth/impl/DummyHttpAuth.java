/*
 * Copyright (c) 2009-2014 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.http.auth.impl;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthException;

import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 06/mag/2014
 * @author GreenVulcano Developer Team
 */
public class DummyHttpAuth implements HttpAuth
{

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.http.auth.HttpAuth#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws HttpAuthException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.http.auth.HttpAuth#setAuthentication(org.apache.commons.httpclient.HttpClient)
     */
    @Override
    public void setAuthentication(HttpClient client, String host, int port, GVBuffer gvBuffer, Map<String, Object> props) 
            throws HttpAuthException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.http.auth.HttpAuth#setAuthentication(org.apache.axis2.client.Options)
     */
    @Override
    public void setAuthentication(Options options, String host, GVBuffer gvBuffer, Map<String, Object> props) 
            throws HttpAuthException {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.http.auth.HttpAuth#setProxyAuthentication(org.apache.commons.httpclient.HttpClient)
     */
    @Override
    public void setProxyAuthentication(HttpClient client, String host, int port, GVBuffer gvBuffer, Map<String, 
            Object> props) throws HttpAuthException {
        try {
            client.getHostConfiguration().setProxy(host, port);
        }
        catch (Exception exc) {
            throw new HttpAuthException("Error using Dummy proxy authenticator", exc);
        }
    }
    
   /*
    * (non-Javadoc)
    * 
    * @see
    * it.greenvulcano.gvesb.http.auth.HttpAuth#setProxyAuthentication(org.apache.axis2.client.Options)
    */
   @Override
   public void setProxyAuthentication(Options options, String host, int port, GVBuffer gvBuffer, Map<String, Object> props) 
           throws HttpAuthException {
       try {
           HttpTransportProperties.ProxyProperties proxyProperties = new HttpTransportProperties.ProxyProperties();
           proxyProperties.setProxyName(host);
           proxyProperties.setProxyPort(port);

           options.setProperty(HTTPConstants.PROXY, proxyProperties);
       }
       catch (Exception exc) {
           throw new HttpAuthException("Error using Dummy proxy authenticator", exc);
       }
   }
}
