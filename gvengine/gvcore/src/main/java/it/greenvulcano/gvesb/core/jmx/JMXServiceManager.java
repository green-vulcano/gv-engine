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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.throughput.ThroughputData;
import it.greenvulcano.jmx.JMXUtils;
import it.greenvulcano.util.xpath.XPathDOMBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public final class JMXServiceManager implements ConfigurationListener
{
    private static final Logger      logger          = org.slf4j.LoggerFactory.getLogger(JMXServiceManager.class);

    /**
     * the object JMX descriptor
     */
    public static final String       DESCRIPTOR_NAME = "JMXServiceManager";
    /**
     * The Services info Map
     */
    private Map<String, ServiceInfo> mapSvc      = null;
    /**
     * The Systems info Map
     */
    private Map<String, SystemInfo>  mapSys       = null;
    /**
     * The Groups info Map
     */
    private Map<String, GroupInfo>   groupMap        = null;
    /**
     * Singleton instance
     */
    private static JMXServiceManager _instance       = null;
    /**
     * If true must be reload the configuration
     */
    private boolean                  confChangedFlag = true;

    /**
     * Constructor
     */
    private JMXServiceManager()
    {
        mapSvc = new TreeMap<String, ServiceInfo>();
        mapSys = new TreeMap<String, SystemInfo>();
        groupMap = new TreeMap<String, GroupInfo>();

        ServiceOperationInfoManager.instance().setAdministrator(true);

        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getServicesConfigFileName());
        XMLConfig.addConfigurationListener(this, GreenVulcanoConfig.getSystemsConfigFileName());

        logger.debug("GreenVulcano JMX Service Manager created");
    }

    /**
     * Singleton entry point
     *
     * @return The singleton instance reference
     * @throws Exception
     *         If initialization errors occurs
     */
    public static JMXServiceManager instance() throws Exception
    {
        if (_instance == null) {
            _instance = new JMXServiceManager();
            _instance.init();
        }
        return _instance;
    }

    /**
     * Initialize the instance
     *
     * @throws Exception
     *         If initialization errors occurs
     */
    private synchronized void init() throws Exception
    {
        if (confChangedFlag) {
            logger.debug("Executing JMXServiceManager.init()");
            initGroupList();
            initSystemList();
            initServicesList();
            initServiceOperationList();

            confChangedFlag = false;
            logger.debug("END - JMXServiceManager.init()");
        }
    }

    /**
     * @throws Exception
     *         If errors occurs
     */
    protected void clearMap() throws Exception
    {
        Object[] params = new Object[0];
        String[] signature = new String[0];
        JMXUtils.invoke("GreenVulcano:*,Group=management,Internal=Yes,Component=ServiceOperationInfoManager", "clearMap",
                params, signature, true, logger);
    }

    /**
     * Query the ServiceOperationInfoManager instances to synchronize data for all
     * ServiceOperationInfo
     *
     * @throws Exception
     *         If errors occurs
     */
    public void synchronizeStatus() throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        Object[] params = new Object[0];
        String[] signature = new String[0];
        JMXUtils.invoke("GreenVulcano:*,Group=management,Internal=Yes,Component=ServiceOperationInfoManager",
                "synchronizeStatus", params, signature, true, logger);
    }

    /**
     * Query the ServiceOperationInfoManager instances to synchronize data for the
     * given ServiceOperationInfo
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void synchronizeStatus(String service) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        Object[] params = new Object[]{service};
        String[] signature = new String[]{"java.lang.String"};
        JMXUtils.invoke("GreenVulcano:*,Group=management,Internal=Yes,Component=ServiceOperationInfoManager",
                "synchronizeStatus", params, signature, true, logger);
    }

    /**
     * Query the ServiceOperationInfoManager instances to reset the counters of all
     * ServiceOperationInfo
     *
     * @throws Exception
     *         If errors occurs
     */
    public void resetCounter() throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        Object[] params = new Object[0];
        String[] signature = new String[0];
        JMXUtils.invoke("GreenVulcano:*,Group=management,Internal=Yes,Component=ServiceOperationInfoManager",
                "resetCounter", params, signature, false, logger);
    }

    /**
     * Query the ServiceOperationInfoManager instances to reset the counters of the
     * given ServiceOperationInfo
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void resetCounter(String service) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        Object[] params = new Object[]{service};
        String[] signature = new String[]{"java.lang.String"};
        JMXUtils.invoke("GreenVulcano:*,Group=management,Internal=Yes,Component=ServiceOperationInfoManager",
                "resetCounter", params, signature, false, logger);
    }

    /**
     * Returns the configuration data of the requested ServiceOperationInfo
     * instance
     *
     * @param serviceInfoData
     *        The requested ServiceOperationInfo key
     * @return The requested ServiceOperationInfo instance data
     * @throws Exception
     *         If initialization errors occurs
     */
    public Map<String, Object> getServiceOperationInfoData(Map<String, Object> serviceInfoData) throws Exception
    {
        logger.debug("BEGIN -  JMXServiceManager.getServiceOperationInfoData()");
        ServiceOperationInfo serviceInfo = null;

        if (confChangedFlag) {
            init();
        }

        serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(serviceInfoData, true);

        serviceInfoData = serviceInfo.getProperties(serviceInfoData, true);

        logger.debug("END - JMXServiceManager.getServiceOperationInfoData()");
        return serviceInfoData;
    }

    /**
     * Returns the configuration data of the requested OperationInfo instance
     *
     * @param operationInfoData
     *        The requested OperationInfo key
     * @return The requested OperationInfo instance data
     * @throws Exception
     *         If initialization errors occurs
     */
    public Map<String, Object> getOperationInfoData(Map<String, Object> operationInfoData) throws Exception
    {
        logger.debug("BEGIN - JMXServiceManager.getOperationInfoData()");
        OperationInfo operationInfo = null;

        if (confChangedFlag) {
            init();
        }

        operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(operationInfoData, true);

        operationInfoData = operationInfo.getProperties(operationInfoData, true);

        logger.debug("END - JMXServiceManager.getOperationInfoData()");
        return operationInfoData;
    }
    
    /**
     * Returns the configuration data of the requested SubFlowInfo instance
     *
     * @param subflowInfoData
     *        The requested SubFlowInfo key
     * @return The requested SubFlowInfo instance data
     * @throws Exception
     *         If initialization errors occurs
     */
    public Map<String, Object> getSubFlowInfoData(Map<String, Object> subflowInfoData) throws Exception
    {
        logger.debug("BEGIN - JMXServiceManager.getSubFlowInfoData()");
        SubFlowInfo subflowInfo = null;

        if (confChangedFlag) {
            init();
        }

        subflowInfo = ServiceOperationInfoManager.instance().getSubFlowInfo(subflowInfoData, true);

        subflowInfoData = subflowInfo.getProperties(subflowInfoData, true);

        logger.debug("END - JMXServiceManager.getSubFlowInfoData()");
        return subflowInfoData;
    }

    /**
     * Returns a XML descriptions of the configured Groups
     *
     * @return A XML descriptions of the configured Groups
     * @throws Exception
     *         If errors occurs
     */
    public String getGroups() throws Exception
    {
        logger.debug("BEGIN - JMXServiceManager.getGroups()");
        if (confChangedFlag) {
            init();
        }
        XPathDOMBuilder xpdb = new XPathDOMBuilder();
        Document document = xpdb.createNewDocument();

        int i = 1;
        for (GroupInfo groupInfo : groupMap.values()) {
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Groups[1]/Group[" + i + "]", "group",
                    groupInfo.getName());
            String activation = groupInfo.getIsActive() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Groups[1]/Group[" + i + "]", "groupActivation",
                    activation);
            i++;
        }
        logger.debug("END - JMXServiceManager.getGroups()");
        return XPathDOMBuilder.printDoc(document);
    }

    /**
     * Returns a XML descriptions of the configured Systems
     *
     * @return A XML descriptions of the configured Systems
     * @throws Exception
     *         If errors occurs
     */
    public String getSystems() throws Exception
    {
        logger.debug("BEGIN - JMXServiceManager.getSystems()");
        if (confChangedFlag) {
            init();
        }
        XPathDOMBuilder xpdb = new XPathDOMBuilder();
        Document document = xpdb.createNewDocument();

        int i = 1;
        for (SystemInfo systemInfo : mapSys.values()) {
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Systems[1]/System[" + i + "]", "system",
                    systemInfo.getName());
            String activation = systemInfo.getActivation() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Systems[1]/System[" + i + "]", "systemActivation",
                    activation);
            i++;
        }
        logger.debug("END - JMXServiceManager.getSystems()");
        return XPathDOMBuilder.printDoc(document);
    }

    /**
     * Returns a XML descriptions of the configured Services
     *
     * @return A XML descriptions of the configured Services
     * @throws Exception
     *         If errors occurs
     */
    public String getServices() throws Exception
    {
        logger.debug("BEGIN - JMXServiceManager.getServices()");
        if (confChangedFlag) {
            init();
        }
        XPathDOMBuilder xpdb = new XPathDOMBuilder();
        Document document = xpdb.createNewDocument();

        int i = 1;
        for (ServiceInfo serviceInfo : mapSvc.values()) {
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]", "service",
                    serviceInfo.getName());
            String activation = serviceInfo.getActivation() ? "on" : "off";
            xpdb.addAttribute(document, "/GreenVulcanoStatus[1]/Services[1]/Service[" + i + "]", "serviceActivation",
                    activation);
            i++;
        }

        logger.debug("END - JMXServiceManager.getServices()");
        return XPathDOMBuilder.printDoc(document);
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
        if (confChangedFlag) {
            init();
        }
        return ServiceOperationInfoManager.instance().getServicesOperations();
    }

    /**
     * Set the activation status for the given Group 'on'
     *
     * @param name
     *        The Group name
     * @throws Exception
     *         If errors occurs
     */
    public void groupOn(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        GroupInfo groupInfo = groupMap.get(name);
        groupInfo.setIsActive(true);
    }

    /**
     * Set the activation status for the given Group 'off'
     *
     * @param name
     *        The Group name
     * @throws Exception
     *         If errors occurs
     */
    public void groupOff(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        GroupInfo groupInfo = groupMap.get(name);
        groupInfo.setIsActive(false);
    }

    /**
     * Set the activation status for the given System 'on'
     *
     * @param name
     *        The System name
     * @throws Exception
     *         If errors occurs
     */
    public void systemOn(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        SystemInfo systemInfo = mapSys.get(name);
        systemInfo.setActivation(true);
    }

    /**
     * Set the activation status for the given System 'off'
     *
     * @param name
     *        The System name
     * @throws Exception
     *         If errors occurs
     */
    public void systemOff(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        SystemInfo systemInfo = mapSys.get(name);
        systemInfo.setActivation(false);
    }

    /**
     * Set the activation status for the given Service 'on'
     *
     * @param name
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void serviceOn(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        ServiceInfo serviceInfo = mapSvc.get(name);
        serviceInfo.setActivation(true);
    }

    /**
     * Set the activation status for the given Service 'off'
     *
     * @param name
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void serviceOff(String name) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        ServiceInfo serviceInfo = mapSvc.get(name);
        serviceInfo.setActivation(false);
    }

    /**
     * Set the activation status for the given Service:Operation 'on'
     *
     * @param service
     *        The Service name
     * @param operation
     *        The Operation name
     * @throws Exception
     *         If errors occurs
     */
    public void serviceOperationOn(String service, String operation) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        OperationInfo operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(service, operation, true);
        operationInfo.on();
    }

    /**
     * Set the activation status for the given Service:Operation 'on'
     *
     * @param service
     *        The Service name
     * @param operation
     *        The Operation name
     * @throws Exception
     *         If errors occurs
     */
    public void serviceOperationOff(String service, String operation) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        OperationInfo operationInfo = ServiceOperationInfoManager.instance().getOperationInfo(service, operation, true);
        operationInfo.off();
    }

    /**
     * Set the statistics activation status for the given Service 'on'
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void statisticsOn(String service) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        ServiceOperationInfo serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(service, true);
        serviceInfo.statisticsOn();
    }

    /**
     * Set the statistics activation status for the given Service 'off'
     *
     * @param service
     *        The Service name
     * @throws Exception
     *         If errors occurs
     */
    public void statisticsOff(String service) throws Exception
    {
        if (confChangedFlag) {
            init();
        }
        ServiceOperationInfo serviceInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(service, true);
        serviceInfo.statisticsOff();
    }

    /**
     * Initialize the Groups list
     */
    private void initGroupList()
    {
        logger.debug("Executing JMXServiceManager.initGroupList()");
        try {
            groupMap.clear();
            NodeList groups = XMLConfig.getNodeList(GreenVulcanoConfig.getServicesConfigFileName(),
            "/GVServices/Groups/Group");

            int num = groups.getLength();
            for (int i = 0; i < num; i++) {
                String name = XMLConfig.get(groups.item(i), "@id-group");
                boolean activation = XMLConfig.getBoolean(groups.item(i), "@group-activation", true);
                groupMap.put(name, new GroupInfo(name, activation));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Unable to initialize Groups list.", exc);
            groupMap.clear();
        }
        logger.debug("END - JMXServiceManager.initGroupList()");
    }

    /**
     * Initialize the Systems list
     */
    private void initSystemList()
    {
        logger.debug("BEGIN - JMXServiceManager.initSystemList()");
        try {
            mapSys.clear();
            NodeList systems = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(),
            "/GVSystems/Systems/System");

            int num = systems.getLength();
            for (int i = 0; i < num; i++) {
                String name = XMLConfig.get(systems.item(i), "@id-system");
                boolean activation = XMLConfig.getBoolean(systems.item(i), "@system-activation", true);
                mapSys.put(name, new SystemInfo(name, activation));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Unable to initialize Systems list.", exc);
            mapSys.clear();
        }
        logger.debug("END - JMXServiceManager.initSystemList()");
    }

    /**
     * Initialize the Services list
     */
    private void initServicesList()
    {
        logger.debug("BEGIN - JMXServiceManager.initServicesList()");
        try {
            mapSvc.clear();
            NodeList services = XMLConfig.getNodeList(GreenVulcanoConfig.getServicesConfigFileName(),
            "/GVServices/Services/Service");

            int num = services.getLength();
            for (int i = 0; i < num; i++) {
                String name = XMLConfig.get(services.item(i), "@id-service");
                boolean activation = XMLConfig.getBoolean(services.item(i), "@service-activation", true);
                String loggerLevel = XMLConfig.get(services.item(i), "@loggerLevel", XMLConfig.get(services.item(i), "../@loggerLevel", "ALL"));
                mapSvc.put(name, new ServiceInfo(name, activation, loggerLevel));
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Unable to initialize Services list.", exc);
            mapSvc.clear();
        }
        logger.debug("END - JMXServiceManager.initServicesList()");
    }

    /**
     * Initialize the Service:Operation list
     */
    private void initServiceOperationList()
    {
        logger.debug("BEGIN - JMXServiceManager.initServiceOperationList()");
        try {
            NodeList services = XMLConfig.getNodeList(GreenVulcanoConfig.getServicesConfigFileName(),
            "/GVServices/Services/Service");

            int numServ = services.getLength();
            for (int i = 0; i < numServ; i++) {
                String service = XMLConfig.get(services.item(i), "@id-service");
                initOperationList(service, services.item(i));
            }
        }
        catch (Exception exc) {
            logger.error("Unable to initialize Service/Operation list.", exc);
        }
        logger.debug("END - JMXServiceManager.initServiceOperationList()");
    }

    /**
     * @param service
     *        the service name
     * @param svcNode
     *        the service node
     * @throws Exception
     *         if errors occurs
     */
    private void initOperationList(String service, Node svcNode) throws Exception
    {
        NodeList operations = XMLConfig.getNodeList(svcNode, "Operation");
        int numOp = operations.getLength();
        for (int x = 0; x < numOp; x++) {
            String operation = XMLConfig.get(operations.item(x), "@name");
            if (operation.equals("Forward")) {
                operation = XMLConfig.get(operations.item(x), "@forward-name");
            }
            //ServiceOperationInfoManager.instance().getOperationInfo(service, operation, true);
            initSubFlowList(service, operation, operations.item(x));
        }
    }
    
    /**
     * @param service
     *        the service name
     * @param opNode
     *        the operation node
     * @throws Exception
     *         if errors occurs
     */
    private void initSubFlowList(String service, String operation, Node opNode) throws Exception
    {
        NodeList subflows = XMLConfig.getNodeList(opNode, "SubFlow");
        int numSf = subflows.getLength();
        for (int x = 0; x < numSf; x++) {
            String subflow = XMLConfig.get(subflows.item(x), "@name");
            ServiceOperationInfoManager.instance().getSubFlowInfo(service, operation, subflow, true);
        }
    }

    /**
     * Configuration changed. When the configuration changes, the internal
     * cache is removed.
     *
     * @param event
     *        The configuration event received
     */
    @Override
    public void configurationChanged(ConfigurationEvent event)
    {
        logger.debug("BEGIN - Operation (configurationChanged)");

        if ((event.getCode() == ConfigurationEvent.EVT_FILE_LOADED)
                && (event.getFile().equals(GreenVulcanoConfig.getServicesConfigFileName()) || event.getFile().equals(
                        GreenVulcanoConfig.getSystemsConfigFileName()))) {
            logger.debug("JMXServiceManager - Operation (configurationChanged)");
            confChangedFlag = true;
            /*try {
                clearMap();
            }
            catch (Exception exc) {
                // do nothing
            }*/
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
     * Get the History Throughput
     *
     * @return the History Throughput
     */
    public Map<String, ThroughputData> getThroughputData()
    {
        ThroughputData[] data = ServiceOperationInfoManager.instance().getThroughputData();
        Map<String, ThroughputData> tMap = null;

        if (data != null) {
            float tNodAll = 0;
            float tSvcAll = 0;
            float historyTNodAll = 0;
            float historyTSvcAll = 0;
            ThroughputData troughputGlobal = new ThroughputData();
            troughputGlobal.setLocation("GLOBAL");
            tMap = new HashMap<String, ThroughputData>();

            for (ThroughputData element : data) {
                logger.debug("Inserting ThroughputData " + element);
                tMap.put(element.getLocation(), element);
                tNodAll += element.getThroughputNod();
                tSvcAll += element.getThroughputSvc();
                historyTNodAll += element.getHistoryThroughputNod();
                historyTSvcAll += element.getHistoryThroughputSvc();
            }

            troughputGlobal.setThroughputNod(tNodAll);
            troughputGlobal.setThroughputSvc(tSvcAll);
            troughputGlobal.setHistoryThroughputNod(historyTNodAll);
            troughputGlobal.setHistoryThroughputSvc(historyTSvcAll);
            tMap.put(troughputGlobal.getLocation(), troughputGlobal);
            logger.debug("Inserting ThroughputData " + troughputGlobal);
        }

        return tMap;
    }

    /**
     * Get the History Throughput
     *
     * @return the History Throughput
     */
    public float getHistoryThroughputSvc()
    {
        return ServiceOperationInfoManager.instance().getHistoryThroughputSvc();
    }

    /**
     * Get an Array String containing the Location and the Maximum throughput
     * value.
     *
     * @return Max Throughput
     */
    public Object[] getMaxThroughputSvc()
    {
        return ServiceOperationInfoManager.instance().getMaxThroughputSvc();
    }

    /**
     * Get an Array String containing the Location and the Minimum throughput
     * value.
     *
     * @return the requested values
     */
    public Object[] getMinThroughputSvc()
    {
        return ServiceOperationInfoManager.instance().getMinThroughputSvc();
    }

    /**
     * Get throughput value for services.
     *
     * @return the requested value
     */
    public float getThroughputSvc()
    {
        return ServiceOperationInfoManager.instance().getThroughputSvc();
    }

    /**
     * Get Total hints executed.
     *
     * @return The total hints
     */
    public long getTotalHintsSvc()
    {
        return ServiceOperationInfoManager.instance().getTotalHintsSvc();
    }

    /**
     * Get the history throughput value.
     *
     * @return history throughput
     */
    public float getHistoryThroughputNod()
    {
        return ServiceOperationInfoManager.instance().getHistoryThroughputNod();
    }

    /**
     * Get an Array String containing the Location and the Maximum throughput
     * value.
     *
     * @return the maximum throughput value
     */
    public Object[] getMaxThroughputNod()
    {
        return ServiceOperationInfoManager.instance().getMaxThroughputNod();
    }

    /**
     * Get an Array String containing the Location and the Minimum throughput
     * value.
     *
     * @return the minimum throughput
     */
    public Object[] getMinThroughputNod()
    {
        return ServiceOperationInfoManager.instance().getMinThroughputNod();
    }

    /**
     * Get throughput value for nodes.
     *
     * @return throughput value for nodes
     */
    public float getThroughputNod()
    {
        return ServiceOperationInfoManager.instance().getThroughputNod();
    }

    /**
     * Get the total hints for nodes.
     *
     * @return total hints
     */
    public long getTotalHintsNod()
    {
        return ServiceOperationInfoManager.instance().getTotalHintsNod();
    }
}