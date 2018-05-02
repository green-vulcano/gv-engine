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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.throughput.ThroughputData;
import it.greenvulcano.jmx.JMXNotificationSender;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.jmx.ModelMBeanUser;
import it.greenvulcano.util.xpath.XPathDOMBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.slf4j.Logger;
import org.w3c.dom.Document;

/**
 *
 * @version 3.2.0 Set 10, 2011
 * @author GreenVulcano Developer Team
 *
 */
public final class ServiceOperationInfoManager implements ConfigurationListener, ModelMBeanUser, JMXNotificationSender
{
    private static final Logger             logger                  = org.slf4j.LoggerFactory.getLogger(ServiceOperationInfoManager.class);

    /**
     * the object JMX descriptor
     */
    public static final String              DESCRIPTOR_NAME         = "ServiceOperationInfoManager";
    /**
     * the mbeans that publish that instance on JMX server
     */
    private Map<ObjectName, ModelMBean>     mbeans                  = new HashMap<ObjectName, ModelMBean>();
    /**
     * The Services info Map
     */
    private Map<String, ServiceOperationInfo>  serviceMap              = null;
    /**
     * If true the instance runs on Administration Server
     */
    private boolean                         isAdmin                 = false;
    /**
     * If true the instance call the administration server for objects
     * initialization
     */
    private boolean                         callAdministratorOnInit = false;
    /**
     * Singleton instance
     */
    private static ServiceOperationInfoManager _instance               = null;
    /**
     * The number indicating the order in relation of events from the source. To
     * ability the listeners to sort the notifications.
     */
    private int                             sequenceNumber          = 1;
    /**
     * the application server instance
     */
    private String                          location                = "";
    /**
     * the throughput data for service and flow node in the VCL
     */
    private ThroughputData                  throughputData          = null;
    /**
     * If true must be reload the configuration
     */
    private boolean                         confChangedFlag         = true;

    /**
     * Constructor
     */
    private ServiceOperationInfoManager()
    {
        serviceMap = new HashMap<String, ServiceOperationInfo>();
        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getServicesConfigFileName());
        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getSystemsConfigFileName());
    }

    /**
     * Singleton entry point
     *
     * @return The singleton instance reference
     */
    public static ServiceOperationInfoManager instance()
    {
        if (_instance == null) {
            _instance = new ServiceOperationInfoManager();
            _instance.init();
        }
        return _instance;
    }

    /**
     * Initialize the instance
     *
     */
    private void init()
    {
        if (confChangedFlag) {
            logger.debug("Executing ServiceOperationInfoManager.init()");
            setCallAdministratorOnInit(XMLConfig.getBoolean(GreenVulcanoConfig.getServicesConfigFileName(),
                    "/GVServices/Management/@call-administration-on-init", false));

            confChangedFlag = false;
            logger.debug("END - ServiceOperationInfoManager.init()");
        }
    }

    /**
     * Returns the requested ServiceOperationInfo instance
     *
     * @param service
     *        The Service name
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested ServiceOperationInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public synchronized ServiceOperationInfo getServiceOperationInfo(String service, boolean register)
    throws Exception
    {
        logger.debug("Executing ServiceOperationInfoManager.getServiceInfo()");
        if (confChangedFlag) {
            init();
        }
        ServiceOperationInfo serviceInfo = null;
        String key = service;

        serviceInfo = serviceMap.get(key);
        if (serviceInfo == null) {
            String groupXPath = "/GVServices/Services/Service[@id-service='" + service + "']/@group-name";
            String group = XMLConfig.get(GreenVulcanoConfig.getServicesConfigFileName(), groupXPath, "NO_GROUP");
            serviceInfo = new ServiceOperationInfo(service, group);
            serviceInfo.setAdministrator(isAdmin);
            serviceInfo.setCallAdministratorOnInit(callAdministratorOnInit);

            if (isAdmin || !callAdministratorOnInit) {
                Map<String, Object> objectData = getLocalObjectData(service, group);
                serviceInfo.init(objectData);
            }
            else {
                Map<String, Object> objectData = null;
                String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component="
                    + JMXServiceManager.getDescriptorName();
                try {
                    Map<String, Object> properties = serviceInfo.getProperties(null, false);
                    objectData = getRemoteObjectData(properties, jmxFilterLocal);
                    if (objectData == null) {
                        throw new Exception();
                    }
                }
                catch (Exception exc) {
                    logger.warn("Error occurred contacting '" + jmxFilterLocal
                            + "'. Using local configuration for initialization of service '" + key + "'.");
                    objectData = getLocalObjectData(service, group);
                }
                serviceInfo.init(objectData);
            }

            serviceInfo.register(register);
            serviceMap.put(key, serviceInfo);
        }

        logger.debug("END - ServiceOperationInfoManager.getServiceInfo()");
        return serviceInfo;
    }

    /**
     * Returns the requested ServiceOperationInfo instance
     *
     * @param serviceInfoData
     *        The object data
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested ServiceOperationInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public synchronized ServiceOperationInfo getServiceOperationInfo(Map<String, Object> serviceInfoData, boolean register)
    throws Exception
    {
        return getServiceOperationInfo((String) serviceInfoData.get("IDService"), register);
    }

    /**
     * Query the Administrator instance for the requested ServiceOperationInfo
     * initialization data
     *
     * @param properties
     *        Input ServiceOperationInfo data
     * @param jmxFilterLocal
     *        The remote JMX ServiceOperationInfo filter
     * @return The ServiceOperationInfo initialization data
     * @throws Exception
     *         If errors occurs
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getRemoteObjectData(Map<String, Object> properties, String jmxFilterLocal)
    throws Exception
    {
        logger.debug("Executing ServiceOperationInfoManager.getRemoteObjectData()");
        Map<String, Object> objectData = new HashMap<String, Object>(properties);
        Object[] params = new Object[]{objectData};
        String[] signature = new String[]{"java.util.Hashtable"};
        objectData = (Map<String, Object>) JMXUtils.invoke(jmxFilterLocal, "getServiceOperationInfoData", params,
                signature, true, logger);
        logger.debug("ServiceOperationInfoManager - Reading remote configuration data for " + objectData.get("IDService"));
        return objectData;
    }

    /**
     * Returns the configuration data of the requested ServiceOperationInfo
     * instance
     *
     * @param service
     *        The Service name
     * @param group
     *        The Group name
     * @return The requested ServiceOperationInfo instance data
     */
    private Map<String, Object> getLocalObjectData(String service, String group)
    {
        logger.debug("Executing ServiceOperationInfoManager.getLocalObjectData()");
        Map<String, Object> objectData = new HashMap<String, Object>();
        boolean activation = XMLConfig.getBoolean(GreenVulcanoConfig.getServicesConfigFileName(),
                "/GVServices/Groups/Group[@id-group='" + group + "']/@group-activation", true);
        objectData.put("groupActivation", new Boolean(activation));
        activation = XMLConfig.getBoolean(GreenVulcanoConfig.getServicesConfigFileName(),
                "/GVServices/Services/Service[@id-service='" + service + "']/@service-activation", true);
        objectData.put("serviceActivation", new Boolean(activation));
        activation = XMLConfig.getBoolean(GreenVulcanoConfig.getServicesConfigFileName(),
                "/GVServices/Services/Service[@id-service='" + service + "']/@statistics", false);
        objectData.put("statisticsEnabled", new Boolean(activation));
        logger.debug("ServiceOperationInfoManager - Reading local configuration data for " + service);
        return objectData;
    }

    /**
     * Returns the requested OperationInfo instance
     *
     * @param service
     *        The Service name
     * @param operation
     *        The Operation name
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested OperationInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public OperationInfo getOperationInfo(String service, String operation, boolean register)
    throws Exception
    {
        ServiceOperationInfo serviceInfo = getServiceOperationInfo(service, register);
        return serviceInfo.getOperationInfo(operation, register);
    }

    /**
     * Returns the requested SubFlowInfo instance
     *
     * @param service
     *        The Service name
     * @param operation
     *        The Operation name
     * @param subflow
     *        The SubFlow name
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested SubFlowInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public SubFlowInfo getSubFlowInfo(String service, String operation, String subflow, boolean register)
    throws Exception
    {
        ServiceOperationInfo serviceInfo = getServiceOperationInfo(service, register);
        OperationInfo operationInfo =  serviceInfo.getOperationInfo(operation, register);
        return operationInfo.getSubFlowInfo(subflow, register);
    }
    
    /**
     * Returns the requested OperationInfo instance
     *
     * @param operationInfoData
     *        The object data
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested OperationInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public OperationInfo getOperationInfo(Map<String, Object> operationInfoData, boolean register) throws Exception
    {
        return getOperationInfo((String) operationInfoData.get("IDService"), (String) operationInfoData.get("IDOperation"), register);
    }

    /**
     * Returns the requested SubFlowInfo instance
     *
     * @param subflowInfoData
     *        The object data
     * @param register
     *        If true register the instance as a JMX object
     * @return The requested SubFlowInfo instance
     * @throws Exception
     *         If errors occurs
     */
    public SubFlowInfo getSubFlowInfo(Map<String, Object> subflowInfoData, boolean register) throws Exception
    {
        return getSubFlowInfo((String) subflowInfoData.get("IDService"), (String) subflowInfoData.get("IDOperation"), (String) subflowInfoData.get("IDSubFlow"), register);
    }

    /**
     * Returns a XML descriptions of the configured Services:Operations
     *
     * @return A XML descriptions of the configured Services:Operations
     * @throws Exception
     *         If errors occurs
     */
    public String getServicesOperations() throws Exception
    {
        logger.debug("Executing ServiceOperationInfoManager.getServicesOperations()");
        if (confChangedFlag) {
            init();
        }
        XPathDOMBuilder xpdb = new XPathDOMBuilder();
        Document document = xpdb.createNewDocument();

        int i = 1;
        for (ServiceOperationInfo serviceInfo : serviceMap.values()) {
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]", "service",
                    serviceInfo.getService());
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]", "group",
                    serviceInfo.getGroup());
            String activation = serviceInfo.getServiceActivation() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]",
                    "serviceActivation", activation);
            activation = serviceInfo.getGroupActivation() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]",
                    "groupActivation", activation);
            activation = serviceInfo.getActivation() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]",
                    "activation", activation);
            activation = serviceInfo.getStatisticsEnabled() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]",
                    "statisticsEnabled", activation);

            Map<String, OperationInfo> operationMap = serviceInfo.getGVOperationMap();
            int j = 1;
            for (OperationInfo operationInfo : operationMap.values()) {
                xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i
                        + "]/Operation[" + j + "]", "operation", operationInfo.getOperation());
                activation = operationInfo.getOperationActivation() ? "on" : "off";
                xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i
                        + "]/Operation[" + j + "]", "operationActivation", activation);
                Map<String, SubFlowInfo> subflowMap = operationInfo.getGVSubFlowMap();
                int k = 1;
                for (SubFlowInfo subflowInfo : subflowMap.values()) {
                    xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i
                            + "]/Operation[" + j + "]/SubFlow[" + k + "]", "subflow", subflowInfo.getSubFlow());
                    k++;
                }
                j++;
            }
            i++;
        }

        logger.debug("END - ServiceOperationInfoManager.getServicesOperations()");
        return XPathDOMBuilder.printDoc(document);
    }

    /**
     * Configuration changed. When the configuration changes, the internal cache
     * is removed.
     *
     * @param event
     *        The configuration event received
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && (event.getFile().equals(GreenVulcanoConfig.getServicesConfigFileName()) || event.getFile().equals(
                        GreenVulcanoConfig.getSystemsConfigFileName()))) {
            logger.debug("ServiceOperationInfoManager - Operation (configurationChanged)");
            confChangedFlag = true;
            try {
                clearMap();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * Query the Administrator instance for all the ServiceOperationInfo instances
     * initialization data
     *
     * @throws Exception
     *         If errors occurs
     */
    public synchronized void synchronizeStatus() throws Exception
    {
        if (isAdmin) {
            return;
        }

        for (ServiceOperationInfo serviceInfo : serviceMap.values()) {
            synchronizeStatus(serviceInfo.getService());
        }
    }

    /**
     * Query the Administrator instance for the given ServiceOperationInfo
     * instances initialization data
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public synchronized void synchronizeStatus(String service) throws Exception
    {
        if (isAdmin) {
            return;
        }
        String key = service;
        ServiceOperationInfo serviceInfo = serviceMap.get(key);
        if (serviceInfo == null) {
            return;
        }
        Map<String, Object> properties = serviceInfo.getProperties(null, false);

        String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component="
            + JMXServiceManager.getDescriptorName();
        try {
            properties = getRemoteObjectData(properties, jmxFilterLocal);
        }
        catch (Exception exc) {
            logger.warn("Error occurred contacting '" + jmxFilterLocal + "'. Syncronization failed for systemService '"
                    + key + "'.");
        }
        serviceInfo.synchronizeStatus(properties);
    }

    /**
     * Reset the counters of all ServiceOperationInfo
     *
     * @throws Exception
     *         If errors occurs
     */
    public synchronized void resetCounter() throws Exception
    {
        for (ServiceOperationInfo serviceInfo : serviceMap.values()) {
            serviceInfo.resetCounter();
        }
    }

    /**
     * Reset the counters of the given ServiceOperationInfo instance
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public synchronized void resetCounter(String service) throws Exception
    {
        String key = service;
        ServiceOperationInfo serviceInfo = serviceMap.get(key);
        if (serviceInfo == null) {
            return;
        }
        serviceInfo.resetCounter();
    }

    /**
     * Clear the ServiceOperationInfo instances map
     */
    public synchronized void clearMap()
    {
        logger.debug("Executing ServiceOperationInfoManager.clearMap()");
        for (ServiceOperationInfo serviceInfo : serviceMap.values()) {
            try {
                serviceInfo.deregister(true);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        serviceMap.clear();
    }

    /**
     * @return True if the instance runs on Administration Server
     */
    public boolean isAdministrator()
    {
        return isAdmin;
    }

    /**
     * @param isAdmin
     *        If true the instance runs on Administration Server
     */
    public void setAdministrator(boolean isAdmin)
    {
        this.isAdmin = isAdmin;
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
        for (ServiceOperationInfo serviceInfo : serviceMap.values()) {
            serviceInfo.setCallAdministratorOnInit(callAdministratorOnInit);
        }
    }

    /**
     * @return Returns the descriptorName.
     */
    public static String getDescriptorName()
    {
        return DESCRIPTOR_NAME;
    }

    /**
     * Return a publishing mbean
     *
     * @param oname
     *        the object name
     * @return the requested mbean
     */
    @Override
    public ModelMBean getMBean(ObjectName oname)
    {
        return mbeans.get(oname);
    }

    /**
     * Set a publishing MBean
     *
     * @param mbean
     *        the MBean to cache
     * @param oname
     *        the object name
     */
    @Override
    public void setMBean(ModelMBean mbean, ObjectName oname)
    {
        logger.debug("Setting MBean for ObjectName: " + oname);
        mbeans.put(oname, mbean);
    }

    /**
     * This method set the sequenceNumber and send the Notification object
     *
     * @see it.greenvulcano.jmx.JMXNotificationSender#sendJMXNotification(javax.management.Notification,
     *      javax.management.ObjectName)
     */
    @Override
    public void sendJMXNotification(Notification notification, ObjectName oname) throws Exception
    {
        logger.debug("Sending Notification for ObjectName: " + oname);
        ModelMBean mbean = getMBean(oname);
        if (mbean == null) {
            throw new Exception("Invalid value for parameter 'oname': " + oname);
        }
        notification.setSequenceNumber(sequenceNumber++);
        mbean.sendNotification(notification);
        logger.debug("Notification ok");
    }

    /**
     * Get the Location value
     *
     * @return location The location value.
     */
    private String getLocation()
    {
        if (Optional.ofNullable(location).orElse("").equals("")) {
            Iterator<ObjectName> it = mbeans.keySet().iterator();
            location = it.next().getKeyProperty("Location");
        }
        return location;
    }

    /**
     * Get the Attribute value from every object on different virtual machine.
     *
     * @param attributeName
     *        The attribute name
     * @return object the buffer object containing the attribute name value
     * @exception Exception
     *            if errors occurs
     */
    private Object[] getAttributes(String attributeName) throws Exception
    {
        String jmxFilterLocal = "GreenVulcano:*,Group=management,Internal=Yes,Component=" + DESCRIPTOR_NAME;
        Object[] object = JMXUtils.get(jmxFilterLocal, attributeName, true, logger);
        return object;
    }

    /**
     * Get the history average for objects on different Virtual machine
     *
     * @return total The history throughput average for services
     */
    public float getHistoryThroughputSvc()
    {
        try {
            Object[] object = getAttributes("historyThroughputSvc_Internal");
            int size = object.length;
            float total = 0;
            for (int i = 0; i < size; i++) {
                Float f = (Float) object[i];
                if (!f.isNaN()) {
                    total += f.floatValue();
                }
            }
            return total;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the history Throughput for Services = ", exc);
        }
        return 0;
    }

    /**
     * Get the history average for objects on different Virtual machine
     *
     * @return total The history throughput average for services
     */
    public ThroughputData[] getThroughputData()
    {
        try {
            logger.debug("Called getThroughputData on node " + getLocation());
            Object[] obj = getAttributes("throughputData_Internal");
            ThroughputData[] tdata = new ThroughputData[obj.length];
            System.arraycopy(obj, 0, tdata, 0, obj.length);
            return tdata;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the Throughput data.", exc);
        }

        return null;
    }

    /**
     * Get the history average for internal object
     *
     * @return total The history throughput average for services
     */
    public float getHistoryThroughputSvc_Internal()
    {
        return ServiceOperationInfo.getHistoryThroughputSvc();
    }

    /**
     * Get the maximum total for objects on different Virtual machine
     *
     * @return total The maximum total for services
     */
    public Object[] getMaxThroughputSvc()
    {

        try {
            Object[] object = getAttributes("maxThroughputSvc_Str_Internal");
            return object;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the max Throughput for Services = ", exc);
        }

        return new Object[0];
    }

    /**
     * Get the min throughput for the single location. (This method is call on
     * every node.)
     *
     * @return The string componend by Location and the max throughput for this
     *         location.
     */
    public String getMaxThroughputSvc_Str_Internal()
    {
        return "Location=" + getLocation() + "::" + ServiceOperationInfo.getMaxThroughputSvc();
    }

    /**
     * Get the maximum total for internal object
     *
     * @return total The maximum total for services
     */
    public float getMaxThroughputSvc_Internal()
    {
        return ServiceOperationInfo.getMaxThroughputSvc();
    }

    /**
     *
     * @return throughputData ThroughputData object
     */
    public ThroughputData getThroughputData_Internal()
    {
        logger.debug("Called getThroughputData_Internal on node " + getLocation());
        try {
            if (throughputData == null) {
                throughputData = new ThroughputData();
            }
            throughputData.setLocation(getLocation());
            throughputData.setThroughputNod(OperationInfo.getThroughputNod());
            throughputData.setThroughputSvc(ServiceOperationInfo.getThroughputSvc());
            throughputData.setHistoryThroughputSvc(ServiceOperationInfo.getHistoryThroughputSvc());
            throughputData.setHistoryThroughputNod(OperationInfo.getHistoryThroughputNod());
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while calling getThroughputData_Internal", exc);
        }
        return throughputData;
    }

    /**
     * Get the minimum total for objects on different Virtual machine
     *
     * @return total The minimum total for services
     */
    public Object[] getMinThroughputSvc()
    {

        try {
            Object[] object = getAttributes("minThroughputSvc_Str_Internal");

            return object;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the max Throughput for Services = ", exc);
        }

        return new Object[0];
    }

    /**
     * Get the minimum total for internal object
     *
     * @return total The minimum total for services
     */
    public float getMinThroughputSvc_Internal()
    {
        return ServiceOperationInfo.getMinThroughputSvc();
    }

    /**
     * Get the min throughput for the single location. (This method is call on
     * every node.)
     *
     * @return The string componend by Location and the min throughput for this
     *         location.
     */
    public String getMinThroughputSvc_Str_Internal()
    {
        return "Location=" + getLocation() + "::" + ServiceOperationInfo.getMinThroughputSvc();
    }

    /**
     * Get the throughput for objects on different Virtual machine
     *
     * @return total The throughput total for services
     */
    public float getThroughputSvc()
    {
        try {
            Object[] object = getAttributes("throughputSvc_Internal");
            int size = object.length;
            float total = 0;

            for (int i = 0; i < size; i++) {
                Float f = (Float) object[i];
                if (!f.isNaN()) {
                    total += f.floatValue();
                }
            }
            return total;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the Throughput for Services = ", exc);
        }
        return 0;
    }

    /**
     * Get the throughput for internal object
     *
     * @return total The throughput total for services
     */
    public float getThroughputSvc_Internal()
    {
        return ServiceOperationInfo.getThroughputSvc();
    }

    /**
     * Get the total hints for objects on different Virtual machine
     *
     * @return total The total hint for services
     */
    public long getTotalHintsSvc()
    {
        try {
            Object[] object = getAttributes("totalHintsSvc_Internal");
            int size = object.length;
            long total = 0;

            for (int i = 0; i < size; i++) {
                Long l = (Long) object[i];
                total += l.longValue();
            }
            return total;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the Total hints for Services = ", exc);
        }
        return 0;
    }

    /**
     * Get the total hints for internal object
     *
     * @return total The total hint for services
     */
    public long getTotalHintsSvc_Internal()
    {
        return ServiceOperationInfo.getTotalHintsSvc();
    }

    /**
     * Get the history average for objects on different Virtual machine
     *
     * @return total The total history average for Nodes
     */
    public float getHistoryThroughputNod()
    {

        try {
            Object[] object = getAttributes("historyThroughputNod_Internal");
            int size = object.length;
            float total = 0;
            for (int i = 0; i < size; i++) {
                Float f = (Float) object[i];
                if (!f.isNaN()) {
                    total += f.floatValue();
                }
            }

            return total;

        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the history Throughput for Nodes = ", exc);
        }
        return 0;
    }

    /**
     * Get the total history average for objects on different Virtual machine
     *
     * @return total The total history average for Nodes
     */
    public float getHistoryThroughputNod_Internal()
    {
        return OperationInfo.getHistoryThroughputNod();
    }

    /**
     * Get the maximum for objects on different Virtual machine
     *
     * @return total The maximum for Nodes
     */
    public Object[] getMaxThroughputNod()
    {

        try {
            Object[] object = getAttributes("maxThroughputNod_Str_Internal");
            return object;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the max Throughput for Nodes = ", exc);
        }

        return new Object[0];
    }

    /**
     * Get the maximum for interanel object
     *
     * @return total The maximum for Nodes
     */
    public float getMaxThroughputNod_Internal()
    {
        return OperationInfo.getMaxThroughputNod();
    }

    /**
     * Get the min throughput for the single location. (This method is call on
     * every node.)
     *
     * @return The string componend by Location and the Max throughput for this
     *         location.
     */
    public String getMaxThroughputNod_Str_Internal()
    {
        return "Location=" + getLocation() + "::" + OperationInfo.getMaxThroughputNod();
    }

    /**
     * Get the minimum for objects on different Virtual machine (This method is
     * call only on the administration server)
     *
     * @return total The minimum for Nodes
     */
    public Object[] getMinThroughputNod()
    {
        try {
            Object[] object = getAttributes("minThroughputNod_Str_Internal");
            return object;
        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the min Throughput for Nodes = ", exc);
        }

        return new Object[0];
    }

    /**
     * Get the minimum for internal object
     *
     * @return total The minimum for Nodes
     */
    public float getMinThroughputNod_Internal()
    {
        return OperationInfo.getMinThroughputNod();
    }

    /**
     * Get the min throughput for the single location. (This method is call on
     * every node.)
     *
     * @return The string componend by Location and min throughput for this
     *         location.
     */
    public String getMinThroughputNod_Str_Internal()
    {
        return "Location=" + getLocation() + "::" + OperationInfo.getMinThroughputNod();
    }

    /**
     * Get the throughput for objects on different Virtual machine
     *
     * @return total The throughput for Nodes
     */
    public float getThroughputNod()
    {

        try {
            Object[] object = getAttributes("throughputNod_Internal");
            int size = object.length;
            float total = 0;
            for (int i = 0; i < size; i++) {
                Float f = (Float) object[i];
                if (!f.isNaN()) {
                    total += f.floatValue();
                }
            }

            return total;

        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the Throughput for Nodes = ", exc);
        }
        return 0;
    }

    /**
     * Get the throughput for internal object
     *
     * @return total The throughput for Nodes
     */
    public float getThroughputNod_Internal()
    {
        return OperationInfo.getThroughputNod();
    }

    /**
     * Get the total hints for objects on different Virtual machine
     *
     * @return total The total hints for Nodes
     */
    public long getTotalHintsNod()
    {

        try {
            Object[] object = getAttributes("totalHintsNod_Internal");
            int size = object.length;
            long total = 0;
            for (int i = 0; i < size; i++) {
                Long l = (Long) object[i];
                total += l.longValue();
            }
            return total;

        }
        catch (Exception exc) {
            logger.error("An Exception occurred while getting the total hints for Nodes = ", exc);
        }
        return 0;
    }

    /**
     * Get the total hints for internal object
     *
     * @return total The total hints for Nodes
     */
    public long getTotalHintsNod_Internal()
    {
        return OperationInfo.getTotalHintsNod();
    }
}