/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
import it.greenvulcano.management.component.StartComponentAction;
import it.greenvulcano.management.component.StopComponentAction;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.Stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ServiceOperationInfo class.
 * 
 * @version 3.2.0 Set 10, 2011
 * @author GreenVulcano Developer Team
 * 
 */
public class ServiceOperationInfo
{
    private static final Logger        logger                  = org.slf4j.LoggerFactory.getLogger(ServiceOperationInfo.class);

    /**
     * the object JMX descriptor
     */
    public static final String         DESCRIPTOR_NAME         = "ServiceOperationInfo";
    /**
     * the associated opInfo map
     */
    private Map<String, OperationInfo> opMap                   = new HashMap<String, OperationInfo>();
    /**
     * the service name
     */
    private String                     service                 = "";
    /**
     * the group name
     */
    private String                     group                   = "";
    /**
     * the service jmx key
     */
    private String                     jmxSrvcKey              = "";
    /**
     * the jmx filter for inter-instances communication
     */
    private String                     jmxFilter               = "";
    /**
     * if true the instance runs on administration/support server
     */
    private boolean                    isAdministrator         = false;
    /**
     * If true the instance call the administration server for objects
     * initialization
     */
    private boolean                    callAdministratorOnInit = false;
    /**
     * The status of the statistics activation.
     */
    private boolean                    statisticsEnabled       = false;
    /**
     * The status of the group activation.
     */
    private boolean                    groupActivation         = true;
    /**
     * The status of the service activation.
     */
    private boolean                    serviceActivation       = true;
    /**
     * The Object to calculate the GreenVulcano throughput
     */
    private static Stats               statServices            = null;
    /**
     * The Object to calculate the GreenVulcano throughput
     */
    private Stats                      statService             = null;

    /**
     * Static initializer
     */
    static {
        // perform calculation with: 1 sec, 1 sec, 30 sec
        statServices = new Stats(1000, 1000, 30);
    }

    /**
     * Constructor
     * 
     * @param srv
     *        the service name
     * @param grp
     *        the group name
     */
    public ServiceOperationInfo(String srv, String grp)
    {
        service = srv;
        group = grp;
        jmxSrvcKey = ",Group=management,Internal=Yes,IDGroup=" + group + ",IDService=" + service;
        jmxFilter = "GreenVulcano:*,Component=" + DESCRIPTOR_NAME + jmxSrvcKey;
        // perform calculation with: 1 sec, 1 sec, 30 sec
        statService = new Stats(1000, 1000, 30);
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
        setGroupActivation(((Boolean) initData.get("groupActivation")).booleanValue());
        setServiceActivation(((Boolean) initData.get("serviceActivation")).booleanValue());
        setStatisticsEnabled(((Boolean) initData.get("statisticsEnabled")).booleanValue());
    }

    /**
     * Register the instance on JMX server
     * 
     * @param register
     *        the registration flag
     * @throws Exception
     *         if errors occurs
     */
    public void register(boolean register) throws Exception
    {
        if (register) {
            String key = service + ":" + group;
            Map<String, String> properties = getJMXProperties(null);
            deregister(false);
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
     * @param clearOps
     *        if true cancel the child op
     * @throws Exception
     *         if errors occurs
     */
    public void deregister(boolean clearOps) throws Exception
    {
        Map<String, String> properties = getJMXProperties(null);
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

        for (OperationInfo operationInfo : opMap.values()) {
            try {
                operationInfo.deregister(properties);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        if (clearOps) {
            opMap.clear();
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
        properties.put("IDService", service);
        properties.put("IDGroup", group);
        if (full) {
            properties.put("groupActivation", new Boolean(groupActivation));
            properties.put("serviceActivation", new Boolean(serviceActivation));
            properties.put("statisticsEnabled", new Boolean(statisticsEnabled));
        }
        return properties;
    }

    private Map<String, String> getJMXProperties(Map<String, String> properties)
    {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        properties.put("IDService", service);
        properties.put("IDGroup", group);
        return properties;
    }

    /**
     * @return the total successful invocations
     */
    public long getTotalSuccess()
    {
        long totalSuccess = 0;

        for (OperationInfo operationInfo : opMap.values()) {
            try {
                totalSuccess += operationInfo.getTotalSuccess();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return totalSuccess;
    }

    /**
     * @return the total failed invocations
     */
    public long getTotalFailure()
    {
        long totalFailure = 0;

        for (OperationInfo operationInfo : opMap.values()) {
            try {
                totalFailure += operationInfo.getTotalFailure();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return totalFailure;
    }

    /**
     * @return the total invocations
     */
    public long getTotal()
    {
        int total = 0;

        for (OperationInfo operationInfo : opMap.values()) {
            try {
                total += operationInfo.getTotal();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return total;
    }

    /**
     * reset the internal counter
     */
    public void resetCounter()
    {
        for (OperationInfo operationInfo : opMap.values()) {
            try {
                operationInfo.resetCounter();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * Return a sting representing a matrix of Id/Thread/GVFlowNode id for the
     * associated flow status
     * 
     * @return the current associated flow status
     */
    public String getFlowsStatus()
    {
        Set<String> keySet = opMap.keySet();
        if (keySet.size() == 0) {
            return "";
        }
        String[] operations = new String[keySet.size()];
        keySet.toArray(operations);
        String flowsStatus = "";

        for (String operation : operations) {
            try {
                OperationInfo opInfo = getOperationInfo(operation, true);
                flowsStatus += operation + ": (Success = " + opInfo.getTotalSuccess() + "; Failure = "
                        + opInfo.getTotalFailure() + ")\n";
                Map<String, Map<String, String>> opFlow = opInfo.getFlowsStatus();
                for (String id : opFlow.keySet()) {
                    Map<String, String> thStatus = opFlow.get(id);
                    for (String th : thStatus.keySet()) {
                        String status = thStatus.get(th);
                        flowsStatus += id + " = [Th:" + th + "]:" + status + "\n";
                    }
                }
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return flowsStatus;
    }

    /**
     * Mark an associated flow as terminated
     * 
     * @param operation
     *        the operation name
     * @param flowId
     *        the flow id
     * @param success
     *        the termination status
     */
    public void flowTerminated(String operation, String flowId, boolean success)
    {
        try {
            statService.hint();
            statServices.hint();
            getOperationInfo(operation, true).flowTerminated(flowId, success);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * Return the required OperationInfo instance
     * 
     * @param operation
     *        the operation name
     * @param register
     *        if true register the created operation
     * @return the requested operation
     * @throws Exception
     *         if errors occurs
     */
    public synchronized OperationInfo getOperationInfo(String operation, boolean register) throws Exception
    {
        OperationInfo opInfo = opMap.get(operation);
        if (opInfo == null) {
            Map<String, Object> properties = getProperties(null, false);
            properties.put("IDOperation", operation);
            opInfo = new OperationInfo(group, service, operation, jmxSrvcKey);
            opInfo.setAdministrator(isAdministrator);
            opInfo.setCallAdministratorOnInit(callAdministratorOnInit);

            if (isAdministrator || !callAdministratorOnInit) {
                Map<String, Object> objectData = getLocalObjectData(operation);
                opInfo.init(objectData);
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
                            + "'. Using local configuration for initialization of operation '" + operation + "'.");
                    objectData = getLocalObjectData(operation);
                }
                opInfo.init(objectData);
            }

            opInfo.setServiceActivation(getActivation());
            opInfo.register(MapUtils.convertToHMStringString(properties), register);
            opMap.put(operation, opInfo);
        }
        return opInfo;
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
        objectData = (Map<String, Object>) JMXUtils.invoke(jmxFilterLocal, "getOperationInfoData", params, signature,
                true, logger);
        logger.debug("ServiceOperationInfo - Reading remote configuration data for " + objectData.get("IDSystem") + "#"
                + objectData.get("IDService") + "#" + objectData.get("IDOperation"));
        return objectData;
    }

    /**
     * Read configuration data from a local configuration file
     * 
     * @param operation
     *        the operation name
     * @return the required data
     * @throws XMLConfigException
     *         if errors occurs
     */
    private Map<String, Object> getLocalObjectData(String operation) throws XMLConfigException
    {
        Map<String, Object> objectData = new HashMap<String, Object>();
        String fileName = GreenVulcanoConfig.getServicesConfigFileName();
        Node svcNode = XMLConfig.getNode(fileName, "/GVServices/Services/Service[@id-service='" + service + "']");
        Node opNode = XMLConfig.getNode(svcNode, "Operation[(@name='" + operation + "') or (@forward-name='" + operation
                + "')]");
        boolean activation = XMLConfig.getBoolean(opNode, "@operation-activation", true);
        objectData.put("operationActivation", new Boolean(activation));
        objectData.put("loggerLevel", XMLConfig.get(opNode, "@loggerLevel", XMLConfig.get(svcNode, "@loggerLevel", XMLConfig.get(svcNode, "../@loggerLevel", "ALL"))));
        if (XMLConfig.exists(opNode, "OperationManagement/FailureAction")) {
            String component = XMLConfig.get(opNode, "OperationManagement/FailureAction/@component", "");
            int failureRateo = XMLConfig.getInteger(opNode, "OperationManagement/FailureAction/@max-failure-rate",
                    Integer.MAX_VALUE);
            StopComponentAction failureAction = new StopComponentAction("stop_component_onFailure:" + component, true);
            failureAction.setComponent(component);
            objectData.put("failureAction", failureAction);
            objectData.put("failureRateo", new Integer(failureRateo));
        }
        if (XMLConfig.exists(opNode, "OperationManagement/EnableAction")) {
            String component = XMLConfig.get(opNode, "OperationManagement/EnableAction/@component", "");
            StartComponentAction enableAction = new StartComponentAction("start_component_onEnable:" + component);
            enableAction.setComponent(component);
            objectData.put("enableAction", enableAction);
        }
        if (XMLConfig.exists(opNode, "OperationManagement/DisableAction")) {
            String component = XMLConfig.get(opNode, "OperationManagement/DisableAction/@component", "");
            StopComponentAction disableAction = new StopComponentAction("stop_component_onDisable:" + component, false);
            disableAction.setComponent(component);
            objectData.put("disableAction", disableAction);
        }
        logger.debug("ServiceOperationInfo - Reading local configuration data for " + service + "#" + operation);
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
        serviceActivation = XMLConfig.getBoolean(node, "@serviceActivation", true);
        groupActivation = XMLConfig.getBoolean(node, "@groupActivation", true);
        statisticsEnabled = XMLConfig.getBoolean(node, "@statisticsEnabled", false);
        NodeList operationList = XMLConfig.getNodeList(node, "Operation");

        int num = operationList.getLength();
        for (int i = 0; i < num; i++) {
            Node opNode = operationList.item(i);
            String operation = XMLConfig.get(node, "@operation");
            OperationInfo opInfo = getOperationInfo(operation, true);
            opInfo.synchronizeStatus(opNode);
        }
        updateOpActivation();
    }

    /**
     * @param initData
     *        the hashtable from wich read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void synchronizeStatus(Map<String, Object> initData) throws Exception
    {
        if (initData == null) {
            return;
        }
        init(initData);

        Map<String, Object> properties = new HashMap<String, Object>(initData);

        String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component="
                + JMXServiceManager.getDescriptorName();

        for (OperationInfo operationInfo : opMap.values()) {
            String operation = operationInfo.getOperation();
            properties.put("IDOperation", operation);
            try {
                properties = getRemoteObjectData(properties, jmxFilterLocal);
                operationInfo.init(properties);
            }
            catch (Exception exc) {
                logger.warn("Error occurred contacting '" + jmxFilterLocal
                        + "'. Syncronization failed for serviceOperation '" + service + ":" + operation + "'.");
            }
            operationInfo.synchronizeStatus(properties);
        }
        updateOpActivation();
    }

    /**
     * @return the service name
     */
    public String getService()
    {
        return service;
    }

    /**
     * @return the group name
     */
    public String getGroup()
    {
        return group;
    }

    /**
     * Get the status of the group activation. <br/>
     * <br/>
     * 
     * @return The Group activation flag
     */
    public boolean getGroupActivation()
    {
        return groupActivation;
    }

    /**
     * Get the history average througput for services.
     * 
     * @return the history throughput
     */
    public static float getHistoryThroughputSvc()
    {
        return statServices.getHistoryThroughput();
    }

    /**
     * Get the maximum througput for services.
     * 
     * @return the maximum througput
     */
    public static float getMaxThroughputSvc()
    {
        return statServices.getMaxThroughput();
    }

    /**
     * Get the minimum througput for services.
     * 
     * @return the minimum throughtput
     */
    public static float getMinThroughputSvc()
    {
        return statServices.getMinThroughput();
    }

    /**
     * Get the througput for services.
     * 
     * @return the throughput
     */
    public static float getThroughputSvc()
    {
        return statServices.getThroughput();
    }

    /**
     * Get the total hint for services.
     * 
     * @return the total hints
     */
    public static long getTotalHintsSvc()
    {
        return statServices.getTotalHints();
    }

    /**
     * Get the history average througput for one service.
     * 
     * @return the history throughput
     */
    public float getHistoryThroughput()
    {
        return statService.getHistoryThroughput();
    }

    /**
     * Get the maximum througput for one service.
     * 
     * @return the max throughput
     */
    public float getMaxThroughput()
    {
        return statService.getMaxThroughput();
    }

    /**
     * Get the minimum througput for one service.
     * 
     * @return the minimum throughput
     */
    public float getMinThroughput()
    {
        return statService.getMinThroughput();
    }

    /**
     * Get the throughput for one service.
     * 
     * @return the throughput
     */
    public float getThroughput()
    {
        return statService.getThroughput();
    }

    /**
     * Get the total hint for one service.
     * 
     * @return the total hints
     */
    public long getTotalHints()
    {
        return statService.getTotalHints();
    }

    /**
     * Get the status of the service activation.
     * 
     * @return The Service activation flag
     */
    public boolean getServiceActivation()
    {
        return serviceActivation;
    }

    /**
     * @return the service activation
     */
    public boolean getActivation()
    {
        return (serviceActivation && groupActivation);
    }

    private void updateOpActivation()
    {
        boolean activation = getActivation();
        for (OperationInfo operationInfo : opMap.values()) {
            operationInfo.setServiceActivation(activation);
        }
    }

    /**
     * @param gActivation
     *        the activation flag value
     */
    public void setGroupActivation(boolean gActivation)
    {
        groupActivation = gActivation;
        updateOpActivation();
    }

    /**
     * @param sActivation
     *        the activation flag value
     */
    public void setServiceActivation(boolean sActivation)
    {
        serviceActivation = sActivation;
        updateOpActivation();
    }

    /**
     * @return true if the instance run on administrator/support server
     */
    public boolean isAdministrator()
    {
        return isAdministrator;
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
        for (OperationInfo operationInfo : opMap.values()) {
            operationInfo.setCallAdministratorOnInit(callAdministratorOnInit);
        }
    }

    /**
     * @return the OperationInfo map
     */
    public Map<String, OperationInfo> getGVOperationMap()
    {
        return opMap;
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
     * Set the activation status at true for the given service on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void on() throws Exception
    {
        JMXUtils.set(jmxFilter, "serviceActivation", new Boolean(true), false, logger);
    }

    /**
     * Set the activation status at false for the given service on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void off() throws Exception
    {
        JMXUtils.set(jmxFilter, "serviceActivation", new Boolean(false), false, logger);
    }

    /**
     * Set the statistics activation status at true for the given
     * system::service on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void statisticsOn() throws Exception
    {
        JMXUtils.set(jmxFilter, "statisticsEnabled", new Boolean(true), false, logger);
    }

    /**
     * Set the statistics activation status at false for the given
     * system::service on every server
     * 
     * @throws Exception
     *         if errors occurs
     */
    public void statisticsOff() throws Exception
    {
        JMXUtils.set(jmxFilter, "statisticsEnabled", new Boolean(false), false, logger);
    }

    /**
     * @return Returns the statisticsEnabled.
     */
    public boolean getStatisticsEnabled()
    {
        return statisticsEnabled;
    }

    /**
     * @param statEnabled
     *        The statisticsEnabled to set.
     */
    public void setStatisticsEnabled(boolean statEnabled)
    {
        statisticsEnabled = statEnabled;
    }
}