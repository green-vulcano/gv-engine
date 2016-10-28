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
package it.greenvulcano.gvesb.adapter.http.formatters.handlers;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.adapter.http.formatters.Formatter;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterExecutionException;
import it.greenvulcano.gvesb.adapter.http.formatters.FormatterInitializationException;
import it.greenvulcano.gvesb.adapter.http.utils.AdapterHttpConstants;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.buffer.Id;

import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.txt.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * InboundParamHandlerFormatter class
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class InboundParamHandlerFormatter implements Formatter
{
    private static Logger                                 logger                          = org.slf4j.LoggerFactory.getLogger(InboundParamHandlerFormatter.class);

    /**
     * HTTP MIME-Type value "text/html"
     */
    public final static String                            TEXTHTML_MIMETYPE_NAME          = "text/html";

    /**
     * HTTP MIME-Type value "application/octet-stream"
     */
    public final static String                            OCTETSTREAM_MIMETYPE_NAME       = "application/octet-stream";

    private Map<String, List<InterfaceParametersHandler>> reqParamHandlers                = null;

    private List<InterfaceParametersHandler>              reqContentHandlers              = null;

    private Map<String, String>                           reqGVBufferDefaults             = null;

    private String                                        reqParamEntrySeparator          = null;

    private String                                        reqParamNameValueSeparator      = null;

    private String                                        reqCharacterEncoding            = null;

    private String                                        defaultOperationType            = null;

    private List<String>                                  respParamList                   = null;

    private Map<String, List<InterfaceParametersHandler>> respParamHandlers               = null;

    private Map<String, List<InterfaceParametersHandler>> respParamErrorHandlers          = null;

    private Map<String, String>                           respParamNamesWithinQueryString = null;

    private boolean                                       respURLEncoding                 = false;

    private String                                        respParamEntrySeparator         = null;

    private String                                        respParamNameValueSeparator     = null;

    private String                                        respCharacterEncoding           = null;

    private String                                        respContentType                 = null;

    private String                                        formatterId                     = null;

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#init(org.w3c.
     *      dom.Node)
     */
    @Override
    public void init(Node configurationNode) throws FormatterInitializationException
    {
        try {
            logger.debug("InboundParamHandlerFormatter - Formatter init start");
            reqCharacterEncoding = XMLConfig.get(configurationNode, "@ReqCharacterEncoding", "UTF-8");
            respCharacterEncoding = XMLConfig.get(configurationNode, "@RespCharacterEncoding", "UTF-8");
            respContentType = XMLConfig.get(configurationNode, "@RespContentType",
                    AdapterHttpConstants.TEXTHTML_MIMETYPE_NAME);
            formatterId = XMLConfig.get(configurationNode, "@ID");
            initRequestParameters(configurationNode);
            initResponseParameters(configurationNode);
            logger.debug("InboundParamHandlerFormatter - Formatter init stop");
        }
        catch (XMLConfigException exc) {
            throw new FormatterInitializationException("ERROR INIT FORMATTER", exc);
        }
        catch (ParameterHandlerFactoryException exc) {
            throw new FormatterInitializationException("ERROR INIT FORMATTER", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#marshall(java.util.Map)
     */
    @Override
    public void marshall(Map<String, Object> environment) throws FormatterExecutionException
    {
        String operationType = null;
        GVBuffer inputGVBuffer = null;

        logger.debug("InboundParamHandlerFormatter: BEGIN marshall");
        String requestContent = null;

        try {
            environment.put(AdapterHttpConstants.ENV_KEY_MARSHALL_ENCODING, reqCharacterEncoding);
            inputGVBuffer = setDefaultGVBufferFields(inputGVBuffer);

            HttpServletRequest httpRequest = (HttpServletRequest) environment.get(AdapterHttpConstants.ENV_KEY_HTTP_SERVLET_REQUEST);
            if (httpRequest == null) {
                logger.error("InboundParamHandlerFormatter - Error: HttpServletRequest object received is null");
                throw new FormatterExecutionException("GVHTTP_HTTP_SERVLET_REQUEST_MISSING");
            }
            String reqMethod = httpRequest.getMethod();
            if (reqMethod.equals("GET")) {
                requestContent = URLDecoder.decode(httpRequest.getQueryString(), reqCharacterEncoding);
            }
            else if (reqMethod.equals("POST")) {
                BufferedInputStream bIn = new BufferedInputStream(httpRequest.getInputStream());
                int contentBufferSize = (httpRequest.getContentLength() == -1 ? 10240 : httpRequest.getContentLength());
                byte[] buf = new byte[1024];
                ByteArrayOutputStream bOut = new ByteArrayOutputStream(contentBufferSize);
                int c;
                while ((c = bIn.read(buf, 0, buf.length)) != -1) {
                    bOut.write(buf, 0, c);
                }
                bOut.flush();
                bOut.close();
                byte[] requestContentBytes = bOut.toByteArray();
                logger.debug("INPUT - HTTP request content:\n");
                logger.debug(new Dump(requestContentBytes).toString());
                requestContent = convertContentToString(requestContentBytes, reqCharacterEncoding);
            }

            if (reqParamHandlers.size() > 0) {
                Properties propParam = new Properties();
                List<String> entriesList = TextUtils.splitByStringSeparator(requestContent, reqParamEntrySeparator);
                for (int i = 0; i < entriesList.size(); i++) {
                    String element = (String) entriesList.get(i);
                    logger.debug("manageRequest - Token estratto dalla risposta : " + element);
                    int indexSeparator = element.indexOf(reqParamNameValueSeparator);
                    String paramName = element.substring(0, indexSeparator);
                    String paramValue = element.substring(indexSeparator + reqParamNameValueSeparator.length());
                    logger.debug("manageRequest - paramName: " + paramName + " - paramValue: " + paramValue);
                    propParam.put(paramName, paramValue);
                }

                Iterator<String> paramNames = reqParamHandlers.keySet().iterator();
                while (paramNames.hasNext()) {
                    String currParamName = (String) paramNames.next();
                    List<InterfaceParametersHandler> currParamHandlerList = reqParamHandlers.get(currParamName);
                    String reqParamValue = propParam.getProperty(currParamName);
                    logger.debug("manageRequest - request parameter " + currParamName + " = [" + reqParamValue + "]");
                    if ((reqParamValue != null) && !reqParamValue.equals("")) {
                        for (int i = 0; i < currParamHandlerList.size(); i++) {
                            InterfaceParametersHandler currHandler = (InterfaceParametersHandler) currParamHandlerList.get(i);
                            currHandler.setCharEncoding(reqCharacterEncoding);
                            if (currHandler.getHandlerType() == InterfaceParametersHandler.OPTYPE_HANDLER) {
                                operationType = (String) currHandler.build(reqParamValue, null);
                                logger.debug("manageRequest - current handler is an OpType handler"
                                        + "- operationType = " + operationType);
                            }
                            else if (currHandler.getHandlerType() == InterfaceParametersHandler.GVBUFFER_HANDLER) {
                                inputGVBuffer = (GVBuffer) currHandler.build(reqParamValue, inputGVBuffer);
                                logger.debug("manageRequest - current handler is an GVBuffer handler");
                            }
                        }
                    }
                    else {
                        logger.error("manageRequest - mandatory request parameter missing: " + currParamName);
                        throw new FormatterExecutionException("GVHTTP_MISSING_HTTP_REQUEST_PARAMETER_ERROR",
                                new String[][]{{"paramName", currParamName}});
                    }
                }
            }
            else if (reqContentHandlers.size() > 0) {
                String reqParamValue = requestContent;

                if ((reqParamValue != null) && !reqParamValue.equals("")) {
                    for (int i = 0; i < reqContentHandlers.size(); i++) {
                        InterfaceParametersHandler currHandler = (InterfaceParametersHandler) reqContentHandlers.get(i);
                        currHandler.setCharEncoding(reqCharacterEncoding);
                        if (currHandler.getHandlerType() == InterfaceParametersHandler.OPTYPE_HANDLER) {
                            operationType = (String) currHandler.build(reqParamValue, null);
                            logger.debug("manageRequest - operationType = " + operationType);
                        }
                        else if (currHandler.getHandlerType() == InterfaceParametersHandler.GVBUFFER_HANDLER) {
                            inputGVBuffer = (GVBuffer) currHandler.build(reqParamValue, inputGVBuffer);
                            logger.debug("manageRequest - current handler is an GVBuffer handler");
                        }
                    }
                }
                else {
                    logger.error("manageRequest - request content missing.");
                    throw new FormatterExecutionException("GVHTTP_BAD_HTTP_REQUEST_ERROR", new String[][]{{"errorName",
                            "http request content missing"}});
                }
            }

            if (inputGVBuffer == null) {
                logger.error("manageRequest - input data missing.");
                throw new FormatterExecutionException("GVHTTP_HANDLER_ERROR", new String[][]{{"errorName",
                        "input GVBuffer not generated"}});
            }
            GVTransactionInfo transInfo = null;
            transInfo = (GVTransactionInfo) environment.get(AdapterHttpConstants.ENV_KEY_TRANS_INFO);
            if (transInfo == null) {
                transInfo = new GVTransactionInfo();
                environment.put(AdapterHttpConstants.ENV_KEY_TRANS_INFO, transInfo);
            }
            transInfo.setSystem(inputGVBuffer.getSystem());
            transInfo.setService(inputGVBuffer.getService());
            transInfo.setId(inputGVBuffer.getId());

            environment.put(AdapterHttpConstants.ENV_KEY_GVBUFFER_INPUT, inputGVBuffer);

            if ((operationType == null) || operationType.equals("")) {
                if ((defaultOperationType != null) && !defaultOperationType.equals("")) {
                    operationType = defaultOperationType;
                }
                else {
                    logger.error("manageRequest - Operation type information missing.");
                    throw new FormatterExecutionException("GVHTTP_UNDEFINED_OPERATION_TYPE");
                }
            }

            environment.put(AdapterHttpConstants.ENV_KEY_OP_TYPE, operationType);
            logger.debug("InboundParamHandlerFormatter: END marshall");
        }
        catch (FormatterExecutionException exc) {
            throw exc;
        }
        catch (HttpParamHandlerException exc) {
            if (requestContent != null) {
                logger.error(new Dump(requestContent.getBytes(), -1).toString());
            }
            else {
                logger.error("InboundParamHandlerFormatter - Unable to decode the http request body");
            }
            throw new FormatterExecutionException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", reqCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("InboundParamHandlerFormatter - Error while converting inbound request content to string: "
                    + exc);
            throw new FormatterExecutionException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", reqCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (IOException exc) {
            logger.error("InboundParamHandlerFormatter - Error while reading inbound request content: " + exc);
            throw new FormatterExecutionException("GVHTTP_BAD_HTTP_REQUEST_ERROR", new String[][]{{"errorName",
                    "" + exc}}, exc);
        }
        catch (HandlerException exc) {
            logger.error("InboundParamHandlerFormatter - Error while building input GVBuffer object or retrieving operation type: "
                    + exc);
            throw new FormatterExecutionException("GVHTTP_HANDLER_ERROR", new String[][]{{"errorName", "" + exc}}, exc);
        }
        catch (GVException exc) {
            logger.error("manageRequest - Error while setting GVBuffer fields with default values: " + exc);
            throw new FormatterExecutionException("GVHTTP_GVBUFFER_SETTING_ERROR", new String[][]{{"errorName",
                    "" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error("InboundParamHandlerFormatter - Runtime error while managing inbound request: " + exc);
            throw new FormatterExecutionException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "managing inbound request"}, {"errorName", "" + exc}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#unMarshall(java.util.Map)
     */
    @Override
    public void unMarshall(Map<String, Object> environment) throws FormatterExecutionException
    {
        logger.debug("InboundParamHandlerFormatter: BEGIN unmarshall");
        List<String> paramEntries = new ArrayList<String>();

        Object obj = environment.get(AdapterHttpConstants.ENV_KEY_GVBUFFER_OUTPUT);

        try {
            environment.put(AdapterHttpConstants.ENV_KEY_UNMARSHALL_ENCODING, respCharacterEncoding);
            environment.put(AdapterHttpConstants.ENV_KEY_RESPONSE_CONTENT_TYPE, respContentType);

            String responseString = null;
            if (obj instanceof GVBuffer) {
                GVBuffer outputGVBuffer = (GVBuffer) obj;
                Iterator<String> paramNames = respParamList.iterator();
                while (paramNames.hasNext()) {
                    String currParamEntry = null;
                    String currRespParamName = paramNames.next();
                    String currRespParamValue = null;

                    List<InterfaceParametersHandler> currParamHandlerList = respParamHandlers.get(currRespParamName);
                    if (currParamHandlerList != null) {
                        for (int i = 0; i < currParamHandlerList.size(); i++) {
                            InterfaceParametersHandler currHandler = (InterfaceParametersHandler) currParamHandlerList.get(i);
                            currHandler.setCharEncoding(respCharacterEncoding);
                            logger.debug("manageResponse - response parameter '" + currRespParamName + "' handler n. "
                                    + (i + 1) + " is a " + currHandler.getClass().getName());
                            currRespParamValue = (String) currHandler.build(outputGVBuffer, currRespParamValue);
                            logger.debug("manageResponse - response parameter '" + currRespParamName
                                    + "' value set to: " + currRespParamValue + " by handler n. " + (i + 1));
                        }

                        if (respURLEncoding) {
                            currRespParamValue = URLEncoder.encode(currRespParamValue, "");
                            logger.debug("manageResponse - response parameter '" + currRespParamName
                                    + "' value URLEncoded to: " + currRespParamValue);
                        }

                        currParamEntry = respParamNamesWithinQueryString.get(currRespParamName) + currRespParamValue;
                    }
                    paramEntries.add(currParamEntry);
                }
                responseString = buildResponseString(paramEntries);
            }
            else if (obj instanceof Throwable) {
                GVTransactionInfo transInfo = (GVTransactionInfo) environment.get(AdapterHttpConstants.ENV_KEY_TRANS_INFO);
                if (transInfo == null) {
                    logger.error("InboundParamHandlerFormatter - Error: GVTransactionInfo object received is null");
                    throw new FormatterExecutionException("GVHTTP_HTTP_TRANSACTION_INFO_MISSING");
                }

                Iterator<String> paramNames = respParamList.iterator();
                while (paramNames.hasNext()) {
                    String currParamEntry = null;
                    String currParamName = (String) paramNames.next();
                    String errorValue = null;
                    List<InterfaceParametersHandler> currParamErrorHandlerList = respParamErrorHandlers.get(currParamName);
                    if (currParamErrorHandlerList != null) {
                        for (int i = 0; i < currParamErrorHandlerList.size(); i++) {
                            InterfaceParametersHandler currErrorHandler = (InterfaceParametersHandler) currParamErrorHandlerList.get(i);
                            currErrorHandler.setCharEncoding(respCharacterEncoding);

                            logger.debug("manageErrorResponse - response parameter '" + currParamName
                                    + "' error handler n." + (i + 1) + " is a " + currErrorHandler.getClass().getName());
                            errorValue = (String) currErrorHandler.build(transInfo, errorValue);
                            logger.debug("manageErrorResponse - response parameter '" + currParamName
                                    + "' error value set to: " + errorValue + " by error handler n. " + (i + 1));
                        }

                        if (respURLEncoding) {
                            errorValue = URLEncoder.encode(errorValue, respCharacterEncoding);
                            logger.debug("manageErrorResponse - response parameter '" + currParamName
                                    + "' error value URLEncoded to: " + errorValue);
                        }
                        currParamEntry = respParamNamesWithinQueryString.get(currParamName) + errorValue;
                    }
                    paramEntries.add(currParamEntry);
                }
                responseString = buildResponseString(paramEntries);
                logger.debug("manageErrorResponse - resulting query string is: " + responseString);
            }

            environment.put(AdapterHttpConstants.ENV_KEY_RESPONSE_STRING, responseString);
        }
        catch (FormatterExecutionException exc) {
            throw exc;
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("manageResponse - Can't encode response to invoking system using encoding "
                    + respCharacterEncoding + ": " + exc);
            throw new FormatterExecutionException("GVHTTP_CHARACTER_ENCODING_ERROR", new String[][]{
                    {"encName", respCharacterEncoding}, {"errorName", "" + exc}}, exc);
        }
        catch (HandlerException exc) {
            logger.error("manageResponse - Error while handling response parameters: " + exc);
            throw new FormatterExecutionException("GVHTTP_HANDLER_ERROR", new String[][]{{"errorName", "" + exc}}, exc);
        }
        catch (Throwable exc) {
            logger.error(
                    "manageResponse - Runtime error while returning GreenVulcanoESB response to invoking system: ", exc);
            throw new FormatterExecutionException("GVHTTP_RUNTIME_ERROR", new String[][]{
                    {"phase", "returning GreenVulcanoESB response to invoking system"}, {"errorName", "" + exc}}, exc);
        }
        finally {
            logger.debug("InboundParamHandlerFormatter: END unmarshall");
        }
    }

    /**
     * @see it.greenvulcano.gvesb.adapter.http.formatters.Formatter#getId()
     */
    @Override
    public String getId()
    {
        return formatterId;
    }


    /**
     * @param configurationNode
     * @throws XMLConfigException
     * @throws ParameterHandlerFactoryException
     */
    private void initRequestParameters(Node configurationNode) throws XMLConfigException,
            ParameterHandlerFactoryException
    {
        logger.debug("init - BEGIN Request configuration");
        reqParamHandlers = new HashMap<String, List<InterfaceParametersHandler>>();
        reqContentHandlers = new ArrayList<InterfaceParametersHandler>();
        reqGVBufferDefaults = new HashMap<String, String>();

        reqParamEntrySeparator = XMLConfig.get(configurationNode, "RequestParams/@ParamEntrySeparator", "&");
        reqParamNameValueSeparator = XMLConfig.get(configurationNode, "RequestParams/@ParamNameValueSeparator",
                "=");

        NodeList paramNodes = XMLConfig.getNodeList(configurationNode, "RequestParams/RequestParam");
        if ((paramNodes != null) && (paramNodes.getLength() > 0)) {
            for (int i = 0; i < paramNodes.getLength(); i++) {
                Node currParam = paramNodes.item(i);
                String paramName = XMLConfig.get(currParam, "@Name");
                logger.debug("init - paramName = " + paramName);
                NodeList currParamHandlerNodes = XMLConfig.getNodeList(currParam, "*[@ItemType='Handler']");
                List<InterfaceParametersHandler> handlerList = new ArrayList<InterfaceParametersHandler>(currParamHandlerNodes.getLength());
                int handlerNumber = 0;
                for (int j = 0; j < currParamHandlerNodes.getLength(); j++) {
                    Node currHandler = currParamHandlerNodes.item(j);
                    handlerNumber++;
                    logger.debug("init - instantiating handler n." + handlerNumber + " for request parameter "
                            + paramName);
                    InterfaceParametersHandler handler = InterfaceParametersHandlerFactory.getHandler(currHandler);
                    handlerList.add(handler);
                }
                reqParamHandlers.put(paramName, handlerList);

            }
            logger.debug("init - current handler cache content is: " + dumpHandlersCache(reqParamHandlers, ""));
        }
        else {
            Node contentNode = XMLConfig.getNode(configurationNode, "RequestContent");
            if (contentNode != null) {
                NodeList handlerNodes = XMLConfig.getNodeList(contentNode, "*[@ItemType='Handler']");
                int handlerNumber = 0;
                for (int i = 0; i < handlerNodes.getLength(); i++) {
                    Node currHandler = handlerNodes.item(i);
                    handlerNumber++;
                    logger.debug("init - instantiating handler n." + handlerNumber + " for request content.");
                    InterfaceParametersHandler handler = InterfaceParametersHandlerFactory.getHandler(currHandler);
                    reqContentHandlers.add(handler);
                }
                logger.debug("init - current handler list content is: " + dumpHandlersList(reqContentHandlers));
            }
        }

        NodeList gvFieldDefaultNodes = XMLConfig.getNodeList(configurationNode,
                "GVBufferDefaultValues/GVBufferFieldDefaultValue");
        if ((gvFieldDefaultNodes != null) && (gvFieldDefaultNodes.getLength() > 0)) {
            for (int i = 0; i < gvFieldDefaultNodes.getLength(); i++) {
                Node currDefault = gvFieldDefaultNodes.item(i);
                String fieldName = XMLConfig.get(currDefault, "@FieldName");
                String fieldValue = XMLConfig.get(currDefault, "@FieldValue");
                reqGVBufferDefaults.put(fieldName, fieldValue);
            }
        }

        NodeList gvPropertyDefaultNodes = XMLConfig.getNodeList(configurationNode,
                "GVBufferDefaultValues/GVBufferPropertyDefaultValue");
        if ((gvPropertyDefaultNodes != null) && (gvPropertyDefaultNodes.getLength() > 0)) {
            for (int i = 0; i < gvPropertyDefaultNodes.getLength(); i++) {
                Node currDefault = gvPropertyDefaultNodes.item(i);
                String fieldName = AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTY
                        + AdapterHttpConfig.TOKEN_SEPARATOR + XMLConfig.get(currDefault, "@FieldName");
                String fieldValue = XMLConfig.get(currDefault, "@FieldValue");
                reqGVBufferDefaults.put(fieldName, fieldValue);
            }
        }

        if (reqGVBufferDefaults.size() > 0) {
            logger.debug("init - GVBuffer fields default values: \n" + dumpGVBufferDefaultCache(reqGVBufferDefaults));
        }

        defaultOperationType = XMLConfig.get(configurationNode, "OpTypeDefaultValue/@Value", "");
        logger.debug("init - Operation type default value: " + defaultOperationType);
        logger.debug("init - END Request configuration");
    }

    /**
     * @param configurationNode
     * @throws XMLConfigException
     * @throws ParameterHandlerFactoryException
     */
    private void initResponseParameters(Node configurationNode) throws XMLConfigException,
            ParameterHandlerFactoryException
    {
        logger.debug("init - BEGIN Response configuration");
        respParamList = new ArrayList<String>();
        respParamHandlers = new HashMap<String, List<InterfaceParametersHandler>>();
        respParamErrorHandlers = new HashMap<String, List<InterfaceParametersHandler>>();
        respParamNamesWithinQueryString = new HashMap<String, String>();

        respURLEncoding = XMLConfig.getBoolean(configurationNode, "ResponseParams/@URLEncoding", false);
        logger.debug("init - encode response parameters = " + respURLEncoding);

        respParamEntrySeparator = XMLConfig.get(configurationNode, "ResponseParams/@ParamEntrySeparator", "&");
        respParamNameValueSeparator = XMLConfig.get(configurationNode, "ResponseParams/@ParamNameValueSeparator", "=");
        logger.debug("init - paramNameValueSeparator = '" + respParamNameValueSeparator + "'"
                + " - paramEntrySeparator = '" + respParamEntrySeparator + "'");

        NodeList paramNodes = XMLConfig.getNodeList(configurationNode, "ResponseParams/ResponseParam");
        for (int i = 0; i < paramNodes.getLength(); i++) {
            Node currParam = paramNodes.item(i);
            String paramName = XMLConfig.get(currParam, "@Name");
            logger.debug("init - paramName = " + paramName);
            respParamList.add(paramName);
            boolean sendParamName = XMLConfig.getBoolean(currParam, "@SendParameterName");
            logger.debug("init - sendParamName = " + sendParamName);

            if (sendParamName) {
                respParamNamesWithinQueryString.put(paramName, paramName + respParamNameValueSeparator);
            }
            else {
                respParamNamesWithinQueryString.put(paramName, "");
            }

            NodeList currParamHandlerNodes = XMLConfig.getNodeList(currParam, "*[@ItemType='Handler']");
            List<InterfaceParametersHandler> handlerList = new ArrayList<InterfaceParametersHandler>(currParamHandlerNodes.getLength());
            int handlerNumber = 0;
            for (int j = 0; j < currParamHandlerNodes.getLength(); j++) {
                Node currHandler = currParamHandlerNodes.item(j);
                handlerNumber++;
                logger.debug("init - instantiating handler n." + handlerNumber + " for response parameter " + paramName);
                InterfaceParametersHandler handler = InterfaceParametersHandlerFactory.getHandler(currHandler);
                handlerList.add(handler);
            }
            respParamHandlers.put(paramName, handlerList);

            NodeList currParamErrorHandlerNodes = XMLConfig.getNodeList(currParam, "*[@ItemType='ErrorHandler']");
            List<InterfaceParametersHandler> errorHandlerList = new ArrayList<InterfaceParametersHandler>(currParamErrorHandlerNodes.getLength());
            handlerNumber = 0;
            for (int j = 0; j < currParamErrorHandlerNodes.getLength(); j++) {
                Node currErrorHandler = currParamErrorHandlerNodes.item(j);
                handlerNumber++;
                logger.debug("init - instantiating error handler n." + handlerNumber + " for response parameter "
                        + paramName);
                InterfaceParametersHandler errorHandler = InterfaceParametersHandlerFactory.getHandler(currErrorHandler);
                errorHandlerList.add(errorHandler);
            }
            respParamErrorHandlers.put(paramName, errorHandlerList);

        }
        logger.debug("init - handler cache content is: " + dumpHandlersCache(respParamHandlers, ""));
        logger.debug("init - error handler cache content is: " + dumpHandlersCache(respParamErrorHandlers, "Error"));
        logger.debug("init - END Request configuration");
    }

    /**
     * Gets a <code>List</code> of strings in the format name=value and
     * concatenate them into a response string.
     * 
     * @param params
     *        a <code>List</code> of strings in the format name=value.
     * @return the resulting response query string.
     */
    private String buildResponseString(List<String> params)
    {
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < params.size(); i++) {
            String currEntry = params.get(i);
            buf.append(currEntry);
            if (i < (params.size() - 1)) {
                buf.append(respParamEntrySeparator);
            }
        }
        String result = buf.toString();
        logger.debug("buildResponseString - resulting query string is: " + result);
        return result;
    }

    /**
     * Convert a byte array to a String according to the character encoding of
     * the invoking system.
     */
    private String convertContentToString(byte[] content, String configCharEncoding)
            throws UnsupportedEncodingException
    {
        String result;
        String usedEncoding;
        if ((configCharEncoding != null) && !configCharEncoding.equals("")) {
            result = new String(content, configCharEncoding);
            usedEncoding = configCharEncoding;
        }
        else {
            result = new String(content);
            usedEncoding = "Java default";
        }

        logger.debug("convertContentToString - using character encoding: " + usedEncoding);
        return result;
    }

    /**
     * Sets configured default values (if any) for input GVBuffer object fields.
     * 
     * @param inputgvBuffer
     *        the inbounds request <tt>GVBuffer</tt> object.
     * @return an <tt>GVBuffer</tt> object with some fields set to default
     *         values, or null if no default values were configured.
     * @throws IBException
     *         if any error occurs.
     */
    private GVBuffer setDefaultGVBufferFields(GVBuffer inputgvBuffer) throws GVException
    {
        if (reqGVBufferDefaults.size() > 0) {
            if (inputgvBuffer == null) {
                inputgvBuffer = new GVBuffer();
            }

            Iterator<String> iter = reqGVBufferDefaults.keySet().iterator();
            while (iter.hasNext()) {
                String currFieldName = iter.next();
                String currFieldValue = reqGVBufferDefaults.get(currFieldName);

                if (currFieldName.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_SYSTEM)) {
                    inputgvBuffer.setSystem(currFieldValue);
                    logger.debug("setDefaultGVBufferFields - GVBuffer field 'system' populated with DEFAULT value: "
                            + currFieldValue);
                }
                else if (currFieldName.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_SERVICE)) {
                    inputgvBuffer.setService(currFieldValue);
                    logger.debug("setDefaultGVBufferFields - GVBuffer field 'service' populated with DEFAULT value: "
                            + currFieldValue);
                }
                else if (currFieldName.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_ID)) {
                    inputgvBuffer.setId(new Id(currFieldValue));
                    logger.debug("setDefaultGVBufferFields - GVBuffer field 'tid' populated with DEFAULT value: "
                            + currFieldValue);
                }
                else if (currFieldName.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_RETCODE)) {
                    inputgvBuffer.setRetCode(Integer.parseInt(currFieldValue));
                    logger.debug("setDefaultGVBufferFields - GVBuffer field 'retCode' populated with DEFAULT value: "
                            + currFieldValue);
                }
                else if (currFieldName.equals(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_GVBUFFER)) {
                    inputgvBuffer.setObject(currFieldValue);
                    logger.debug("setDefaultGVBufferFields - GVBuffer field 'object' populated with DEFAULT value: "
                            + currFieldValue);
                }
                else if (currFieldName.startsWith(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTY)) {
                    inputgvBuffer.setProperty(getPropertyName(currFieldName), currFieldValue);
                    logger.debug("setDefaultGVBufferFields - GVBuffer property '" + getPropertyName(currFieldName)
                            + "' populated with DEFAULT value: " + currFieldValue);
                }
            }
        }

        return inputgvBuffer;
    }

    /**
     * Returns the name of a configured <tt>GVBuffer</tt> (or
     * <tt>GVTransactionInfo</tt>) property.
     * 
     * @param variable
     *        the string from which the <tt>GVBuffer</tt> (or
     *        <tt>GVTransactionInfo</tt>) property name must be extracted.
     * @return the name of a configured <tt>GVBuffer</tt> (or
     *         <tt>GVTransactionInfo</tt>) property.
     */
    private String getPropertyName(String variable)
    {
        String fieldName = null;
        if (variable.startsWith(AdapterHttpConfig.MAPPING_VARIABLE_VALUE_PROPERTY)) {
            int idx = variable.indexOf(AdapterHttpConfig.TOKEN_SEPARATOR);
            fieldName = variable.substring(idx + 1);
        }
        return fieldName;
    }

    /**
     * Dumps current parameter handlers cache content
     * 
     * @return a string representation of current parameter handlers cache
     *         content
     */
    private String dumpHandlersCache(Map<String, List<InterfaceParametersHandler>> cache, String handlerType)
    {
        StringBuilder buf = new StringBuilder("\n");
        for (Entry<String, List<InterfaceParametersHandler>> entry : cache.entrySet()) {
            String currParam = entry.getKey();
            if ((currParam != null) && !currParam.equals("")) {
                buf.append(handlerType + " Handlers for parameter " + currParam + ":\n");
                List<InterfaceParametersHandler> handlerList = entry.getValue();
                for (InterfaceParametersHandler currHandler : handlerList) {
                    if (currHandler != null) {
                        String handlerfullClassname = currHandler.getClass().getName();
                        String handlerClassname = handlerfullClassname.substring(handlerfullClassname.lastIndexOf(".") + 1);
                        buf.append("\t").append(handlerClassname).append("\n");
                    }
                }
            }
        }
        return buf.toString();
    }

    /**
     * Dumps current content handlers list
     * 
     * @return a string representation of current content handlers list
     */
    private String dumpHandlersList(List<InterfaceParametersHandler> list)
    {
        StringBuilder buf = new StringBuilder("\n");
        buf.append("Response content handlers:\n");
        for (InterfaceParametersHandler currHandler : list) {
            if (currHandler != null) {
                String handlerfullClassname = currHandler.getClass().getName();
                String handlerClassname = handlerfullClassname.substring(handlerfullClassname.lastIndexOf(".") + 1);
                buf.append("\t").append(handlerClassname).append("\n");
            }
        }
        return buf.toString();
    }

    /**
     * Dumps current default GVBuffer value content
     * 
     * @return a string representation of current default GVBuffer value cache
     *         content
     */
    private String dumpGVBufferDefaultCache(Map<String, String> defaults)
    {
        StringBuilder buf = new StringBuilder("\n");
        for (Entry<String, String> entry : defaults.entrySet()) {
            String currParam = entry.getKey();
            if ((currParam != null) && !currParam.equals("")) {
                buf.append("Default for field " + currParam + ": '").append(entry.getValue()).append("'\n");
            }
        }
        return buf.toString();
    }

}
