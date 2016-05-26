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
package it.greenvulcano.util.xpath.jmx;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.xpath.search.XPathAPI;

import javax.management.MBeanServer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * RegisterXPathReloader class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 **/
public class RegisterXPathReloader implements MBeanServerInitializer, ConfigurationListener
{
    /**
     *
     */
    public static final String XPATH_CONF = "gv-xpath.xml";

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     *
     * @param conf
     *        the configuration node
     * @throws Exception
     */
    public final void init(Node conf) throws Exception
    {
        // do nothing
    }

    /**
     * Initialize the given <code>MBeanServer</code>.
     *
     * @param server
     *        the MBean Server instance
     * @throws Exception
     *         if error occurs
     */
    public final void initializeMBeanServer(MBeanServer server) throws Exception
    {
        XMLConfig.addConfigurationListener(this, XPATH_CONF);
        //init();
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && evt.getFile().equals(XPATH_CONF)) {
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    init();
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for XPath Framework");
            bt.setDaemon(true);
            bt.start();
        }
    }

    /**
     * 
     */
    private void init() {
        try {
            NodeList xpathNamespaces = XMLConfig.getNodeList(XPATH_CONF, "//XPathNamespace");
            for (int i = 0; i < xpathNamespaces.getLength(); ++i) {
                Node node = xpathNamespaces.item(i);
                String prefix = XMLConfig.get(node, "@prefix");
                String namespace = XMLConfig.get(node, "@namespace");
                if ((prefix == null) || "".equals(prefix)) {
                    System.out.println("### XPath namespace NOT re-installed... prefix empty: " + prefix + " -> " + namespace);
                    continue;
                }
                if (namespace == null) {
                    namespace = "";
                }
                try {
                    XPathAPI.installNamespace(prefix, namespace);
                    System.out.println("### XPath namespace re-installed...: " + prefix + " -> " + namespace);
                }
                catch (Exception e) {
                    System.out.println("ERROR: cannot re-install namespace: " + prefix + "->" + namespace);
                    e.printStackTrace();
                }
            }
        }
        catch (Exception exc) {
            System.out.println("Error reloading XPath namespaces definitions: " + exc);
            exc.printStackTrace();
        }
    }
}
