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
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * GVSubFlowCallNode class.
 * 
 * @version 3.2.0 Mar 02, 2011
 * @author GreenVulcano Developer Team
 */
public class GVSubFlowCallNode extends GVFlowNode
{
    private static final Logger logger           = org.slf4j.LoggerFactory.getLogger(GVSubFlowCallNode.class);
    /**
     * the default flow node id
     */
    private String              defaultId        = "";
    /**
     * the onException flow node id
     */
    private String              onExceptionId    = "";
    /**
     * the routing condition vector
     */
    private Vector<GVRouting>   routingVector    = new Vector<GVRouting>();
    /**
     * Definition node.
     */
    private Node                defNode          = null;
    /**
     * The SubFlow name to invoke.
     */
    private String              flowOp           = "";
    /**
     * If true the input SubFlow name are handled as metadata and resolved at runtime.
     */
    private boolean             isSubFlowNameDynamic = false;
    /**
     * The current SubFlow instance.
     */
    private GVSubFlow           subFlow          = null;
    /**
     * The SubFlow instances cache.
     */
    private Map<String, GVSubFlow> subFlowMap       = null;
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices  = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices = new GVInternalServiceHandler();

    /**
     * If true update the log context.
     */
    private boolean             changeLogContext = false;

    private String              inputRefDP       = null;
    private String              outputRefDP      = null;

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
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

        initSubFlow(defNode);
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
        logger.info("Executing GVSubFlowCallNode '" + getId() + "'");
        checkInterrupted("GVSubFlowCallNode", logger);
        dumpEnvironment(logger, true, environment);

        Object inData = environment.get(input);
        if (Throwable.class.isInstance(inData)) {
            environment.put(output, inData);
            logger.debug("END - Execute GVSubFlowCallNode '" + getId() + "' with Exception input -> " + onExceptionId);
            return onExceptionId;
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

            try {
                NMDC.push();

                String localFlowOp = createSubFlow(internalData);

                if (changeLogContext) {
                    NMDC.setOperation(localFlowOp);
                    GVBufferMDC.put(internalData);
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
                internalData = subFlow.perform(internalData, onDebug);
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
            }
            environment.put(output, internalData);
            if (logger.isDebugEnabled() || isDumpInOut()) {
                logger.info(GVFormatLog.formatOUTPUT(internalData, false, false).toString());
            }
        }
        catch (InterruptedException exc) {
            logger.error("GVSubFlowCallNode [" + getId() + "] interrupted!", exc);
            throw exc;
        }
        catch (Exception exc) {
            environment.put(output, exc);
        }

        String nextNodeId = "";
        String conditionName = "";
        int i = 0;
        Throwable lastException = (Throwable) environment.get(GVNodeCheck.LAST_GV_EXCEPTION);
        Object outputObject = environment.get(output);

        try {
            while ((i < routingVector.size()) && nextNodeId.equals("")) {
                GVRouting routing = routingVector.elementAt(i);
                nextNodeId = routing.getNodeId(output, environment);
                conditionName = routing.getConditionName();
                i++;
            }
        }
        catch (Exception exc) {
            logger.error("Exception caught while checking routing condition - GVSubFlowCallNode '" + getId() + "'", exc);
            nextNodeId = onExceptionId;
            lastException = exc;
            conditionName = "EXCEPTION";
        }

        if (nextNodeId.equals("")) {
            if (!Throwable.class.isInstance(outputObject)) {
                if (defaultId.equals("")) {
                    lastException = new GVCoreConfException("GVCORE_BAD_ROUTING_CFG_ERROR", new String[][]{{"id",
                            getId()}});
                    environment.put(output, lastException);
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
                lastException = (Throwable) outputObject;
                conditionName = "EXCEPTION";
            }
        }
        environment.put(GVNodeCheck.LAST_GV_EXCEPTION, lastException);
        logger.info("Executing GVSubFlowCallNode '" + getId() + "' - '" + conditionName + "' -> '" + nextNodeId + "'");

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVSubFlowCallNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
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
        return defaultId;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException
    {
        inputServices.cleanUp();
        outputServices.cleanUp();
        for (GVRouting r : routingVector) {
            r.cleanUp();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException
    {
        defNode = null;
        subFlow = null;
        inputServices = null;
        outputServices = null;
        routingVector.clear();
        if (subFlowMap != null) {
            Iterator<String> i = subFlowMap.keySet().iterator();
            while (i.hasNext()) {
                subFlowMap.get(i.next()).destroy();
            }
            subFlowMap.clear();
        }
    }

    /**
     * @param defNode
     *        the flow node definition
     * @throws CoreConfigException
     *         if errors occurs
     */
    private void initSubFlow(Node defNode) throws GVCoreConfException
    {
        try {
            subFlowMap = new HashMap<String, GVSubFlow>();
            this.defNode = defNode;
            changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
            isSubFlowNameDynamic = XMLConfig.getBoolean(defNode, "@dynamic", false);
            logger.debug("isSubFlowNameDynamic  = " + isSubFlowNameDynamic);
            flowOp = XMLConfig.get(defNode, "@subflow");
            logger.debug("subflow  = " + flowOp);
            inputRefDP = XMLConfig.get(defNode, "@input-ref-dp", "");
            outputRefDP = XMLConfig.get(defNode, "@output-ref-dp", "");

            if (flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }

            createSubFlow(null);
            
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
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (PropertiesHandlerException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

    /**
     * @param data
     * @throws XMLConfigException
     * @throws GVCoreConfException
     * @throws PropertiesHandlerException 
     */
    private String createSubFlow(GVBuffer data) throws XMLConfigException, GVCoreConfException, PropertiesHandlerException {
        String localFlowOp = flowOp;
        if (isSubFlowNameDynamic) {
            if (data == null) {
                return localFlowOp;
            }
            Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(data, true);
            localFlowOp = PropertiesHandler.expand(localFlowOp, props, data);
            logger.debug("Calling SubFlow: " + localFlowOp);
        }
        subFlow = subFlowMap.get(localFlowOp);
        if (subFlow == null) {
            Node fNode = XMLConfig.getNode(defNode, "ancestor::Operation/SubFlow[@name='" + localFlowOp + "']");
            if (fNode == null) {
                throw new GVCoreConfException("GVCORE_INVALID_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"subflow", localFlowOp}, {"node", XPathFinder.buildXPath(defNode)}});
            }
    
            subFlow = new GVSubFlow();
            subFlow.init(fNode, true);
            subFlowMap.put(localFlowOp, subFlow);
        }
        return localFlowOp;
    }

}
