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
package it.greenvulcano.management;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.management.component.ComponentData;

import org.w3c.dom.Node;

/**
 * DomainManager class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public final class DomainManager implements DomainManagerInt, ConfigurationListener
{
    /**
     * Configuration file name.
     */
    public static final String   CFG_FILE        = "GVDomainManager.xml";
    /**
     * Component descriptor name.
     */
    public static final String   DESCRIPTOR_NAME = "DomainManager";
    /**
     * Singleton reference.
     */
    private static DomainManager instance       = null;
    /**
     * Real domain manager.
     */
    private DomainManagerInt     manager         = null;
    /**
     * If true the configuration is changed.
     */
    @SuppressWarnings("unused")
    private boolean              confChangedFlag = false;

    /**
     * Constructor.
     */
    private DomainManager()
    {
        // do nothing
    }

    /**
     * Singleton entry point.
     *
     * @return the instance reference
     * @throws DomainManagerException
     *         if error occurs
     */
    public static synchronized DomainManager instance() throws DomainManagerException
    {
        if (instance == null) {
            instance = new DomainManager();
            instance.init();
            XMLConfig.addConfigurationListener(instance, CFG_FILE);
        }
        return instance;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE)) {
            confChangedFlag = true;
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
     * Execute the given domain action.
     *
     * @param action
     *        the action to execute
     * @throws DomainManagerException
     *         if error occurs
     */
    public void executeDomainAction(DomainAction action) throws DomainManagerException
    {
        manager.executeDomainAction(action);
    }

    /**
     * Start the given component.
     *
     * @param name
     *        the name of component to start
     * @throws DomainManagerException
     *         if error occurs
     */
    public void startComponent(String name) throws DomainManagerException
    {
        manager.startComponent(name);
    }

    /**
     * Stop the given component.
     *
     * @param name
     *        the name of component to stop
     * @param autoEnable
     *        if true the component can be started automatically
     * @throws DomainManagerException
     *         if error occurs
     */
    public void stopComponent(String name, boolean autoEnable) throws DomainManagerException
    {
        manager.stopComponent(name, autoEnable);
    }

    /**
     * @return the list of managed domain components
     * @throws DomainManagerException
     *         if error occurs
     */
    public ComponentData[] getComponentList() throws DomainManagerException
    {
        return manager.getComponentList();
    }

    /**
     * Initialize the instance.
     *
     * @param node
     *        the configuration node
     * @throws DomainManagerException
     *         if error occurs
     */
    public void init(Node node) throws DomainManagerException
    {
        // do nothing
    }

    /**
     * Perform cleanup operation.
     */
    public void destroy()
    {
        manager.destroy();
    }

    /**
     * Initialize the instance.
     *
     * @throws DomainManagerException
     *         if error occurs
     */
    private void init() throws DomainManagerException
    {
        try {
            Node node = XMLConfig.getNode(CFG_FILE, "/DomainManager/*[@type='domain-manager']");
            String className = XMLConfig.get(node, "@class");
            manager = (DomainManagerInt) Class.forName(className).newInstance();
            manager.init(node);
        }
        catch (DomainManagerException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new DomainManagerException("Error initializing DomainManager: " + exc, exc);
        }
    }
}
