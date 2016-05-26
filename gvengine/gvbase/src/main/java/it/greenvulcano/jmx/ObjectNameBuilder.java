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
package it.greenvulcano.jmx;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ObjectNameBuilder class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ObjectNameBuilder
{
    /**
     * Apply only to current class
     */
    private static final int            ONLY_CURRENT              = 0;
    /**
     * Apply only to current class or descendant
     */
    private static final int            CURRENT_OR_DESCENDANT     = 1;
    /**
     * Apply only to current class descendant
     */
    private static final int            ONLY_DESCENDANT           = 2;
    /**
     * Apply only class that differs for current class and descendant
     */
    private static final int            NO_CURRENT_NOR_DESCENDANT = 3;

    /**
     * Class to check for
     */
    private Class<?>                    classToApply              = null;
    /**
     * String description of the logic to apply to classToApply
     */
    private String                      sApplyToClass             = "";
    /**
     * Logic to apply to classToApply
     */
    private int                         iApplyToClass             = ONLY_CURRENT;
    /**
     * descriptor name to check
     */
    private String                      descriptorName            = "";
    /**
     * if true check the descriptor
     */
    private boolean                     applyToDescriptor         = true;
    /**
     * Map of resolved properties
     */
    private Map<String, String>         resolvedKeyProperties     = new HashMap<String, String>();
    /**
     * Map of unresolved properties
     */
    private Map<String, PropertyValues> toresolveKeyProperties    = new HashMap<String, PropertyValues>();
    /**
     * Set of unresolved properties names
     */
    private Set<String>                 toresolveKeySet           = null;
    /**
     * Iterator on toresolveKeySet
     */
    private Iterator<String>            toresolveKeySetIt         = null;

    /**
     * @version 3.0.0 Feb 27, 2010
     * @author nunzio
     *
     */
    public class PropertyValues
    {
        /**
         *
         */
        public String value    = "";
        /**
         *
         */
        public String defValue = "";

        /**
         * @param value
         * @param defValue
         */
        public PropertyValues(String value, String defValue)
        {
            this.value = value;
            this.defValue = defValue;
        }
    }

    /**
     * Constructor
     */
    public ObjectNameBuilder()
    {
        // do nothing
    }

    /**
     * Initializes the instance
     *
     * @param node
     *        node from which read configuration data
     * @throws Exception
     *         if errors occurs
     */
    public void init(Node node) throws Exception
    {
        descriptorName = XMLConfig.get(node, "@name", "");
        String className = XMLConfig.get(node, "@class", "");

        if (!descriptorName.equals("")) {
            applyToDescriptor = XMLConfig.getBoolean(node, "@apply-to-name", true);
        }

        if (!className.equals("")) {
            classToApply = Class.forName(className);
            sApplyToClass = XMLConfig.get(node, "@apply-to-class", "only-current");
            if (sApplyToClass.equals("only-current")) {
                iApplyToClass = ONLY_CURRENT;
            }
            else if (sApplyToClass.equals("current-or-descendent")) {
                iApplyToClass = CURRENT_OR_DESCENDANT;
            }
            else if (sApplyToClass.equals("only-descendent")) {
                iApplyToClass = ONLY_DESCENDANT;
            }
            else if (sApplyToClass.equals("no-current-nor-descendent")) {
                iApplyToClass = NO_CURRENT_NOR_DESCENDANT;
            }
        }

        NodeList list = XMLConfig.getNodeList(node, "ObjectNameProperty");
        for (int i = 0; i < list.getLength(); ++i) {
            Node n = list.item(i);
            String name = XMLConfig.get(n, "@name", "undefined");
            String defValue = XMLConfig.get(n, "@default-value", "undefined");
            String value = XMLConfig.get(n, "@value", defValue);
            value = PropertiesHandler.expand(value);
            if (value.equals("")) {
                value = defValue;
            }
            if (PropertiesHandler.isExpanded(value)) {
                resolvedKeyProperties.put(name, value);
            }
            else {
                PropertyValues propertyValues = new PropertyValues(value, defValue);
                toresolveKeyProperties.put(name, propertyValues);
            }
        }
        toresolveKeySet = toresolveKeyProperties.keySet();
    }

    /**
     * Add input Properties, if required, a set of properties
     *
     * @param descrName
     *        descriptor name of the MBean to register
     * @param inProperties
     *        partially defined ObjectName properties list
     * @param obj
     *        object to encapsulate on MBean
     * @return the enriched inProperties
     */
    @SuppressWarnings("unchecked")
    public Hashtable<String, String> resolve(String descrName, Map<String, String> inProperties, Object obj)
    {
        boolean descriptorNameOK = true;
        boolean classToApplyOK = true;
        Hashtable<String, Object> properties = new Hashtable<String, Object>(inProperties);

        if (descriptorName.length() != 0) {
            descriptorNameOK = (applyToDescriptor == descriptorName.equals(descrName));
        }

        if (classToApply != null) {
            switch (iApplyToClass) {
                case ONLY_CURRENT :
                    classToApplyOK = classToApply.getName().equals(obj.getClass().getName());
                    break;
                case CURRENT_OR_DESCENDANT :
                    classToApplyOK = classToApply.isInstance(obj);
                    break;
                case ONLY_DESCENDANT :
                    classToApplyOK = !(classToApply.getName().equals(obj.getClass().getName()) || !(!classToApply.getName().equals(
                            obj.getClass().getName()) && classToApply.isInstance(obj)));
                    break;
                case NO_CURRENT_NOR_DESCENDANT :
                    classToApplyOK = !classToApply.isInstance(obj);
                    break;
                default :
                    classToApplyOK = false;
            }
        }

        if (descriptorNameOK && classToApplyOK) {
            properties.putAll(resolvedKeyProperties);

            toresolveKeySetIt = toresolveKeySet.iterator();

            while (toresolveKeySetIt.hasNext()) {
                String name = toresolveKeySetIt.next();
                PropertyValues propertyValues = toresolveKeyProperties.get(name);
                String value = "";
                try {
                    value = PropertiesHandler.expand(propertyValues.value, properties, obj);
                }
                catch (PropertiesHandlerException exc) {
                    exc.printStackTrace();
                }
                if (value.equals("")) {
                    value = propertyValues.defValue;
                }
                properties.put(name, value);
            }
        }

        return new Hashtable(properties);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ("ObjectNameBuilder - " + descriptorName + " - " + applyToDescriptor + " - " + classToApply + " - " + sApplyToClass);
    }
}
