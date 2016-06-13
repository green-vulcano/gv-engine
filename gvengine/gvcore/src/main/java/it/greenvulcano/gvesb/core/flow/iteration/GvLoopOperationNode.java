/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow.iteration;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.flow.GVFlowNode;
import it.greenvulcano.gvesb.core.flow.iteration.controller.BaseLoopController;
import it.greenvulcano.util.xpath.XPathFinder;

/**
 * An alternative iteration logic in GVESB.
 * 
 * To reduce complexity in configuration, 
 * this implementation works without DataProvider, InputServce and OutputService,
 * handling a collection provided in the input buffer.
 * 
 * @version 4.0.0 20160603
 * @author GreenVulcano Developer Team
 * 
 */
public class GvLoopOperationNode extends GVFlowNode {

	private static final Logger LOG = LoggerFactory.getLogger(GvLoopOperationNode.class);
	
	private String nextNodeId = "";
	private LoopController loopController;
	
	/**
	 * Required attributes are: 
	 * <ul>
	 * <li><b>next-node-id</b>: the next node in the flow</li>
	 * <li><b>collection-type</b>: map a {@link LoopController} implementation </li>
	 * </ul>
     *
     * @see GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException   {
        super.init(defNode);

        nextNodeId = XMLConfig.get(defNode, "@next-node-id", "");
        if (nextNodeId.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'next-node-id'"},
                    {"node", XPathFinder.buildXPath(defNode)}});
        }
        try {
	        String collectionTypeId = XMLConfig.get(defNode, "@collection-type", "invalid");
	        LoopController.Type collectionType = LoopController.Type.getById(collectionTypeId).orElseThrow(NoSuchElementException::new); 
        
        	loopController = BaseLoopController.create(collectionType, defNode);
        } catch (Exception e) {
        	throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'collection-type'"},
                {"node", XPathFinder.buildXPath(defNode)}});
        }
       
    }	
	
	@Override
	public String getDefaultNextNodeId() {	
		return nextNodeId;
	}

	@Override
	public String execute(Map<String, Object> environment, boolean onDebug)	throws GVCoreException, InterruptedException {
		 long startTime = System.currentTimeMillis();
	        Object data = null;
	        String input = getInput();
	        String output = getOutput();
	        LOG.info("Executing OperationNode '" + getId() + "'");
	        checkInterrupted("OperationNode", LOG);
	        dumpEnvironment(LOG, true, environment);

	        data = environment.get(input);
	        if (Throwable.class.isInstance(data)) {
	            environment.put(output, data);
	            LOG.debug("END - Execute OperationNode '" + getId() + "'");
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
	            
	            internalData = loopController.executeLoop(internalData, onDebug);
	           
	            environment.put(output, internalData);
	        } catch (Exception exc) {
	            environment.put(output, exc);
	        }

	        dumpEnvironment(LOG, false, environment);
	        long endTime = System.currentTimeMillis();
	        LOG.info("END - Execute OperationNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
	        return nextNodeId;
	}

	@Override
	public void cleanUp() throws GVCoreException {
		// do nothing

	}

	@Override
	public void destroy() throws GVCoreException {
		// do nothing

	}

}
