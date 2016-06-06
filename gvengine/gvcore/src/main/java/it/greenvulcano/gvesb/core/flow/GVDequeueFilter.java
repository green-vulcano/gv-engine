/*
 * Copyright (c) 2009-2016 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.core.exc.GVCoreWrongInterfaceException;
import it.greenvulcano.gvesb.virtual.Operation;
import it.greenvulcano.util.xpath.XPathFinder;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for Dequeue with filter.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public class GVDequeueFilter
{
    private static final Logger logger            = org.slf4j.LoggerFactory.getLogger(GVDequeueFilter.class);

    /**
     * define the property name for GVBuffer Return Code
     */
    public static final String  RET_CODE_PROPERTY = "RETCODE";
    /**
     * define the property name for GVBuffer Service
     */
    public static final String  SERVICE_PROPERTY  = "SERVICE";
    /**
     * define the property name for GVBuffer System
     */
    public static final String  SYSTEM_PROPERTY   = "SYSTEM";
    /**
     * define the property name for GVBuffer Id
     */
    public static final String  ID_PROPERTY       = "ID";
    /**
     * define the property name for GVBuffer Properties
     */
    public static final String  PROPERTY_FIELD    = "p$";

    /**
     * filter definition
     */
    private String              filterDef         = "";

    public GVDequeueFilter()
    {
        filterDef = "";
    }

    /**
     * Initialize the instance
     *
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if configuration error occurs
     * @throws GVCoreConfException
     *         if configuration error occurs
     */
    public void init(Node node) throws XMLConfigException, GVCoreConfException
    {
        NodeList nl = XMLConfig.getNodeList(node, "*[@type='filter']");
        if ((nl == null) || (nl.getLength() == 0)) {
            return;
        }

        filterDef = "";
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            String nodeName = n.getNodeName();

            if (nodeName.equals("IDFilter")) {
                initID(n);
            }
            else if (nodeName.equals("RetCodeFilter")) {
                initRetCode(n);
            }
            else if (nodeName.equals("SystemFilter")) {
                initSystem(n);
            }
            else if (nodeName.equals("ServiceFilter")) {
                initService(n);
            }
            else if (nodeName.equals("PropertyFilter")) {
                initProp(n);
            }
            else {
                throw new GVCoreConfException("GVCORE_BAD_FILTER_ERROR", new String[][]{{"element", nodeName},
                        {"node", XPathFinder.buildXPath(node)}});
            }
        }

        logger.debug("Filter defition: " + filterDef);
    }

    /**
     * @param n
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if configuration error occurs
     */
    private void initProp(Node n) throws XMLConfigException
    {
        if (XMLConfig.getBoolean(n, "@enabled", false)) {
            if (!filterDef.equals("")) {
                filterDef += " AND ";
            }
            String name = XMLConfig.get(n, "@property");
            if (XMLConfig.getBoolean(n, "@use-input", false)) {
                filterDef += "(" + PROPERTY_FIELD + name + " = '" + "{{" + name + "}}')";
            }
            else {
                String value = XMLConfig.get(n, "@value");
                filterDef += "(" + PROPERTY_FIELD + name + " = '" + value + "')";
            }
        }
    }

    /**
     * @param n
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if configuration error occurs
     */
    private void initService(Node n) throws XMLConfigException
    {
        if (XMLConfig.getBoolean(n, "@enabled", false)) {
            if (!filterDef.equals("")) {
                filterDef += " AND ";
            }
            if (XMLConfig.getBoolean(n, "@use-input", false)) {
                filterDef += "(" + SERVICE_PROPERTY + " = '" + "{{" + SERVICE_PROPERTY + "}}')";
            }
            else {
                String value = XMLConfig.get(n, "@value");
                filterDef += "(" + SERVICE_PROPERTY + " = '" + value + "')";
            }
        }
    }

    /**
     * @param n
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if configuration error occurs
     */
    private void initSystem(Node n) throws XMLConfigException
    {
        if (XMLConfig.getBoolean(n, "@enabled", false)) {
            if (!filterDef.equals("")) {
                filterDef += " AND ";
            }
            if (XMLConfig.getBoolean(n, "@use-input", false)) {
                filterDef += "(" + SYSTEM_PROPERTY + " = '" + "{{" + SYSTEM_PROPERTY + "}}')";
            }
            else {
                String value = XMLConfig.get(n, "@value");
                filterDef += "(" + SYSTEM_PROPERTY + " = '" + value + "')";
            }
        }
    }

    /**
     * @param n
     *        the node from which read configuration data
     */
    private void initRetCode(Node n)
    {
        if (XMLConfig.getBoolean(n, "@enabled", false)) {
            if (!filterDef.equals("")) {
                filterDef += " AND ";
            }
            if (XMLConfig.getBoolean(n, "@use-input", false)) {
                filterDef += "(" + RET_CODE_PROPERTY + " = '" + "{{" + RET_CODE_PROPERTY + "}}')";
            }
            else {
                int min = XMLConfig.getInteger(n, "@min", Integer.MIN_VALUE);
                int max = XMLConfig.getInteger(n, "@max", Integer.MAX_VALUE);
                filterDef += "(" + RET_CODE_PROPERTY + " BETWEEN " + min + " AND " + max + ")";
            }
        }
    }

    /**
     * @param n
     *        the node from which read configuration data
     */
    private void initID(Node n)
    {
        if (XMLConfig.getBoolean(n, "@enabled", false)) {
            if (!filterDef.equals("")) {
                filterDef += " AND ";
            }
            filterDef += "(" + ID_PROPERTY + " = '" + "{{" + ID_PROPERTY + "}}')";
        }
    }

    /**
     * Replace the properties place holder with the corresponding GVBuffer
     * values
     *
     * @param gvBuffer
     *        the GVBuffer instance from which read actual properties values
     * @param operation
     *        the dequeue operation
     * @return the actual filter value
     * @throws GVCoreException
     *         if errors occurs
     */
    public String getFilterDef(GVBuffer gvBuffer, Operation operation) throws GVCoreException
    {
        StringBuilder filterVal = new StringBuilder();
        logger.debug("Initial Filter value: " + filterDef);
        int begin = -1;
        int end = -1;
        int oldBegin = 0;
        String property = "";

        begin = filterDef.indexOf("{{");
        end = filterDef.indexOf("}}", begin);
        while ((begin != -1) && (end != -1)) {
            filterVal.append(filterDef.substring(oldBegin, begin));
            property = filterDef.substring(begin + 2, end);
            logger.debug("Property: " + property);
            if (property.equals(ID_PROPERTY)) {
                filterVal.append(gvBuffer.getId().toString());
            }
            else if (property.equals(SYSTEM_PROPERTY)) {
                filterVal.append(gvBuffer.getSystem());
            }
            else if (property.equals(SERVICE_PROPERTY)) {
                filterVal.append(operation.getServiceAlias(gvBuffer));
            }
            else if (property.equals(RET_CODE_PROPERTY)) {
                filterVal.append("" + gvBuffer.getRetCode());
            }
            else {
                String value = gvBuffer.getProperty(property);
                if (value == null) {
                    throw new GVCoreWrongInterfaceException("GVCORE_INVALID_PROPERTY_ERROR", new String[][]{{
                            "property", property}});
                }
                filterVal.append(value);
            }
            oldBegin = end + 2;
            logger.debug("Substituted '" + property + "': " + filterVal);
            begin = filterDef.indexOf("{{", end);
            end = filterDef.indexOf("}}", begin);
        }
        filterVal.append(filterDef.substring(oldBegin));

        logger.debug("Final Filter value: " + filterVal);
        return filterVal.toString();
    }
}
