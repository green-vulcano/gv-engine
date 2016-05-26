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
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreTimeoutException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.virtual.DequeueOperation;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.gvesb.virtual.pool.OperationManagerPool;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVOperationNode class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVOperationNode extends GVFlowNode
{
    private static final Logger      logger           = org.slf4j.LoggerFactory.getLogger(GVOperationNode.class);

    /**
     * the VCLOperation instance
     */
    private Operation                vclOperation     = null;
    /**
     * the VCLOperation type
     */
    private String                   vclOpType        = "";
    /**
     * the VCLOperation key
     */
    private GVCoreOperationKey       coreOperationKey = null;

    private OperationManagerPool     operationManager = null;

    /**
     * the next flow node id
     */
    private String                   nextNodeId       = "";
    /**
     * the optional dequeue filter
     */
    private GVDequeueFilter          filter           = null;
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

        initVCL(defNode);
    }

    /**
     * @param defNode
     *        the flow node definition
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private void initVCL(Node defNode) throws GVCoreConfException
    {
        String idSystem = XMLConfig.get(defNode, "@id-system", null);
        String idChannel = XMLConfig.get(defNode, "@id-channel", null);
        String vmOpName = XMLConfig.get(defNode, "@operation-name", null);      
        vclOpType = XMLConfig.get(defNode, "@op-type", "call");

        if (Objects.isNull(idSystem)||Objects.isNull(idChannel)||Objects.isNull(vmOpName)) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "Required: id-system, id-channel, operation-name"}, {"node", XPathFinder.buildXPath(defNode)}});
        }

        Node vmOpNode = null;
        String xPath = "/GVSystems/Systems"
	            		+ "/System[@id-system='" + idSystem + "']"
	            		+ "/Channel[@id-channel='" + idChannel + "']"
	            		+ "/*[@name='" + vmOpName + "']";
        try {
            vmOpNode = Objects.requireNonNull(XMLConfig.getNode(GreenVulcanoConfig.getSystemsConfigFileName(), xPath));
        } catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}, {"xpath", xPath}});
        } catch (NullPointerException nullPointerException) {
        	throw new GVCoreConfException("GVCORE_VCL_OPERATION_SEARCH_ERROR", new String[][]{{"id", getId()},
                {"node", XPathFinder.buildXPath(defNode)}, {"xpath", xPath}});
		}
                  
        try {
            operationManager = ((InvocationContext) InvocationContext.getInstance()).getOperationManager();
            coreOperationKey = new GVCoreOperationKey(GreenVulcanoConfig.getSystemsConfigFileName(),
                    XPathFinder.buildXPath(vmOpNode));
            checkVCLOperation();
            Node intSvcNode = XMLConfig.getNode(defNode, "InputServices");
            if (intSvcNode != null) {
                inputServices.init(intSvcNode, this, true);
            }
            intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                outputServices.init(intSvcNode, this, false);
            }
            if (vclOpType.equals("dequeue")) {
                Node filterDef = null;
                try {
                    filterDef = XMLConfig.getNode(defNode, "DequeueFilter");
                }
                catch (XMLConfigException exc) {
                    // do nothing
                }
                if (filterDef != null) {
                    filter = new GVDequeueFilter();
                    filter.init(filterDef);
                }
            }
        }
        catch (GVCoreConfException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Generic error initializing VCL Operation", exc);
            throw new GVCoreConfException("GVCORE_OPERATION_NODE_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}, {"message", "" + exc}}, exc);
        }
        finally {
            operationManager = null;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, InterruptedException
    {
        long startTime = System.currentTimeMillis();
        Object data = null;
        String input = getInput();
        String output = getOutput();
        logger.info("Executing GVOperationNode '" + getId() + "'");
        checkInterrupted("GVOperationNode", logger);
        dumpEnvironment(logger, true, environment);

        data = environment.get(input);
        if (Throwable.class.isInstance(data)) {
            environment.put(output, data);
            logger.debug("END - Execute GVOperationNode '" + getId() + "'");
            return nextNodeId;
        }

        boolean isError = false;
        Exception error = null;
        try {
            GVBuffer internalData = null;
            if (input.equals(output)) {
                internalData = (GVBuffer) data;
            }
            else {
                internalData = new GVBuffer((GVBuffer) data);
            }

            internalData = inputServices.perform(internalData);
            operationManager = ((InvocationContext) InvocationContext.getInstance()).getOperationManager();
            vclOperation = createVCLOperation();
            if (filter != null) {
                ((DequeueOperation) vclOperation).setFilter(filter.getFilterDef(internalData, vclOperation));
            }
            try {
                internalData = performVCLOpCall(internalData);
            }
            catch (GVCoreWrongInterfaceException exc) {
                if (vclOpType.equals("dequeue")) {
                    throw new GVCoreTimeoutException("GVCORE_VCLOP_OUT_NULL_ERROR", exc);
                }
                throw exc;
            }
            internalData = outputServices.perform(internalData);
            environment.put(output, internalData);
        }
        catch (InterruptedException exc) {
            logger.error("GVOperationNode [" + getId() + "] interrupted!", exc);
            throw exc;
        }
        catch (Exception exc) {
            isError = true;
            error = exc;
            environment.put(output, exc);
        }

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        if (isError) {
            logger.error("END - Execute GVOperationNode '" + getId() + "'. Exception: " + error);
        }
        else {
            logger.info("END - Execute GVOperationNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
        }
        return nextNodeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return nextNodeId;
    }

    /**
     * Call cleanUp() of VCLOperations.
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        if (vclOperation != null) {
            vclOperation.cleanUp();
            if (operationManager != null) {
                try {
                    operationManager.releaseOperation(vclOperation);
                }
                catch (Exception exc) {
                    throw new GVCoreException("GVCORE_GVVCL_RELEASE_ERROR", exc);
                }
                finally {
                    vclOperation = null;
                    operationManager = null;
                }
            }
        }
        vclOperation = null;
        inputServices.cleanUp();
        outputServices.cleanUp();
    }

    /**
     * Call cleanUp() and release VCLOperations
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        cleanUp();
        inputServices = null;
        outputServices = null;
    }

    /**
     * Perform of the Virtual Communication Layer for External Services.
     * 
     * @param gvBuffer
     *        The GreenVulcano GVBuffer coming from the client (the request
     *        buffer)
     * @return The GreenVulcano GVBuffer elaborated by the service called (it
     *         may be a server, a PlugIn, ...)
     * @throws GVCoreException
     *         if an error occurs at Virtual Communication Layer or core level
     */
    protected GVBuffer performVCLOpCall(GVBuffer gvBuffer) throws GVCoreException, InterruptedException
    {
        GVBuffer outputGVBuffer = null;

        logger.info("BEGIN - Perform Remote Call");

        long totalTime = 0;
        long endTime = 0;
        long startTime = System.currentTimeMillis();

        try {
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatINPUT(gvBuffer, false, false).toString());
            }
            outputGVBuffer = vclOperation.perform(gvBuffer);
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatOUTPUT(outputGVBuffer, false, false).toString());
            }
        }
        catch (InterruptedException exc) {
            logger.error("VCLOperation in GVOperationNode '" + getId() + "' interrupted.");
            throw exc;
        }
        catch (VCLException exc) {
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", getId()},
                    {"exception", exc.getMessage()}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Error invoking VCLOperation in GVOperationNode '" + getId() + "'. Exception: ", exc);
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", getId()},
                    {"exception", exc.getMessage()}}, exc);
        }

        if (outputGVBuffer == null) {
            throw new GVCoreWrongInterfaceException("GVCORE_INVALID_GVBUFFER_ERROR", new String[][]{{"id", getId()},
                    {"exception", "GVBuffer can't be null"}});
        }

        try {
            if (vclOpType.equals("dequeue")) {
                ((DequeueOperation) vclOperation).acknowledge(outputGVBuffer.getId());
            }
        }
        catch (Throwable exc) {
            logger.warn("An error occurred when confirming a message : ", exc);
        }

        endTime = System.currentTimeMillis();
        if (endTime != 0) {
            totalTime = endTime - startTime;
        }

        logger.info("END - Perform Remote Call - ExecutionTime (" + totalTime + ")");
        return outputGVBuffer;
    }

    /**
     * Check the correct configuration of the requested VCLOperation
     * 
     * @throws GVCoreConfException
     *         if error occurs
     */
    private void checkVCLOperation() throws GVCoreException
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
     * Initialize the requested VCLOperation
     * 
     * @return the required VCLOperation
     * @throws GVCoreConfException
     *         if error occurs
     */
    private Operation createVCLOperation() throws GVCoreException
    {
        Operation operation = null;

        try {
            operation = operationManager.getOperation(coreOperationKey, coreOperationKey.getType());
        }
        catch (GVException exc) {
            logger.error("GVCORE_VCL_OPERATION_INIT_ERROR", exc);
            throw new GVCoreConfException("GVCORE_VCL_OPERATION_INIT_ERROR", new String[][]{{"node",
                    XPathFinder.buildXPath(coreOperationKey.getNode())}}, exc);
        }
        return operation;
    }
}