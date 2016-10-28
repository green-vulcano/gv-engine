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
package it.greenvulcano.gvesb.virtual.ws;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.DomMessageContext;
import it.greenvulcano.gvesb.http.auth.HttpAuth;
import it.greenvulcano.gvesb.http.auth.HttpAuthFactory;
import it.greenvulcano.gvesb.http.proxy.HttpProxy;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.ws.dynamic.invoker.DynamicInvoker;
import it.greenvulcano.gvesb.virtual.ws.module.ModuleHandler;
import it.greenvulcano.gvesb.virtual.ws.module.ModuleHandlerFactory;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
@SuppressWarnings("deprecation")
public class GVWebServiceInvoker
{
    /**
     * The log4j logger
     */
    private static final Logger        logger      = org.slf4j.LoggerFactory.getLogger(GVWebServiceInvoker.class);

    /**
     * The reference to the data provider configured
     */
    private String                     refDP       = null;

    private Vector<OMNamespace>        nsVector    = null;

    private String                     service     = "";

    private String                     serviceNS   = "";

    private String                     operation   = "";

    private boolean                    emptyAction = false;

    private String                     portName    = "";

    private String                     wsdlURL     = "";
    
    private String                     wsEndpointURL  = "";

    private long                       timeout     = -1;

    private String                     returnType  = "";

    private Map<String, ModuleHandler> modulesMap  = new HashMap<String, ModuleHandler>();

    private boolean                    useSAAJ     = false;

    private boolean                    throwsFault = false;
    
    private HttpProxy                  proxy       = null;
    private HttpAuth                   auth        = null;

    /**
     * It initializes the object whit the his configuration.
     * 
     * @param configNode
     *        org.w3c.dom.Node configuration
     * @throws WSCallException
     *         if an error occurred.
     */
    public void init(Node configNode) throws WSCallException
    {
        logger.debug("BEGIN init(Configuration config)");

        try {
            serviceNS = XMLConfig.get(configNode, "@serviceNS", "");
            service = XMLConfig.get(configNode, "@service", "");
            operation = XMLConfig.get(configNode, "@operation", "");
            portName = XMLConfig.get(configNode, "@portName", null);
            timeout = XMLConfig.getLong(configNode, "@timeout", -1);
            returnType = XMLConfig.get(configNode, "@returnType", "context");
            throwsFault = XMLConfig.getBoolean(configNode, "@throwsFault", false);
            emptyAction = XMLConfig.getBoolean(configNode, "@emptyAction", false);

            logger.debug("Create WSDLInfo");
            // WSDLInfo
            Node wsdlConfiguration = XMLConfig.getNode(configNode, "*[@type='wsdlinfo']");
            wsdlURL = PropertiesHandler.expand(XMLConfig.get(wsdlConfiguration, "@wsdl"));
            if (wsdlURL != null) {
                wsdlURL= TextUtils.replaceSubstring(wsdlURL, "\\", "/");
            }
            wsEndpointURL = XMLConfig.get(wsdlConfiguration, "@ws-endpoint-url", null);
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("The GVWebServiceInvoker initialization parameters value:\n");
                sb.append("\twsdlURL '").append(wsdlURL).append("'\n");
                sb.append("\twsEndpointURL '");
                if (wsEndpointURL != null) {
                    sb.append(wsEndpointURL).append("'\n");
                }
                sb.append("'\n");
                sb.append("\tserviceNS '").append(serviceNS).append("'\n");
                sb.append("\tservice '").append(service).append("'\n");
                sb.append("\toperation '").append(operation).append("'\n");
                sb.append("\temptyAction '").append(emptyAction).append("'\n");
                sb.append("\tportName '");
                if (portName != null) {
                    sb.append(portName);
                }
                sb.append("'\n");
                sb.append("\treturnType '").append(returnType).append("'\n");
                logger.debug(sb.toString());
            }

            refDP = XMLConfig.get(configNode, "@ref-dp", "");
            useSAAJ = XMLConfig.getBoolean(configNode, "@use-saaj", false);

            addNamespaces(XMLConfig.getNodeList(configNode, "XPathNamespace"));

            registerModules(XMLConfig.getNodeList(configNode, "EngageModule"));
            
            proxy = new HttpProxy();
            proxy.init(XMLConfig.getNode(configNode, "Proxy"));
            
            auth = HttpAuthFactory.getInstance(XMLConfig.getNode(configNode, "*[@type='http-auth']"));
        }
        catch (Exception exc) {
            logger.error("Exception: " + exc, exc);
            throw new WSCallException("GVVM_WS_INIT_ERROR:" + exc, exc);
        }
        logger.debug("END init(Node configNode)");
    }

    /**
     * It performs the invocation of the web service.
     * 
     * @param gvBuffer
     *        the input GVBuffer
     * @return the output GVBuffer
     * @throws WSCallException
     *         if an error occurred.
     */
    public GVBuffer perform(GVBuffer gvBuffer) throws WSCallException
    {
        logger.debug("BEGIN perform(GVBuffer data)");
        GVBuffer output = null;

        DynamicInvoker invoker = null;
        try {
            logger.debug("Create and initialize the Axis Dynamic Invoker");
            // Axis2 Dynamic Invoker
            invoker = DynamicInvoker.getInvoker(wsdlURL);
            invoker.setService(serviceNS, service, null);
            invoker.setOperation(operation, portName);
            invoker.setEmptyAction(emptyAction);
            invoker.setTimeout(timeout);
            invoker.setThrowsFault(throwsFault);

            Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO(gvBuffer, true);
            Object result = null;

            if (!useSAAJ) {
            	DomMessageContext messageContext = null;
                OperationClient operationClient = invoker.prepareOperationClient();
                
                if ((refDP != null) && (refDP.length() > 0)) {
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                    logger.debug("Calling configured Data Provider: " + dataProvider);
                    try {
                        dataProvider.setContext(operationClient.getOptions());
                        dataProvider.setObject(gvBuffer);
                        messageContext = (DomMessageContext) dataProvider.getResult();
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(refDP, dataProvider);
                    }
                }
                else {
                    messageContext = (DomMessageContext) gvBuffer.getObject();
                }

                String wsEp = gvBuffer.getProperty("WS_ENDPOINT_URL");
                if (wsEp == null) {
                    wsEp = wsEndpointURL;
                }
                if (wsEp == null) {
                    wsEp = operationClient.getOptions().getTo().getAddress();
                }
                wsEp = PropertiesHandler.expand(wsEp, params, gvBuffer);

                URL ws = new URL(wsEp);
                String host = ws.getHost();
                /*int port = ws.getPort();
                if (port == -1) {
                    port = ws.getDefaultPort();
                }*/
                auth.setAuthentication(messageContext.getOptions(), host, gvBuffer, params);
                proxy.setProxy(messageContext.getOptions(), gvBuffer, params);

                // Invoke the web service
                result = invoker.execute(operationClient, messageContext, modulesMap, wsEp);
                MessageContext resMCtx = (MessageContext) result;
                if (!resMCtx.getAttachmentMap().getMap().isEmpty()) {
                    Attachments atts = resMCtx.getAttachmentMap();
                    atts.getAllContentIDs();
                }

                if (returnType.equals("context")) {
                    // do nothing
                }
                else if (returnType.equals("envelope")) {
                    result = resMCtx.getEnvelope().toString();
                }
                else if (returnType.equals("body")) {
                    result = resMCtx.getEnvelope().getBody().toString();
                }
                else if (returnType.equals("body-element")) {
                    result = resMCtx.getEnvelope().getBody().getFirstElement().toString();
                }
                else if (returnType.equals("header")) {
                    result = resMCtx.getEnvelope().getHeader().toString();
                }
                else if (returnType.equals("envelope-om")) {
                    result = resMCtx.getEnvelope();
                }
                else if (returnType.equals("body-om")) {
                    result = resMCtx.getEnvelope().getBody();
                }
                else if (returnType.equals("body-element-om")) {
                    result = resMCtx.getEnvelope().getBody().getFirstElement();
                }
                else { // returnType.equals("header-om")
                    result = resMCtx.getEnvelope().getHeader();
                }
            }
            else {
                SOAPMessage message = null;

                if ((refDP != null) && (refDP.length() > 0)) {
                    logger.debug("Calling configured Data Provider: " + refDP);
                    DataProviderManager dataProviderManager = DataProviderManager.instance();
                    IDataProvider dataProvider = dataProviderManager.getDataProvider(refDP);
                    try {
                        dataProvider.setContext(invoker.getOperationDescription().isUseSOAP12Namespace()
                                ? SOAPConstants.SOAP_1_2_PROTOCOL
                                : SOAPConstants.SOAP_1_1_PROTOCOL);
                        dataProvider.setObject(gvBuffer);
                        message = (SOAPMessage) dataProvider.getResult();
                    }
                    finally {
                        dataProviderManager.releaseDataProvider(refDP, dataProvider);
                    }
                }
                else {
                    message = (SOAPMessage) gvBuffer.getObject();
                }

                String wsEp = gvBuffer.getProperty("WS_ENDPOINT_URL");
                if (wsEp == null) {
                    wsEp = invoker.getServiceDescription().getAddress();
                }
                wsEp = PropertiesHandler.expand(wsEp, params, gvBuffer);

                // Invoke the web service
                result = invoker.execute(message, wsEp);
            }

            output = new GVBuffer(gvBuffer);
            output.setObject(result);
        }
        catch (Exception exc) {
            logger.error("Exception on perform the operation call: " + exc, exc);
            throw new WSCallException("GVM_WS_ERROR", new String[][]{{"exception", "" + exc}}, exc);
        }
        finally {
            DynamicInvoker.returnInvoker(invoker);
        }
        logger.debug("END perform(GVBuffer data)");
        return output;
    }

    private void addNamespaces(NodeList configuration) throws XMLConfigException
    {
        nsVector = new Vector<OMNamespace>();
        for (int i = 0; i < configuration.getLength(); i++) {
            Node conf = configuration.item(i);
            nsVector.add(new OMNamespaceImpl(XMLConfig.get(conf, "@namespace"), XMLConfig.get(conf, "@prefix")));
        }
    }

    private void registerModules(NodeList modules) throws InitializationException
    {
        if (modules == null) {
            return;
        }
        for (int i = 0; i < modules.getLength(); i++) {
            Node config = modules.item(i);
            ModuleHandler mh = ModuleHandlerFactory.getModuleHandler(config);
            modulesMap.put(mh.getName(), mh);
        }
    }

    /**
     * Used to force <code>DynamicInvoker</code> to clean up invokers using the
     * current WSDL
     */
    public void destroy()
    {
        DynamicInvoker.reload(wsdlURL);
    }

    /**
     * Does nothing
     */
    public void cleanUp()
    {
        // do nothing
    }
}