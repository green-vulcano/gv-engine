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
import it.greenvulcano.gvesb.core.exc.GVCoreSecurityException;
import it.greenvulcano.gvesb.gvdp.DataProviderManager;
import it.greenvulcano.gvesb.gvdp.IDataProvider;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.internal.data.GVBufferPropertiesHelper;
import it.greenvulcano.gvesb.log.GVBufferMDC;
import it.greenvulcano.gvesb.policy.ACLManager;
import it.greenvulcano.gvesb.policy.impl.GVCoreServiceKey;
import it.greenvulcano.gvesb.virtual.CallException;
import it.greenvulcano.gvesb.virtual.EnqueueException;
import it.greenvulcano.gvesb.virtual.InitializationException;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.OperationKey;
import it.greenvulcano.gvesb.virtual.VCLException;
import it.greenvulcano.gvesb.virtual.VCLOperationKey;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Helper class, contains all the iteration handling logic.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class IteratorController
{
    private static final Logger    logger                      = org.slf4j.LoggerFactory.getLogger(IteratorController.class);

    /**
     * Call operation type
     */
    public final static int        ITERATOR_OPTYPE_CALL        = 1;
    /**
     * Enqueue operation type
     */
    public final static int        ITERATOR_OPTYPE_ENQUEUE     = 2;
    /**
     * Call Service operation type
     */
    public final static int        ITERATOR_OPTYPE_CALLSERVICE = 3;
    /**
     * Call SubFlow operation type
     */
    public final static int        ITERATOR_OPTYPE_CALLSUBFLOW = 4;

    /**
     * The type of this iterator operation.
     */
    private int                    operationType               = 0;

    /**
     * The <code>Operation</code> object to be used for each iteration call.
     */
    private Operation              operation                   = null;

    /**
     * The <code>java.lang.Class</code> object associated to an exception type
     * which is supposed to break the iteration loop, if caught while executing
     * one of the call iterations.
     */
    private Class<?>               exitLoopExceptionClass      = null;

    private Map<String, GVSubFlow> subFlows                    = new HashMap<String, GVSubFlow>();

    /**
     * <code>true</code> if the exception which broke the iteration loop must be
     * re-thrown, <code>false</code> otherwise.
     */
    private boolean                rethrowExitLoopException    = false;

    private String                 service                     = null;
    private String                 system                      = null;
    private String                 flowOp                      = null;
    private String                 subflow                     = null;
    /**
     * If true the input id_system, id_service and operation are handled as
     * metadata and resolved at runtime.
     */
    private boolean                isFlowSysSvcOpDynamic       = false;
    private String                 call_dp                     = null;
    private boolean                changeLogContext            = true;
    /**
     * If true update the log master service file.
     */
    private boolean                changeLogMasterService      = false;
    private boolean                accumulateOutput            = false;
    private boolean                returnFullIterOutput        = false;

    /**
     * Keeps reference to <code>IDataProvider</code> implementation.
     */
    protected String               collectionDP;
    /**
     * The ESB context instance.
     */
    private InvocationContext      gvContext                   = null;

    private Node                   defNode                     = null;
    private String                 id                          = null;


    /**
     * Configuration method.
     * 
     * @param node
     *        configuration node. Use this node with <code>XMLConfig</code> in
     *        order to read configuration parameters.
     * 
     * @exception InitializationException
     *            if an error occurs during initialization
     */
    public void doInit(Node node, String id) throws InitializationException
    {
        try {
            this.id = id;
            defNode = node;
            accumulateOutput = XMLConfig.getBoolean(node, "@accumulate-output", true);
            returnFullIterOutput = XMLConfig.getBoolean(node, "@full-iteration-output", false);
            collectionDP = XMLConfig.get(node, "@collection-dp", "");

            String exitLoopExceptionClassname = XMLConfig.get(node, "exit-loop-condition/exception-event/@value", null);
            if (exitLoopExceptionClassname != null) {
                exitLoopExceptionClass = Class.forName(exitLoopExceptionClassname);
                rethrowExitLoopException = XMLConfig.getBoolean(node, "exit-loop-condition/exception-event/@rethrow",
                        true);
            }

            if (XMLConfig.exists(node, "*[@type='call']")) {
                operationType = ITERATOR_OPTYPE_CALL;
                logger.debug("operationType=ITERATOR_OPTYPE_CALL");
                OperationKey opKey = new VCLOperationKey(XMLConfig.getNode(node, "*[@type='call']"));
         
                operation = OperationFactory.createCall(opKey);
            }
            else if (XMLConfig.exists(node, "*[@type='enqueue']")) {
                operationType = ITERATOR_OPTYPE_ENQUEUE;
                logger.debug("operationType=ITERATOR_OPTYPE_ENQUEUE");
                OperationKey opKey = new VCLOperationKey(XMLConfig.getNode(node, "*[@type='enqueue']"));
                operation = OperationFactory.createEnqueue(opKey);
            }
            else if (XMLConfig.exists(node, "CoreCall")) {
                operationType = ITERATOR_OPTYPE_CALLSERVICE;
                logger.debug("operationType=ITERATOR_CORE_CALL");
                changeLogContext = XMLConfig.getBoolean(node, "CoreCall/@change-log-context", true);
                changeLogMasterService = changeLogContext && XMLConfig.getBoolean(defNode, "CoreCall/@change-log-master-service", false);
                system = XMLConfig.get(node, "CoreCall/@id-system", GVBuffer.DEFAULT_SYS);
                logger.debug("system=" + system);
                service = XMLConfig.get(node, "CoreCall/@id-service");
                logger.debug("service=" + service);
                flowOp = XMLConfig.get(node, "CoreCall/@operation");
                logger.debug("flowOp=" + flowOp);
                isFlowSysSvcOpDynamic = XMLConfig.getBoolean(node, "CoreCall/@dynamic", false);
                logger.debug("isFlowSysSvcOpDynamic  = " + isFlowSysSvcOpDynamic);
                call_dp = XMLConfig.get(node, "CoreCall/@ref-dp");
                logger.debug("call_dp=" + call_dp);
                gvContext = new InvocationContext();
            }
            else if (XMLConfig.exists(node, "SubFlowCall")) {
                operationType = ITERATOR_OPTYPE_CALLSUBFLOW;
                logger.debug("operationType=ITERATOR_SUBFLOW_CALL");
                changeLogContext = XMLConfig.getBoolean(node, "SubFlowCall/@change-log-context", true);
                subflow = XMLConfig.get(node, "SubFlowCall/@subflow");
                logger.debug("subflow=" + subflow);
                isFlowSysSvcOpDynamic = XMLConfig.getBoolean(node, "SubFlowCall/@dynamic", false);
                logger.debug("isFlowSysSvcOpDynamic  = " + isFlowSysSvcOpDynamic);
                call_dp = XMLConfig.get(node, "SubFlowCall/@ref-dp");
                logger.debug("call_dp=" + call_dp);
                gvContext = new InvocationContext();
            }
            else {
                logger.error("An error occurred while configuring the " + getClass().getName()
                        + " plug-in: unsupported iterator operation type");
                throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message",
                        "Unsupported iterator operation type"}});
            }
        }
        catch (InitializationException exc) {
            throw exc;

        }
        catch (XMLConfigException exc) {
            logger.error("An error occurred while configuring the " + getClass().getName() + " plug-in: ", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
        catch (Exception exc) {
            logger.error("A generic error occurred while initializing the " + getClass().getName() + " plug-in: ", exc);
            throw new InitializationException("GV_CONFIGURATION_ERROR", new String[][]{{"message", exc.getMessage()}},
                    exc);
        }
    }

    /**
     * Execute the operation using an <code>GVBuffer</code>.
     * 
     * @param gvBuffer
     *        input data for the operation.
     * @param onDebug 
     * 
     * @return an <code>GVBuffer</code> containing the operation result.
     * 
     * @exception VCLException
     *            if an error occurs performing the operation. GreenVulcano ESB
     *            should retry later to perform the operation.
     * 
     * @see it.greenvulcano.gvesb.virtual.Operation#perform(it.greenvulcano.gvesb.buffer.GVBuffer)
     */
    @SuppressWarnings("unchecked")
    public GVBuffer doPerform(GVBuffer gvBuffer, boolean onDebug) throws VCLException, InterruptedException
    {
        logger.info("INIT doPerform");
        GVBuffer gvBufferOutput = new GVBuffer(gvBuffer);
        Collection<Object> input = null;
        Collection<Object> output = new ArrayList<Object>();
        try {
            DataProviderManager dataProviderManager = DataProviderManager.instance();
            IDataProvider dataProvider = dataProviderManager.getDataProvider(collectionDP);
            try {
                logger.debug("Working on data provider: " + dataProvider);
                dataProvider.setObject(gvBuffer);
                input = (Collection<Object>) dataProvider.getResult();
            }
            finally {
                dataProviderManager.releaseDataProvider(collectionDP, dataProvider);
            }
            Object[] nl = input.toArray();
            int iterations = nl.length;
            output = new ArrayList<Object>();
            for (int i = 0; (i < iterations) && !isInterrupted(); i++) {
                Object currNode = nl[i];
                if (logger.isDebugEnabled()) {
                    logger.debug("currNode=" + currNode.toString());
                }
                if (currNode != null) {
                    GVBuffer currIterInput = gvBuffer;
                    currIterInput.setObject(currNode);
                    Exception caughtExc = null;
                    boolean caughtException = false;

                    if (i == (iterations - 1)) {
                        // This is the last iteration in the loop:
                        // insert the 'GVIC_LAST' property to notify this
                        // to the target operation object
                        currIterInput.setProperty("GVIC_LAST", "true");
                    }

                    try {
                        Object currIterOutput = null;
                        if (logger.isDebugEnabled()) {
                            logger.debug("currIterInput=" + currIterInput.toString());
                        }
                        if (operationType == ITERATOR_OPTYPE_CALLSERVICE) {
                            currIterOutput = executeCoreCall(currIterInput, onDebug);
                        }
                        else if (operationType == ITERATOR_OPTYPE_CALLSUBFLOW) {
                            currIterOutput = executeSubFlow(currIterInput, onDebug);
                        }
                        else {
                            currIterOutput = operation.perform(currIterInput);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("currIterOutput=" + currIterOutput.toString());
                        }

                        if (currIterOutput instanceof GVBuffer) {
                            if (accumulateOutput) {
                                GVBuffer currIterData = (GVBuffer) currIterOutput;
                                if (returnFullIterOutput) {
                                    output.add(currIterData);
                                }
                                else {
                                    output.add(currIterData.getObject());
                                }
                            }
                        }
                        else if (currIterOutput instanceof Exception) {
                            Exception currIterOutputExc = (Exception) currIterOutput;
                            currIterOutputExc.printStackTrace();
                            throw currIterOutputExc;
                        }
                    }
                    catch (InterruptedException exc) {
                        logger.error("Iteration n. " + (i+1) + " interrupted.");
                        throw exc;
                    }
                    catch (Exception exc) {
                        logger.warn("Performing iteration n." + (i + 1) + " caused a " + exc.getClass().getName());
                        caughtException = true;
                        caughtExc = exc;
                    }

                    if (caughtException) {
                        boolean exitLoopOnException = false;
                        if (exitLoopExceptionClass != null) {
                            if (exitLoopExceptionClass.isInstance(caughtExc)) {
                                exitLoopOnException = true;
                            }
                        }

                        if (exitLoopOnException) {
                            if (rethrowExitLoopException) {
                                logger.warn("Caught exception requires exiting loop AND rethrowing the exception...");
                                throw caughtExc;
                            }
                            logger.warn("Caught exception requires exiting loop...");
                            break;
                        }
                        else {
                            if (accumulateOutput && returnFullIterOutput) {
                                output.add(caughtExc);
                            }
                        }
                        logger.warn("Caught exception DOES NOT require exiting loop");

                        continue;
                    }
                }
            }
            if (isInterrupted()) {
                logger.error("IteratorController interrupted.");
                throw new InterruptedException("IteratorController interrupted.");
            }
            logger.info("END doPerform");
            if (accumulateOutput) {
                gvBufferOutput.setObject(output);
            }
            return gvBufferOutput;
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("An error occurred while performing business logic", exc);
            ThreadUtils.checkInterrupted(exc);
            if ((operationType == ITERATOR_OPTYPE_CALL) || (operationType == ITERATOR_OPTYPE_CALLSERVICE)) {
                throw new CallException("GV_CALL_SERVICE_ERROR", new String[][]{
                        {"service", gvBufferOutput.getService()}, {"system", gvBufferOutput.getSystem()},
                        {"id", gvBufferOutput.getId().toString()}, {"message", exc.getMessage()}}, exc);
            }
            else if (operationType == ITERATOR_OPTYPE_ENQUEUE) {
                throw new EnqueueException("GV_GENERIC_ERROR", new String[][]{{"service", gvBufferOutput.getService()},
                        {"system", gvBufferOutput.getSystem()}, {"id", gvBufferOutput.getId().toString()},
                        {"message", exc.getMessage()}}, exc);
            }
            else {
                throw new RuntimeException("Initialization is not done correctly!");
            }
        }
    }

    /**
     *
     */
    public void cleanUp()
    {
        if (operation != null) {
            operation.cleanUp();
        }
    }

    /**
     *
     */
    public void destroy()
    {
        if (operation != null) {
            operation.destroy();
        }
    }

    private GVBuffer executeCoreCall(GVBuffer internalGVBuffer, boolean onDebug) throws GVException, InterruptedException
    {
        GVBuffer result = null;
        Object inputCall = null;
        try {
            String localSystem = (GVBuffer.DEFAULT_SYS.equals(system) ? internalGVBuffer.getSystem() : system);
            String localService = service;
            String localFlowOp = flowOp;

            if (isFlowSysSvcOpDynamic) {
                Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalGVBuffer, true);
                localSystem = PropertiesHandler.expand(localSystem, props, internalGVBuffer);
                localService = PropertiesHandler.expand(localService, props, internalGVBuffer);
                localFlowOp = PropertiesHandler.expand(localFlowOp, props, internalGVBuffer);
            }

            internalGVBuffer.setService(localService);
            internalGVBuffer.setSystem(localSystem);
            GVServiceConf gvsConfig = null;
            InvocationContext mainCtx = (InvocationContext) InvocationContext.getInstance();
            gvContext.setContext(localFlowOp, internalGVBuffer);
            gvContext.setGVServiceConfigManager(mainCtx.getGVServiceConfigManager());
            gvContext.setStatisticsDataManager(mainCtx.getStatisticsDataManager());
            gvContext.setExtraField("DTE_CONTROLLER", mainCtx.getExtraField("DTE_CONTROLLER"));

            ServiceConfigManager svcMgr = gvContext.getGVServiceConfigManager();
            gvsConfig = svcMgr.getGVSConfig(internalGVBuffer);
            if (!ACLManager.canAccess(new GVCoreServiceKey(gvsConfig.getGroupName(), gvsConfig.getServiceName(),
                    localFlowOp))) {
                throw new GVCoreSecurityException("GV_SERVICE_POLICY_ERROR", new String[][]{
                        {"service", internalGVBuffer.getService()}, {"system", internalGVBuffer.getSystem()},
                        {"id", internalGVBuffer.getId().toString()}, {"user", GVIdentityHelper.getName()}});
            }
            GVFlow flow = gvsConfig.getGVOperation(internalGVBuffer, localFlowOp);
            if ((call_dp != null) && (call_dp.length() > 0)) {
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                IDataProvider dataProvider = dataProviderManager.getDataProvider(call_dp);
                try {
                    logger.debug("Working on data provider: " + dataProvider);
                    dataProvider.setObject(internalGVBuffer);
                    inputCall = dataProvider.getResult();
                    internalGVBuffer.setObject(inputCall);
                }
                finally {
                    dataProviderManager.releaseDataProvider(call_dp, dataProvider);
                }
            }
            NMDC.push();
            try {
                gvContext.push();
                if (changeLogContext) {
                    GVBufferMDC.put(internalGVBuffer);
                    NMDC.setOperation(localFlowOp);
                    NMDC.put(GVBuffer.Field.SERVICE.toString(), localService);
                    NMDC.put(GVBuffer.Field.SYSTEM.toString(), localSystem);
                }
              
                String masterService = null;
                try {
                    if (changeLogMasterService) {
                        masterService = GVBufferMDC.changeMasterService(localService);
                    }
                    

                    result = flow.perform(internalGVBuffer, onDebug);
                }
                finally {
                  
                    if (changeLogMasterService) {
                        GVBufferMDC.changeMasterService(masterService);
                    }
                }
            }
            finally {
                NMDC.pop();
                try {
                    gvContext.pop();
                    gvContext.cleanup();
                }
                catch (Exception exc) {
                    logger.warn("Failed cleanUp() of InvocationContext", exc);
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error performing operation", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new GVException("Error performing operation: " + exc);
        }
        return result;
    }

    private GVBuffer executeSubFlow(GVBuffer internalGVBuffer, boolean onDebug) throws GVException, InterruptedException
    {
        GVBuffer result = null;
        Object inputCall = null;
        try {
            String localSubFlow = subflow;

            if (isFlowSysSvcOpDynamic) {
                Map<String, Object> props = GVBufferPropertiesHelper.getPropertiesMapSO(internalGVBuffer, true);
                localSubFlow = PropertiesHandler.expand(subflow, props, internalGVBuffer);
            }

            InvocationContext mainCtx = (InvocationContext) InvocationContext.getInstance();
            gvContext.setContext(localSubFlow, internalGVBuffer);
            gvContext.setGVServiceConfigManager(mainCtx.getGVServiceConfigManager());
            gvContext.setStatisticsDataManager(mainCtx.getStatisticsDataManager());
            gvContext.setExtraField("DTE_CONTROLLER", mainCtx.getExtraField("DTE_CONTROLLER"));

            GVSubFlow subFlow = getSubFlow(localSubFlow);

            if ((call_dp != null) && (call_dp.length() > 0)) {
                DataProviderManager dataProviderManager = DataProviderManager.instance();
                IDataProvider dataProvider = dataProviderManager.getDataProvider(call_dp);
                try {
                    logger.debug("Working on data provider: " + dataProvider);
                    dataProvider.setObject(internalGVBuffer);
                    inputCall = dataProvider.getResult();
                    internalGVBuffer.setObject(inputCall);
                }
                finally {
                    dataProviderManager.releaseDataProvider(call_dp, dataProvider);
                }
            }
            NMDC.push();
            try {
                gvContext.push();
                if (changeLogContext) {
                    NMDC.setOperation(localSubFlow);
                    GVBufferMDC.put(internalGVBuffer);
                }
                result = subFlow.perform(internalGVBuffer, onDebug);
            }
            finally {
                NMDC.pop();
                try {
                    gvContext.pop();
                    gvContext.cleanup();
                }
                catch (Exception exc) {
                    logger.warn("Failed cleanUp() of InvocationContext", exc);
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error performing operation", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new GVException("Error performing operation: " + exc);
        }
        return result;
    }

    private GVSubFlow getSubFlow(String name) throws GVCoreConfException
    {
        GVSubFlow subFlow = subFlows.get(name);
        if (subFlow == null) {
            try {
                Node fNode = XMLConfig.getNode(defNode, "ancestor::Operation/SubFlow[@name='" + name + "']");
                if (fNode == null) {
                    throw new GVCoreConfException("GVCORE_SUB_FLOW_SEARCH_ERROR", new String[][]{{"name", "'subflow'"},
                            {"node", XPathFinder.buildXPath(defNode)}});
                }
                subFlow = new GVSubFlow();
                subFlow.init(fNode, true);
                subFlows.put(name, subFlow);
            }
            catch (GVCoreConfException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new GVCoreConfException("GVCORE_SUB_FLOW_INIT_ERROR", new String[][]{{"id", id},
                        {"node", XPathFinder.buildXPath(defNode)}}, exc);
            }
        }
        return subFlow;
    }
    
    /**
     * 
     * @return
     *        the current Thread interrupted state
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

}
