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
package it.greenvulcano.gvesb.log.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Hashtable;

import javax.management.MBeanServer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RegisterGVBufferDump implements MBeanServerInitializer
{
    private Hashtable<String, String> properties = new Hashtable<String, String>();
    private String                    descriptorName;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     * 
     * @param conf
     *        the initialization node
     * @throws Exception
     *         if error occurs
     */
    @Override
    public final void init(Node conf) throws Exception
    {
        try {
            NodeList list = XMLConfig.getNodeList(conf, "property");
            for (int i = 0; i < list.getLength(); ++i) {
                Node node = list.item(i);
                String key = XMLConfig.get(node, "@name", "undef");
                String value = XMLConfig.get(node, "@value", "undef");
                properties.put(key, PropertiesHandler.expand(value));
            }
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        descriptorName = XMLConfig.get(conf, "descriptor/@name", "GVBufferDump");
    }

    /**
     * Register the MBean to the given MBeanServer.
     * 
     * @param server
     *        the server on which register the MBean
     * @throws Exception
     *         if error occurs
     */
    @Override
    public final void initializeMBeanServer(MBeanServer server) throws Exception
    {
        JMXEntryPoint jmx = JMXEntryPoint.instance();
        NMDC.push();
        try {
            NMDC.setServer(JMXEntryPoint.getServerName());

            GVBufferDumpProxy gvDatadump = new GVBufferDumpProxy();

            try {
                jmx.unregisterObject(gvDatadump, descriptorName, properties);
            }
            catch (Exception exc) {
                // do nothing
            }

            try {
                jmx.unregisterObject(gvDatadump, descriptorName + "_Internal", properties);
            }
            catch (Exception exc) {
                // do nothing
            }

            jmx.registerObject(gvDatadump, descriptorName, properties);

            jmx.registerObject(gvDatadump, descriptorName + "_Internal", properties);
        }
        finally {
            NMDC.pop();
        }
    }
}
