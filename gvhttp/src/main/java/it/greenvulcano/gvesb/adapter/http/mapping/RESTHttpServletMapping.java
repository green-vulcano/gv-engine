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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.http.HttpServletMapping;
import it.greenvulcano.gvesb.adapter.http.HttpServletTransactionManager;
import it.greenvulcano.gvesb.adapter.http.exc.GVRequestException;
import it.greenvulcano.gvesb.adapter.http.exc.InboundHttpResponseException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterManager;
import it.greenvulcano.gvesb.adapter.http.formatters.handlers.GVTransactionInfo;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpExecutionException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpInitializationException;
import it.greenvulcano.gvesb.adapter.http.utils.DumpUtils;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.GVPublicException;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;

import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RESTHttpServletMapping class
 * 
 * @version 3.5.0 July 20, 2014
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RESTHttpServletMapping implements HttpServletMapping
{
    private static Logger                 logger              = org.slf4j.LoggerFactory.getLogger(RESTHttpServletMapping.class);

    private HttpServletTransactionManager transactionManager  = null;
    private String                        action              = null;
    private boolean                       dump                = false;
    private String                        responseContentType = null;
    private String                        responseCharacterEncoding = null;
    private List<PatternResolver>         operationMappings   = new ArrayList<PatternResolver>();

    static private class PatternResolver {
        private String pattern;
        private String method;
        private String service;
        private String system;
        private String operation;
        private boolean extractHdr;
        private List<String> propNames = new ArrayList<String>();
        private List<Pattern> patterns = new ArrayList<Pattern>();
        
        public PatternResolver() {
            // do nothing
        }

        public void init(Node node) throws AdapterHttpInitializationException {
            try {
                this.pattern = XMLConfig.get(node, "@pattern");
                if ((pattern == null) || "".equals(pattern)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern: empty");
                }
                this.method = XMLConfig.get(node, "@method");
                if ((method == null) || "".equals(method)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + pattern + "]: empty @method");
                }
                this.service = XMLConfig.get(node, "@service");
                if ((service == null) || "".equals(service)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + method + "#" + pattern + "]: empty @service");
                }
                this.system = XMLConfig.get(node, "@system", GVBuffer.DEFAULT_SYS);
                this.operation = XMLConfig.get(node, "@operation");
                if ((operation == null) || "".equals(operation)) {
                    throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + method + "#" + pattern + "]: empty @operation");
                }
                this.extractHdr = XMLConfig.getBoolean(node, "@extract-headers", false);
            }
            catch (XMLConfigException exc) {
                throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern: error reading configuration", exc);
            }
            compile();
        }

        private void compile() throws AdapterHttpInitializationException {
            logger.debug("Compile - BEGIN");
            logger.debug("Pattern: " + method + "#" + pattern);
            String locPattern = pattern;
            if (locPattern.startsWith("/")) {
                locPattern = locPattern.substring(1);
            }
            String[] list = locPattern.split("/");
            for (int i = 0; i < list.length; i++) {
                String elem = list[i].trim();
                if (!"".equals(elem)) {
                    logger.debug("Element: " + elem);
                    String pN = elem.split("=")[0].trim();
                    String p = elem.split("=")[1].trim();
                    propNames.add(pN);
                    patterns.add(Pattern.compile(p));
                    logger.debug("[" + pN + "]=[" + p + "]");
                }
            }
            logger.debug("Compile - END");
            if (patterns.isEmpty()) {
                throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing Pattern[" + method + "#" + pattern + "]: empty");
            }
        }
        
        public String match(HttpServletRequest request, String methodName, String path, GVBuffer data) throws AdapterHttpExecutionException {
            try {
                logger.debug("Checking [" + method + "#" + pattern +"] on [" + methodName + "#" + path + "]");
                if (!method.equalsIgnoreCase(methodName)) {
                    logger.debug("Pattern [" + method + "#" + pattern + "] NOT matched");
                    return null;
                }
                List<String> values = new ArrayList<String>();
                String locPath = path;
                if (locPath.startsWith("/")) {
                    locPath = locPath.substring(1);
                }
                String[] parts = locPath.split("/");
                if (parts.length == patterns.size()) {
                    for (int i = 0; i < parts.length; i++) {
                        Matcher m = patterns.get(i).matcher(parts[i]);
                        if (m.matches()) {
                            values.add(parts[i]);
                        }
                        else {
                            logger.debug("Pattern [" + method + "#" + pattern +"] NOT matched");
                            return null;
                        }
                    }
    
                    data.setService(service);
                    data.setSystem(system);
                    for (int i = 0; i < propNames.size(); i++) {
                        data.setProperty(propNames.get(i), values.get(i));
                    }
                    logger.debug("Pattern [" + method + "#" + pattern +"] matched");
                    return operation;
                }
                logger.debug("Pattern [" + method + "#" + pattern +"] NOT matched");
                return null;
            }
            catch (Exception exc) {
                throw new AdapterHttpExecutionException("RESTHttpServletMapping - Error evaluating Pattern[" + method + "#" + pattern + "]", exc);
            }
        }

        public boolean isExtractHdr() {
            return this.extractHdr;
        }

        @Override
        public String toString() {
            return method + "#" + pattern + " -> " + service + "/" + system + "/" + operation;
        }
    }

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
            responseContentType = XMLConfig.get(configurationNode, "@RespContentType",
                    AdapterHttpConstants.APPXML_MIMETYPE_NAME);
            responseCharacterEncoding = XMLConfig.get(configurationNode, "@RespCharacterEncoding", "UTF-8");

            NodeList opMaps = XMLConfig.getNodeList(configurationNode, "OperationMappings/Mapping");
            for (int i = 0; i < opMaps.getLength(); i++) {
                Node opM = opMaps.item(i);
                operationMappings.add(buildPatternResolver(opM));
            }
        }
        /*catch (AdapterHttpInitializationException exc) {
            throw exc;
        }*/
        catch (Exception exc) {
            logger.error("RESTHttpServletMapping - Error initializing action '" + action + "'", exc);
            throw new AdapterHttpInitializationException("RESTHttpServletMapping - Error initializing action '" + action
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
       
        Throwable exception = null;
    	GVBuffer response = null;
        	
        try {
            if (dump) {
                StringBuffer sb = new StringBuffer();
                DumpUtils.dump(req, sb);
                logger.info(sb.toString());
            }

            String path = req.getPathInfo();
            if (path == null) {
                path = "/";
            }

            String query = req.getQueryString();
            if (query == null) {
                query = "";
            }
            
            GVBuffer request = new GVBuffer();
            String operationType = null;
            PatternResolver pr = null;
            Iterator<PatternResolver> i = operationMappings.iterator();
            while (i.hasNext()) {
                pr = i.next();
                operationType = pr.match(req, methodName, path, request);
                if (operationType != null) {
                    break;
                }
            }
            
            if (operationType == null) {
                logger.error(action + " - handleRequest - Error while handling request parameters: unable to decode requested operation [" + methodName + "#" + path + "]");
                resp.sendError(400, "Unable to decode the requested operation [" + methodName + "#" + path + "]");
                
            }

            transInfo.setService(request.getService());
            transInfo.setSystem(request.getSystem());
            transInfo.setId(request.getId());
            transInfo.setOperation(operationType);

            request.setProperty("HTTP_ACTION", action);
            request.setProperty("HTTP_PATH", path);
            request.setProperty("HTTP_QUERY", query);
            request.setProperty("HTTP_METHOD", methodName);
            // get remote transport address...
            String remAddr = req.getRemoteAddr();
            request.setProperty("HTTP_REMOTE_ADDR", (remAddr != null ? remAddr : ""));

            parseRequest(req, methodName, pr, request);
            
            GVBufferMDC.put(request);
            NMDC.setOperation(operationType);
            logger.info(GVFormatLog.formatBEGINOperation(request).toString());

            transactionManager.begin(request);

            response = executeService(operationType, request);

            transactionManager.commit(transInfo, true);

            manageHttpResponse(response, resp);

            transactionManager.commit(transInfo, false);
                     
            logger.debug("handleRequest stop");
        }
        catch (Throwable exc) {
        	exception = exc;
          
            logger.error("handleRequest - Service request failed", exc);
            try {
                resp.sendError(500, "" + exc);
            }
            catch (IOException exc1) {
                throw new InboundHttpResponseException("GVHTTP_INBOUND_HTTP_RESPONSE_ERROR", new String[][]{{"errorName",
                    "" + exc1}}, exc1);
            }
        }
        finally {
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
            }
            else {
                if (response != null) {
                    gvFormatLog = GVFormatLog.formatENDOperation(response, totalTime);
                }
                else {
                    gvFormatLog = GVFormatLog.formatENDOperation(totalTime);
                }
            }
            logger.info(gvFormatLog.toString());
        }
        
    }

    /**
     * @param req
     * @param request
     * @throws GVException
     */
    private void parseRequest(HttpServletRequest req, String methodName, PatternResolver pr, GVBuffer request) throws GVException {
        try {
            Map<String, String[]> params = req.getParameterMap();
            Iterator<String> i = params.keySet().iterator();
            while (i.hasNext()) {
                String n = i.next();
                String v = params.get(n)[0];

                request.setProperty(n, ((v != null) && !"".equals(v)) ? v : "NULL");
            }

            String ct = req.getContentType();
            request.setProperty("HTTP_REQ_CONTENT_TYPE", (ct != null) ? ct : "NULL");
            String acc = req.getHeader("Accept");
            request.setProperty("HTTP_REQ_ACCEPT", (acc != null) ? acc : "NULL");
            if (methodName.equals("POST") || methodName.equals("PUT")) {
                if (!ct.startsWith(AdapterHttpConstants.URLENCODED_MIMETYPE_NAME)) {
                    Object requestContent = IOUtils.toByteArray(req.getInputStream());
                    if (ct.startsWith(AdapterHttpConstants.APPXML_MIMETYPE_NAME) ||
                        ct.startsWith(AdapterHttpConstants.APPJSON_MIMETYPE_NAME) ||
                        ct.startsWith("text/")) {
                        /* GESTIRE ENCODING!!! */
                        requestContent = new String((byte[]) requestContent);
                    }
                    request.setObject(requestContent);
                }
            }

            if (pr.isExtractHdr()) {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    Document doc = parser.newDocument("Hdr");
                    Element root = doc.getDocumentElement();

                    Enumeration<?> hn = req.getHeaderNames();
                    while (hn.hasMoreElements()) {
                        Element h = parser.insertElement(root, "h");
                        String name = (String) hn.nextElement();
                        String val = req.getHeader(name);
                        parser.setAttribute(h, "n", name);
                        parser.setAttribute(h, "v", val);
                    }
                    request.setProperty("HTTP_REQ_HEADERS", parser.serializeDOM(doc, true, false));
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
            }
            
        }
        catch (Exception exc) {
            throw new AdapterHttpExecutionException("RESTHttpServletMapping - Error parsing request data", exc);
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
        transactionManager = null;
        operationMappings.clear();
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
     * @throws AdapterHttpInitializationException 
     */
    /*public GVBuffer getResponse()
    {
        return response;
    }*/

    private PatternResolver buildPatternResolver(Node n) throws AdapterHttpInitializationException {
        PatternResolver pr = new PatternResolver();
        pr.init(n);
        return pr;
    }

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
                logger.error("FAILED",exc);
                throw exc;
            }
            finally {
                NMDC.pop();
                endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                logger.info( "END - Perform Remote Call(GVCore) - Operation(" + operationType
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
    @SuppressWarnings("deprecation")
	private void manageHttpResponse(GVBuffer response, HttpServletResponse resp) throws InboundHttpResponseException
    {
        logger.debug("manageHttpResponse start");
        String respCharacterEncoding = null;
        try {
            String respStatusCode = response.getProperty("HTTP_RESP_STATUS_CODE");
            String respStatusMsg = response.getProperty("HTTP_RESP_STATUS_MSG");
            if (respStatusCode != null) {
                if (respStatusMsg == null) {
                    resp.setStatus(Integer.parseInt(respStatusCode));
                }
                else {
                	
                    //resp.sendError(Integer.parseInt(respStatusCode), respStatusMsg);
                    resp.setStatus(Integer.parseInt(respStatusCode), respStatusMsg);
                }
            }

            /*Map<String, String> headerMap = (Map<String, String>) environment.get(AdapterHttpConstants.ENV_KEY_HTTP_HEADER);
            if (headerMap != null) {
                for (Entry<String, String> entry : headerMap.entrySet()) {
                    String paramName = entry.getKey();
                    String value = entry.getValue();
                    resp.setHeader(paramName, value);
                }
            }*/

            Object data = response.getObject();
            if (data != null) {
                respCharacterEncoding = response.getProperty("HTTP_RESP_CHAR_ENCODING");
                if (respCharacterEncoding == null) {
                    respCharacterEncoding = responseCharacterEncoding;
                }
                String respContentType = response.getProperty("HTTP_RESP_CONTENT_TYPE");
                if (respContentType == null) {
                    respContentType = responseContentType;
                }
                String fileName = response.getProperty("HTTP_RESP_FILE_NAME");
                if ((fileName != null) && !"".equals(fileName)) {
                    int fileSize = -1;
                    if (data instanceof byte[]) {
                        fileSize = ((byte[]) data).length;
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
                    setRespDownloadHeaders(resp, respContentType, fileName, fileSize);
                }
                else {
                    setRespContentTypeAndCharset(resp, respContentType, respCharacterEncoding);
                }
            	
                OutputStream out = resp.getOutputStream();
                if (respContentType.equals(AdapterHttpConstants.APPXML_MIMETYPE_NAME) ||
                    respContentType.equals(AdapterHttpConstants.APPJSON_MIMETYPE_NAME) ||
                    respContentType.startsWith("text/")) {
                    if (data instanceof byte[]) {
                        IOUtils.write((byte[]) data, out);
                    }
                    else if (data instanceof String) {
                        IOUtils.write((String) data, out, respCharacterEncoding);
                    }
                    else if (data instanceof Node) {
                        XMLUtils.serializeDOMToStream_S((Node) data, out, respCharacterEncoding, false, false);
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
                }
                else {
                    if (data instanceof byte[]) {
                        IOUtils.write((byte[]) data, out);
                    }
                    else {
                        throw new InboundHttpResponseException("Invalid GVBuffer content: " + data.getClass().getName());
                    }
                }
                out.flush();
                out.close();
            }

            if (dump) {
                StringBuffer sb = new StringBuffer();
                DumpUtils.dump(resp, sb);
                logger.debug(sb.toString());
            }
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("manageResponse - Can't encode response to invoking system using encoding "
                    + respCharacterEncoding + ": " + exc);
            throw new InboundHttpResponseException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", respCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (Exception exc) {
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
        resp.setContentType(contentType);
        resp.setCharacterEncoding(charset);
    }

    /**
     * Sets header fields for file download.
     * 
     * @param resp
     *        An HttpServletResponse object
     * @param contentType
     *        A string containing the declared response's content type
     * @param fileName
     *        A string containing the downloaded file name
     * @param fileSize
     *        A string containing the downloaded file size
     */
    private void setRespDownloadHeaders(HttpServletResponse resp, String contentType, String fileName, int fileSize)
    {
        resp.setContentType(contentType);
        resp.setContentLength(fileSize);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setHeader("Connection", "close");
        resp.setHeader("Expires", "-1");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
    }
}
