/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.core.config.GVServiceConf;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.config.ServiceConfigManager;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreSecurityException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVOperationNode class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class GVCoreCallNode extends GVFlowNode
{
    private static final Logger logger                = org.slf4j.LoggerFactory.getLogger(GVCoreCallNode.class);
    /**
     * the next flow node id
     */
    private String              nextNodeId            = "";
    /**
     * The id_system for the flow to invoke.
     */
    private String              system                = "";
    /**
     * The id_service for the flow to invoke.
     */
    private String              service               = "";
    /**
     * The operation for the flow to invoke.
     */
    private String              flowOp                = "";
    /**
     * If true overwrite the input id_system and id_service.
     */
    private boolean             isFlowSysSvcSet       = false;
    /**
     * If true the input id_system, id_service and operation are handled as
     * metadata and resolved at runtime.
     */
    private boolean             isFlowSysSvcOpDynamic = false;
    /**
     * If true update the log context.
     */
    private boolean             changeLogContext      = false;
    /**
     * If true update the log master service file.
     */
    private boolean             changeLogMasterService = false;
    /**
     * GVBuffer instance to be used only for accessing to ServiceConfigManager.
     */
    private GVBuffer            flowGVBuffer          = null;

    private String              inputRefDP            = null;
    private String              outputRefDP           = null;
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices    = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices   = new GVInternalServiceHandler();

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        super.init(defNode);

        nextNodeId = XMLConfig.get(defNode, "@next-node-id", "");
        if (nextNodeId.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }

        initNode(defNode);
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        inputServices.cleanUp();
        outputServices.cleanUp();
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        inputServices = null;
        outputServices = null;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        GVBuffer internalData = null;
        String input = getInput();
        String output = getOutput();
        logger.info("Executing GVCoreCallNode '" + getId() + "'");
        logger.info("xxxxxxCALL GVFlowWF origSystem ");
        checkInterrupted("GVCoreCallNode", logger);
        dumpEnvironment(logger, true, environment);

        Object inData = environment.get(input);
        if (Throwable.class.isInstance(inData)) {
            environment.put(output, inData);
            logger.debug("END - Execute GVCoreCallNode '" + getId() + "'");
            return nextNodeId;
        }
        try {
        	GVBuffer data = (GVBuffer) inData;
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatINPUT(data, false, false).toString());
            }
            if (input.equals(output)) {
                internalData = data;
            }
            else {
                internalData = new GVBuffer(data);
            }
            String origSystem = internalData.getSystem();
            String origService = internalData.getService();
            logger.debug("origSystem  = " + origSystem);
            logger.debug("origService = " + origService);
            
            logger.info("CALL GVFlowWF origSystem " + origSystem + "");

            String localSystem = (GVBuffer.DEFAULT_SYS.equals(system) ? origSystem : system);
            String localService = service;
            String localFlowOp = flowOp;

            if (isFlowSysSvcOpDynamic) {
                Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalData, true);
                localSystem = PropertiesHandler.expand(localSystem, props, internalData);
                localService = PropertiesHandler.expand(localService, props, internalData);
                flowGVBuffer.setService(localService);
                flowGVBuffer.setSystem(localSystem);
                localFlowOp = PropertiesHandler.expand(localFlowOp, props, internalData);
            }
            GVServiceConf gvsConfig = null;
            InvocationContext gvCtx = new InvocationContext((InvocationContext) InvocationContext.getInstance());
            try {
            	gvCtx.setService(localService);
            	gvCtx.setSystem(localSystem);
            	gvCtx.setOperation(localFlowOp);
            	gvCtx.push();
            
	            ServiceConfigManager svcMgr = gvCtx.getGVServiceConfigManager();
	            gvsConfig = svcMgr.getGVSConfig(flowGVBuffer);
	            if (!ACLManager.canAccess(new GVCoreServiceKey(gvsConfig.getGroupName(), gvsConfig.getServiceName(),
	                    localFlowOp))) {
	                throw new GVCoreSecurityException("GV_SERVICE_POLICY_ERROR", new String[][]{
	                        {"service", flowGVBuffer.getService()}, {"system", flowGVBuffer.getSystem()},
	                        {"id", flowGVBuffer.getId().toString()}, {"user", GVIdentityHelper.getName()}});
	            }
	            GVFlow gvOp = gvsConfig.getGVOperation(flowGVBuffer, localFlowOp);
	
	            try {
	                NMDC.push();
	
	                if (isFlowSysSvcSet) {
	                    internalData.setService(localService);
	                    internalData.setSystem(localSystem);
	                }
	
	                if (changeLogContext) {
	                    GVBufferMDC.put(internalData);
	                    NMDC.setOperation(localFlowOp);
	                    NMDC.put(GVBuffer.Field.SERVICE.toString(), localService);
	                    NMDC.put(GVBuffer.Field.SYSTEM.toString(), localSystem);
	                }
	                DataProviderManager dataProviderManager = DataProviderManager.instance();
	                if ((inputRefDP != null) && (inputRefDP.length() > 0)) {
	                    IDataProvider dataProvider = dataProviderManager.getDataProvider(inputRefDP);
	                    try {
	                        logger.debug("Working on Input data provider: " + dataProvider);
	                        dataProvider.setObject(internalData);
	                        Object inputCall = dataProvider.getResult();
	                        internalData.setObject(inputCall);
	                    }
	                    finally {
	                        dataProviderManager.releaseDataProvider(inputRefDP, dataProvider);
	                    }
	                }
	                internalData = inputServices.perform(internalData);
	               
	                String masterService = null;
	                try {
	                    if (changeLogMasterService) {
	                        masterService = GVBufferMDC.changeMasterService(localService);
	                    }
	                  
	                    logger.info("CALL GVFlowWF perform " + internalData + "");
	                    internalData = gvOp.perform(internalData, onDebug);
	                }
	                finally {
	                    
	                    if (changeLogMasterService) {
	                        GVBufferMDC.changeMasterService(masterService);
	                    }
	                }
	                internalData = outputServices.perform(internalData);
	                if ((outputRefDP != null) && (outputRefDP.length() > 0)) {
	                    IDataProvider dataProvider = dataProviderManager.getDataProvider(outputRefDP);
	                    try {
	                        logger.debug("Working on Output data provider: " + dataProvider);
	                        dataProvider.setObject(internalData);
	                        Object outputCall = dataProvider.getResult();
	                        internalData.setObject(outputCall);
	                    }
	                    finally {
	                        dataProviderManager.releaseDataProvider(outputRefDP, dataProvider);
	                    }
	                }
	            }
	            finally {
	                NMDC.pop();
	                if (isFlowSysSvcSet) {
	                    internalData.setSystem(origSystem);
	                    internalData.setService(origService);
	                }
	            }
	            environment.put(output, internalData);
	            if (logger.isDebugEnabled() || isDumpInOut()) {
	                logger.info(GVFormatLog.formatOUTPUT(internalData, false, false).toString());
	            }
            }
            finally {
            	gvCtx.pop();
            }
        }
        catch (InterruptedException exc) {
            logger.error("GVCoreCallNode [" + getId() + "] interrupted!", exc);
            throw exc;
        }
        catch (Exception exc) {
            environment.put(output, exc);
        }

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVCoreCallNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
        return nextNodeId;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return nextNodeId;
    }

    /**
     * @param defNode
     *        the flow node definition
     * @throws CoreConfigException
     *         if errors occurs
     */
    private void initNode(Node defNode) throws GVCoreConfException
    {
        changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
        changeLogMasterService = changeLogContext && XMLConfig.getBoolean(defNode, "@change-log-master-service", false);
        try {
            system = XMLConfig.get(defNode, "@id-system", GVBuffer.DEFAULT_SYS);
            logger.debug("system  = " + system);
            service = XMLConfig.get(defNode, "@id-service");
            logger.debug("service = " + service);
            flowOp = XMLConfig.get(defNode, "@operation");
            logger.debug("flowOp  = " + flowOp);
            isFlowSysSvcOpDynamic = XMLConfig.getBoolean(defNode, "@dynamic", false);
            logger.debug("isFlowSysSvcOpDynamic  = " + isFlowSysSvcOpDynamic);
            isFlowSysSvcSet = XMLConfig.getBoolean(defNode, "@overwrite-sys-svc", false);
            logger.debug("isFlowSysSvcSet = " + isFlowSysSvcSet);
            flowGVBuffer = new GVBuffer(system, service);
            outputRefDP = XMLConfig.get(defNode, "@output-ref-dp", "");
            inputRefDP = XMLConfig.get(defNode, "@input-ref-dp", "");

            if (service.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'id-service'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            if (flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'operation'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            
            Node intSvcNode = XMLConfig.getNode(defNode, "InputServices");
            if (intSvcNode != null) {
                inputServices.init(intSvcNode, this, true);
            }
            intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                outputServices.init(intSvcNode, this, false);
            }
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }

    }

}
