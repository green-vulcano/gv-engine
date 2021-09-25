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
import it.greenvulcano.gvesb.core.exc.GVCoreSkippedException;
import it.greenvulcano.gvesb.core.flow.parallel.BaseParallelNode;
import it.greenvulcano.gvesb.core.flow.parallel.GVSubFlowPool;
import it.greenvulcano.gvesb.core.flow.parallel.ParallelExecutor;
import it.greenvulcano.gvesb.core.flow.parallel.Result;
import it.greenvulcano.gvesb.core.flow.parallel.SubFlowTask;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.log.GVFormatLog;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVSubFlowSplittedNode class.
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 */
public class GVSubFlowSplittedNode extends BaseParallelNode
{
    private static final Logger logger           = org.slf4j.LoggerFactory.getLogger(GVSubFlowSplittedNode.class);

    /**
     * the default next flow node id
     */
    private String              defaultId        = "";
    /**
     * the onException flow node id
     */
    private String              onExceptionId    = "";
    /**
     * the onTimeout flow node id
     */
    private String              onTimeoutId      = "";
    /**
     * the onSkip flow node id
     */
    private String              onSkipId         = "";
    /**
     * The SubFlow name to invoke.
     */
    private String              flowOp           = "";
    /**
     * The used to change the SubFlow input
     */
    private String              inputRefDP       = null;
    /**
     * Keeps reference to <code>IDataProvider</code> implementation.
     */
    private String              partitionDP;
    /**
     * The SubFlowPool instance.
     */
    private GVSubFlowPool       subFlowPool      = null;
    /**
     * the input services
     */
    private GVInternalServiceHandler inputServices  = new GVInternalServiceHandler();
    /**
     * the output services
     */
    private GVInternalServiceHandler outputServices = new GVInternalServiceHandler();
    /**
     * The SubFlow executor
     */
    private ParallelExecutor    executor         = null;
    /**
     * Max executor Thread pool
     */
    private int                 maxThread        = 5; 
    /**
     * The desired termination mode
     */
    private ParallelExecutor.TerminationMode termMode;
    /**
     * Execution timeout, in seconds
     */
    private int                 timeout          = 30;
    /**
     * If true update the log context.
     */
    private boolean             changeLogContext = false;


    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node defNode) throws GVCoreConfException {
        super.init(defNode);

        try {
            defaultId = XMLConfig.get(defNode, "@default-id", "");
            if (defaultId.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'default-id'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            onExceptionId = XMLConfig.get(defNode, "@on-exception-id", "");
            onTimeoutId = XMLConfig.get(defNode, "@on-timeout-id", "");
            onSkipId = XMLConfig.get(defNode, "@on-skip-id", "");
            maxThread = XMLConfig.getInteger(defNode, "@max-thread", 5);
            timeout = XMLConfig.getInteger(defNode, "@timeout", 30);
            termMode = ParallelExecutor.TerminationMode.fromString(XMLConfig.get(defNode, "@termination-mode", 
                    "normal-end"));
            
            changeLogContext = XMLConfig.getBoolean(defNode, "@change-log-context", true);
            flowOp = XMLConfig.get(defNode, "@subflow");
            logger.debug("subflow  = " + flowOp);
            partitionDP = XMLConfig.get(defNode, "@partition-dp", "");
            inputRefDP = XMLConfig.get(defNode, "@ref-dp", "");
    
            if (flowOp.equals("")) {
                throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'subflow'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }

            Node intSvcNode = XMLConfig.getNode(defNode, "InputServices");
            if (intSvcNode != null) {
                inputServices.init(intSvcNode, this, true);
            }
            intSvcNode = XMLConfig.getNode(defNode, "OutputServices");
            if (intSvcNode != null) {
                outputServices.init(intSvcNode, this, true);
            }
            initSubFlowPool(defNode);
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_INIT_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#execute(java.util.Map,
     *      boolean)
     */
    @Override
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException, InterruptedException {
        long startTime = System.currentTimeMillis();
        GVBuffer internalData = null;
        List<Result> results = null;
        boolean isSkipped = false;
        boolean isError = false;
        String input = getInput();
        String output = getOutput();
        logger.info("Executing GVSubFlowSplittedNode '" + getId() + "'");
        checkInterrupted("GVSubFlowSplittedNode", logger);
        dumpEnvironment(logger, true, environment);

        Object inData = environment.get(input);
        if (Throwable.class.isInstance(inData)) {
            environment.put(output, inData);
            logger.debug("END - Execute GVSubFlowSplittedNode '" + getId() + "' with Exception input -> " + onExceptionId);
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

                /*if (changeLogContext) {
                    NMDC.setOperation(flowOp);
                    GVBufferMDC.put(internalData);
                }*/
                
                internalData = inputServices.perform(internalData);
                
                results = processSubFlow(internalData, onDebug);
                checkInterrupted("GVSubFlowSplittedNode", logger);
                internalData = processOutput(internalData, results);
                
                internalData = outputServices.perform(internalData);
            }
            catch (GVCoreSkippedException exc) {
                logger.warn("Get Skipped notification");
                isSkipped = true;
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
            logger.error("GVSubFlowSplittedNode [" + getId() + "] interrupted!", exc);
            throw exc;
        }
        catch (Exception exc) {
            environment.put(output, exc);
            isError = true;
        }

        String nextNodeId = defaultId;
        String conditionName = "DEFAULT";
        if (isError) {
            conditionName = "EXCEPTION";
            nextNodeId = onExceptionId;
        }
        else if (isSkipped) {
            conditionName = "SKIPPED";
            nextNodeId = ("".equals(onSkipId) ? defaultId : onSkipId);
        }
        else if (executor.isTimedout()) {
            conditionName = "TIMEOUT";
            nextNodeId = ("".equals(onTimeoutId) ? defaultId : onTimeoutId);
        }
        else {
            //environment.put(GVNodeCheck.LAST_GV_EXCEPTION, lastException);
        }

        logger.info("Executing GVSubFlowSplittedNode '" + getId() + "' - '" + conditionName + "' -> '" + nextNodeId + "'");

        dumpEnvironment(logger, false, environment);
        long endTime = System.currentTimeMillis();
        logger.info("END - Execute GVSubFlowSplittedNode '" + getId() + "' - ExecutionTime (" + (endTime - startTime) + ")");
        return nextNodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#getDefaultNextNodeId()
     */
    @Override
    public String getDefaultNextNodeId() {
        return defaultId;
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#cleanUp()
     */
    @Override
    public void cleanUp() throws GVCoreException {
        if (executor != null) {
            executor.cleanup(true);
        }
        inputServices.cleanUp();
        outputServices.cleanUp();
    }

    /**
     * @see it.greenvulcano.gvesb.core.flow.GVFlowNode#destroy()
     */
    @Override
    public void destroy() throws GVCoreException {
        if (executor != null) {
            executor.destroy();
        }
        executor = null;
        if (subFlowPool != null) {
            subFlowPool.destroy();
        }
        subFlowPool = null;
        inputServices = null;
        outputServices = null;
    }

    
    /**
     * @param defNode
     *        the flow node definition
     * @throws CoreConfigException
     *         if errors occurs
     */
    private void initSubFlowPool(Node defNode) throws GVCoreConfException {
        try {
            Node sfNode = XMLConfig.getNode(defNode, "ancestor::Operation/SubFlow[@name='" + flowOp + "']");
            if (sfNode == null) {
                throw new GVCoreConfException("GVCORE_INVALID_CFG_PARAM_ERROR", new String[][]{{"name", "'operation'"},
                        {"node", XPathFinder.buildXPath(defNode)}});
            }
            subFlowPool = new GVSubFlowPool();
            subFlowPool.init(defNode, sfNode);
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
        catch (GVException exc) {
            throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"id", getId()},
                    {"node", XPathFinder.buildXPath(defNode)}}, exc);
        }
    }

    @SuppressWarnings("unchecked")
	private List<Result> processSubFlow(GVBuffer internalData, boolean onDebug) throws CallException, 
                                       GVCoreSkippedException, InterruptedException {
        logger.info("BEGIN ProcessSubFlow");
        Map<String, String> logContext = NMDC.getCurrentContext();
        List<SubFlowTask> tasks = new ArrayList<SubFlowTask>();
        List<Result> output = null;
        try {
            if (executor == null) {
                executor = new ParallelExecutor(getId(), maxThread, logger);
            }
            DataProviderManager dataProviderManager = DataProviderManager.instance();
            IDataProvider partitionDataProvider = dataProviderManager.getDataProvider(partitionDP);
            Collection<Object> input;
            try {
                logger.debug("Working on data provider: " + partitionDataProvider);
                partitionDataProvider.setObject(internalData);
                input = (Collection<Object>) partitionDataProvider.getResult();
            }
            finally {
                dataProviderManager.releaseDataProvider(partitionDP, partitionDataProvider);
            }

            IDataProvider inputDataProvider = null;
            try {
                if ((inputRefDP != null) && (inputRefDP.length() > 0)) { 
                     inputDataProvider = dataProviderManager.getDataProvider(inputRefDP);
                }
    
                Iterator<Object> itInput = input.iterator();
                while (itInput.hasNext() && !isInterrupted()) {
                    Object currData = itInput.next();
    
                    GVBuffer currInput = new GVBuffer(internalData, false);
                    currInput.setObject(currData);
                    if (inputDataProvider != null) {
                        inputDataProvider.setObject(currInput); 
                        Object inputCall = inputDataProvider.getResult();
                        currInput.setObject(inputCall);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("currInput= " + currInput.toString());
                    }
                    
                    tasks.add(new SubFlowTask(subFlowPool, currInput, onDebug, changeLogContext, logContext, null, resultProcessor.needsOutput()));
                }

                checkInterrupted("GVSubFlowSplittedNode", logger);

                if (tasks.isEmpty()) {
                    throw new GVCoreSkippedException("GV_SKIPPED_SPLITTED_SERVICE_ERROR", new String[][]{
                            {"service", internalData.getService()}, {"system", internalData.getSystem()},
                            {"id", internalData.getId().toString()}, {"message", "No parallel task to execute"}});
                }
                output = executor.execute(internalData.getId(), tasks, termMode, timeout);
            }
            finally {
                if (inputDataProvider != null) {
                    dataProviderManager.releaseDataProvider(inputRefDP, inputDataProvider);
                }
            }
            logger.info("END ProcessSubFlow");

            return output;
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (GVCoreSkippedException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("An error occurred while performing splitted call", exc);
            throw new CallException("GV_CALL_SPLITTED_SERVICE_ERROR", new String[][]{
                      {"service", internalData.getService()}, {"system", internalData.getSystem()},
                      {"id", internalData.getId().toString()}, {"message", exc.getMessage()}}, exc);
        }
        finally {
            tasks.clear();
        }
    }

}
