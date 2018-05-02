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
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVFlow node for check.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVNodeCheck extends GVFlowNode
{
    private static final Logger logger            = org.slf4j.LoggerFactory.getLogger(GVNodeCheck.class);

    public static final String LAST_GV_EXCEPTION  = "LAST_GV_EXCEPTION";

    /**
     * the default flow node id
     */
    private String              defaultId         = "";
    /**
     * the onException flow node id
     */
    private String              onExceptionId     = "";
    /**
     * the routing condition vector
     */
    private Vector<GVRouting>   routingVector     = new Vector<GVRouting>();

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

        defaultId = XMLConfig.get(defNode, "@default-id", "");
        try {
            onExceptionId = XMLConfig.get(defNode, "@on-exception-id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'on-exception-id'"}, {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }

        NodeList nl = null;
        try {
            nl = XMLConfig.getNodeList(defNode, "GVRouting");
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if ((nl != null) && (nl.getLength() > 0)) {
            for (int i = 0; i < nl.getLength(); i++) {
                GVRouting routing = new GVRouting();
                routing.init(nl.item(i), defNode);
                routingVector.add(routing);
            }
        }

        if (defaultId.equals("") && (routingVector.size() == 0)) {
            throw new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id", getId()}});
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
        logger.debug("BEGIN - Execute GVNodeCheck '" + getId() + "'");
        checkInterrupted("GVNodeCheck", logger);
        dumpEnvironment(logger, true, environment);

        String input = getInput();
        String nextNodeId = "";
        String conditionName = "";
        int i = 0;
        Throwable lastException = (Throwable) environment.get(LAST_GV_EXCEPTION);

        Object inputObject = environment.get(input);
        if (logger.isDebugEnabled() || isDumpInOut()) {
            if (inputObject instanceof GVBuffer) {
                GVBuffer data = (GVBuffer) inputObject;
                logger.info(GVFormatLog.formatINPUT(data, false, false).toString());
            }
            else {
                logger.info("DUMP INPUT BUFFER NOT A GVBUFFER: " + inputObject);
            }
        }

        try {
            while ((i < routingVector.size()) && nextNodeId.equals("") && !isInterrupted()) {
                GVRouting routing = routingVector.elementAt(i);
                nextNodeId = routing.getNodeId(input, environment);
                conditionName = routing.getConditionName();
                i++;
            }
            checkInterrupted("GVNodeCheck", logger);
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Exception caught while checking routing condition - GVNodeCheck '" + getId()
                    + "' - Exception: " + exc);
            nextNodeId = onExceptionId;
            lastException = exc;
            conditionName = "EXCEPTION";
        }

        if (nextNodeId.equals("")) {
            if (!Throwable.class.isInstance(environment.get(input))) {
                if (defaultId.equals("")) {
                    lastException = new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id",
                            getId()}});
                    environment.put(input, lastException);
                    nextNodeId = onExceptionId;
                    conditionName = "EXCEPTION";
                }
                else {
                    nextNodeId = defaultId;
                    conditionName = "DEFAULT";
                }
            }
            else {
                nextNodeId = onExceptionId;
                lastException = (Throwable) environment.get(input);
                conditionName = "EXCEPTION";
            }
        }
        environment.put(LAST_GV_EXCEPTION, lastException);
        long endTime = System.currentTimeMillis();
        logger.info("Executing GVNodeCheck '" + getId() + "' - '" + conditionName + "' -> '" + nextNodeId + "'");
        logger.info("END - Execute GVNodeCheck '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
        return nextNodeId;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId()
    {
        return defaultId;
    }

    /**
     * Do nothing.
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        for (GVRouting r : routingVector) {
            r.cleanUp();
        }
    }

    /**
     * Do nothing.
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        routingVector.clear();
    }
}
