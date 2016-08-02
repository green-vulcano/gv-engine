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
package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.flow.condition.GVConditionFactory;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.GVConditionException;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * GVFlow node for GVRouting.
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
 *
 */
public class GVRouting {
    private static Logger logger     = org.slf4j.LoggerFactory.getLogger(GVRouting.class);
    /**
     * the condition name
     */
    private String        condition  = "";
    /**
     * the flow node id to use if condition is true
     */
    private String        nextId = "";
    /**
     * the GVCondition instance
     */
    private GVCondition   check      = null;

    /**
     * Initialize the instance
     *
     * @param node
     *            definition node of the instance
     * @param defNode
     *            definition node of the GVNodeCheck
     * @throws GVCoreConfException
     *             on configuration error
     */
    public void init(Node node, Node defNode) throws GVCoreConfException {
        try {
            condition = XMLConfig.get(node, "@condition");
            nextId = XMLConfig.get(node, "@next-node-id");
        }
        catch (XMLConfigException exc) {
            throw new GVCoreConfException("GVCORE_MISSED_CFG_PARAM_ERROR", new String[][] {
                    { "name", "'condition' or 'next-node-id'" }, { "node", XPathFinder.buildXPath(node) } }, exc);
        }
        logger.debug("Searching condition '" + condition + "' at Flow or SubFlow level");
        Node checkNode = getNode(defNode, "../Conditions/*[@condition='" + condition + "']");
        if (checkNode == null) {
            logger.debug("Searching condition '" + condition + "' at Operation level");
            checkNode = getNode(defNode, "../../Conditions/*[@condition='" + condition + "']");
        }
        if (checkNode == null) {
            logger.debug("Searching condition '" + condition + "' at Service level");
            checkNode = getNode(defNode, "../../../Conditions/*[@condition='" + condition + "']");
        }
        if (checkNode == null) {
            throw new GVCoreConfException("GVCORE_CONDITION_NOT_FOUND_ERROR",
                    new String[][] { { "condition", condition } });
        }
        String className = checkNode.getNodeName();
        try {
          
            logger.debug("Creating GVCondition of class " + className);
            check = GVConditionFactory.make(className);
            check.init(checkNode);
        }
        catch (NoSuchElementException exc) {
            throw new GVCoreConfException("GVCORE_CLASS_NOT_FOUND_ERROR",
                    new String[][] { { "className", className } }, exc);
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_NEW_INSTANCE_ERROR_ERROR", new String[][] {
                    { "className", className }, { "exception", exc.getMessage() } }, exc);
        }
    }

    /**
     * Check the condition
     *
     * @param dataName
     *            the name of the object in the environment
     * @param environment
     *            the flow execution environment
     * @return the next flow node if condition is verified
     * @throws GVConditionException
     */
    public String getNodeId(String dataName, Map<String, Object> environment) throws GVConditionException {
        if (check.check(dataName, environment)) {
            return nextId;
        }
        return "";
    }

    /**
     * @return the condition name
     */
    public String getConditionName() {
        return condition;
    }

    /**
     * perform cleanup operations.
     *
     */
    public void cleanUp() {
        check.cleanUp();
    }

    /**
     * Search a configuration node
     *
     * @param node
     *            the context node
     * @param xPath
     *            the XPath to apply
     * @return the founded node
     */
    private Node getNode(Node node, String xPath) {
        try {
            Node result = XMLConfig.getNode(node, xPath);
            return result;
        }
        catch (XMLConfigException exc) {
            return null;
        }
    }
}