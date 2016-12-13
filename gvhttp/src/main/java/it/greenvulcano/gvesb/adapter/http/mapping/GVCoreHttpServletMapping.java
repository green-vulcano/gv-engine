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
import it.greenvulcano.gvesb.adapter.http.exc.GVRequestException;
import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.Formatter;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterExecutionException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.GVTransactionInfo;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.gvesb.adapter.http.utils.DumpUtils;
import it.greenvulcano.gvesb.adapter.http.utils.RetCodeHandler;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;

import it.greenvulcano.log.NMDC;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVCoreHttpServletMapping class
 * 
 * @version 3.1.0 Feb 07, 2011
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVCoreHttpServletMapping implements HttpServletMapping
{
    private static Logger                 logger              = org.slf4j.LoggerFactory.getLogger(GVCoreHttpServletMapping.class);

    private Formatter                     formatter           = null;
    private HttpServletTransactionManager transactionManager  = null;
    private String                        action              = null;
    private boolean                       dump                = false;
    private RetCodeHandler                retCodeHandlerIn    = null;
    private RetCodeHandler                retCodeHandlerOut   = null;
    private String                        responseContentType = null;

    /**
     * @param transactionManager
     * @param formatterMgr
     * @param configurationNode
     * @param configurationFile
     * @throws AdapterHttpInitializationException
     */
    public void init(HttpServletTransactionManager transactionManager, FormatterManager formatterMgr,
            Node configurationNode) throws AdapterHttpInitializationException
    {
        this.transactionManager = transactionManager;

        try {
            action = XMLConfig.get(configurationNode, "@Action");
            dump = XMLConfig.getBoolean(configurationNode, "@dump-in-out", false);
            String formatterID = XMLConfig.get(configurationNode, "@FormatterID");
            formatter = formatterMgr.getFormatter(formatterID);
            retCodeHandlerIn = new RetCodeHandler();
            retCodeHandlerIn.init(XMLConfig.getNode(configurationNode, "RetCodeConversionIn"));
            retCodeHandlerOut = new RetCodeHandler();
            retCodeHandlerOut.init(XMLConfig.getNode(configurationNode, "RetCodeConversionOut"));
            responseContentType = XMLConfig.get(configurationNode, "@RespContentType",
                    AdapterHttpConstants.TEXTHTML_MIMETYPE_NAME);
        }
        catch (AdapterHttpInitializationException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("GVCoreHttpServletMapping - Error initializing action '" + action + "'", exc);
            throw new AdapterHttpInitializationException("GVCoreHttpServletMapping - Error initializing action '" + action
                    + "'", exc);
        }
    }

    /**
     * @param req
     * @param resp
     * @return if request handling was successful
     * @throws InboundHttpResponseException
     */
    public void handleRequest(String methodName, HttpServletRequest req, HttpServletResponse resp) throws InboundHttpResponseException
    {
        logger.debug("handleRequest start");
        long startTime = System.currentTimeMillis();
       
        GVTransactionInfo transInfo = new GVTransactionInfo();
        Map<String, Object> environment = new HashMap<String, Object>();
             
    	GVBuffer response = null;    	    	
        Exception exception = null;
        
        if (dump) {
            try {
            	StringBuffer sb = new StringBuffer();
                DumpUtils.dump(req, sb);
                logger.info(sb.toString());
            } catch (IOException ioException) {
            	logger.error("Request dump failed", ioException);
            }
        }

        try {
            environment.put(AdapterHttpConstants.ENV_KEY_TRANS_INFO, transInfo);
            environment.put(AdapterHttpConstants.ENV_KEY_HTTP_SERVLET_REQUEST, req);
            environment.put(AdapterHttpConstants.ENV_KEY_HTTP_SERVLET_RESPONSE, resp);

            formatter.marshall(environment);

            GVBuffer request = retCodeHandlerIn.transformInput((GVBuffer) environment.get(AdapterHttpConstants.ENV_KEY_GVBUFFER_INPUT));

            String path = req.getPathInfo();
            if (path == null) {
                path = "/";
            }

            String query = req.getQueryString();
            if (query == null) {
                query = "";
            }

            request.setProperty("HTTP_ACTION", action);
            request.setProperty("HTTP_PATH", path);
            request.setProperty("HTTP_QUERY", query);
            request.setProperty("HTTP_METHOD", methodName);
            // get remote transport address...
            String remAddr = req.getRemoteAddr();
            request.setProperty("HTTP_REMOTE_ADDR", (remAddr != null ? remAddr : ""));

            
            GVBufferMDC.put(request);
            String operationType = (String) environment.get(AdapterHttpConstants.ENV_KEY_OP_TYPE);
            NMDC.setOperation(operationType);
            logger.info(GVFormatLog.formatBEGINOperation(request).toString());

            transactionManager.begin(request);

            response = executeService(operationType, request);

            if (response != null) {
                response = retCodeHandlerOut.transformInput(response);
            }
            environment.put(AdapterHttpConstants.ENV_KEY_GVBUFFER_OUTPUT, response);

            transactionManager.commit(transInfo, true);
            manageHttpResponse(environment);
            transactionManager.commit(transInfo, false);
          
        }  catch (GVException gvException) {
        	exception = gvException;
            environment.put(AdapterHttpConstants.ENV_KEY_GVBUFFER_OUTPUT, gvException);
            logger.error(action + " - handleRequest failed ", gvException);
            transInfo.setErrorCode(gvException.getErrorCode());
            transInfo.setErrorMessage(gvException.getMessage());
            manageHttpResponse(environment);
        } catch (RuntimeException runtimeException) {
        	exception = runtimeException;
           
            environment.put(AdapterHttpConstants.ENV_KEY_GVBUFFER_OUTPUT, runtimeException);
            logger.error("handleRequest - Service request failed for a runtime error", runtimeException);
            AdapterHttpException adpEx = new AdapterHttpException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "managing inbound request"}, {"errorName", "" + runtimeException}}, runtimeException);
            transInfo.setErrorCode(adpEx.getErrorCode());
            transInfo.setErrorMessage(adpEx.getMessage());
            manageHttpResponse(environment);
        }   finally {
            
        	if (transInfo.isError()) {
                try {
                    transactionManager.rollback(transInfo, false);
                }
                catch (Exception exc) {
                    logger.error("handleRequest - Transaction failed: " + exc);
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            GVFormatLog gvFormatLog = null;
            if (exception != null) {
                gvFormatLog = GVFormatLog.formatENDOperation(exception, totalTime);
            } else {
                if (response != null) {
                    gvFormatLog = GVFormatLog.formatENDOperation(response, totalTime);
                }
                else {
                    gvFormatLog = GVFormatLog.formatENDOperation(totalTime);
                }
            }
            logger.info(gvFormatLog.toString());

            environment.clear();
        }
        
    }

    @Override
    public boolean isDumpInOut() {
        return dump;
    }

    /**
     *
     */
    public void destroy()
    {
        formatter = null;
        transactionManager = null;
        retCodeHandlerIn = null;
        retCodeHandlerOut = null;
    }

    /**
     * @return the servlet action
     */
    public String getAction()
    {
        return action;
    }

    /**
     * @return the <code>GVBuffer</code> response
     */
    /*public GVBuffer getResponse()
    {
        return response;
    }*/

    /**
     * Invoke GVCore object method corresponding to the specified
     * operationType passing it the <code>GVBuffer</code> object
     * <code>gvdInput</code> as input. Returns an <code>GVBuffer</code> object
     * encapsulating response.
     * 
     * @param operationType
     *        the type of communication paradigm to be used.
     * @param gvdInput
     *        the input <code>GVBuffer</code> object.
     * @return an <code>GVBuffer</code> object encapsulating GVCore
     *         response
     * @throws GVRequestException
     *         if service request to GVConnector fails.
     */
    private GVBuffer executeService(String operationType, GVBuffer gvInput) throws GVRequestException,
            GVPublicException
    {
        logger.info("BEGIN - Perform Remote Call(GVCore) - Operation(" + operationType + ")");
        GVBuffer gvOutput = null;
        String status = "OK";
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        long totalTime = 0;
        NMDC.push();
        try {
      
            GreenVulcanoPool greenVulcanoPool = GreenVulcanoPoolManager.instance()	
            												.getGreenVulcanoPool(AdapterHttpConstants.SUBSYSTEM)
            												.orElseGet(GreenVulcanoPoolManager::getDefaultGreenVulcanoPool);
            if (greenVulcanoPool == null) {
                throw new InboundHttpResponseException("GVHTTP_GREENVULCANOPOOL_NOT_CONFIGURED");
            }

            try {
                gvOutput = greenVulcanoPool.forward(gvInput, operationType);
                return gvOutput;
            }
            catch (Exception exc) {
                status = "FAILED";
              
                throw exc;
            }
            finally {
                NMDC.pop();
                endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                logger.debug("END - Perform Remote Call(GVCore) - Operation(" + operationType
                        + ") - ExecutionTime (" + totalTime + ") - Status: " + status);
            }
        }
        catch (GVPublicException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("executeServiceGVC - Runtime error while invoking GVCore: ", exc);
            throw new GVRequestException("GVHTTP_RUNTIME_ERROR", new String[][]{{"phase", "invoking GVCore"},
                    {"errorName", "" + exc}}, exc);
        }
    }

    /**
     * Handle GreenVulcano response to service request from external systems
     * communicating via HTTP.
     * 
     * @throws InboundHttpResponseException
     *         if any error occurs.
     */
    @SuppressWarnings("unchecked")
    private void manageHttpResponse(Map<String, Object> environment) throws InboundHttpResponseException
    {
        logger.debug("manageHttpResponse start");
        String respCharacterEncoding = null;
        try {
            formatter.unMarshall(environment);
            String responseString = (String) environment.get(AdapterHttpConstants.ENV_KEY_RESPONSE_STRING);
            HttpServletResponse resp = (HttpServletResponse) environment.get(AdapterHttpConstants.ENV_KEY_HTTP_SERVLET_RESPONSE);

            Map<String, String> headerMap = (Map<String, String>) environment.get(AdapterHttpConstants.ENV_KEY_HTTP_HEADER);
            if (headerMap != null) {
                for (Entry<String, String> entry : headerMap.entrySet()) {
                    String paramName = entry.getKey();
                    String value = entry.getValue();
                    resp.setHeader(paramName, value);
                }
            }

            Integer status = (Integer) environment.get(AdapterHttpConstants.ENV_KEY_RESPONSE_STATUS);
            if (status != null) {
                resp.sendError(status.intValue(), responseString);
                return;
            }
            respCharacterEncoding = (String) environment.get(AdapterHttpConstants.ENV_KEY_UNMARSHALL_ENCODING);
            String contentType = (String) environment.get(AdapterHttpConstants.ENV_KEY_RESPONSE_CONTENT_TYPE);
            if (contentType == null) {
                contentType = responseContentType;
            }
            setRespContentTypeAndCharset(resp, contentType, respCharacterEncoding);

            OutputStreamWriter out = new OutputStreamWriter(resp.getOutputStream(), respCharacterEncoding);
            out.write(responseString);
            out.flush();
            out.close();
            
            if (dump) {
                StringBuffer sb = new StringBuffer();
                DumpUtils.dump(resp, sb);
                logger.info(sb.toString());
            }
        }
        catch (FormatterExecutionException exc) {
            logger.error("manageResponse - Error while handling response parameters: " + exc);
            throw new InboundHttpResponseException("GVHTTP_HANDLER_ERROR", new String[][]{{"errorName", "" + exc}}, exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("manageResponse - Can't encode response to invoking system using encoding "
                    + respCharacterEncoding + ": " + exc);
            throw new InboundHttpResponseException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", respCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (IOException exc) {
            logger.error("manageResponse - Can't send response to invoking system: " + exc);
            throw new InboundHttpResponseException("GVHTTP_INBOUND_HTTP_RESPONSE_ERROR", new String[][]{{"errorName",
                    "" + exc}}, exc);
        }
        finally {
            logger.debug("manageHttpResponse stop");
        }
    }

    /**
     * Sets content type and charset header fields of the servlet response.
     * 
     * @param resp
     *        An HttpServletResponse object
     * @param contentType
     *        A string containing the declared response's content type
     * @param charset
     *        A string containing the declared response's charset
     */
    private void setRespContentTypeAndCharset(HttpServletResponse resp, String contentType, String charset)
    {
        resp.setContentType(contentType + "; charset=" + charset);
    }
}
