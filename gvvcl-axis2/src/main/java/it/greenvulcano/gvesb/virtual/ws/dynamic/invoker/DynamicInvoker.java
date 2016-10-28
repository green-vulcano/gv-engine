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
package it.greenvulcano.gvesb.virtual.ws.dynamic.invoker;

import it.greenvulcano.gvesb.virtual.ws.WSCallException;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.OperationDescription;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ServiceDescrBuilder;
import it.greenvulcano.gvesb.virtual.ws.dynamic.descr.ServiceDescription;
import it.greenvulcano.gvesb.virtual.ws.module.ModuleHandler;
import it.greenvulcano.gvesb.ws.axis2.context.Axis2ConfigurationContextHelper;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.slf4j.Logger;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DynamicInvoker
{
    private final static Logger                        logger              = org.slf4j.LoggerFactory.getLogger(DynamicInvoker.class);

    private static Map<String, Stack<DynamicInvoker>>  invokersCache       = new LinkedHashMap<String, Stack<DynamicInvoker>>();
    private static Map<String, Vector<DynamicInvoker>> inUseInvokers       = new LinkedHashMap<String, Vector<DynamicInvoker>>();

    private String                                     wsdlLocation        = null;
    private String                                     _serviceNS;
    private String                                     _serviceName;
    private String                                     _portTypeName;
    private String                                     _operationName;
    private String                                     _portName;
    private boolean                                    throwsFault         = false;
    private boolean                                    emptyAction         = false;

    private long                                       timeout             = -1;

    private Map<String, ServiceDescription>            serviceDescriptions = null;

    private ServiceClient                              client              = null;

    /**
     * Class constructor.
     * 
     * @param wsdlURL
     *        The WSDL URL
     * @throws WSCallException
     *         if an error occurred
     */
    private DynamicInvoker(String wsdlLoc) throws WSCallException
    {
        ServiceDescrBuilder desc = new ServiceDescrBuilder(wsdlLoc);
        serviceDescriptions = desc.getServices();
        wsdlLocation = wsdlLoc;
    }

    /**
     * @param wsdlLocation
     * @return the cached invoker
     * @throws WSCallException
     */
    public static synchronized DynamicInvoker getInvoker(String wsdlLocation) throws WSCallException
    {
        if (wsdlLocation == null) {
            throw new WSCallException("WSDL_LOCATION_NULL", new String[][]{{"cause",
                    "WSDL Location parameter cannot be null."}});
        }
        Stack<DynamicInvoker> invokersStack = invokersCache.get(wsdlLocation);
        if (invokersStack == null) {
            invokersStack = new Stack<DynamicInvoker>();
            invokersCache.put(wsdlLocation, invokersStack);
        }

        // Get from cache if present
        DynamicInvoker invoker = null;
        if (!invokersStack.isEmpty()) {
            invoker = invokersStack.pop();
        }
        // else create new
        if (invoker == null) {
            invoker = new DynamicInvoker(wsdlLocation);
        }

        // put in inUse map
        Vector<DynamicInvoker> inUseVector = inUseInvokers.get(wsdlLocation);
        if (inUseVector == null) {
            inUseVector = new Vector<DynamicInvoker>();
            inUseInvokers.put(wsdlLocation, inUseVector);
        }
        inUseVector.add(invoker);

        return invoker;
    }

    /**
     * @param invoker
     */
    public synchronized static void returnInvoker(DynamicInvoker invoker)
    {
        if (invoker != null) {
            invoker.cleanup();
            String wsdlLocation = invoker.getWsdlLocation();
            Vector<DynamicInvoker> inUseVector = inUseInvokers.get(wsdlLocation);
            if (inUseVector != null) {
                // if invoker was used and has not been discarded
                // must be reinserted in invokers stack to get reused
                if (inUseVector.remove(invoker)) { // && !invoker.discard
                    Stack<DynamicInvoker> invokersStack = invokersCache.get(wsdlLocation);
                    if (invokersStack == null) {
                        invokersStack = new Stack<DynamicInvoker>();
                        invokersCache.put(invoker.getWsdlLocation(), invokersStack);
                    }
                    invokersStack.push(invoker);
                }
            }
        }
    }

    private String getWsdlLocation()
    {
        return wsdlLocation;
    }

    /**
     * @param serviceNS
     * @param serviceName
     * @param portTypeName
     * @throws WSCallException
     */
    public void setService(String serviceNS, String serviceName, String portTypeName) throws WSCallException
    {
        if (serviceName == null) {
            throw new WSCallException("SERVICE_NAME_NULL", new String[][]{{"cause", "Service name cannot be null"}});
        }
        if (((serviceNS != null) && !serviceNS.equals(_serviceNS)) || !serviceName.equals(_serviceName)
                || ((portTypeName != null) && !portTypeName.equals(_portTypeName))) {
            _serviceNS = serviceNS;
            _serviceName = serviceName;
            _portTypeName = portTypeName;
        }
    }

    /**
     * @param operationName
     * @param portName
     * @throws WSCallException
     */
    public void setOperation(String operationName, String portName) throws WSCallException
    {
        if (operationName == null) {
            throw new WSCallException("OPERATION_NAME_NULL", new String[][]{{"cause", "Operation name cannot be null"}});
        }
        if (!operationName.equals(_operationName) || ((portName != null) && !portName.equals(_portName))) {
            _operationName = operationName;
            _portName = portName;
        }
    }

    /**
     * @param timeout
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return the loaded WSDLs
     */
    public synchronized static String[] getLoadedWSDL()
    {
        try {
            Set<String> keys = invokersCache.keySet();
            String[] arr = new String[keys.size()];
            keys.toArray(arr);
            return arr;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of loaded WSDL files", exc);
            return new String[0];
        }
    }

    /**
     * @return the number of invokers actually in use.
     */
    public synchronized static Map<String, Integer> getInUseInvokers()
    {
        try {
            Map<String, Integer> ret = new HashMap<String, Integer>();
            Iterator<String> itr = inUseInvokers.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                Vector<?> value = inUseInvokers.get(key);
                ret.put(key, new Integer(value.size()));
            }
            return ret;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of in-use invokers", exc);
            return new HashMap<String, Integer>();
        }
    }

    /**
     * @return the number of invokers actually in cache.
     */
    public synchronized static Map<String, Integer> getInCacheInvokers()
    {
        try {
            HashMap<String, Integer> ret = new HashMap<String, Integer>();
            Iterator<String> itr = invokersCache.keySet().iterator();
            while (itr.hasNext()) {
                String key = itr.next();
                Vector<?> value = invokersCache.get(key);
                ret.put(key, new Integer(value.size()));
            }
            return ret;
        }
        catch (Exception exc) {
            logger.warn("JMX: Error while returning the list of cached invokers", exc);
            return new LinkedHashMap<String, Integer>();
        }
    }

    /**
     * @param wsdl
     */
    public synchronized static void reload(String wsdl)
    {
        inUseInvokers.remove(wsdl);
        invokersCache.remove(wsdl);
    }

    /**
     *
     */
    public synchronized static void reloadAll()
    {
        inUseInvokers.clear();
        invokersCache.clear();
    }

    /**
     * @param messageContext
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(MessageContext messageContext) throws WSCallException
    {
        return execute(messageContext, null, null);
    }

    /**
     * @return the new <code>OperationClient</code> to invoke the WebService
     * @throws WSCallException
     * @throws AxisFault
     * @throws MalformedURLException
     * @throws PropertiesHandlerException
     */
    public OperationClient prepareOperationClient() throws WSCallException, AxisFault, MalformedURLException,
            PropertiesHandlerException
    {
        ServiceDescription svcDesc = getServiceDescription();
        OperationDescription opDesc = getOperationDescription();

        client = new ServiceClient(Axis2ConfigurationContextHelper.getConfigurationContext(), null);
        OperationClient operationClient = client.createClient(ServiceClient.ANON_OUT_IN_OP);
        Options options = operationClient.getOptions();

        // Prevent "Content-lenght required" error
        options.setProperty(HTTPConstants.CHUNKED, false);
        
        boolean isREST = opDesc.getVerb() != null;
        if (timeout > 0) {
            long timeoutInMilliseconds = timeout * 1000;
            options.setTimeOutInMilliSeconds(timeoutInMilliseconds);
        }
        if (emptyAction) {
        	logger.debug("Setting Action Header to: ");
        	options.setAction("");
        	options.setProperty(org.apache.axis2.Constants.Configuration.DISABLE_SOAP_ACTION, true);
        }
        else {
            String soapAction = opDesc.getSOAPAction();
            if (soapAction != null) {
                logger.debug("Setting Action Header to: " + soapAction);
                options.setAction(soapAction);
            }
            else {
                logger.debug("Setting Action Header to: " + _operationName);
                options.setAction(_operationName);
            }
        }
        String epr = svcDesc.getAddress();
        if (isREST) {
            epr += "/" + _operationName;
            logger.debug("Setting REST mode");
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            logger.debug("Setting REST Verb: " + opDesc.getVerb());
            options.setProperty(Constants.Configuration.HTTP_METHOD, opDesc.getVerb());
            logger.debug("Setting REST MessageType: " + opDesc.getMediaType());
            // options.setProperty(Constants.Configuration.MESSAGE_TYPE, opDesc.getMediaType());
            options.setProperty(Constants.Configuration.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_X_WWW_FORM);
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_X_WWW_FORM);
        }
        logger.debug("Setting EPR: " + epr);
        options.setTo(new EndpointReference(epr));

        logger.debug("Current SoapVersionURI: " + options.getSoapVersionURI());
        if (opDesc.isUseSOAP12Namespace()) {
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            logger.debug("Overridden SoapVersionURI: " + options.getSoapVersionURI());
        }
        return operationClient;
    }

    /**
     * @param messageContext
     * @param modules
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(MessageContext messageContext, Map<String, ModuleHandler> modules, String wsEp)
            throws WSCallException
    {
        return execute(null, messageContext, modules, wsEp);
    }

    /**
     * @param operationClient
     * @param messageContext
     * @param modules
     * @return the result of the execution
     * @throws WSCallException
     */
    public MessageContext execute(OperationClient operationClient, MessageContext messageContext,
            Map<String, ModuleHandler> modules, String wsEp) throws WSCallException
    {
        logger.debug("Invoking operation '" + _operationName + "' using WSDL from " + getWsdlLocation());

        MessageContext result = null;
        debugMessageContext(messageContext, "INPUT");
        try {
            if (operationClient == null) {
                operationClient = prepareOperationClient();
            }

            Exception fault = null;
            operationClient.addMessageContext(messageContext);

            if ((wsEp != null) && !wsEp.equals("")) {
                operationClient.getOptions().setTo(new EndpointReference(wsEp));
                logger.debug("DynamicInvoker - Forced EndPoint: " + wsEp);
            }

            try {
                preSendOperations(client, modules);
                operationClient.execute(true);
                postSendOperations(client, modules);
            }
            catch (AxisFault exc) {
                fault = exc;
                logger.error("Error invoking operation " + _operationName, exc);
                postSendFaultOperations(client, modules);
                if (throwsFault) {
                    throw new WSCallException("ERROR_INVOKING_OPERATION", new String[][]{
                            {"cause", "Error invoking operation " + _operationName}, {"fault", "" + exc}}, exc);
                }
            }
            result = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            if ((result != null) && (result.getEnvelope() != null)) {
                logger.debug("OUTPUT: " + result);
            }
            else {
                logger.debug("Service request returned with NULL message!");
                if (fault != null) {
                    throw new WSCallException("ERROR_INVOKING_OPERATION", new String[][]{
                            {"cause", "Error invoking operation " + _operationName}, {"fault", "" + fault}}, fault);
                }
            }
        }
        catch (RemoteException re) {
            logger.error("Cannot execute service", re);
            throw new WSCallException("REMOTE_EXCEPTION_OCCURRED", new String[][]{{"cause", "Cannot execute service"}},
                    re);
        }
        catch (WSCallException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Cannot execute service", e);
            throw new WSCallException("EXCEPTION_OCCURRED", new String[][]{{"cause", "Cannot execute service"}}, e);
        }

        if (result != null) {
            debugMessageContext(result, "OUTPUT");
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Service request returned with NULL message!");
        }
        return result;
    }

    /**
     * @param message
     * @return the result of the execution
     * @throws WSCallException
     */
    public SOAPMessage execute(SOAPMessage message, String wsEp) throws WSCallException
    {
        logger.info("Invoking operation '" + _operationName + "' using WSDL from " + getWsdlLocation());

        debugSOAPMessage(message, "INPUT");

        if (!getOperationDescription().isUseSOAP12Namespace()) {
            MimeHeaders headers = message.getMimeHeaders();
            if (headers != null) {
                String soapAction[] = headers.getHeader("SOAPAction");
                if ((soapAction == null) || (soapAction.length == 0)) {
                    headers.setHeader("SOAPAction", "\"\"");
                }
            }
        }

        SOAPMessage result = null;
        try {
            URL urlEndpoint = new URL(getServiceDescription().getAddress());
            if ((wsEp != null) && !wsEp.equals("")) {
                urlEndpoint = new URL(wsEp);
                logger.debug("DynamicInvoker - Force EndPoint: " + urlEndpoint);
            }

            // Send the message to the endpoint using the connection.
            SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();
            System.out.println("SOAP Connection Impl: " + con);
            result = con.call(message, urlEndpoint);
        }
        catch (Exception exc) {
            logger.error("Error invoking operation " + _operationName, exc);
            throw new WSCallException("ERROR_INVOKING_OPERATION", new String[][]{{"cause",
                    "Error invoking operation " + _operationName}}, exc);
        }

        if (result != null) {
            debugSOAPMessage(result, "OUTPUT");
        }
        else {
            logger.debug("Service request returned with NULL message!");
        }
        return result;
    }

    private void debugSOAPMessage(SOAPMessage current, String desc)
    {
        if (logger.isDebugEnabled() && (current != null)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                current.writeTo(baos);
            }
            catch (SOAPException exc) {
                logger.warn("SOAPException debugging " + desc + " message for operation call: " + _operationName, exc);
            }
            catch (IOException exc) {
                logger.warn("IOException debugging " + desc + " message for operation call: " + _operationName, exc);
            }
            logger.debug(desc + ": {\n" + baos.toString() + "\n}");
        }
    }

    private void debugMessageContext(MessageContext current, String desc)
    {
        if (logger.isDebugEnabled() && (current != null)) {
            logger.debug(desc + ": {\n" + current.getEnvelope() + "\n}");
        }
    }

    /**
     *
     */
    public void cleanup()
    {
        try {
            if (client != null) {
                client.cleanupTransport();
            }
        }
        catch (AxisFault exc) {
            exc.printStackTrace();
        }
    }

    private void preSendOperations(ServiceClient client, Map<String, ModuleHandler> modules) throws AxisFault
    {
        if (modules != null) {
            for (ModuleHandler mh : modules.values()) {
                String moduleName = mh.getName();
                logger.debug("preSendOperations - engageModule: " + moduleName);
                client.engageModule(moduleName);
                mh.preSendOperation(client, client.getOptions());
            }
        }
    }

    private void postSendOperations(ServiceClient client, Map<String, ModuleHandler> modules) throws AxisFault
    {
        if (modules != null) {
            for (ModuleHandler mh : modules.values()) {
                mh.postSendOperation(client, client.getOptions());
            }
        }
    }

    private void postSendFaultOperations(ServiceClient client, Map<String, ModuleHandler> modules) throws AxisFault
    {
        if (modules != null) {
            for (ModuleHandler mh : modules.values()) {
                mh.postSendFaultOperation(client, client.getOptions());
            }
        }
    }

    /**
     * @return the service description
     * @throws WSCallException
     */
    public ServiceDescription getServiceDescription() throws WSCallException
    {
        ServiceDescription sd = null;
        String service = (_serviceName != null) && !"".equals(_serviceName) ? _serviceName : "";
        String serviceNS = (_serviceNS != null) && !"".equals(_serviceNS) ? "{" + _serviceNS + "}" : "";
        String port = (_portName != null) && !"".equals(_portName) ? _portName : "";
        String key = serviceNS + service + "_" + port;
        if (serviceDescriptions.containsKey(key)) {
            sd = serviceDescriptions.get(key);
            logger.debug("got service description for key: " + key);
        }
        else {
            int i = 0;
            ServiceDescription firstSD = null;
            String firstKey = null;
            for (String mapKey : serviceDescriptions.keySet()) {
                if (i == 0) {
                    firstKey = mapKey;
                    firstSD = serviceDescriptions.get(mapKey);
                    i++;
                }
                if (mapKey.startsWith(key) || mapKey.endsWith(key)) {
                    sd = serviceDescriptions.get(mapKey);
                    logger.debug("got stub for key: " + mapKey);
                }
            }
            if (sd == null) {
                if (!"".equals(service) || !"".equals(port)) {
                    logger.error("Cannot find service [" + ("".equals(service) ? "any" : service) + "] with port ["
                            + ("".equals(port) ? "any" : port) + "].");
                    throw new WSCallException("SERVICE_NOT_FOUND", new String[][]{{
                            "cause",
                            "Cannot find service [" + ("".equals(service) ? "any" : service) + "] with port ["
                                    + ("".equals(port) ? "any" : port) + "]."}});
                }
                logger.debug("got service description for key: " + firstKey);
                sd = firstSD;
            }
        }
        return sd;
    }

    /**
     * @return the operation description
     * @throws WSCallException
     */
    public OperationDescription getOperationDescription() throws WSCallException
    {
        ServiceDescription sd = getServiceDescription();
        Map<?, ?> operations = sd.getDescriptions();
        OperationDescription opDesc = (OperationDescription) operations.get(_operationName);
        if (opDesc == null) {
            throw new WSCallException("OPERATION_NOT_FOUND", new String[][]{{"cause",
                    "Operation [" + _operationName + "] not found"}});
        }
        return opDesc;
    }

    public boolean isThrowsFault()
    {
        return throwsFault;
    }

    public void setThrowsFault(boolean throwsFault)
    {
        this.throwsFault = throwsFault;
    }
    
    public boolean isEmptyAction()
    {
        return emptyAction;
    }

    public void setEmptyAction(boolean emptyAction)
    {
        this.emptyAction = emptyAction;
    }
}
