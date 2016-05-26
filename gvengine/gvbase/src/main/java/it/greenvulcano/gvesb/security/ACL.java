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
import java.util.List;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.security.callers.Caller;

/**
 * ACL class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class ACL
{
    /**
     * The default logger instance.
     */
    private static final Logger defaultLogger  = org.slf4j.LoggerFactory.getLogger(ACL.class);

    /**
     * Logger instance.
     */
    private Logger              logger;

    /**
     * The list of access rule.
     */
    List<AccessRule>            accessRuleList = new ArrayList<AccessRule>();

    /**
     * The default constructor.
     */
    public ACL()
    {
        logger = defaultLogger;
    }

    /**
     * The contructor with param.
     *
     * @param logger
     *        The logger
     */
    public ACL(Logger logger)
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
    public void init(Node nodeConf) throws XMLConfigException
    {
        logger.debug("BEGIN ACL init(Node node)");

        NodeList accessRuleNodeConflist = XMLConfig.getNodeList(nodeConf, "access-rule");
        if (accessRuleNodeConflist != null) {
            for (int i = 0; i < accessRuleNodeConflist.getLength(); i++) {
                AccessRule accessRule = new AccessRule(logger);
                accessRule.init(accessRuleNodeConflist.item(i));
                accessRuleList.add(accessRule);
            }
        }

        logger.debug("END ACL init(Node node)");
    }

    /**
     * The access rules are verified in sequence. The verification finishes at
     * the first access-rule satisfied.
     *
     * @param caller
     *        the Caller
     * @return true if the caller is authorized otherwise false
     */
    public boolean isAllowed(Caller caller)
    {
        for (Iterator<AccessRule> iter = accessRuleList.iterator(); iter.hasNext();) {
            AccessRule accessRule = iter.next();

            if (accessRule.isVerified(caller)) {
                boolean isAllowed = accessRule.getActionAsBoolean();
                if (!isAllowed) {
                    logger.warn("Action 'DENY' on the Access Rule verified: " + accessRule.toString());
                }
                else {
                    logger.debug("Action 'GRANT' on the Access Rule verified: " + accessRule.toString());
                }
                return isAllowed;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("AccessRule '" + accessRule.getAccessRuleName() + "' isn't verified: "
                        + accessRule.toString());
            }
        }
        logger.warn("Action 'DENY' because none Access Rule has been verified");
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("ACL[");
        buffer.append("accessRuleList: '").append(accessRuleList).append("'");
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * It release the resources.
     */
    public final void destroy()
    {
        accessRuleList = null;
    }
}
