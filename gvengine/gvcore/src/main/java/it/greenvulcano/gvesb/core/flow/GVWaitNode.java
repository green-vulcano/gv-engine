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
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVFlow node for GVWaitNode.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public class GVWaitNode extends GVFlowNode
{
    private static final Logger logger             = org.slf4j.LoggerFactory.getLogger(GVWaitNode.class);

    private static final int    SLEEP_IF_GVBUFFER  = 1;
    private static final int    SLEEP_IF_EXCEPTION = 2;
    private static final int    SLEEP_IF_BOTH      = 3;
    private long                timeout            = 0;
    private String              timeoutMeta        = null;
    private int                 sleepIf            = SLEEP_IF_EXCEPTION;
    private String              nextNodeId         = "";

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
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

        String sSleepIf = XMLConfig.get(defNode, "@sleep-if", "exception");

        if (sSleepIf.equals("exception")) {
            sleepIf = SLEEP_IF_EXCEPTION;
        }
        else if (sSleepIf.equals("gvbuffer")) {
            sleepIf = SLEEP_IF_GVBUFFER;
        }
        else if (sSleepIf.equals("both")) {
            sleepIf = SLEEP_IF_BOTH;
        }
        else {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'sleep-if'"},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }

        timeout = 0;
        timeoutMeta = XMLConfig.get(defNode, "@timeout", null);
        if ((timeoutMeta != null) && !"".equals(timeoutMeta)) {
            if (PropertiesHandler.isExpanded(timeoutMeta)) {
                timeout = Long.parseLong(timeoutMeta);
                timeoutMeta = null;
            }
        }
        logger.debug("GVWaitNode(" + getId() + ") timeout: " + timeout + " [" + timeoutMeta + "]");
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
        long locTimeout = timeout;
        boolean sleep = (sleepIf == SLEEP_IF_BOTH);

        logger.info("Executing GVWaitNode '" + getId() + "'");
        checkInterrupted("GVWaitNode", logger);
        dumpEnvironment(logger, true, environment);

        data = environment.get(getInput());
        try {

            if (timeoutMeta != null) {
                if (GVBuffer.class.isInstance(data)) {
                    Map<String, Object> params = GVBufferPropertiesHelper.getPropertiesMapSO((GVBuffer) data, true);
                    locTimeout = Long.parseLong(PropertiesHandler.expand(timeoutMeta, params, data));
                }
                else {
                    throw new GVCoreWrongInterfaceException("GVCORE_INVALID_GVBUFFER_ERROR", new String[][]{
                            {"id", getId()}, {"exception", "GVBuffer can't be null"}});
                }
            }
            if (locTimeout > 0) {
                if (!sleep) {
                    if (Throwable.class.isInstance(data) && (sleepIf == SLEEP_IF_EXCEPTION)) {
                        sleep = true;
                    }
                    else if (GVBuffer.class.isInstance(data) && (sleepIf == SLEEP_IF_GVBUFFER)) {
                        sleep = true;
                    }
                }
                if (sleep) {
                    logger.info("Executing sleep [" + locTimeout + " ms]");
                    try {
                        Thread.sleep(locTimeout);
                    }
                    catch (InterruptedException exc) {
                        logger.warn("GVWaitNode '" + getId() + "' sleep interrupted");
                        throw exc;
                    }
                }
            }
        }
        catch (Exception exc) {
            ThreadUtils.checkInterrupted(exc);
            logger.error("Error in GVWaitNode[" + getId() + "]", exc);
            environment.put(getInput(), exc);
        }

        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVWaitNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
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
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        // do nothing
    }
}