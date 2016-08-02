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
package it.greenvulcano.gvesb.security;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.security.callers.Caller;

/**
 * AccessCondition class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class AccessCondition
{
    /**
     * The Logger instance.
     */
    private Logger   logger;

    /**
     * The access condition name.
     */
    private String   accessConditionName;

    /**
     * The list of roles.
     */
    private String[] roles;

    /**
     * The roles.
     */
    private String   rolesString;

    /**
     * The list of users.
     */
    private String[] users;

    /**
     * The users.
     */
    private String   usersString;

    /**
     * If true the protocol is secure otherwise is in plain.
     */
    private boolean  secure;

    /**
     * It can take the values: plain/encripted and null if it is not configured.
     */
    private String   protocol;

    /**
     * If true the single tests are negate.
     */
    private boolean  negate;

    /**
     * The contructor with param.
     *
     * @param logger
     *        The logger
     */
    public AccessCondition(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * The initialization method.
     *
     * @param nodeConf
     *        configuration node
     * @throws XMLConfigException
     *         if a configuration error occurred
     */
    public final void init(Node nodeConf) throws XMLConfigException
    {
        logger.debug("BEGIN AccessCondition init(Node node)");

        // NAME
        accessConditionName = XMLConfig.get(nodeConf, "@name");
        // MODE
        String mode = XMLConfig.get(nodeConf, "@mode", "assert");
        negate = mode.equalsIgnoreCase("negative");

        // PROTOCOL
        protocol = XMLConfig.get(nodeConf, "@protocol", "plain");
        secure = protocol.equalsIgnoreCase("encrypted");

        users = readList(nodeConf, "@users");
        roles = readList(nodeConf, "@roles");

        usersString = writeList(users);
        rolesString = writeList(roles);

        logger.debug("END AccessCondition init(Node node)");
    }

    /**
     * The verification consists in verify the conditions: - Name of the caller.
     * The condition is verified if caller name is in the list of the users
     * specify. - Role of the caller. The condition is verified if the caller
     * role is in the list of the roles specify. - Protocol. It indicates if the
     * protocol must be encrypted or not.
     *
     * It is possible to define the modality of the test: assert/negative. If
     * specific negative, the single criterion is denies and not the final
     * result.
     *
     * If no criterion comes specified the condition it is satisfied if mode is
     * 'assert', denied if mode is 'negatives'.
     *
     * @param caller
     *        the Caller
     * @return true if the condition is verified otherwise false
     */
    public final boolean isVerified(Caller caller)
    {
        // it verifies that the conditions have been configured
        if ((protocol == null) && (users.length == 0) && (roles.length == 0)) {
            return !negate;
        }

        boolean checkProtocol = isCheckSecure(caller);
        boolean checkCallerUser = isCheckCallerUser(caller);
        boolean chechCallerRole = isCheckCallerRole(caller);

        if (logger.isDebugEnabled()) {
            logger.debug("The AccessCondition '" + accessConditionName + "': isCheckSecure '" + checkProtocol
                    + " - isCheckCallerUser '" + checkCallerUser + " - isCheckCallerRole '" + chechCallerRole + "'");
        }

        if (checkProtocol && checkCallerUser && chechCallerRole) {
            return true;
        }
        return false;
    }

    /**
     * @return Returns the accessConditionName.
     */
    public String getAccessConditionName()
    {
        return accessConditionName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AccessCondition[");
        buffer.append("name: '").append(accessConditionName).append("'");
        buffer.append(" - mode: '").append(negate).append("'");
        buffer.append(" - protocol: '").append(protocol).append("'");
        buffer.append(" - users: '").append(usersString).append("'");
        buffer.append(" - roles: '").append(rolesString).append("'");
        buffer.append("]");
        return buffer.toString();
    }


    /**
     * @param nodeConf
     *        The configuration node
     * @param xpath
     *        the xPath to use
     * @return the string[] of the list
     */
    private String[] readList(Node nodeConf, String xpath)
    {
        String list = XMLConfig.get(nodeConf, xpath, "");
        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        String[] elements = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            elements[i] = tokenizer.nextToken().trim();
            i++;
        }
        return elements;
    }

    /**
     * It is used for log utility.
     *
     * @param list
     *        String[]
     * @return String
     */
    private String writeList(String[] list)
    {
        StringBuilder stringBuffer = new StringBuilder();
        for (int j = 0; j < list.length; j++) {
            stringBuffer.append("{").append(list[j]).append("}");
        }
        return stringBuffer.toString();
    }

    /**
     * Verification that the protocol of the caller is like us waited for.
     *
     * @param caller
     *        the Caller
     * @return boolean
     */
    private boolean isCheckSecure(Caller caller)
    {
        if (protocol == null) {
            return true;
        }
        else {
            boolean checkSecure = secure == caller.isSecure();
            if (negate) {
                return !checkSecure;
            }
            return checkSecure;
        }
    }

    /**
     * @param caller
     *        the Caller
     * @return true if the user name of caller is in the roles list.
     */
    private boolean isCheckCallerUser(Caller caller)
    {
        if (users.length == 0) {
            return true;
        }

        boolean checkUser = false;
        String callerName = caller.getCallerName();
        logger.debug("The caller name is '" + callerName + "'");
        if (callerName != null) {
            for (int i = 0; (i < users.length) && !checkUser; i++) {
                if (callerName.equals(users[i])) {
                    checkUser = true;
                }
            }
        }

        if (negate) {
            return !checkUser;
        }

        return checkUser;
    }

    /**
     * @param caller
     *        the Caller
     * @return true if the caller role is in the roles list.
     */
    private boolean isCheckCallerRole(Caller caller)
    {
        if (roles.length == 0) {
            return true;
        }

        boolean checkRole = false;
        for (int i = 0; (i < roles.length) && !checkRole; i++) {
            if (caller.isCallerInRole(roles[i])) {
                checkRole = true;
            }
        }

        if (negate) {
            return !checkRole;
        }

        return checkRole;
    }
}
