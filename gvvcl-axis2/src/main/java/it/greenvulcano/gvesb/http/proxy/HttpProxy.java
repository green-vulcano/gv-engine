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
package it.greenvulcano.gvesb.http.proxy;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.http.HttpException;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthFactory;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;

import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Node;

/**
 *
 * @version 3.4.0 07/mag/2014
 * @author GreenVulcano Developer Team
 *
 */
public class HttpProxy
{
    private boolean     init = false;
    private String      host = null;
    private int         port = 8080;
    private HttpAuth    auth = null;
    
    public void init(Node node) throws HttpException
    {
        if (node == null) {
            return;
        }
        try {
            host = XMLConfig.get(node, "@host");
            port = XMLConfig.getInteger(node, "@port", 8080);

            auth = HttpAuthFactory.getInstance(XMLConfig.getNode(node, "*[@type='http-auth']"));
            init = true;
        }
        catch (Exception exc) {
            throw new HttpException("Error initializing Proxy configuration", exc);
        }
    }
    

    public void setProxy(HttpClient client, GVBuffer gvBuffer, Map<String, Object> props) throws HttpException {
        if (!init) {
            return;
        }
        try {
            if (props == null) {
                props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            }

            auth.setProxyAuthentication(client, host, port, gvBuffer, props);
        }
        catch (Exception exc) {
            throw new HttpException("Error setting Proxy", exc);
        }
    }
    
   public void setProxy(Options options, GVBuffer gvBuffer, Map<String, Object> props) throws HttpException {
       if (!init) {
           return;
       }
       try {
           if (props == null) {
               props = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
           }

           auth.setProxyAuthentication(options, host, port, gvBuffer, props);
       }
       catch (Exception exc) {
           throw new HttpException("Error setting Proxy", exc);
       }
   }
}
