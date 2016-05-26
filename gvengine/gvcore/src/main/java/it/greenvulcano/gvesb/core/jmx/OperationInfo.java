/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.management.DomainAction;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.Stats;
import it.greenvulcano.util.thread.ThreadUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * OperationInfo class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class OperationInfo
{
    private static Logger                    logger                  = org.slf4j.LoggerFactory.getLogger(OperationInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String               DESCRIPTOR_NAME         = "OperationInfo";
    /**
     * the associated sfInfo map
     */
    private Map<String, SubFlowInfo>         sfMap                   = new HashMap<String, SubFlowInfo>();
    /**
     * the status of the currently running associated flow, by ID and Thread
     */
    private Map<String, Map<String, String>> flowsStatusMap          = new ConcurrentHashMap<String, Map<String, String>>();
    /**
     * the group name
     */
    private String                           group                   = "";
    /**
     * the service name
     */
    private String                           service                 = "";
    /**
     * the operation name
     */
    private String                           operation               = "";
    /**
     * the total successful invocation
     */
    private long                             totalSuccess            = 0;
    /**
     * the total failed invocation
     */
    private long                             totalFailure            = 0;
    /**
     * the activation flag
     */
    private boolean                          opActivation            = true;
    /**
     * the service activation flag
     */
    private boolean                          serviceActivation       = true;
    /**
     * the previous activation flag
     */
    private boolean                          oldActivation           = true;
    /**
     * the operation jmx key
     */
    private String                           jmxOperKey              = "";
    /**
     * the jmx filter for inter-instances communication
     */
    private String                           jmxFilter               = "";
    /**
     * if tru the instance runs on administration/support server
     */
    private boolean                          isAdministrator         = false;
    /**
     * If true the instance call the administration server for objects
     * initialization
     */
    private boolean                          callAdministratorOnInit = false;
    /**
     * The Object to calculate the GreenVulcano throughput
     */
    private static Stats                     statNodes               = null;
    /**
     * The Object to calculate the Operation failures throughput
     */
    private Stats                            statFailures            = null;
    private int                              maxFailuresRateo        = Integer.MAX_VALUE;
    private DomainAction                     failureAction           = null;
    private DomainAction                     enableAction            = null;
    private DomainAction                     disableAction           = null;

    /**
     * Static initializer
     */
    static {
        // perform calculation with: 1 sec, 1 sec, 30 sec
        statNodes = new Stats(1000, 1000, 30);
    }

    /**
     * Constructor
     * 
     * @param service
     *        the service name
     * @param operation
     *        the operation name
     * @param jmxSrvcKey
     *        the holding service jmx key
     */
    public OperationInfo(String group, String service, String operation, String jmxSrvcKey)
    {
        this.group = group;
        this.service = service;
        this.operation = operation;
        jmxOperKey = jmxSrvcKey + ",IDOperation=" + operation;
        jmxFilter = "GreenVulcano:*,Component=" + DESCRIPTOR_NAME + jmxSrvcKey + ",IDOperation=" + operation;
        statFailures = new Stats(1000, 1000, 1);
    }

    /**
     * Initialize the instance
     * 
     * @param initData
     *        initialization data
     */
    public void init(Map<String, Object> initData)
    {
        if (initData == null) {
            return;
        }
        failureAction = (DomainAction) initData.get("failureAction");
        enableAction = (DomainAction) initData.get("enableAction");
        disableAction = (DomainAction) initData.get("disableAction");
        Integer integer = (Integer) initData.get("failureRateo");
        if (integer != null) {
            maxFailuresRateo = integer.intValue();
        }
        else {
            maxFailuresRateo = Integer.MAX_VALUE;
        }
        setOperationActivation(((Boolean) initData.get("operationActivation")).booleanValue());

    }

    /**
     * Register the instance on JMX server
     * 
     * @param properties
     *        the object name properties
     * @param register
     *        the registration flag
     * @throws Exception
     *         if errors occurs
     */
    public void register(Map<String, String> properties, boolean register) throws Exception
    {
        if (register) {
            String key = properties.get("IDService") + ":" + properties.get("IDGroup") + "#" + operation;
            properties = getJMXProperties(properties);
            deregister(properties);
            JMXEntryPoint jmx = JMXEntryPoint.instance();

            logger.debug("Adding " + DESCRIPTOR_NAME + " for " + key);
            jmx.registerObject(this, DESCRIPTOR_NAME, properties);
            logger.debug("Adding " + DESCRIPTOR_NAME + "_Internal for " + key);
            jmx.registerObject(this, DESCRIPTOR_NAME + "_Internal", properties);
        }
    }

    /**
     * Deregister the instance from JMX server
     * 
     * @param properties
     *        the object name properties
     * @throws Exception
     *         if errors occurs
     */
    public void deregister(Map<String, String> properties) throws Exception
    {
        properties = getJMXProperties(properties);
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        try {
            jmx.unregisterObject(this, DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            // do nothing
        }
        try {
            jmx.unregisterObject(this, DESCRIPTOR_NAME + "_Internal", properties);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * Return the instance properties
     * 
     * @param properties
     *        properties list to enrich
     * @param full
     *        if true insert also status data
     * @return the property list
     */
    public Map<String, Object> getProperties(Map<String, Object> properties, boolean full)
    {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put("IDGroup", group);
        properties.put("IDService", service);
        properties.put("IDOperation", operation);
        if (full) {
            properties.put("operationActivation", new Boolean(opActivation));
            if (failureAction != null) {
                properties.put("failureAction", failureAction);
                properties.put("failureRateo", new Integer(maxFailuresRateo));
            }
            if (enableAction != null) {
                properties.put("enableAction", enableAction);
            }
            if (disableAction != null) {
                properties.put("disableAction", disableAction);
            }
        }
        return properties;
    }

    private Map<String, String> getJMXProperties(Map<String, String> properties)
    {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put("IDGroup", group);
        properties.put("IDService", service);
        properties.put("IDOperation", operation);
        return properties;
    }
    
    /**
     * Return the required SubFlowInfo instance
     * 
     * @param subflow
     *        the subflow name
     * @param register
     *        if true register the created operation
     * @return the requested operation
     * @throws Exception
     *         if errors occurs
     */
    public synchronized SubFlowInfo getSubFlowInfo(String subflow, boolean register) throws Exception
    {
        SubFlowInfo sfInfo = sfMap.get(subflow);
        if (sfInfo == null) {
            Map<String, Object> properties = getProperties(null, false);
            properties.put("IDSubFlow", subflow);
            sfInfo = new SubFlowInfo(subflow, jmxOperKey);
            sfInfo.setAdministrator(isAdministrator);
            sfInfo.setCallAdministratorOnInit(callAdministratorOnInit);

            if (isAdministrator || !callAdministratorOnInit) {
                Map<String, Object> objectData = getLocalObjectData(subflow);
                sfInfo.init(objectData);
            }
            else {
                Map<String, Object> objectData = null;
                String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component="
                        + JMXServiceManager.getDescriptorName();
                try {
                    objectData = getRemoteObjectData(properties, jmxFilterLocal);
                    if (objectData == null) {
                        throw new Exception();
                    }
                }
                catch (Exception exc) {
                    logger.warn("Error occurred contacting '" + jmxFilterLocal
                            + "'. Using local configuration for initialization of subflow '" + subflow + "'.");
                    objectData = getLocalObjectData(subflow);
                }
                sfInfo.init(objectData);
            }

            sfInfo.register(MapUtils.convertToHMStringString(properties), register);
            sfMap.put(subflow, sfInfo);
        }
        return sfInfo;
    }

    /**
     * Read configuration data from a remote object
     * 
     * @param properties
     *        the object name / configuration data
     * @param jmxFilterLocal
     *        the jmx filter to use
     * @return the required data
     * @throws Exception
     *         if errors occurs
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRemoteObjectData(Map<String, Object> properties, String jmxFilterLocal)
            throws Exception
    {
        Map<String, Object> objectData = new HashMap<String, Object>(properties);
        Object[] params = new Object[]{objectData};
        String[] signature = new String[]{"java.util.Hashtable"};
        objectData = (Map<String, Object>) JMXUtils.invoke(jmxFilterLocal, "getSubFlowInfoData", params, signature,
                true, logger);
        logger.debug("OperationInfo - Reading remote configuration data for " + objectData.get("IDSystem") + "#"
                + objectData.get("IDService") + "#" + objectData.get("IDOperation") + "#" + objectData.get("IDSubFlow"));
        return objectData;
    }

    /**
     * Read configuration data from a local configuration file
     * 
     * @param subflow
     *        the subflow name
     * @return the required data
     * @throws XMLConfigException
     *         if errors occurs
     */
    private Map<String, Object> getLocalObjectData(String subflow) throws XMLConfigException
    {
        Map<String, Object> objectData = new HashMap<String, Object>();
        String fileName = GreenVulcanoConfig.getServicesConfigFileName();
        Node svcNode = XMLConfig.getNode(fileName, "/GVServices/Services/Service[@id-service='" + service + "']");
        Node opNode = XMLConfig.getNode(svcNode, "Operation[(@name='" + operation + "') or (@forward-name='" + operation
                + "')]");
        Node sfNode = XMLConfig.getNode(opNode, "SubFlow[@name='" + subflow + "']");

        objectData.put("loggerLevel", XMLConfig.get(sfNode, "@loggerLevel", XMLConfig.get(opNode, "@loggerLevel", XMLConfig.get(svcNode, "@loggerLevel", XMLConfig.get(svcNode, "../@loggerLevel", "ALL")))));
        logger.debug("OperationInfo - Reading local configuration data for " + service + "#" + operation + "#" + subflow);
        return objectData;
    }

    /**
     * @param node
     *        the node from wich read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void synchronizeStatus(Node node) throws Exception
    {
        setOperationActivation(XMLConfig.getBoolean(node, "@operationActivation", true));
        
        NodeList subflowList = XMLConfig.getNodeList(node, "SubFlow");
        int num = subflowList.getLength();
        for (int i = 0; i < num; i++) {
            Node opNode = subflowList.item(i);
            String subflow = XMLConfig.get(node, "@subflow");
            SubFlowInfo sfInfo = getSubFlowInfo(subflow, true);
            sfInfo.synchronizeStatus(opNode);
        }
    }

    /**
     * @param initData
     *        the hashtable from wich read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void synchronizeStatus(Map<String, Object> initData) throws Exception
    {
        init(initData);
        
        Map<String, Object> properties = new HashMap<String, Object>(initData);

        String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component="
                + JMXServiceManager.getDescriptorName();

        for (SubFlowInfo sfInfo : sfMap.values()) {
            String subflow = sfInfo.getSubFlow();
            properties.put("IDSubFlow", subflow);
            try {
                properties = getRemoteObjectData(properties, jmxFilterLocal);
                sfInfo.init(properties);
            }
            catch (Exception exc) {
                logger.warn("Error occurred contacting '" + jmxFilterLocal
                        + "'. Syncronization failed for subflow '" + service + ":" + operation + ":" + subflow + "'.");
            }
            sfInfo.synchronizeStatus(properties);
        }
    }

    /**
     * @return the total successfull invocations
     */
    public long getTotalSuccess()
    {
        return totalSuccess;
    }

    /**
     * @return the total failed invocations
     */
    public long getTotalFailure()
    {
        return totalFailure;
    }

    /**
     * @return the total invocations
     */
    public long getTotal()
    {
        return (totalSuccess + totalFailure);
    }

    /**
     * reset the internal counter
     */
    public void resetCounter()
    {
        totalSuccess = 0;
        totalFailure = 0;
    }

    /**
     * Return a matrix of Id/GVFlowNode id for the associated flow status, by ID
     * and Thread
     * 
     * @return the current associated flow status
     */
    public Map<String, Map<String, String>> getFlowsStatus()
    {
        return Collections.unmodifiableMap(flowsStatusMap);
    }

    /**
     * Set the status of an associated flow
     * 
     * @param flowId
     *        the flow id
     * @param id
     *        the flow node id
     */
    public void setFlowStatus(String flowId, String id)
    {
        statNodes.hint();
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                flowsStatusMap.put(flowId, thFlows);
            }
            String threadName = Thread.currentThread().getName();
            thFlows.put(threadName, id);
        }
    }

    /**
     * Gets the status of an associated flow
     * 
     * @param flowId
     *        the flow id
     * @return the flow node id
     */
    public String getFlowStatus(String threadName, String flowId)
    {
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows == null) {
                thFlows = new ConcurrentHashMap<String, String>();
                flowsStatusMap.put(flowId, thFlows);
            }
            String flowStatus = thFlows.get(threadName);
            return flowStatus;
        }
    }

    /**
     * Mark an associated flow as terminated
     * 
     * @param flowId
     *        the flow id
     * @param success
     *        the termination status
     */
    public void flowTerminated(String flowId, boolean success)
    {
        if (success) {
            totalSuccess++;
        }
        else {
            statFailures.hint();
            totalFailure++;
            execFailureAction();
        }
        String threadName = Thread.currentThread().getName();
        synchronized (flowsStatusMap) {
            Map<String, String> thFlows = flowsStatusMap.get(flowId);
            if (thFlows != null) {
                thFlows.remove(threadName);
                if (thFlows.isEmpty()) {
                    flowsStatusMap.remove(flowId);
                }
            }
        }
    }
    
    public boolean interruptFlow(String threadName, String flowId) {
        try {
            logger.info("Interrupting flow [" + flowId + "/" + threadName + "] on Operation [" + operation + "]");
            boolean found = false;
            synchronized (flowsStatusMap) {
                Map<String, String> thFlows = flowsStatusMap.get(flowId);
                if (thFlows != null) {
                    found = thFlows.containsKey(threadName);
                }
            }

            if (found) {
                Thread th = ThreadUtils.getThread(threadName);
                if (th != null) {
                    th.interrupt();
                    logger.info("Interrupted flow [" + flowId + "/" + threadName + "] on Operation [" + operation + "]");
                    return true;
                }
            }
            logger.info("Failed interruption of flow [" + flowId + "/" + threadName + "] on Operation [" + operation + "] - Not found active flows");
        }
        catch (Exception exc) {
            logger.error("Error occurred executing Flow interruption", exc);
        }
        return false;
    }

    private void execFailureAction()
    {
        if ((failureAction != null) && (statFailures.getThroughput() > maxFailuresRateo)) {
            statFailures.reset();
            try {
                Object[] params = new Object[]{failureAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing FailureAction", exc);
            }
        }
    }

    private void execEnableAction()
    {
        if ((enableAction != null) && (oldActivation != (opActivation && serviceActivation))) {
            oldActivation = (opActivation && serviceActivation);
            try {
                Object[] params = new Object[]{enableAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing EnableAction", exc);
            }
        }
    }

    private void execDisableAction()
    {
        if ((disableAction != null) && (oldActivation != (opActivation && serviceActivation))) {
            oldActivation = (opActivation && serviceActivation);
            try {
                Object[] params = new Object[]{disableAction};
                String[] signature = new String[]{"it.greenvulcano.gvesb.management.DomainAction"};
                JMXUtils.invoke("*:*,Type=DomainManager", "executeDomainAction", params, signature, true, logger);
            }
            catch (Exception exc) {
                logger.error("Error occurred executing DisableAction", exc);
            }
        }
    }

    /**
     * @return the operation name
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @return the activation flag
     */
    public boolean getOperationActivation()
    {
        return opActivation;
    }

    /**
     * @param operActivation
     *        the activation flag value
     */
    public void setOperationActivation(boolean operActivation)
    {
        opActivation = operActivation;
        if (operActivation) {
            execEnableAction();
        }
        else {
            execDisableAction();
        }
    }

    /**
     * @param sActivation
     *        the activation flag value
     */
    public void setServiceActivation(boolean sActivation)
    {
        serviceActivation = sActivation;
        if (sActivation) {
            execEnableAction();
        }
        else {
            execDisableAction();
        }
    }
    
    /**
     * @param loggerLevel
     *        the master logger level value
     */
    public void setLoggerLevel(String loggerLevel) throws Exception
    {
      
        JMXUtils.set(jmxFilter, "loggerLevelj", Level.toLevel(loggerLevel), false, logger);
    }

    
    /**
     * @return true if the instance run on administrator/support server
     */
    public boolean isAdministrator()
    {
        return isAdministrator;
    }

    /**
     * @param isAdmin
     *        the flag value
     */
    public void setAdministrator(boolean isAdmin)
    {
        isAdministrator = isAdmin;
    }

    /**
     * @return the SubFlowInfo map
     */
    public Map<String, SubFlowInfo> getGVSubFlowMap()
    {
        return sfMap;
    }
    
    /**
     * @return True if the instance can call the Administration Server on
     *         objects initialization
     */
    public boolean canCallAdministratorOnInit()
    {
        return callAdministratorOnInit;
    }

    /**
     * @param call
     *        If true the instance can call the Administration Server on object
     *        initialization
     */
    public void setCallAdministratorOnInit(boolean call)
    {
        callAdministratorOnInit = call;
    }

    /**
     * Set the activation status at true for the given operation on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void on() throws Exception
    {
        JMXUtils.set(jmxFilter, "operationActivation", new Boolean(true), false, logger);
    }

    /**
     * Set the activation status at false for the given operation on every
     * server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void off() throws Exception
    {
        JMXUtils.set(jmxFilter, "operationActivation", new Boolean(false), false, logger);
    }

    /**
     * Get the history average throughput for Nodes.
     * 
     * @return the history average throughput value
     */
    public static float getHistoryThroughputNod()
    {
        return statNodes.getHistoryThroughput();
    }

    /**
     * Get the maximum throughput for Nodes.
     * 
     * @return the maximum throughput value
     */
    public static float getMaxThroughputNod()
    {
        return statNodes.getMaxThroughput();
    }

    /**
     * Get the minimum average throughput for Nodes.
     * 
     * @return minimum average throughput value
     */
    public static float getMinThroughputNod()
    {
        return statNodes.getMinThroughput();
    }

    /**
     * Get the throughput for Nodes.
     * 
     * @return throughput value
     */
    public static float getThroughputNod()
    {
        return statNodes.getThroughput();
    }

    /**
     * Get the total hints for Nodes.
     * 
     * @return total hints value
     */
    public static long getTotalHintsNod()
    {
        return statNodes.getTotalHints();
    }


}
