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
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.config.InvocationContext;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.savepoint.SavePointController;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVFlow node indicating a flow end.
 *
 * @version 3.1.0 Feb 11, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class GVSavePointNode extends GVFlowNode
{
    private static final Logger logger     = org.slf4j.LoggerFactory.getLogger(GVSavePointNode.class);

    /**
     * the next flow node id
     */
    private String              nextNodeId = "";
    private Map<String, String> propDefs   = new HashMap<String, String>();

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

        nextNodeId = XMLConfig.get(defNode, "@next-node-id", "");
        if (nextNodeId.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }

        try {
            NodeList pnl = XMLConfig.getNodeList(defNode, "SpProperties/PropertyDef");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    propDefs.put(XMLConfig.get(n, "@name"), XMLConfig.get(n, "@value"));
                }
            }
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_SAVEPOINT_NODE_INIT_ERROR", new String[][]{{"id", getId()},
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
        logger.info("Executing GVSavePointNode '" + getId() + "'");
        checkInterrupted("GVSavePointNode", logger);
        dumpEnvironment(logger, true, environment);

        Object data = environment.get(getInput());
        if (Throwable.class.isInstance(data)) {
            // TODO: dovrebbe lanciare una eccezione???
            environment.put(getOutput(), data);
            logger.debug("SKIP - Execute GVSavePointNode '" + getId() + "'");
            return nextNodeId;
        }

        try {
            Map<String, String> properties = new HashMap<String, String>();

            GVBuffer internalData = (GVBuffer) data;

            for (Map.Entry<String, String> p : propDefs.entrySet()) {
                String value = p.getValue();
                properties.put(p.getKey(), PropertiesHandler.expand(value, null, internalData));
            }
            // must be set prior to save the environment!!!
            environment.put("IS_SAVE_POINT", new Boolean(true));

            InvocationContext ctx = (InvocationContext) InvocationContext.getInstance();
            SavePointController.instance().save(internalData.getId().toString(), ctx.getSystem(), ctx.getService(),
                    ctx.getOperation(), getId(), environment, properties);
        }
        catch (Exception exc) {
            environment.put(getOutput(), exc);
        }

        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVSavePointNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
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
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        // do nothing
    }

    /**
     *
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        // do nothing
    }
}