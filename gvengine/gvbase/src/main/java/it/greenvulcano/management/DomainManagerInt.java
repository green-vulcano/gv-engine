/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.greenvulcano.management;

import it.greenvulcano.management.component.ComponentData;

import org.w3c.dom.Node;

/**
 * DomainManagerInt interface.
 *
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
*/
public interface DomainManagerInt {
    /**
     * Initialize the instance.
     *
     * @param node
     *            the configuration node
     * @throws DomainManagerException
     *             if error occurs
     */
    void init(Node node) throws DomainManagerException;

    /**
     * Execute the given domain action.
     *
     * @param action
     *            the action to execute
     * @throws DomainManagerException
     *             if error occurs
     */
    void executeDomainAction(DomainAction action) throws DomainManagerException;

    /**
     * @return the list of managed domain components
     * @throws DomainManagerException
     *             if error occurs
     */
    ComponentData[] getComponentList() throws DomainManagerException;

    /**
     * Start the given component.
     *
     * @param name
     *            the name of component to start
     * @throws DomainManagerException
     *             if error occurs
     */
    void startComponent(String name) throws DomainManagerException;

    /**
     * Stop the given component.
     *
     * @param name
     *            the name of component to stop
     * @param autoEnable
     *            if true tha component can be started automatically
     * @throws DomainManagerException
     *             if error occurs
     */
    void stopComponent(String name, boolean autoEnable) throws DomainManagerException;

    /**
     * Perform cleanup operation.
     */
    void destroy();
}
