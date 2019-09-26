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
package it.greenvulcano.gvesb.j2ee.jndi;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 05/lug/2010
 * @author GreenVulcano Developer Team
 */
public class JNDIBuilderManager implements ConfigurationListener, ShutdownEventListener
{
    private static final Logger logger             = org.slf4j.LoggerFactory.getLogger(JNDIBuilderManager.class);

    private static String       CONFIGURATION_FILE = "GVJNDIBuildersConfig.xml";

    private Set<JNDIBuilder>    jndiBuilders       = new HashSet<JNDIBuilder>();

    /**
     *
     */
    public JNDIBuilderManager() throws Exception
    {
        init();
    }

    private void init() throws Exception
    {
        destroy();

        NodeList list = XMLConfig.getNodeList(CONFIGURATION_FILE, "/GVJNDIBuildersConfig/*[@type='jndi-builder']");

        for (int i = 0; i < list.getLength(); ++i) {
            Node n = list.item(i);
            String className = XMLConfig.get(n, "@class");
            String name = XMLConfig.get(n, "@name");
            try {
                Class<?> cls = Class.forName(className);
                JNDIBuilder builder = (JNDIBuilder) cls.getConstructor().newInstance();
                builder.init(n);
                jndiBuilders.add(builder);
                builder.build();
            }
            catch (Exception exc) {
                logger.error("Error initializing jndi-builder[" + name + "] " + className, exc);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.configuration.ConfigurationListener#configurationChanged
     * (it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    private void destroy()
    {
        for (JNDIBuilder builder : jndiBuilders) {
            try {
                builder.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        jndiBuilders.clear();
    }

}
