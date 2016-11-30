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
package it.greenvulcano.gvesb.core.config;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreDisabledServiceException;
import it.greenvulcano.gvesb.core.exc.GVCoreServiceNotFoundException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongOpException;
import it.greenvulcano.gvesb.core.flow.GVFlow;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfo;
import it.greenvulcano.gvesb.core.jmx.ServiceOperationInfoManager;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>GVServiceConf</code> contains all the generic informations of a
 * configured service. It reads the informations from the GreenVulcano
 * configuration file using the current GVBuffer setting to point to the right
 * service.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVServiceConf
{
    private static final Logger   logger                 = org.slf4j.LoggerFactory.getLogger(GVServiceConf.class);

    /**
     * The GV ESB configuration file.
     */
    private String                file                   = "";
    /**
     * The key of this instance.
     */
    private String                key                    = "";
    /**
     * The name of the service requested.
     */
    private String                serviceName            = "";
    /**
     * The name of the group.
     */
    private String                groupName              = "";
    /**
     * The status of the group activation.
     */
    private boolean               groupActivation        = true;
    /**
     * The status of the service activation.
     */
    private boolean               serviceActivation      = true;
    /**
     * The JMX management object
     */
    private ServiceOperationInfo  gvServiceOperationInfo = null;
    /**
     * The name to GreenVulcano Operation map
     */
    private Map<String, GVFlow>   gvOperationMap         = null;
    /**
     * The Flows names
     */
    private Set<String>           gvOperationNames       = null;
    /**
     * The Service configuration node, used for Flows instantiation
     */
    private Node                  serviceNode            = null;
    /**
     * The alias list
     */
    private Vector<String>        aliasList              = null;
    /**
     * The alias to manage
     */
    private String                aliasName              = "";
    /**
     * The Statistics StatisticsDataManager to be used
     */
    private StatisticsDataManager statisticsDataManager  = null;
    /**
     * The status of the statistics activation.
     */
    private boolean               statisticsEnabled      = false;
   
    public GVServiceConf()
    {
        // do nothing
    }

    /**
     * Initialize the class reading the needed attributes for the current
     * service from the configuration. <br/>
     *
     * @param gvBuffer
     *        The current client buffer necessary to initialize this class
     *        (SYSTEM and SERVICE)
     * @throws GVCoreConfException
     *         The XML Configuration Exception
     * @throws GVCoreWrongInterfaceException
     *         If the gvData parameter is invalid
     * @throws GVCoreServiceNotFoundException
     *         If the given system::service isn't configured
     */
    public void init(GVBuffer gvBuffer) throws GVCoreConfException, GVCoreWrongInterfaceException,  GVCoreServiceNotFoundException
    {
        file = GreenVulcanoConfig.getServicesConfigFileName();
        serviceName = gvBuffer.getService();
        gvOperationMap = new HashMap<String, GVFlow>();
        gvOperationNames = new HashSet<String>();

        logger.debug("BEGIN - Init Service Configuration - " + serviceName);

        serviceNode = findServiceNode();
        Node groupNode = findGroupNode(serviceNode);

        groupActivation = XMLConfig.getBoolean(groupNode, "@group-activation", true);
        serviceActivation = XMLConfig.getBoolean(serviceNode, "@service-activation", true);
        statisticsEnabled = XMLConfig.getBoolean(serviceNode, "@statistics", false);
             
        checkGVOperations();

        key = serviceName;

        logger.debug("END - Init Service Configuration");
    }

    /**
     * @param svcNode
     *        the service configuration node
     * @return the group configuration node
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private Node findGroupNode(Node svcNode) throws GVCoreConfException
    {
        String xPath;
        groupName = XMLConfig.get(svcNode, "@group-name", "");
        if (groupName.equals("")) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'group-name'"},
                    {"node", XPathFinder.buildXPath(svcNode)}});
        }

        xPath = "/GVServices/Groups/Group[@id-group='" + groupName + "']";
        Node groupNode = null;
        try {
            groupNode = XMLConfig.getNode(file, xPath);
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if (groupNode == null) {
            throw new GVCoreConfException("GVCORE_GROUP_NOT_FOUND_ERROR", new String[][]{{"group", groupName}});
        }
        return groupNode;
    }

    /**
     * @return the service configuration node
     * @throws GVCoreWrongInterfaceException
     *         if errors occurs
     * @throws GVCoreServiceNotFoundException
     *         if errors occurs
     * @throws GVCoreConfException
     *         if errors occurs
     */
    private Node findServiceNode() throws GVCoreWrongInterfaceException, GVCoreServiceNotFoundException, GVCoreConfException
    {
        String xPath = "/GVServices/Services/Service[@id-service='" + serviceName + "']";
        String xPathAlias = "/GVServices/Services/Service[AliasList/Alias/@alias='" + serviceName + "']";

        boolean isAlias = false;
        Node svcNode = null;
        try {
            svcNode = XMLConfig.getNode(file, xPath);
            if (svcNode == null) {
                svcNode = XMLConfig.getNode(file, xPathAlias);
                isAlias = true;
            }
            if (svcNode == null) {
                throw new XMLConfigException("Node null : '" + xPath + "' or '" + xPathAlias);
            }
        }
        catch (XMLConfigException exc) {
            // do nothing
        }
        if (svcNode == null) {
            manageServiceNodeErrors();
        }

        if (isAlias) {
            serviceName = XMLConfig.get(svcNode, "@id-service", "");
        }

        buildAliasList(svcNode);
        return svcNode;
    }

    /**
     * @throws GVCoreWrongInterfaceException
     *         if errors occurs
     * @throws GVCoreServiceNotFoundException
     *         if errors occurs
     */
    private void manageServiceNodeErrors() throws GVCoreWrongInterfaceException, GVCoreServiceNotFoundException
    {
        if (serviceName.equals("")) {
            throw new GVCoreWrongInterfaceException("GVCORE_MALFORMED_GVBUFFER_ERROR", new String[][]{{"field",
            "Service"}});
        }
        throw new GVCoreServiceNotFoundException("GVCORE_SERVICE_NOT_FOUND_ERROR", new String[][]{{"service",
            serviceName}});
    }

    /**
     * Execute cleanup operations
     */
    public void destroy()
    {
        logger.debug("serviceConfig destroy " + serviceName + " : Start");

        for (GVFlow ebOp : gvOperationMap.values()) {
            ebOp.destroy();
        }
        gvOperationMap.clear();

        logger.debug("serviceConfig destroy " + serviceName + ": End");
    }

    /**
     * Get the name of the GreenVulcano Core Configuration File Name.
     *
     * @return The name of the GreenVulcano Configuration File
     */
    public String getConfigFileName()
    {
        return file;
    }

    /**
     * Get the name of the service called by the client.
     *
     * @return The name of the service
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * Get the name of the service group.
     *
     * @return The Group name
     */
    public String getGroupName()
    {
        return groupName;
    }

    /**
     * Get the status of the group activation.
     *
     * @return The Group activation flag
     */
    public boolean getGroupActivation()
    {
        return groupActivation;
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
     * Get the global activation status.
     *
     * @return The global activation flag
     */
    public boolean getActivation()
    {
        getServiceOperationInfo();
        if (gvServiceOperationInfo != null) {
            return gvServiceOperationInfo.getActivation();
        }
        return (serviceActivation && groupActivation);
    }

    /**
     * Get the list of Operations name
     *
     * @return The list of Operations name
     */
    public String[] getGVOperationNames()
    {
        String[] names = new String[gvOperationNames.size()];
        gvOperationNames.toArray(names);
        return names;
    }

    /**
     * Return the requested GVOperation, if exists and is active.
     *
     * @param name
     *        The required Operation name
     * @return The GVFlow instance implementing the requested Operation
     * @throws GVCoreWrongOpException
     *         If name is invalid
     * @throws GVCoreConfException
     *         If initialization error occurs
     * @throws GVCoreDisabledServiceException
     *         If the requested Operation is disabled
     */
    public GVFlow getGVOperation(GVBuffer gvBuffer, String name) throws GVCoreConfException, GVCoreWrongOpException, GVCoreDisabledServiceException
    {
        GVFlow gvOp = gvOperationMap.get(name);
        if (gvOp == null) {
            gvOp = initGVOperation(name);
        }
        if (!(getActivation() && gvOp.getActivation())) {
            throw new GVCoreDisabledServiceException("GVCORE_GVOPERATION_NOT_ACTIVE_ERROR", new String[][]{
                    {"service", serviceName}, {"operation", name}});
        }
        if (gvServiceOperationInfo != null) {
            setStatisticsEnabled(gvServiceOperationInfo.getStatisticsEnabled());
        }
        return gvOp;
    }


    /**
     * Build the service's alias list, if configured
     *
     * @param svcNode
     *        The service root node
     * @throws GVCoreConfException
     *         If configuration error occurs
     */
    private void buildAliasList(Node svcNode) throws GVCoreConfException
    {
        logger.debug("BEGIN - Build Alias List");
        aliasList = new Vector<String>();

        NodeList aliasNodeList = null;
        try {
            aliasNodeList = XMLConfig.getNodeList(svcNode, "AliasList/Alias");
        }
        catch (XMLConfigException exc) {
            return;
        }
        if ((aliasNodeList != null) && (aliasNodeList.getLength() != 0)) {
            for (int i = 0; i < aliasNodeList.getLength(); i++) {
                Node node = aliasNodeList.item(i);
                String alias = "";
                try {
                    alias = XMLConfig.get(node, "@alias");
                }
                catch (XMLConfigException exc) {
                    throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'alias'"},
                            {"node", XPathFinder.buildXPath(node)}}, exc);
                }
                aliasList.add(alias);
            }
        }
        logger.debug("END - Build Alias List");
    }

    /**
     * Get the Operation's names configured for the service
     *
     * @throws GVCoreConfException
     *         If configuration error occurs
     */
    private void checkGVOperations() throws GVCoreConfException
    {
        logger.debug("BEGIN - Check GVOperations - " + serviceName + "");

        NodeList gvOpList = null;
        try {
            gvOpList = XMLConfig.getNodeList(serviceNode, "*[@type='operation']");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_EMPTY_GVOPERATION_LIST_ERROR",
                    new String[][]{{"service", serviceName}});
        }
        if ((gvOpList == null) || (gvOpList.getLength() == 0)) {
            throw new GVCoreConfException("GVCORE_EMPTY_GVOPERATION_LIST_ERROR",
                    new String[][]{{"service", serviceName}});
        }

        for (int i = 0; i < gvOpList.getLength(); i++) {
            Node node = gvOpList.item(i);
            String gvOpName = XMLConfig.get(node, "@forward-name", "");
            if (gvOpName.equals("")) {
                try {
                    gvOpName = XMLConfig.get(node, "@name");
                }
                catch (XMLConfigException exc) {
                    throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "'name'"},
                            {"node", XPathFinder.buildXPath(node)}}, exc);
                }
            }
            gvOperationNames.add(gvOpName);
        }
        logger.debug("END - Check GVOperations");
    }

    /**
     * Create and initialize the Operation configured for the service
     *
     * @param name
     *        The Operation name
     * @return the created GVFlow
     * @throws GVCoreConfException
     *         If configuration error occurs
     * @throws GVCoreWrongOpException
     *         If name is invalid
     */
    private GVFlow initGVOperation(String name) throws GVCoreConfException, GVCoreWrongOpException
    {
    
        
       
            logger.debug("BEGIN - Init GVOperation - " + serviceName + ":" + name);
            GVFlow gvOp = null;
            Node opNode = null;
            try {
                opNode = XMLConfig.getNode(serviceNode, "*[@type='operation' and (@name='" + name + "' or @forward-name='"
                        + name + "')]");
            }
            catch (XMLConfigException exc) {
                throw new GVCoreWrongOpException("GVCORE_BAD_GVOPERATION_NAME_ERROR", new String[][]{
                        {"service", serviceName}, {"operation", name}}, exc);
            }
            if (opNode == null) {
                throw new GVCoreWrongOpException("GVCORE_BAD_GVOPERATION_NAME_ERROR", new String[][]{
                        {"service", serviceName}, {"operation", name}});
            }
            String clazz = null;
            try{
            	clazz = XMLConfig.get(opNode, "@class");
    	        gvOp = (GVFlow) Class.forName(clazz).newInstance();
    	        gvOp.init(opNode);
    	        gvOp.setStatisticsEnabled(statisticsEnabled);
    	        gvOp.setStatisticsDataManager(statisticsDataManager);
    	        gvOperationMap.put(name, gvOp);
    	        logger.debug("Set GreenVulcano Operation : " + name + " (" + gvOp.toString() + ")");
    	
    	        logger.debug("END - Init GVOperation");
            }
            catch(GVCoreConfException exc){
            	throw exc;
            }
            catch(Exception exc){
            	 throw new GVCoreWrongOpException("GVCORE_BAD_GVOPERATION_NAME_ERROR", new String[][]{
                         {"service", serviceName}, {"operation", name}, {"class", clazz}});
            }
            
            return gvOp;
        
       
    }

    /**
     * @return The Statistics StatisticsDataManager to be used
     */
    public StatisticsDataManager getStatisticsDataManager()
    {
        return statisticsDataManager;
    }

    /**
     * @param manager
     *        Set the Statistics StatisticsDataManager to be used
     */
    public void setStatisticsDataManager(StatisticsDataManager manager)
    {
        statisticsDataManager = manager;
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
     *        the flag value
     */
    public void setStatisticsEnabled(boolean statEnabled)
    {
        if (statisticsEnabled != statEnabled) {
            statisticsEnabled = statEnabled;
            for (GVFlow gvOp : gvOperationMap.values()) {
                gvOp.setStatisticsEnabled(statisticsEnabled);
            }
        }
    }
    
    
    /**
     * @return Returns the key.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return Returns the aliasList.
     */
    public Vector<String> getAliasList()
    {
        return aliasList;
    }

    /**
     * Set the GVBuffer service field to serviceName
     *
     * @param gvData
     *        The input GVBuffer
     */
    public void manageAliasInput(GVBuffer gvData)
    {
        try {
            aliasName = gvData.getService();
            gvData.setService(serviceName);
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * Set the GVBuffer service field to aliasName
     *
     * @param gvData
     *        The input GVBuffer
     */
    public void manageAliasOutput(GVBuffer gvData)
    {
        try {
            gvData.setService(aliasName);
        }
        catch (Exception exc) {
            // do nothing
        }
        finally {
            aliasName = "";
        }
    }

    /**
     * Create the associated ServiceOperationInfo instance.
     */
    private void getServiceOperationInfo()
    {
        if (gvServiceOperationInfo == null) {
            try {
                gvServiceOperationInfo = ServiceOperationInfoManager.instance().getServiceOperationInfo(serviceName,
                        true);
            }
            catch (Exception exc) {
                logger.warn("Error on MBean registration", exc);
                gvServiceOperationInfo = null;
            }
        }
    }
}
