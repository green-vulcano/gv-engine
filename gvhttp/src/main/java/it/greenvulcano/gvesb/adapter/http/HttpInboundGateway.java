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
package it.greenvulcano.gvesb.adapter.http;

import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.AdapterHttpConfigurationException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.DumpUtils;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.identity.impl.HTTPIdentityInfo;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.NMDC;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

/**
 * This class implements an entry point into GreenVulcano for external systems
 * which communicate via HTTP protocol.
 * <p>
 * There will be an instance of this class for each external system
 * communicating via HTTP.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class HttpInboundGateway extends HttpServlet
{
    private static final long         serialVersionUID = -6337761728589142613L;

    private static Logger             logWriter        = org.slf4j.LoggerFactory.getLogger(HttpInboundGateway.class);

    private HttpServletMappingManager mappingManager   = null;

    /**
     * Initialization method.
     * 
     * @param config
     *        the <tt>ServletConfig</tt> object.
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        NMDC.push();
        NMDC.clear();
        NMDC.setSubSystem(AdapterHttpConstants.SUBSYSTEM);
       
        try {
            super.init(config);
            logWriter.debug("HttpInboundGateway - BEGIN init");

            mappingManager = new HttpServletMappingManager();

            logWriter.debug("HttpInboundGateway - END init");
        }
        catch (ServletException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            if (mappingManager != null) {
                mappingManager.destroy();
                mappingManager = null;
            }
            logWriter.error("HttpInboundGateway - Unexpected error: ", exc);
            throw new ServletException("Unexpected error: ", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * Cleanup method.
     */
    @Override
    public void destroy()
    {
        if (mappingManager != null) {
            mappingManager.destroy();
            mappingManager = null;
        }
        logWriter.debug("AdapterHTTP inbound gateway stopped");
    }

    /**
     * Handle HTTP requests from external system submitted with method GET.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("GET", req, resp);
    }

    /**
     * Handle HTTP requests from external system submitted with method POST.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("POST", req, resp);
    }

    /**
     * Handle HTTP requests from external system submitted with method PUT.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("PUT", req, resp);
    }
    
    /**
     * Handle HTTP requests from external system submitted with method HEAD.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("HEAD", req, resp);
    }
    
    /**
     * Handle HTTP requests from external system submitted with method OPTIONS.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("OPTIONS", req, resp);
    }

    /**
     * Handle HTTP requests from external system submitted with method DELETE.
     * 
     * @param req
     *        An HttpServletRequest object
     * @param resp
     *        An HttpServletResponse object
     * @throws ServletException
     *         if any error occurs.
     */
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        perform("DELETE", req, resp);
    }
    
    /**
     * @param req
     * @param resp
     * @throws ServletException
     */
    private void perform(String method, HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        long startTime = 0;
        long endTime = 0;
        long totalTime = 0;

        NMDC.push();
        NMDC.clear();
        
        NMDC.setSubSystem(AdapterHttpConstants.SUBSYSTEM);

        Exception exception = null;
        HttpServletMapping smapping = null;
        
        startTime = System.currentTimeMillis();
        
        String mapping = req.getPathInfo();
        if (mapping == null) {
            mapping = "/";
        }
        String gvAction = mapping;

        try {
            req = new MultiReadHttpServletRequest(req);
            resp = new MultiReadHttpServletResponse(resp);

            NMDC.put("HTTP_METHOD", method);
            NMDC.put("HTTP_ACTION", gvAction);
            logWriter.info(method + " - BEGIN " + gvAction);

            smapping = mappingManager.getMapping(gvAction);
            if (smapping == null) {
                throw new AdapterHttpConfigurationException("HttpServletMappingManager - Mapping '" + gvAction
                        + "' not found");
            }

            // Create and insert the caller in the security context
            GVIdentityHelper.push(new HTTPIdentityInfo(req));
            
            smapping.handleRequest(method, req, resp);

        }  catch (InboundHttpResponseException exc) {
            
            logWriter.error(method + " " + gvAction + " - Can't send response to client system: " + exc);
            exception = exc;
            throw new ServletException("Can't send response to client system - " + gvAction, exc);
        } catch (AdapterHttpConfigurationException exc) {
         
            logWriter.error(method + " " + gvAction + " - Can't handle request from client system", exc);
            exception = exc;
            throw new ServletException("Can't handle request from client system - " + gvAction, exc);
        }  catch (IOException exc) {
           
            logWriter.error(method + " " + gvAction + " - Can't read request data: " + exc);
            exception = exc;
            throw new ServletException("Can't read request data - " + gvAction, exc);
        } finally {
        	try {
	            endTime = System.currentTimeMillis();
	            totalTime = endTime - startTime;
	            if (exception!=null) {
	                StringBuffer sb = new StringBuffer();
	                try {
	                    DumpUtils.dump(req, sb);
	                }
	                catch (Exception exc) {
                        sb.append("--- ERROR MAKING HTTP DUMP: ").append(exc);
                    }
	               
	            }
	            if (exception != null) {
	            	GVFormatLog gvFormatLog = GVFormatLog.formatENDOperation(exception, totalTime);
	                logWriter.warn(gvFormatLog.toString());
	            }
	            logWriter.debug(method + " - END " + gvAction + " - ExecutionTime (" + totalTime + ")");
        	}
        	finally {
        		// Remove the caller from the security context
        		GVIdentityHelper.pop();

        		NMDC.pop();
        	}
        }
    }
}
