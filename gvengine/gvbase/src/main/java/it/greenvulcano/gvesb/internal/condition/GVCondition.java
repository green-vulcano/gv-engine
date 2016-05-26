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

import it.greenvulcano.configuration.XMLConfigException;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public interface GVCondition
{

    /**
     * Initialize the instance
     *
     * @param node
     *        the node from which read configuration data
     * @throws XMLConfigException
     *         if errors occurs
     */
    void init(Node node) throws GVConditionException;

    /**
     * 
     * @return the condition name
     */
    String getName();
    
    /**
     * Perform the condition check
     *
     * @param dataName
     *        the name to use for getting data from the environment
     * @param environment
     *        the environment from which retrieve the data to check
     * @return the check result
     * @throws GVConditionException
     *         on errors
     */
    boolean check(String dataName, Map<String, Object> environment) throws GVConditionException;

    /**
     * Perform the condition check
     *
     * @param obj
     *        the object to check
     * @return the check result
     * @throws GVConditionException
     *         on errors
     */
    boolean check(Object obj) throws GVConditionException;

    /**
     * Perform cleanup operation.
     */
    void cleanUp();
}
