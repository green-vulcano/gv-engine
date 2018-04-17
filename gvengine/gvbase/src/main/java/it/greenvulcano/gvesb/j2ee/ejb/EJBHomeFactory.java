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
package it.greenvulcano.gvesb.j2ee.ejb;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Questa classe realizza il pattern EJBHomeFactory tramite una cache di home
 * interfaces. Le sincronizzazioni dei thread non sono accurate, ci� � voluto in
 * quanto non si vuole introdurre ulteriori sincronizzazioni tra i thread. Nel
 * caso pi� sfortunato si possono effettuare una o due lookup in pi�, ma
 * certamente ci� non inficia il funzionamento dell'applicazione.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class EJBHomeFactory implements ConfigurationListener
{
    /**
     *
     */
    public static final String      CONFIGURATION_FILE = "ejbHomeFactory.xml";

    private static EJBHomeFactory   _instance          = null;
    private static final Logger     logger             = org.slf4j.LoggerFactory.getLogger(EJBHomeFactory.class);

    private Map<EJBHomeKey, Object> homeInterfaces     = new ConcurrentHashMap<EJBHomeKey, Object>();
    private boolean                 enabled            = false;
    private List<String>            configurationFiles = null;
    private boolean                 initialized        = false;

    private EJBHomeFactory()
    {
        initialized = false;
        init();
    }

    /**
     * @return the instance of this singleton
     */
    public static synchronized EJBHomeFactory instance()
    {
        if (_instance == null) {
            _instance = new EJBHomeFactory();
        }
        return _instance;
    }

    /**
     * @param jndiName
     * @param jndiHelper
     * @return the object from JNDI
     * @throws NamingException
     */
    public Object lookup(String jndiName, JNDIHelper jndiHelper) throws NamingException
    {
        if (!initialized) {
            init();
        }

        if (enabled) {
            EJBHomeKey key = new EJBHomeKey(jndiName, jndiHelper);
            Object homeInterface = homeInterfaces.get(key);
            if (homeInterface != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning cached EJBHome: " + jndiName + " on " + jndiHelper.getProviderURL());
                }
                return homeInterface;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Looking up EJBHome: " + jndiName + " on " + jndiHelper.getProviderURL());
            }
            homeInterface = jndiHelper.lookup(jndiName);
            homeInterfaces.put(key, homeInterface);
            return homeInterface;
        }
        else {
            return jndiHelper.lookup(jndiName);
        }
    }

    private synchronized void init()
    {
        if (initialized) {
            return;
        }

        try {
            enabled = false;
            homeInterfaces.clear();
            initialized = false;

            List<String> files;
            try {
                Node configuration = XMLConfig.getNode(CONFIGURATION_FILE, "/EJBHomeFactory");
                enabled = XMLConfig.getBoolean(configuration, "@enabled");

                files = new LinkedList<String>();
                files.add(CONFIGURATION_FILE);
                NodeList configFiles = XMLConfig.getNodeList(configuration, "configuration");
                for (int i = 0; i < configFiles.getLength(); ++i) {
                    files.add(XMLConfig.get(configFiles.item(i), "@file"));
                }
            }
            catch (XMLConfigException exc) {
                enabled = false;
                logger.warn("Cannot initialize the EJBHomeFactory", exc);
                return;
            }

            configurationFiles = files;
            logger.debug("EJBHomeFactory registered on files: " + configurationFiles);

            initialized = true;
        }
        finally {
            logger.debug(enabled ? "EJBHomeFactory enabled" : "EJBHomeFactory disabled");
        }
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent evt)
    {
        if (evt.getType().equals(ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(CONFIGURATION_FILE)) {
            initialized = false;
        }
    }
}
