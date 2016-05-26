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
package it.greenvulcano.gvesb.internal.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class GVBufferCondition implements GVCondition
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(GVBufferCondition.class);

    /**
     * The Condition name.
     */
    private String              condition      = "";
    /**
     * The system value to check.
     */
    private String              system         = "";

    /**
     * The service value to check.
     */
    private String              service        = "";

    /**
     * If true an execution exception must be propagate to the caller.
     */
    private boolean             throwException = false;
    /**
     * The list of ReturnCode ranges to check.
     */
    private Vector<IntRangeDef> retCodeVector  = new Vector<IntRangeDef>();

    private class GVBufferPropertyGroup implements GVBufferProperty
    {
        /**
         * The group name.
         */
        @SuppressWarnings("unused")
        private String                   name             = "";

        /**
         * The list of property values to check.
         */
        private Vector<GVBufferProperty> propertiesVector = new Vector<GVBufferProperty>();

        public GVBufferPropertyGroup(String name)
        {
            this.name = name;
        }

        @Override
        public boolean isGroup()
        {
            return false;
        }

        @Override
        public String getGroup()
        {
            return "";
        }

        public void addProperty(GVBufferProperty property)
        {
            propertiesVector.add(property);
        }

        @Override
        public boolean check(GVBuffer gvBuffer)
        {
            boolean propertyANDMatch = false;
            if (propertiesVector.size() > 0) {
                Iterator<GVBufferProperty> propsIt = propertiesVector.iterator();
                propertyANDMatch = true;
                while (propsIt.hasNext() && propertyANDMatch) {
                    GVBufferProperty property = propsIt.next();
                    propertyANDMatch = property.check(gvBuffer);
                }
            }
            return propertyANDMatch;
        }
    }

    class GVBufferPropertyCompare implements GVBufferProperty
    {
        /**
         * The first field name.
         */
        private String  nameA         = "";
        /**
         * The second field name.
         */
        private String  nameB         = "";
        /**
         * If true the value is managed as string.
         */
        private boolean valueIsString = true;
        /**
         * The AND group at which the instance belongs to.
         */
        private String  group         = "";
        /**
         * The operator to be used.
         */
        private int     operator      = GVB_PROP_EQUAL;

        public GVBufferPropertyCompare()
        {
            // do nothing
        }

        public void init(Node node) throws XMLConfigException
        {
            nameA = XMLConfig.get(node, "@name-a", "");
            nameB = XMLConfig.get(node, "@name-b", "");
            valueIsString = XMLConfig.get(node, "@value-type", "text").equals("text");
            group = XMLConfig.get(node, "@group", "");
            String sop = XMLConfig.get(node, "@operator", "");
            if (!sop.equals("")) {
                if (sop.equals("equal")) {
                    operator = GVB_PROP_EQUAL;
                }
                else if (sop.equals("lesser")) {
                    operator = GVB_PROP_LESSER;
                }
                else if (sop.equals("lesser-equal")) {
                    operator = GVB_PROP_LESSER_EQUAL;
                }
                else if (sop.equals("greater")) {
                    operator = GVB_PROP_GREATER;
                }
                else if (sop.equals("greater-equal")) {
                    operator = GVB_PROP_GREATER_EQUAL;
                }
                else if (sop.equals("different")) {
                    operator = GVB_PROP_DIFFERENT;
                }
                else {
                    throw new XMLConfigException("Invalid value '" + sop + "' for attribute 'operator' in element "
                            + XPathFinder.buildXPath(node));
                }
            }
        }

        /**
         * @see it.greenvulcano.gvesb.internal.condition.GVBufferProperty#isGroup()
         */
        @Override
        public boolean isGroup()
        {
            return !group.equals("");
        }

        /**
         * @see it.greenvulcano.gvesb.internal.condition.GVBufferProperty#getGroup()
         */
        @Override
        public String getGroup()
        {
            return group;
        }

        /**
         * @see it.greenvulcano.gvesb.internal.condition.GVBufferProperty#check(GVBuffer)
         */
        @Override
        public boolean check(GVBuffer data)
        {
            String valueA = data.getProperty(nameA);
            String valueB = data.getProperty(nameB);

            if ((valueA == null) || (valueB == null)) {
                return false;
            }
            if (valueIsString) {
                switch (operator) {
                    case GVB_PROP_EQUAL :
                        return (valueA.compareTo(valueB) == 0);
                    case GVB_PROP_LESSER :
                        return (valueA.compareTo(valueB) < 0);
                    case GVB_PROP_LESSER_EQUAL :
                        return (valueA.compareTo(valueB) <= 0);
                    case GVB_PROP_GREATER :
                        return (valueA.compareTo(valueB) > 0);
                    case GVB_PROP_GREATER_EQUAL :
                        return (valueA.compareTo(valueB) >= 0);
                    case GVB_PROP_DIFFERENT :
                        return (valueA.compareTo(valueB) != 0);
                }
            }
            try {
                int iValueA = Integer.parseInt(valueA);
                int iValueB = Integer.parseInt(valueB);
                switch (operator) {
                    case GVB_PROP_EQUAL :
                        return (iValueA == iValueB);
                    case GVB_PROP_LESSER :
                        return (iValueA < iValueB);
                    case GVB_PROP_LESSER_EQUAL :
                        return (iValueA <= iValueB);
                    case GVB_PROP_GREATER :
                        return (iValueA > iValueB);
                    case GVB_PROP_GREATER_EQUAL :
                        return (iValueA >= iValueB);
                    case GVB_PROP_DIFFERENT :
                        return (iValueA != iValueB);
                }
            }
            catch (Exception exc) {
                // do nothing
            }
            return false;
        }

        @Override
        public String toString()
        {
            return (nameA + " == " + nameB);
        }
    }

    /**
     * The list of property values to check
     */
    private Vector<GVBufferPropertyDef>        propertyVector        = new Vector<GVBufferPropertyDef>();
    /**
     * The list of property to check as AND group
     */
    private Map<String, GVBufferPropertyGroup> propertyGroups        = new HashMap<String, GVBufferPropertyGroup>();
    /**
     * The list of property values to check
     */
    private Vector<GVBufferPropertyCompare>    propertyCompareVector = new Vector<GVBufferPropertyCompare>();

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws GVConditionException
    {
        try {
            condition = XMLConfig.get(node, "@condition", "");
            logger.debug("Initializing GVBufferCondition: " + condition);
    
            throwException = XMLConfig.getBoolean(node, "@throw-exception", false);
            system = XMLConfig.get(node, "@system", "");
            service = XMLConfig.get(node, "@service", "");
    
            setRetCodeVector(node);
            setProperty(node);
        }
        catch (Exception exc) {
            throw new GVConditionException("Error initializing GVBufferCondition", exc);
        }
    }

    @Override
    public String getName() {
        return condition;
    }

    /**
     * Initialize the ReturnCode ranges vector
     * 
     * @param node
     *        The root Node from which read the configuration data
     * @throws Exception
     *         If some configuration error occurs
     */
    private void setRetCodeVector(Node node) throws Exception
    {
        Node retCode = null;
        try {
            retCode = XMLConfig.getNode(node, "RetCodeField");
            if (retCode == null) {
                return;
            }
        }
        catch (XMLConfigException exc) {
            return;
        }

        NodeList nl = XMLConfig.getNodeList(retCode, "RangeDef");

        if ((nl == null) || (nl.getLength() == 0)) {
            throw new XMLConfigException("The RetCodeField element can't be empty. Node: "
                    + XPathFinder.buildXPath(node));
        }

        for (int i = 0; i < nl.getLength(); i++) {
            IntRangeDef range = new IntRangeDef();
            range.init(nl.item(i));
            logger.debug("Adding IntRangeDef: " + range);
            retCodeVector.add(range);
        }
    }

    /**
     * Initialize the property values.
     * 
     * @param node
     *        The root Node from which read the configuration data
     * @throws Exception
     *         If some configuration error occurs
     */
    private void setProperty(Node node) throws Exception
    {
        NodeList nl = XMLConfig.getNodeList(node, "Property");

        if ((nl != null) && (nl.getLength() > 0)) {
            for (int i = 0; i < nl.getLength(); i++) {
                String name = XMLConfig.get(nl.item(i), "@name");
                GVBufferPropertyDef propertyDef = new GVBufferPropertyDef();
                propertyDef.init(nl.item(i));
                logger.debug("Adding Property: " + name + " - " + propertyDef);
                if (propertyDef.isGroup()) {
                    GVBufferPropertyGroup g = propertyGroups.get(propertyDef.getGroup());
                    if (g == null) {
                        g = new GVBufferPropertyGroup(propertyDef.getGroup());
                        propertyGroups.put(propertyDef.getGroup(), g);
                    }
                    g.addProperty(propertyDef);
                }
                else {
                    propertyVector.add(propertyDef);
                }
            }
        }

        nl = XMLConfig.getNodeList(node, "PropertyCompare");

        if ((nl == null) || (nl.getLength() == 0)) {
            return;
        }

        for (int i = 0; i < nl.getLength(); i++) {
            GVBufferPropertyCompare propertyComp = new GVBufferPropertyCompare();
            propertyComp.init(nl.item(i));
            logger.debug("Adding PropertyCompare: " + propertyComp);
            if (propertyComp.isGroup()) {
                GVBufferPropertyGroup g = propertyGroups.get(propertyComp.getGroup());
                if (g == null) {
                    g = new GVBufferPropertyGroup(propertyComp.getGroup());
                    propertyGroups.put(propertyComp.getGroup(), g);
                }
                g.addProperty(propertyComp);
            }
            else {
                propertyCompareVector.add(propertyComp);
            }
        }
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(String,
     *      Map)
     */
    @Override
    public boolean check(String dataName, Map<String, Object> environment) throws GVConditionException
    {
        try {
            Object obj = environment.get(dataName);

            if (obj == null) {
                return false;
            }
            if (!(obj instanceof GVBuffer)) {
                return false;
            }

            GVBuffer data = (GVBuffer) obj;

            return check(data);
        }
        catch (GVConditionException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error occurred on GVBufferCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("GVBUFFER_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        return false;
    }
    
    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(Object)
     */
    @Override
    public boolean check(Object obj) throws GVConditionException {
        try {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof GVBuffer)) {
                return false;
            }

            GVBuffer data = (GVBuffer) obj;

            return check(data);
        }
        catch (GVConditionException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error occurred on GVBufferCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("GVBUFFER_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        return false;
    }

    /**
     * Check if the given GVBuffer match the condition criteria.
     * 
     * @param gvBuffer
     *        The GVBuffer do check
     * @return true If the current condition is verified
     * @throws GVConditionException
     */
    public boolean check(GVBuffer gvBuffer) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking GVBufferCondition[" + condition + "]");
        try {
            boolean systemMatch = true;
            boolean serviceMatch = true;
            boolean retCodeMatch = true;

            boolean propertyMatch = false;
            boolean propertyGroupMatch = false;
            boolean propertyCompareMatch = false;
            boolean propertyChecked = false;

            if (!system.equals("")) {
                systemMatch = system.equals(gvBuffer.getSystem());
            }
            if (!service.equals("")) {
                serviceMatch = service.equals(gvBuffer.getService());
            }
            if (retCodeVector.size() > 0) {
                int appCode = gvBuffer.getRetCode();
                Iterator<IntRangeDef> i = retCodeVector.iterator();
                retCodeMatch = false;
                while (i.hasNext() && !retCodeMatch) {
                    retCodeMatch = i.next().contains(appCode);
                }
            }
            if (propertyVector.size() > 0) {
                Iterator<GVBufferPropertyDef> propertyIt = propertyVector.iterator();
                propertyChecked = true;
                while (propertyIt.hasNext() && !propertyMatch) {
                    GVBufferProperty propertyDef = propertyIt.next();
                    logger.debug("Cheking property: " + propertyDef);
                    propertyMatch = propertyDef.check(gvBuffer);
                }
            }
            if (propertyCompareVector.size() > 0) {
                Iterator<GVBufferPropertyCompare> propertyIt = propertyCompareVector.iterator();
                propertyChecked = true;
                while (propertyIt.hasNext() && !propertyCompareMatch) {
                    GVBufferProperty propertyDef = propertyIt.next();
                    logger.debug("Cheking property compare: " + propertyDef);
                    propertyCompareMatch = propertyDef.check(gvBuffer);
                }
            }
            if (propertyGroups.size() > 0) {
                Iterator<String> propertyIt = propertyGroups.keySet().iterator();
                propertyChecked = true;
                while (propertyIt.hasNext() && !propertyGroupMatch) {
                    GVBufferProperty propertyDef = propertyGroups.get(propertyIt.next());
                    logger.debug("Cheking property group: " + propertyDef);
                    propertyGroupMatch = propertyDef.check(gvBuffer);
                }
            }

            result = (systemMatch && serviceMatch && retCodeMatch && (propertyChecked == (propertyMatch
                    || propertyGroupMatch || propertyCompareMatch)));

            return result;
        }
        catch (Exception exc) {
            logger.error("Error occurred on GVBufferCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("GVBUFFER_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        finally {
            logger.debug("END - Cheking GVBufferCondition[" + condition + "]: " + result);
        }

        return result;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }
}
