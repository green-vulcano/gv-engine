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
package it.greenvulcano.gvesb.security;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.security.callers.Caller;

/**
 * AccessRule class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class AccessRule
{
    /**
     * The Logger instance.
     */
    private Logger                     logger;

    /**
     * The access rule name.
     */
    private String                     accessRuleName;

    /**
     * The boolean action value (DENY=false / GRANT=true).
     */
    private boolean                    actionAsBoolean;

    /**
     * The list of the access conditions.
     */
    private ArrayList<AccessCondition> accessConditionsList = new ArrayList<AccessCondition>();

    /**
     * The constructor with a parameter.
     *
     * @param logger
     *        The logger
     */
    public AccessRule(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * The initialization method.
     *
     * @param nodeConf
     *        the configuration node
     * @throws XMLConfigException
     *         if a configuration error occurred
     */
    public final void init(Node nodeConf) throws XMLConfigException
    {
        logger.debug("BEGIN AccessRule init(Node node)");

        accessRuleName = XMLConfig.get(nodeConf, "@name");
        String action = XMLConfig.get(nodeConf, "@action");
        actionAsBoolean = action.equalsIgnoreCase("GRANT");

        NodeList confNodeList = XMLConfig.getNodeList(nodeConf, "access-condition");
        if (confNodeList != null) {
            for (int i = 0; i < confNodeList.getLength(); i++) {
                AccessCondition accessCondition = new AccessCondition(logger);
                accessCondition.init(confNodeList.item(i));
                accessConditionsList.add(accessCondition);
            }
        }
        accessConditionsList.trimToSize();
        logger.debug("END AccessRule init(Node node)");
    }

    /**
     * The access rule is verified if all its conditions are verified. If there
     * aren't conditions the access rule is verified. If the access rule is
     * satisfied return the specific action (DENY=false / GRANT=true).
     *
     * @param caller
     *        The actual caller
     * @return if it is verified true otherwise false
     */
    public final boolean isVerified(Caller caller)
    {
        Iterator<AccessCondition> iter = accessConditionsList.iterator();
        while (iter.hasNext()) {
            AccessCondition accessCondition = iter.next();
            if (!accessCondition.isVerified(caller)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Access Condition '" + accessCondition.getAccessConditionName() + "' isn't verified: "
                            + accessCondition.toString());
                }
                return false;
            }
        }

        return true;
    }

    /**
     * @return Returns the action as boolean.
     */
    public final boolean getActionAsBoolean()
    {
        return actionAsBoolean;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AccessRule[");
        buffer.append("name: '").append(accessRuleName).append("'");
        buffer.append(" - action: '").append(actionAsBoolean).append("'");
        buffer.append(" - accessConditionsList: '").append(accessConditionsList).append("'");
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @return Returns the accessRuleName.
     */
    public String getAccessRuleName()
    {
        return accessRuleName;
    }
}
