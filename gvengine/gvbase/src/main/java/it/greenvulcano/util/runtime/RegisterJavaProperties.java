/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.util.runtime;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.jmx.MBeanServerInitializer;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This initializer reads from its configuration a list of <i>(name, value)</i>
 * pairs and sets, for each pair, a Java system property with the same name and
 * value, so that the newly set property is available within the whole GreenVulcano ESB
 * runtime environment.
 *
 * The value of the property to be set can be expressed in terms of existing
 * java properties and/or environment variables by putting a proper placeholder
 * into the configured property value.
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public class RegisterJavaProperties implements MBeanServerInitializer {

    private Map<String, String> customProperties;

    /**
     * Initialize the <code>MBeanServerInitializer</code>.
     *
     * @throws Exception
     *             if any error occurs.
     * @see it.greenvulcano.jmx.MBeanServerInitializer#init(org.w3c.dom.Node)
     */
    public void init(Node conf) throws Exception {
        customProperties = new HashMap<String, String>();
        NodeList propertyNodes = XMLConfig.getNodeList(conf, "PropertySetting");
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node currPropNode = propertyNodes.item(i);
            String currPropName = XMLConfig.get(currPropNode, "@name");
            String currPropValue = XMLConfig.get(currPropNode, "@value");

            // Skip properties that we'd better not to overwrite
            if (currPropName.startsWith("sun.") || currPropName.startsWith("os.") || currPropName.startsWith("user.")
                    || currPropName.startsWith("file.") || currPropName.startsWith("path.")
                    || currPropName.startsWith("line.")) {
                continue;
            }

            customProperties.put(currPropName, currPropValue);
        }
    }

    /**
     * The actual property setting is performed inside this method.
     *
     * @throws Exception
     *             if any error occurs.
     * @see it.greenvulcano.jmx.MBeanServerInitializer#initializeMBeanServer(javax.management.MBeanServer)
     */
    public void initializeMBeanServer(MBeanServer server) throws Exception {
        System.out.println("Setting custom Java properties...");
        Set<String> propNames = customProperties.keySet();
        for (String currPropName : propNames) {
            String currPropValue = customProperties.get(currPropName);
            String actualPropValue = PropertiesHandler.expand(currPropValue);
            System.setProperty(currPropName, actualPropValue);
            System.out.println(currPropName + " = " + System.getProperty(currPropName));
        }
    }

}
