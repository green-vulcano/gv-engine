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
package it.greenvulcano.gvesb.policy;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 02/feb/2012
 * @author GreenVulcano Developer Team
 */
public class ACLManager implements ConfigurationListener
{
    public static final String CFG_FILE_NAME = "GVPolicy.xml";
    private static Logger      logger        = LoggerFactory.getLogger(ACLManager.class);
    private static ACLManager  instance      = null;
    private static boolean     configChanged = false;
    private ACL                acl           = null;

    public static boolean canAccess(ResourceKey key) throws ACLException
    {
        ACL locACL = ACLManager.instance().getACL();
        if (locACL != null) {
            return locACL.canAccess(key);
        }
        return false;
    }

    private ACLManager() throws ACLException
    {
        init();
    }

    private static ACLManager instance() throws ACLException
    {
        if ((instance != null) && configChanged) {
            synchronized (ACLManager.class) {
                if (configChanged) {
                    instance.destroy();
                    instance = null;
                }
                configChanged = false;
            }
        }
        if (instance == null) {
            synchronized (ACLManager.class) {
                if (instance == null) {
                    instance = new ACLManager();
                    XMLConfig.addConfigurationListener(instance, CFG_FILE_NAME);
                }
            }
        }

        return instance;
    }

    private ACL getACL()
    {
        return acl;
    }

    private void init() throws ACLException
    {
        try {
            Node node = XMLConfig.getNode(CFG_FILE_NAME, "/GVPolicy/*[@type='acl-manager']");
            String clazz = XMLConfig.get(node, "@class");
            acl = (ACL) Class.forName(clazz).getConstructor().newInstance();
            acl.init(node);
        }
        catch (Exception exc) {
            logger.error("Error initializing ACLManager", exc);
            throw new ACLException("Error initializing ACLManager", new String[][]{{"exc", "" + exc}});
        }
    }

    private void destroy()
    {
        if (instance != null) {
            XMLConfig.removeConfigurationListener(instance);
        }
        instance = null;
        if (acl != null) {
            acl.destroy();
        }
        acl = null;
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && (evt.getFile().equals(CFG_FILE_NAME))) {
            //destroy();
            configChanged = true;
        }
    }
}
