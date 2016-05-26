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
package it.greenvulcano.gvesb.utils.concurrency;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVPublicException;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ConcurrencyHandler class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public final class ConcurrencyHandler implements ConfigurationListener
{
    /**
     * The class represent a specific subsystem
     */
    public class SubSystem
    {
        /**
         * The collection of services configuration for the current subsystem
         */
        private Map<String, ServiceConcurrencyInfo> serviceInfos  = null;

        /**
         * The subsystem name
         */
        private String                              subSystemName = "";

        /**
         * Constructor
         */
        public SubSystem()
        {
            serviceInfos = new HashMap<String, ServiceConcurrencyInfo>();
        }

        /**
         * @param subSystemName
         */
        public SubSystem(String subSystemName)
        {
            this.subSystemName = subSystemName;
            serviceInfos = new HashMap<String, ServiceConcurrencyInfo>();
        }

        /**
         * Initialize the current subsystem
         *
         * @param node
         *        the Node from which to read the configuration data
         * @throws XMLConfigException
         *         if error occurs
         */
        public void init(Node node) throws XMLConfigException
        {
            subSystemName = XMLConfig.get(node, "@name");
            NodeList list = XMLConfig.getNodeList(node, "ConcurrentService");

            if ((list != null) && (list.getLength() > 0)) {
                int size = list.getLength();
                for (int i = 0; i < size; i++) {
                    Node n = list.item(i);
                    String system = XMLConfig.get(n, "@system", GVBuffer.DEFAULT_SYS);
                    String service = XMLConfig.get(n, "@service");
                    ServiceConcurrencyInfo serviceInfo = new ServiceConcurrencyInfo(subSystemName,
                            XMLConfig.getInteger(n, "@max-concurrency", 10));
                    if (GVBuffer.DEFAULT_SYS.equals(system)) {
                        serviceInfos.put(service, serviceInfo);
                    }
                    else {
                        serviceInfos.put(service + "::" + system, serviceInfo);
                    }
                }
            }
        }

        /**
         * Increase the current concurrency for the given service
         *
         * @param gvBuffer
         *        the input GVBuffer instance
         * @return the associated ServiceConcurrencyInfo instance
         * @throws GVPublicException
         *         if max concurrency reached
         */
        public ServiceConcurrencyInfo add(GVBuffer gvBuffer) throws GVPublicException
        {
            ServiceConcurrencyInfo serviceInfo = serviceInfos.get(gvBuffer.getService() + "::" + gvBuffer.getSystem());
            if (serviceInfo == null) {
                serviceInfo = serviceInfos.get(gvBuffer.getService());
            }
            if (serviceInfo != null) {
                serviceInfo.add(gvBuffer);
            }
            if (serviceInfo == null) {
                serviceInfo = new ServiceConcurrencyInfo(subSystemName, 10000);
                serviceInfos.put(gvBuffer.getService() + "::" + gvBuffer.getSystem(), serviceInfo);
                serviceInfo.add(gvBuffer);
            }

            return serviceInfo;
        }

        /**
         * Decrease the current concurrency for the given service
         *
         * @param gvBuffer
         *        the input GVBuffer instance
         */
        public void remove(GVBuffer gvBuffer)
        {
            ServiceConcurrencyInfo serviceInfo = serviceInfos.get(gvBuffer.getService() + "::" + gvBuffer.getSystem());
            if (serviceInfo == null) {
                serviceInfo = serviceInfos.get(gvBuffer.getService());
            }
            if (serviceInfo != null) {
                serviceInfo.remove();
            }
        }
    }

    /**
     * The various subsystems to handle
     */
    private static Map<String, SubSystem> subSystems              = null;

    /**
     * The singleton instance
     */
    private static ConcurrencyHandler     _instance               = null;

    /**
     * If true the configuration must be reloaded
     */
    private static boolean                confChangedFlag         = true;

    /**
     * The configuration file name
     */
    private static final String           CONFIGURATION_FILE_NAME = "GVConcurrencyHandler.xml";

    /**
     * Singleton instance method
     *
     * @return the singleton instance
     * @exception XMLConfigException
     *            if initialization errors occurs
     */
    public static ConcurrencyHandler instance() throws XMLConfigException
    {
        if (_instance == null) {
            _instance = new ConcurrencyHandler();
            _instance.init();
            XMLConfig.addConfigurationListener(_instance, CONFIGURATION_FILE_NAME);
        }

        return _instance;
    }

    /**
     * Constructor
     */
    private ConcurrencyHandler()
    {
        subSystems = new HashMap<String, SubSystem>();
    }

    /**
     * Initialize the subsystems
     *
     * @throws XMLConfigException
     *         if configuration error occurs
     */
    private synchronized void init() throws XMLConfigException
    {
        if (confChangedFlag) {
            confChangedFlag = false;

            subSystems.clear();

            try {
                NodeList list = XMLConfig.getNodeList(CONFIGURATION_FILE_NAME,
                "/GVConcurrencyHandler/SubSystems/SubSystem");

                if ((list != null) && (list.getLength() > 0)) {
                    int size = list.getLength();
                    for (int i = 0; i < size; i++) {
                        Node n = list.item(i);
                        String name = XMLConfig.get(n, "@name");
                        SubSystem subSystem = new SubSystem();
                        subSystem.init(n);
                        subSystems.put(name, subSystem);
                    }
                }
            }
            catch (XMLConfigException exc) {
                subSystems.clear();
            }
        }
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
        // If the configuration file is updated all the configuration objects
        // have to be thrown away.
        //
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)
                && (event.getFile().equals(CONFIGURATION_FILE_NAME))) {
            confChangedFlag = true;
        }
    }

    /**
     * Increase the current concurrency for the given service
     *
     * @param subSystemName
     *        the subsystem name
     * @param gvBuffer
     *        the input GVBuffer instance
     * @return the associated ServiceConcurrencyInfo instance
     * @throws XMLConfigException
     *         if configuration error occurs
     * @throws GVPublicException
     *         if max concurrency reached
     */
    public ServiceConcurrencyInfo add(String subSystemName, GVBuffer gvBuffer) throws XMLConfigException,
    GVPublicException
    {
        if (confChangedFlag) {
            init();
        }
        SubSystem subSystem = subSystems.get(subSystemName);
        ServiceConcurrencyInfo serviceInfo = null;
        if (subSystem == null) {
            subSystem = new SubSystem(subSystemName);
            subSystems.put(subSystemName, subSystem);
        }
        serviceInfo = subSystem.add(gvBuffer);

        return serviceInfo;
    }

    /**
     * Decrease the current concurrency for the given service
     *
     * @param subSystemName
     *        the subsystem name
     * @param gvBuffer
     *        the input GVBuffer instance
     * @exception XMLConfigException
     *            if configuration error occurs
     */
    public void remove(String subSystemName, GVBuffer gvBuffer) throws XMLConfigException
    {
        if (confChangedFlag) {
            init();
        }
        SubSystem subSystem = subSystems.get(subSystemName);
        if (subSystem != null) {
            subSystem.remove(gvBuffer);
        }
    }

    /**
     * @return SubSytems map
     */
    public Map<String, SubSystem> getSubSystems()
    {
        return subSystems;
    }
}
