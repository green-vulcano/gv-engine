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
package it.greenvulcano.gvesb.core.flow.parallel;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.GVConditionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * 
 * @version 3.4.0 Jun 17, 2013
 * @author GreenVulcano Developer Team
 * 
 */
public class FlowDef
{
    private static Logger logger     = org.slf4j.LoggerFactory.getLogger(FlowDef.class);
    
    private String      name;
    private GVCondition check  = null;
    private String      subflow;
    private String      inputRefDP = null;
    
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
    public void init(Node node) throws GVCoreConfException {
        try {
            name = XMLConfig.get(node, "@name");
            subflow = XMLConfig.get(node, "@subflow");
            String condition = XMLConfig.get(node, "@condition", null);
            inputRefDP = XMLConfig.get(node, "@ref-dp", null);

            if (condition != null) {
                logger.debug("Searching condition '" + condition + "' at Flow or SubFlow level");
                Node checkNode = XMLConfig.getNode(node, "../../../Conditions/*[@condition='" + condition + "']");
                if (checkNode == null) {
                    logger.debug("Searching condition '" + condition + "' at Operation level");
                    checkNode = XMLConfig.getNode(node, "../../../../Conditions/*[@condition='" + condition + "']");
                }
                if (checkNode == null) {
                    logger.debug("Searching condition '" + condition + "' at Service level");
                    checkNode = XMLConfig.getNode(node, "../../../../../Conditions/*[@condition='" + condition + "']");
                }
                if (checkNode == null) {
                    throw new GVCoreConfException("GVCORE_CONDITION_NOT_FOUND_ERROR",
                            new String[][] { { "condition", condition } });
                }
                String className = "";
                try {
                    className = XMLConfig.get(checkNode, "@class");
                    logger.debug("Creating GVCondition of class " + className);
                    check = (GVCondition) Class.forName(className).newInstance();
                    check.init(checkNode);
                }
                catch (ClassNotFoundException exc) {
                    throw new GVCoreConfException("GVCORE_CLASS_NOT_FOUND_ERROR",
                            new String[][] { { "className", className } }, exc);
                }
                catch (Exception exc) {
                    throw new GVCoreConfException("GVCORE_NEW_INSTANCE_ERROR_ERROR", new String[][] {
                            { "className", className }, { "exception", exc.getMessage() } }, exc);
                }
            }
        }
        catch (GVCoreConfException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new GVCoreConfException("GVCORE_FLOWDEF_INIT_ERROR", new String[][] {
                    { "name", name }, { "exception", exc.getMessage() } }, exc);
        }
    }
    
    public String getName() {
        return this.name;
    }

    public String getSubflow() {
        return this.subflow;
    }

    public boolean check(GVBuffer input) throws GVConditionException {
        boolean exec = true;
        if (check != null) {
            logger.debug("Cheking condition[" + check.getName() + "] for flowdef[" + getName() + "]");
            exec = check.check(input);
        }
        logger.debug("Execution check for flowdef[" + getName() + "]: " + exec);
        return exec;
    }
    
    public String getInputRefDP() {
        return this.inputRefDP;
    }
}