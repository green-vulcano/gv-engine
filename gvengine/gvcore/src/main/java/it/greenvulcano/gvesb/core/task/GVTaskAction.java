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
package it.greenvulcano.gvesb.core.task;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.GVConditionException;
import it.greenvulcano.scheduler.TaskException;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Initializer for {@link GVServiceCallerTask <code>GVServiceCallerTask</code>}
 * class.
 *
 * @version 3.0.0 Mar 11, 2010
 * @author GreenVulcano Developer Team
 */
public class GVTaskAction
{
    private static Map<String, ActionType> reverseEnum = new HashMap<String, ActionType>();

    /**
     * @version 3.0.0 Mar 11, 2010
     * @author nunzio
     *
     */
    public enum ActionType {
        /**
         *
         */
        NO_ACTION("none"),
        /**
         *
         */
        CO_CONTINUE_ACTION("commit-continue"),
        /**
         *
         */
        RB_EXIT_ACTION("rollback-exit"),
        /**
         *
         */
        CO_EXIT_ACTION("commit-exit"),
        /**
         *
         */
        RB_CONTINUE_ACTION("rollback-continue");


        private String action;

        private ActionType(String action)
        {
            this.action = action;
            reverseEnum.put(action, this);
        }

        /**
         * @see java.lang.Enum#toString()
         */
        public String toString()
        {
            return action;
        }
    }

    private GVCondition         condition = null;
    private String              action    = ActionType.NO_ACTION.toString();
    private Map<String, Object> hm        = new HashMap<String, Object>();

    /**
     *
     */
    public GVTaskAction()
    {
        // do nothing
    }

    /**
     * @param node
     * @throws TaskException
     */
    public void init(Node node) throws TaskException
    {
        try {
            Node n = XMLConfig.getNode(node, "*[@type='condition']");
            String className = XMLConfig.get(n, "@class");
            condition = (GVCondition) Class.forName(className).newInstance();
            condition.init(n);
            action = XMLConfig.get(node, "@action", ActionType.NO_ACTION.toString());
        }
        catch (Exception exc) {
            throw new TaskException("Error occurred initializing GVTaskAction: " + exc, exc);
        }
    }

    /**
     * @param obj
     * @return the action type
     * @throws GVConditionException
     */
    public ActionType check(Object obj) throws GVConditionException
    {
        hm.put("data", obj);
        if (condition.check("data", hm)) {
            return reverseEnum.get(action);
        }
        return ActionType.NO_ACTION;
    }
    
    @Override
    public String toString() {
    	return "Action[" + action + "] -> condition: " + condition.getName();
    }
}
