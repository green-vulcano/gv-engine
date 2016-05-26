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
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.internal.data.ChangeGVBuffer;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVFlow node for change data.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ChangeGVBufferNode extends GVFlowNode
{
    private static final Logger      logger         = org.slf4j.LoggerFactory.getLogger(ChangeGVBufferNode.class);

    /**
     * the ChangeGVBuffer instance
     */
    private ChangeGVBuffer           cGVBuffer      = null;
    /**
     * the next flow node id
     */
    private String                   nextNodeId     = "";
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices = new GVInternalServiceHandler();

    /**
     * Initialize the instance
     *
     * @param defNode
     *        the flow node definition
     * @throws GVCoreConfException
     *         if errors occurs
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException
    {
        super.init(defNode);

        try {
            nextNodeId = XMLConfig.get(defNode, "@next-node-id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }

        Node cGVBufferNode = getNode(defNode, "ChangeGVBuffer");

        try {
            Node intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                outputServices.init(intSvcNode, this, false);
            }
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_CGVBUFFER_NODE_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(cGVBufferNode)}}, exc);
        }

        if (cGVBufferNode != null) {
            cGVBuffer = new ChangeGVBuffer();
            cGVBuffer.setLogger(logger);
            try {
                cGVBuffer.init(cGVBufferNode);
            }
            catch (XMLConfigException exc) {
                logger.error("Error initializing ChangeGVBuffer", exc);
                throw new GVCoreConfException("GVCORE_CGVBUFFER_NODE_INIT_ERROR", new String[][]{{"id", getId()},
                        {"node", XPathFinder.buildXPath(cGVBufferNode)}}, exc);
            }
        }
        else {
            logger.debug("ChangeGVBufferAssignment and ChangeGVBuffer not found. GVFlow node unused");
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
        logger.info("Executing ChangeGVBufferNode '" + getId() + "'");
        checkInterrupted("ChangeGVBufferNode", logger);
        dumpEnvironment(logger, true, environment);

        String input = getInput();
        String output = getOutput();

        Object obj = environment.get(input);
        if (obj == null) {
            environment.put(output, new GVCoreWrongInterfaceException("GVCORE_NULL_DATA_ERROR",
                    new String[][]{{"id", getId()}}));
            logger.debug("END - Execute ChangeGVBufferNode '" + getId() + "'");
            return nextNodeId;
        }
        if (Throwable.class.isInstance(obj)) {
            environment.put(output, obj);
            logger.debug("END - Execute ChangeGVBufferNode '" + getId() + "'");
            return nextNodeId;
        }
        if ((cGVBuffer != null) || outputServices.isValid()) {
            if (obj instanceof GVBuffer) {
                try {
                    GVBuffer data = (GVBuffer) obj;
                    if (logger.isDebugEnabled() || isDumpInOut()) {
                        logger.info(GVFormatLog.formatINPUT(data, false, false).toString());
                    }
                    if (!output.equals("")) {
                        data = new GVBuffer(data);
                    }
                    if (cGVBuffer != null) {
                        data = cGVBuffer.execute(data, environment);
                    }
                    if (outputServices.isValid()) {
                        data = outputServices.perform(data);
                    }
                    environment.put(output, data);
                    if (logger.isDebugEnabled() || isDumpInOut()) {
                        logger.info(GVFormatLog.formatOUTPUT(data, false, false).toString());
                    }
                }
                catch (InterruptedException exc) {
                    logger.error("ChangeGVBufferNode [" + getId() + "] interrupted!", exc);
                    throw exc;
                }
                catch (Throwable exc) {
                    logger.error("Error in ChangeGVBufferNode[" + getId() + "]", exc);
                    environment.put(output, exc);
                }
            }
        }
        else {
            environment.put(output, environment.get(input));
        }

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute ChangeGVBufferNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
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
     * @param node
     *        the context node
     * @param xPath
     *        the XPath to apply
     * @return the founded node
     */
    private Node getNode(Node node, String xPath)
    {
        try {
            Node result = XMLConfig.getNode(node, xPath);
            return result;
        }
        catch (XMLConfigException exc) {
            return null;
        }
    }

    /**
     * Do nothing.
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        if (cGVBuffer != null) {
            cGVBuffer.cleanUp();
        }
        outputServices.cleanUp();
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        cleanUp();
        if (cGVBuffer != null) {
            cGVBuffer.destroy();
        }
        cGVBuffer = null;
        outputServices = null;
    }
}