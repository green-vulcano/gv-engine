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
package it.greenvulcano.gvesb.adapter.http.mapping;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.adapter.http.HttpServletMapping;
import it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager;
import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.gvesb.adapter.http.utils.DumpUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 4.0 25/mar/2016
 * @author GreenVulcano Developer Team
 *
 */
public class ForwardHttpServletMapping implements HttpServletMapping
{
    private static final Logger logger                 = org.slf4j.LoggerFactory.getLogger(ForwardHttpServletMapping.class);
    public static final int     DEFAULT_CONN_TIMEOUT   = 10000;
    public static final int     DEFAULT_SO_TIMEOUT     = 30000;

    private String              action;
    private boolean             dump                   = false;
    private URLConnection       connection;
    private String              host;
    private String              port;
    private boolean             secure                 = false;
    
    private String              contextPath;

    private int                 connTimeout            = DEFAULT_CONN_TIMEOUT;
    private int                 soTimeout              = DEFAULT_SO_TIMEOUT;
    
    
    /**
     * 
     */
    public ForwardHttpServletMapping() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#init(it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager, it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager, org.w3c.dom.Node)
     */
    @Override
    public void init(HttpServletTransactionManager transactionManager, FormatterManager formatterMgr,
            Node configurationNode) throws AdapterHttpInitializationException {
      
        try {
            action = XMLConfig.get(configurationNode, "@Action");
            dump = XMLConfig.getBoolean(configurationNode, "@dump-in-out", false);
            
            Node endpointNode = XMLConfig.getNode(configurationNode, "endpoint");
            host = XMLConfig.get(endpointNode, "@host");
            port = XMLConfig.get(endpointNode, "@port", "80");
            contextPath = XMLConfig.get(endpointNode, "@context-path", "");
            secure = XMLConfig.getBoolean(endpointNode, "@secure", false);
            connTimeout = XMLConfig.getInteger(endpointNode, "@conn-timeout", DEFAULT_CONN_TIMEOUT);
            soTimeout = XMLConfig.getInteger(endpointNode, "@so-timeout", DEFAULT_SO_TIMEOUT);
            
            URL url = new URL(secure ? "https" : "http", host, Integer.valueOf(port), contextPath);
          
            Node proxyConfigNode = XMLConfig.getNode(endpointNode, "Proxy");
            if (proxyConfigNode != null) {
                String proxyHost = XMLConfig.get(proxyConfigNode, "@host");
                int proxyPort = XMLConfig.getInteger(proxyConfigNode, "@port", 80);
                
                Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));                
                connection = url.openConnection(proxy);
                
                String proxyUser = XMLConfig.get(proxyConfigNode, "@user","");
                String proxyPassword = XMLConfig.getDecrypted(proxyConfigNode, "@password", "");
                
                if (proxyUser.trim().length()>0) {
                	String proxyAuthorization = proxyUser+":"+proxyPassword;
                	connection.setRequestProperty("Proxy-Authorization", Base64.getEncoder().encodeToString(proxyAuthorization.getBytes()));
                }
                                
            } else {
            	 connection = url.openConnection();
            }
            
            connection.setConnectTimeout(connTimeout);
            
        }
        catch (Exception exc) {
            throw new AdapterHttpInitializationException("GVHTTP_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }

    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("deprecation")
	@Override
    public void handleRequest(String methodName, HttpServletRequest req, HttpServletResponse resp) throws InboundHttpResponseException {
        
        logger.debug("BEGIN forward: " + req.getRequestURI());
        
        final HttpURLConnection httpConnection = (HttpURLConnection) connection;;
       
        try {       	
        	
        	httpConnection.setRequestMethod(methodName);
        	httpConnection.setReadTimeout(soTimeout);
        	httpConnection.setDoInput(true);
        	httpConnection.setDoOutput(true);
        	
            if (dump) {
            	StringBuffer sb = new StringBuffer();
            	DumpUtils.dump(req, sb);
            	logger.info(sb.toString());
            }

            String mapping = req.getPathInfo();
            if (mapping == null) {
                mapping = "/";
            }
            String destPath = contextPath + mapping;
            String queryString = req.getQueryString(); 
            if (queryString != null) {
                destPath += "?" + queryString; 
            }
            if (!destPath.startsWith("/")) {
                destPath = "/" + destPath; 
            }
            logger.info("Resulting QueryString: " + destPath);
          
           Collections.list(req.getHeaderNames())
           			  .forEach(headerName -> {
           				  		httpConnection.setRequestProperty(headerName, 
           				  				Collections.list(req.getHeaders(headerName))
           				  						   .stream()
           				  						   .collect(Collectors.joining(";")));
           			  });
            
                        
            if (methodName.equalsIgnoreCase("POST") || methodName.equalsIgnoreCase("PUT")) {
            	httpConnection.setDoOutput(true);
            	IOUtils.copy(req.getInputStream(), httpConnection.getOutputStream());
            	            	
            }
           
            httpConnection.connect();
            int status = httpConnection.getResponseCode();
            
            resp.setStatus(status, httpConnection.getResponseMessage());            
        
            httpConnection.getHeaderFields().entrySet().stream()
            	.forEach(header -> {
            		resp.addHeader(header.getKey(), header.getValue().stream().collect(Collectors.joining(";")));
            	});
            
            ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
            IOUtils.copy(httpConnection.getInputStream(), bodyOut);                       
            
            OutputStream out = resp.getOutputStream();
            out.write(bodyOut.toByteArray());
            //IOUtils.copy(method.getResponseBodyAsStream(), out);
            out.flush();
            out.close();
            
            if (dump) {
            	StringBuffer sb = new StringBuffer();
            	DumpUtils.dump(resp, bodyOut, sb);
            	logger.info(sb.toString());
            }
            
            
        }
        catch (Exception exc) {
            logger.error("ERROR on forwarding: " + req.getRequestURI(), exc);
            throw new InboundHttpResponseException("GV_CALL_SERVICE_ERROR", new String[][]{{"message", exc.getMessage()}}, exc);
        }
        finally {
            try {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            }
            catch (Exception exc) {
                logger.warn("Error while releasing connection", exc);
            }
            logger.debug("END forward: " + req.getRequestURI());
        }
    }
    
   
    @Override
    public boolean isDumpInOut() {
        return dump;
    }
    
    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#destroy()
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.adapter.http.HttpServletMapping#getAction()
     */
    @Override
    public String getAction() {
        return action;
    }
    
}
