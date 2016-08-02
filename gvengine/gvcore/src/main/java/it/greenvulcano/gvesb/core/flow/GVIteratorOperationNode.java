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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVIteratorOperationNode extends GVFlowNode
{
    private static final Logger      logger         = org.slf4j.LoggerFactory.getLogger(GVIteratorOperationNode.class);

    /**
     * the next flow node id
     */
    private String                   nextNodeId     = "";
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices  = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices = new GVInternalServiceHandler();
    /**
     * Private instance of <code>IteratorController</code> class, handling the
     * iteration management logic.
     */
    private IteratorController       gvController   = null;

    /**
     * @see GVFlowNode#init(org.w3c.dom.Node)
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
     * @throws CoreConfigException
     *         if errors occurs
     */
    private void initVCL(Node defNode) throws GVCoreConfException
    {
        try {
            Node intSvcNode = XMLConfig.getNode(defNode, "InputServices");
            if (intSvcNode != null) {
                inputServices.init(intSvcNode, this, true);
            }
            intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                outputServices.init(intSvcNode, this, false);
            }
            gvController = new IteratorController();
            gvController.doInit(defNode, getId());
        }
        catch (GVCoreConfException exc) {
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Generic error initializing VCL Operation", exc);
            throw new GVCoreConfException("GVCORE_OPERATION_NODE_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}, {"message", "" + exc}}, exc);
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
        logger.info("Executing OperationNode '" + getId() + "'");
        checkInterrupted("OperationNode", logger);
        dumpEnvironment(logger, true, environment);

        data = environment.get(input);
        if (Throwable.class.isInstance(data)) {
            environment.put(output, data);
            logger.debug("END - Execute OperationNode '" + getId() + "'");
            return nextNodeId;
        }

        try {
            GVBuffer internalData = null;
            if (input.equals(output)) {
                internalData = (GVBuffer) data;
            }
            else {
                internalData = new GVBuffer((GVBuffer) data);
            }

            internalData = inputServices.perform(internalData);
            internalData = performVCLOpCall(internalData, onDebug);
            internalData = outputServices.perform(internalData);
            environment.put(output, internalData);
        }
        catch (InterruptedException exc) {
            logger.error("GVIteratorOperationNode [" + getId() + "] interrupted!", exc);
            throw exc;
        }
        catch (Exception exc) {
            environment.put(output, exc);
        }

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute OperationNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
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
     * Call cleanUp() of VCLOperations.
     *
     * @see GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        gvController.cleanUp();
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
        gvController = null;
        inputServices = null;
        outputServices = null;
    }

    /**
     * Perform of the Virtual Connection Layer for External Services.
     *
     * @param data
     *        The GreenVulcano GVBuffer coming from the client (the request
     *        buffer)
     * @param onDebug 
     * @return The GreenVulcano GVBuffer elaborated by the service called (it
     *         may be a server, a PlugIn, ...)
     * @throws GVCoreException
     *         if an error occurs at connection layer or core level
     */
    protected GVBuffer performVCLOpCall(GVBuffer data, boolean onDebug) throws GVCoreException, InterruptedException
    {
        GVBuffer outputGVBuffer = null;

        logger.info("BEGIN - Perform Remote Call");

        long totalTime = 0;
        long endTime = 0;
        long startTime = System.currentTimeMillis();

        try {
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatINPUT(data, false, false).toString());
            }
            outputGVBuffer = gvController.doPerform(data, onDebug);
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatOUTPUT(outputGVBuffer, false, false).toString());
            }
        }
        catch (InterruptedException exc) {
            logger.error("Iteration in GVIteratorOperationNode '" + getId() + "' interrupted.");
            throw exc;
        }
        catch (VCLException exc) {
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", getId()},
                    {"exception", exc.getMessage()}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Error invoking VCLOperation in OperationNode '" + getId() + "'. Exception: ", exc);
            throw new GVCoreException("GVCORE_VCL_OPERATION_ERROR", new String[][]{{"id", getId()},
                    {"exception", exc.getMessage()}}, exc);
        }

        if (outputGVBuffer == null) {
            throw new GVCoreWrongInterfaceException("GVCORE_INVALID_GVBUFFER_ERROR", new String[][]{{"exception",
            "GVBuffer can't be null"}});
        }

        endTime = System.currentTimeMillis();
        if (endTime != 0) {
            totalTime = endTime - startTime;
        }

        logger.info("END - Perform Remote Call - ExecutionTime (" + totalTime + ")");
        return outputGVBuffer;
    }
}