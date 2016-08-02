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
package it.greenvulcano.gvesb.virtual;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.internal.condition.GVBufferCondition;
import it.greenvulcano.util.xpath.XPathFinder;

import org.w3c.dom.Node;

/**
 * Routing class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author Gianluca Di Maio
 */
public class Routing
{
    /**
     * The GreenVulcano logger utility.
     */
    private static final org.slf4j.Logger logger    = org.slf4j.LoggerFactory.getLogger(Routing.class);
    /**
     *
     */
    private Operation           operation = null;
    /**
    *
    */
    private OperationKey        opKey     = null;
    /**
    *
    */
    private String              opType    = "";
    /**
     *
     */
    private GVBufferCondition   check     = null;
    /**
    *
    */
    private String              condition = "";

    /**
     *
     * @param node
     *        definition node of this object
     * @throws VCLException
     *         on configuration error
     */
    public void init(Node node) throws VCLException
    {
        String opName = "";

        try {
            condition = XMLConfig.get(node, "@condition");
            opName = XMLConfig.get(node, "@operation-name");
            opType = XMLConfig.get(node, "../@type");
        }
        catch (XMLConfigException exc) {
            throw new InitializationException("GVVCL_MISSED_CFG_PARAM_ERROR", new String[][]{
                    {"name", "'condition' or 'operation-name' or 'type'"}, {"node", XPathFinder.buildXPath(node)}}, exc);
        }
        logger.debug("Searching condition '" + condition + "'");

        Node checkNode = getNode(node, "../*[@type='condition' and @condition='" + condition + "']");
        if (checkNode == null) {
            throw new InitializationException("GVVCL_CONDITION_NOT_FOUND_ERROR",
                    new String[][]{{"condition", condition}});
        }
        String className = "";
        try {
            className = XMLConfig.get(checkNode, "./@class");
            logger.debug("Creating GVCondition of class " + className);
            check = new GVBufferCondition();
            check.init(checkNode);
        }
        catch (Exception exc) {
            throw new InitializationException("GVVCL_NEW_INSTANCE_ERROR_ERROR", new String[][]{
                    {"className", className}, {"exception", exc.getMessage()}}, exc);
        }

        Node opNode = getNode(node, "../*[@name='" + opName + "']");
        if (opNode == null) {
            throw new InitializationException("GVVCL_OPERATION_SEARCH_ERROR", new String[][]{{"xpath",
                    "../*[@name='" + opName + "']"}});
        }
        opKey = new VCLOperationKey(opNode);
    }

    /**
     * @param gvBuffer
     * @return the operation corresponding to the key the GVBuffer refers to.
     * @throws ConnectionException
     */
    public Operation getOperation(GVBuffer gvBuffer) throws ConnectionException
    {
        try {
            if (check.check(gvBuffer)) {
                if (operation == null) {
                    operation = OperationFactory.createOperation(opKey, opType);
                }
                return operation;
            }
            return null;
        }
        catch (Exception exc) {
            throw new ConnectionException("GVVCL_ROUTING_INIT_ERROR", new String[][]{{"name", condition}}, exc);
        }
    }

    /**
     *
     */
    public void destroy()
    {
        if (operation != null) {
            operation.destroy();
        }
    }

    private Node getNode(Node node, String xPath)
    {
        try {
            Node result = XMLConfig.getNode(node, xPath);
            return result;
        }
        catch (XMLConfigException exc) {
            return null;
        }
    }
}
