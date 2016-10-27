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
package it.greenvulcano.gvesb.virtual.ws.module.utils;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.virtual.ws.utils.Key2FieldClass;

import java.util.Vector;

import org.apache.axis2.client.Options;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @version 3.0.0 Mar 24, 2010
 * @author nunzio
 *
 *
 */
public class ModulePropertiesMgr
{
    /**
     * @version 3.0.0 Mar 24, 2010
     * @author nunzio
     *
     */
    protected class ModuleProperty
    {
        private Object value = null;
        private String name  = "";

        /**
         * @param configuration
         * @throws XMLConfigException
         */
        public ModuleProperty(Node configuration) throws XMLConfigException
        {
            name = Key2FieldClass.handleKey(XMLConfig.get(configuration, "@name"));
            String type = XMLConfig.get(configuration, "@type", "String");
            String sValue = XMLConfig.get(configuration, "@value");

            if (type.equals("String")) {
                value = new String(sValue);
            }
            else if (type.equals("Boolean")) {
                value = new Boolean(sValue);
            }
            else if (type.equals("Byte")) {
                value = new Byte(sValue);
            }
            else if (type.equals("Short")) {
                value = new Short(sValue);
            }
            else if (type.equals("Int")) {
                value = new Integer(sValue);
            }
            else if (type.equals("Long")) {
                value = new Long(sValue);
            }
            else if (type.equals("Float")) {
                value = new Float(sValue);
            }
            else if (type.equals("Double")) {
                value = new Double(sValue);
            }
            else {
                throw new XMLConfigException("Invalid value for attribute 'type': " + type);
            }
        }

        /**
         * @param options
         */
        public void set(Options options)
        {
            options.setProperty(name, value);
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "Name: " + name + " - value: " + value;
        }
    }

    private Vector<ModuleProperty> properties = new Vector<ModuleProperty>();

    /**
     * @param configuration
     * @throws XMLConfigException
     */
    public void init(Node configuration) throws XMLConfigException
    {
        NodeList modulePropertiesConfiguration = XMLConfig.getNodeList(configuration, "ModuleProperty");
        clearProperties();

        if ((modulePropertiesConfiguration != null) && (modulePropertiesConfiguration.getLength() > 0)) {
            for (int i = 0; i < modulePropertiesConfiguration.getLength(); i++) {
                ModuleProperty property = new ModuleProperty(modulePropertiesConfiguration.item(i));
                properties.add(property);
            }
        }
    }

    /**
     * @param options
     */
    public void set(Options options)
    {
        for (ModuleProperty property : properties) {
            property.set(options);
        }
    }

    /**
     *
     */
    public void clearProperties()
    {
        properties.clear();
    }
}
