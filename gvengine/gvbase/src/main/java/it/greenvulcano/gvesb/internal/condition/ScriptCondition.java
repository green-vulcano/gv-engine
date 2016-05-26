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
import it.greenvulcano.script.ScriptExecutor;
import it.greenvulcano.script.ScriptExecutorFactory;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * Renamed from JavaScriptCondition on Aug 8, 2014.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ScriptCondition implements GVCondition
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(ScriptCondition.class);

    /**
     * The Condition name.
     */
    private String              condition       = "";
    /**
     * The script executor.
     */
    private ScriptExecutor      script          = null;
    /**
     * If true an execution exception must be propagate to the caller.
     */
    private boolean             throwException  = false;

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#init(org.w3c.dom.Node)
     */
    @Override
    public final void init(Node node) throws GVConditionException
    {
        try {
            condition = XMLConfig.get(node, "@condition", "");
            logger.debug("Initializing ScriptCondition: " + condition);
            throwException = XMLConfig.getBoolean(node, "@throw-exception", false);
            script = ScriptExecutorFactory.createSE(XMLConfig.getNode(node, "Script"));
        }
        catch (Exception exc) {
            throw new GVConditionException("Error initializing ScriptCondition", exc);
        }
    }

    @Override
    public String getName() {
        return condition;
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public final boolean check(String dataName, Map<String, Object> environment) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking ScriptCondition[" + condition + "]");
        try {
            script.putProperty("environment", environment);
            script.putProperty("dataName", dataName);
            script.putProperty("logger", logger);
            String jsResult = "" + script.execute(null);
            logger.debug("ScriptCondition::check() -- RESULT: [" + jsResult + "]");
            result = (jsResult.equalsIgnoreCase("true") || jsResult.equalsIgnoreCase("yes") || jsResult.equalsIgnoreCase("on"));

            return result;
        }
        catch (Exception exc) {
            logger.error("Error occurred on ScriptCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("SCRIPT_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"engine", script.getEngineName()}, {"scriptName", script.getScriptName()},
                        {"exception", "" + exc}}, exc);
            }
        }
        finally {
            logger.debug("END - Cheking ScriptCondition[" + condition + "]: " + result);
        }

        return result;
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(Object)
     */
    @Override
    public final boolean check(Object obj) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking ScriptCondition[" + condition + "]");
        try {
            script.putProperty("data", obj);
            script.putProperty("logger", logger);
            String jsResult = "" + script.execute(null);
            logger.debug("ScriptCondition::check() -- RESULT: [" + jsResult + "]");
            result = (jsResult.equalsIgnoreCase("true") || jsResult.equalsIgnoreCase("yes") || jsResult.equalsIgnoreCase("on"));

            return result;
        }
        catch (Exception exc) {
            logger.error("Error occurred on ScriptCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("SCRIPT_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"engine", script.getEngineName()}, {"scriptName", script.getScriptName()},
                        {"exception", "" + exc}}, exc);
            }
        }
        finally {
            logger.debug("END - Cheking ScriptCondition[" + condition + "]: " + result);
        }

        return result;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#cleanUp()
     */
    @Override
    public final void cleanUp()
    {
        if (script != null) {
            script.cleanUp();
        }
    }
}
