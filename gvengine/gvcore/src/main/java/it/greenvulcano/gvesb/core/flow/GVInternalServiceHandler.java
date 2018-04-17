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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreInputServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreOutputServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongParameterException;
import it.greenvulcano.gvesb.internal.GVInternalException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Internal Service Handler.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class GVInternalServiceHandler
{
    private static final Logger            logger           = org.slf4j.LoggerFactory.getLogger(GVInternalServiceHandler.class);

    /**
     * the services vector
     */
    private Vector<GVInternalServiceParam> servicesVector   = new Vector<GVInternalServiceParam>();
    /**
     * the associated flow node
     */
    private GVFlowNode                     fnode            = null;
    /**
     * if true is a input service, otherwise is a output service
     */
    private boolean                        isInput          = false;

    private OperationManagerPool           operationManager = null;

    /**
     * Initialize the input services
     * 
     * @param node
     *        the operation assignment node
     * @param fnode
     *        the associated flow node
     * @param operationManager
     *        the VCLOperation manager
     * @param isInput
     *        if true is a input service, otherwise is a output service
     * @throws GVCoreConfException
     *         if errors occurs
     */
    public void init(Node node, GVFlowNode fnode, boolean isInput) throws GVException
    {
        this.fnode = fnode;
        this.isInput = isInput;

        NodeList serviceNodeList = null;
        GVInternalServiceParam serviceParams = null;

        try {
            serviceNodeList = XMLConfig.getNodeList(node, "*[@type='service']");
        }
        catch (XMLConfigException exc) {
            return;
        }
        if (serviceNodeList != null) {
            operationManager = ((InvocationContext) InvocationContext.getInstance()).getOperationManager();
            try {
                for (int iLoop = 0; iLoop < serviceNodeList.getLength(); iLoop++) {
                    serviceParams = setServiceParam(serviceNodeList.item(iLoop));
                    servicesVector.addElement(serviceParams);
                }
            }
            finally {
                operationManager = null;
            }
        }
    }

    /**
     * Return true if the services are configured
     * 
     * @return true if services are configured
     */
    public boolean isValid()
    {
        return (servicesVector.size() > 0);
    }

    /**
     * Handle the input services (Plug-Ins and Maps). Operate on a clone of the
     * input object.
     * 
     * @param internalData
     *        The GreenVulcano data coming from the client (the request buffer)
     * @return The internal GreenVulcano data modified by the input services
     *         (PlugIns and Maps)
     * @throws GVCoreException
     *         if an error occurs at Communication Layer or core level
     */
    public GVBuffer perform(GVBuffer data) throws GVCoreException, GVInternalException, InterruptedException
    {
        GVBuffer localData = new GVBuffer(data);

        GVInternalServiceParam serviceParam = null;

        if (servicesVector.size() > 0) {
            operationManager = ((InvocationContext) InvocationContext.getInstance()).getOperationManager();
            for (int iLoop = 0; (iLoop < servicesVector.size()) && !fnode.isInterrupted(); iLoop++) {
                serviceParam = servicesVector.get(iLoop);
                try {
                    long startTime = System.currentTimeMillis();
                    if (logger.isInfoEnabled()) {
                        logger.info("Executing Internal Service (" + serviceParam.getName() + "), Critical ("
                                + serviceParam.isCritical() + ")");
                    }
                    localData = handleServices(localData, serviceParam);
                    if (logger.isDebugEnabled() && fnode.isDumpInOut()) {
                        logger.debug(GVFormatLog.formatOUTPUT(localData, false, false).toString());
                    }
                    if (logger.isInfoEnabled()) {
                    	  long endTime = System.currentTimeMillis();
                        logger.info("END - Internal Service (" + serviceParam.getName() + "), RetCode ("
                                + localData.getRetCode() + ") - ExecutionTime (" + (endTime - startTime) + ")");
                    }
                }
                catch (InterruptedException exc) {
                    logger.error("VCLOperation in GVInternalServiceHandler interrupted.");
                    throw exc;
                }
                catch (Throwable exc) {
                    if (!serviceParam.isCritical()) {
                        logger.warn("END - Internal Service (" + serviceParam.getName() + "), Critical ("
                                + serviceParam.isCritical() + ") FAILED!!", exc);
                    }
                    else {
                        logger.error("END - Internal Service (" + serviceParam.getName() + "), Critical ("
                                + serviceParam.isCritical() + ") FAILED!!", exc);
                        if (isInput) {
                            throw new GVCoreInputServiceException("GVCORE_INPUT_SERVICE_ERROR", new String[][]{
                                    {"id", fnode.getId()}, {"service", serviceParam.getName()},
                                    {"exception", exc.toString()}});
                        }
                        throw new GVCoreOutputServiceException("GVCORE_OUTPUT_SERVICE_ERROR", new String[][]{
                                {"id", fnode.getId()}, {"service", serviceParam.getName()},
                                {"exception", exc.toString()}});
                    }
                }
            }
            if (fnode.isInterrupted()) {
                logger.error("VCLOperation in GVInternalServiceHandler[" + fnode.getId() + "] interrupted.");
                throw new InterruptedException("VCLOperation in GVInternalServiceHandler interrupted.");
            }
        }
        return localData;
    }

    /**
     * @param serviceParam
     *        the internal service data
     * @param gvBuffer
     *        the instance on which insert the parameters
     * @throws GVCoreWrongParameterException
     *         if error occurs
     */
    private void setInternalParameters(GVInternalServiceParam serviceParam, GVBuffer gvBuffer)
            throws GVCoreWrongParameterException
    {
        try {
            Map<String, String> params = serviceParam.getParameters();
            for (Entry<String, String> entry : params.entrySet()) {
                String currParamName = entry.getKey();
                String currParamValue = entry.getValue();

                currParamValue = PropertiesHandler.expand(currParamValue, null, gvBuffer);
                gvBuffer.setProperty(currParamName, currParamValue);
            }
        }
        catch (GVException exc) {
            throw new GVCoreWrongParameterException("GVCORE_INT_FIELDS_SET_ERROR", new String[][]{
                    {"id", fnode.getId()}, {"service", serviceParam.getName()}, {"exception", exc.getMessage()}});
        }
        catch (Exception exc) {
            throw new GVCoreWrongParameterException("GVCORE_INT_FIELDS_SET_ERROR", new String[][]{
                    {"id", fnode.getId()}, {"service", serviceParam.getName()}, {"exception", exc.getMessage()}});
        }
    }

    /**
     * @param serviceParam
     *        the internal service data
     * @param gvBuffer
     *        the instance from which remove the parameters
     * @throws GVCoreWrongParameterException
     *         if error occurs
     */
    private void removeInternalParameters(GVInternalServiceParam serviceParam, GVBuffer gvBuffer)
            throws GVCoreWrongParameterException
    {
        gvBuffer.removeProperties(serviceParam.getParameters());
    }

    /**
     * Handle the services.
     * 
     * @param gvBuffer
     *        The GreenVulcano data coming from the client (the request buffer)
     * @param serviceParam
     *        Object containing the parameter and keys for the calls to the
     *        services to apply
     * @return The GreenVulcano data modified by the services (PlugIns and Maps)
     * @throws GVCoreException
     *         if an error occurs at Communication Layer or core level
     */
    private GVBuffer handleServices(GVBuffer gvBuffer, GVInternalServiceParam serviceParam) throws GVCoreException, InterruptedException
    {
        setInternalParameters(serviceParam, gvBuffer);
        gvBuffer = performVCLOpLocalCall(gvBuffer, serviceParam.getVCLOperation(operationManager));
        if (serviceParam.isToRemove()) {
            removeInternalParameters(serviceParam, gvBuffer);
        }
        return gvBuffer;
    }

    /**
     * Perform of the Virtual Communication Layer for Local Internal Services.
     * 
     * @param gvBuffer
     *        The GreenVulcano GVBuffer coming from the client (the request
     *        buffer)
     * @param operation
     *        GVCoreOperationKey The Parameters to retrieve the call from the
     *        Virtual Communication Layer
     * @return The GreenVulcano Data elaborated by the service called (it maybe
     *         a server, a PlugIn, ...)
     * @throws GVCoreException
     *         if an error occurs at Communication Layer or core level
     */
    protected GVBuffer performVCLOpLocalCall(GVBuffer gvBuffer, Operation operation) throws GVCoreException, InterruptedException
    {
        logger.debug("BEGIN - Perform Internal Call");

        try {
            if (logger.isDebugEnabled() && fnode.isDumpInOut()) {
                logger.debug(GVFormatLog.formatINPUT(gvBuffer, false, false).toString());
            }
            gvBuffer = operation.perform(gvBuffer);
            if (logger.isDebugEnabled() && fnode.isDumpInOut()) {
                logger.debug(GVFormatLog.formatOUTPUT(gvBuffer, false, false).toString());
            }
        }
        catch (InterruptedException exc) {
            logger.error("VCLOperation in GVInternalServiceHandler[" + fnode.getId() + "] interrupted.");
            throw exc;
        }
        catch (VCLException exc) {
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", fnode.getId()},
                    {"exception", exc.getMessage()}}, exc);
        }
        catch (Exception exc) {
            logger.error("Error invoking VCLOperation in GVOperationNode '" + fnode.getId() + "'. Exception: ", exc);
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", fnode.getId()},
                    {"exception", exc.getMessage()}}, exc);
        }
        logger.debug("END - Perform Internal Call");
        return gvBuffer;
    }


    /**
     * Set the parameters for the current internal service
     * 
     * @param internalServiceNode
     *        the node from which read configuration data
     * @param operationManager
     *        the VCLOperation manager
     * @return the internal service data
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private GVInternalServiceParam setServiceParam(Node internalServiceNode) throws GVCoreException
    {
        NodeList paramsNodeList = null;
        Node opNode = null;
        String paramNodeName = null;
        String paramNodeValue = null;
        GVInternalServiceParam serviceParams = null;
        serviceParams = new GVInternalServiceParam();
        serviceParams.setCritical(XMLConfig.get(internalServiceNode, "@critical", "yes"));
        serviceParams.setRemoveFields(XMLConfig.get(internalServiceNode, "@remove-fields", "yes"));
        try {
            paramsNodeList = XMLConfig.getNodeList(internalServiceNode, "*[@type='param']");
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if (paramsNodeList != null) {
            for (int iLoop = 0; iLoop < paramsNodeList.getLength(); iLoop++) {
                try {
                    paramNodeName = XMLConfig.get(paramsNodeList.item(iLoop), "@name");
                    paramNodeValue = XMLConfig.get(paramsNodeList.item(iLoop), "@value");
                }
                catch (XMLConfigException exc) {
                    throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR",
                            new String[][]{{"name", "'name' or 'value'"},
                                    {"node", XPathFinder.buildXPath(paramsNodeList.item(iLoop))}}, exc);
                }
                serviceParams.setParam(paramNodeName, paramNodeValue);
            }
        }

        try {
            opNode = XMLConfig.getNode(internalServiceNode, "*[@type='call']");
            serviceParams.setName(XMLConfig.get(opNode, "@name", "unknown"));
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", fnode.getId()},
                    {"node", XPathFinder.buildXPath(internalServiceNode)}, {"xpath", "*[@type='call']"}}, exc);
        }
        if (opNode == null) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", fnode.getId()},
                    {"node", XPathFinder.buildXPath(internalServiceNode)}, {"xpath", "*[@type='call']"}});
        }
        GVCoreOperationKey coreOperationKey = new GVCoreOperationKey(GreenVulcanoConfig.getServicesConfigFileName(),
                XPathFinder.buildXPath(opNode));

        checkVCLOperation(coreOperationKey);
        serviceParams.setVCLOperationKey(coreOperationKey);

        return serviceParams;
    }

    /**
     * Check the correct configuration of the requested VCLOperation
     * 
     * @throws GVCoreConfException
     *         if error occurs
     */
    private void checkVCLOperation(GVCoreOperationKey coreOperationKey) throws GVCoreException
    {
        try {
            if (!operationManager.checkOperation(coreOperationKey, coreOperationKey.getType())) {
                logger.error("GVCORE_VCL_OPERATION_INIT_ERROR");
                throw new GVCoreConfException("GVCORE_VCL_OPERATION_INIT_ERROR", new String[][]{{"node",
                        XPathFinder.buildXPath(coreOperationKey.getNode())}});
            }
        }
        catch (GVCoreConfException exc) {
            throw exc;
        }
        catch (GVException exc) {
            logger.error("GVCORE_VCL_OPERATION_INIT_ERROR", exc);
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_INIT_ERROR", new String[][]{{"node",
                    XPathFinder.buildXPath(coreOperationKey.getNode())}}, exc);
        }
    }

    /**
     * perform cleanup operation on internal services
     */
    public void cleanUp() throws GVCoreException
    {
        try {
            if (servicesVector.size() > 0) {
                for (int iLoop = 0; iLoop < servicesVector.size(); iLoop++) {
                    GVInternalServiceParam serviceParam = servicesVector.get(iLoop);
                    serviceParam.cleanUp(operationManager);
                }
            }
        }
        finally {
            operationManager = null;
        }
    }
}
