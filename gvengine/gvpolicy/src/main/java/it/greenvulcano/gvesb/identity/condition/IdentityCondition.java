/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.identity.condition;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.identity.GVIdentityHelper;
import it.greenvulcano.gvesb.internal.condition.GVCondition;
import it.greenvulcano.gvesb.internal.condition.GVConditionException;

import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 04/feb/2012
 * @author GreenVulcano Developer Team
 */
public class IdentityCondition implements GVCondition
{
	private static final Logger logger  = LoggerFactory.getLogger(IdentityCondition.class);

    /**
     * The Condition name.
     */
    private String              condition = "";

    private Set<String>         roles     = new HashSet<String>();

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws GVConditionException
    {
        try {
            condition = XMLConfig.get(node, "@condition", "");
            logger.debug("Initializing IdentityCondition: " + condition);
    
            NodeList nl = XMLConfig.getNodeList(node, "ACL/RoleRef");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                roles.add(XMLConfig.get(n, "@name"));
            }
        }
        catch (Exception exc) {
            throw new GVConditionException("Error initializing IdentityCondition", exc);
        }

        if (roles.size() == 0) {
            throw new GVConditionException("Must be defined at least one RoleRef element. Node: "
                    + XPathFinder.buildXPath(node));
        }
    }

    @Override
    public String getName() {
        return condition;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(java.lang.String, java.util.Map)
     */
    @Override
    public boolean check(String dataName, Map<String, Object> environment) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking IdentityCondition[" + condition + "]");
        try {
            result = GVIdentityHelper.isInRole(roles);
        }
        catch (Exception exc) {
            logger.error("Error occurred on IdentityCondition.check()", exc);
            throw new GVConditionException("IDENTITY_CONDITION_EXEC_ERROR", new String[][]{{"condition", condition},
                    {"exception", "" + exc}}, exc);
        }
        finally {
            logger.debug("END - Cheking IdentityCondition[" + condition + "]: " + result);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#check(java.lang.Object)
     */
    @Override
    public boolean check(Object obj) throws GVConditionException
    {
        boolean result = false;

        logger.debug("BEGIN - Cheking IdentityCondition[" + condition + "]");
        try {
            result = GVIdentityHelper.isInRole(roles);
        }
        catch (Exception exc) {
            logger.error("Error occurred on IdentityCondition.check()", exc);
            throw new GVConditionException("IDENTITY_CONDITION_EXEC_ERROR", new String[][]{{"condition", condition},
                    {"exception", "" + exc}}, exc);
        }
        finally {
            logger.debug("END - Cheking IdentityCondition[" + condition + "]: " + result);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.gvesb.internal.condition.GVCondition#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // do nothing
    }

}
