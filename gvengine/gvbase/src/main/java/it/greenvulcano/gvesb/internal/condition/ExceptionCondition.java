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
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class ExceptionCondition implements GVCondition
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(ExceptionCondition.class);
    /**
     * The Condition name.
     */
    private String               condition       = "";
    /**
     * If true an execution exception must be propagate to the caller.
     */
    private boolean              throwException  = false;
    /**
     * The vector containing the configured ExceptionDef
     */
    private Vector<ExceptionDef> exceptionVector = new Vector<ExceptionDef>();

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws GVConditionException
    {
        try {
            condition = XMLConfig.get(node, "@condition", "");
            logger.debug("Initializing ExceptionCondition: " + condition);
            throwException = XMLConfig.getBoolean(node, "@throw-exception", false);
            NodeList nl = XMLConfig.getNodeList(node, "ExceptionDef");
    
            if ((nl == null) || (nl.getLength() == 0)) {
                ExceptionDef exception = new ExceptionDef();
                logger.debug("Adding Exception: " + exception);
                exceptionVector.add(exception);
                return;
            }
    
            for (int i = 0; i < nl.getLength(); i++) {
                ExceptionDef exception = new ExceptionDef();
                exception.init(nl.item(i));
                logger.debug("Adding Exception: " + exception);
                exceptionVector.add(exception);
            }
        }
        catch (Exception exc) {
            throw new GVConditionException("Error initializing ExceptionCondition", exc);
        }
    }

    @Override
    public String getName() {
        return condition;
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(String,
     *      Map)
     */
    public boolean check(String dataName, Map<String, Object> environment) throws GVConditionException
    {
        try {
            Object obj = environment.get(dataName);

            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Exception)) {
                return false;
            }

            return check((Exception) obj);
        }
        catch (GVConditionException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error occurred on ExceptionCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("EXCEPTION_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        return false;
    }

    /**
     * @throws GVConditionException
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(Object)
     */
    public boolean check(Object obj) throws GVConditionException
    {
        try {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Exception)) {
                return false;
            }

            return check((Exception) obj);
        }
        catch (GVConditionException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error occurred on ExceptionCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("EXCEPTION_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        return false;
    }

    /**
     * Perform the condition check
     *
     * @param exception
     *        the exception to check
     * @return the check result
     * @throws GVConditionException
     */
    public boolean check(Exception exception) throws GVConditionException
    {
        try {
            boolean match = false;
            int i = 0;

            while ((i < exceptionVector.size()) && !match) {
                ExceptionDef excDef = exceptionVector.elementAt(i);
                match = excDef.match(exception);
                i++;
            }

            return match;
        }
        catch (Exception exc) {
            logger.error("Error occurred on ExceptionCondition.check()", exc);
            if (throwException) {
                throw new GVConditionException("EXCEPTION_CONDITION_EXEC_ERROR", new String[][]{
                        {"condition", condition}, {"exception", "" + exc}}, exc);
            }
        }
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#cleanUp()
     */
    public void cleanUp()
    {
        // do nothing
    }
}
