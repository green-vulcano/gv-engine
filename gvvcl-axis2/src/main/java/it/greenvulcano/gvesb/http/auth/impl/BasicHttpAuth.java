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
package it.greenvulcano.gvesb.http.auth.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthException;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 06/mag/2014
 * @author GreenVulcano Developer Team
 */
public class BasicHttpAuth implements HttpAuth
{
    private String userName = null;
    private String password = null;
    private String realm    = null;

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.http.auth.HttpAuth#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws HttpAuthException {
        try {
            userName = XMLConfig.get(node, "@user", null);
            password = XMLConfig.getDecrypted(node, "@password", "");
            realm = XMLConfig.get(node, "@realm", null);
        }
        catch (Exception exc) {
            throw new HttpAuthException("Error initializing Basic authenticator", exc);
        }
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
        try {
            if (props == null) {
                props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            }

            String currUser = PropertiesHandler.expand(userName, props, gvBuffer);
            String currPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, props, gvBuffer));
            String currRealm = PropertiesHandler.expand(realm, props, gvBuffer);

            Credentials authCred = new UsernamePasswordCredentials(currUser, currPassword);

            client.getState().setCredentials(new AuthScope(host, port, currRealm), authCred);
            client.getParams().setAuthenticationPreemptive(true);
        }
        catch (Exception exc) {
            throw new HttpAuthException("Error using Basic authenticator", exc);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.greenvulcano.gvesb.http.auth.HttpAuth#setAuthentication(org.apache.axis2.client.Options)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void setAuthentication(Options options, String host, GVBuffer gvBuffer, Map<String, Object> props) 
            throws HttpAuthException {
        try {
            if (props == null) {
                props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            }

            String currUser = PropertiesHandler.expand(userName, props, gvBuffer);
            String currPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, props, gvBuffer));
            String currRealm = PropertiesHandler.expand(realm, props, gvBuffer);

            List basicPref = new ArrayList();
            basicPref.add(HttpTransportProperties.Authenticator.BASIC);
            
            Authenticator basicAuth = new HttpTransportProperties.Authenticator();
            basicAuth.setUsername(currUser);
            basicAuth.setPassword(currPassword);
            basicAuth.setRealm(currRealm);
            basicAuth.setAuthSchemes(basicPref);
            basicAuth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
        }
        catch (Exception exc) {
            throw new HttpAuthException("Error using Basic authenticator", exc);
        }
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
            if (props == null) {
                props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            }

            String currUser = PropertiesHandler.expand(userName, props, gvBuffer);
            String currPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, props, gvBuffer));

            client.getHostConfiguration().setProxy(host, port);
            
            if (currUser != null) {
                Credentials authCred = new UsernamePasswordCredentials(currUser, currPassword);
                client.getState().setProxyCredentials(new AuthScope(host, port), authCred);
            }
        }
        catch (Exception exc) {
            throw new HttpAuthException("Error using Basic proxy authenticator", exc);
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
           if (props == null) {
               props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
           }

           String currUser = PropertiesHandler.expand(userName, props, gvBuffer);
           String currPassword = XMLConfig.getDecrypted(PropertiesHandler.expand(password, props, gvBuffer));

           HttpTransportProperties.ProxyProperties proxyProperties = new HttpTransportProperties.ProxyProperties();
           proxyProperties.setProxyName(host);
           proxyProperties.setProxyPort(port);
           if (currUser != null) {
               proxyProperties.setUserName(currUser);
               proxyProperties.setPassWord(currPassword);
           }

           options.setProperty(HTTPConstants.PROXY, proxyProperties);
       }
       catch (Exception exc) {
           throw new HttpAuthException("Error using Basic proxy authenticator", exc);
       }
   }
}
