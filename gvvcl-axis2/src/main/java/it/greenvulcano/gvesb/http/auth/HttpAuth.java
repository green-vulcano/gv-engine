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
package it.greenvulcano.gvesb.http.auth;

import it.greenvulcano.gvesb.buffer.GVBuffer;

import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Node;

/**
 *
 * @version 3.4.0 06/mag/2014
 * @author GreenVulcano Developer Team
 *
 */
public interface HttpAuth
{
    public void init(Node node) throws HttpAuthException;
    
    public void setAuthentication(HttpClient client, String host, int port, 
            GVBuffer gvBuffer, Map<String, Object> props) throws HttpAuthException;
    
    public void setAuthentication(Options options, String host, GVBuffer gvBuffer, Map<String, Object> props) 
            throws HttpAuthException;
    
    public void setProxyAuthentication(HttpClient client, String host, int port, 
            GVBuffer gvBuffer, Map<String, Object> props) throws HttpAuthException;
    
    public void setProxyAuthentication(Options options, String host, int port, GVBuffer gvBuffer, 
            Map<String, Object> props) throws HttpAuthException;
}
