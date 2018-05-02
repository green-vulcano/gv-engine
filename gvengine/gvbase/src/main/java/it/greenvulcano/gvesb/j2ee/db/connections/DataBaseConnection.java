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
package it.greenvulcano.gvesb.j2ee.db.connections;

import it.greenvulcano.gvesb.j2ee.db.GVDBException;

import java.sql.Connection;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This is an interface define the methods useful to create the dataBase
 * connection.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface DataBaseConnection
{
    /**
     * Initialize the connection object info
     *
     * @param node
     *        The configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    void init(Node node) throws GVDBException;

    /**
     * Get the connection object
     *
     * @return The connection
     * @throws GVDBException
     *         if an error occurred
     */
    Connection getConnection() throws GVDBException;

    /**
     *
     * @param key
     *        the key value to get connection from cache
     * @return the connection object
     * @throws GVDBException
     */
    Connection getConnection(String key) throws GVDBException;

    /**
     * Release the connection
     *
     * @param connection
     *        the connection object to release
     * @throws GVDBException
     *         if an error occurred
     */
    void releaseConnection(Connection connection) throws GVDBException;

    /**
     * @param key
     *        The key to remove the connection in cache
     * @throws GVDBException
     */
    void releaseConnection(String key) throws GVDBException;

    /**
     * Set the logger
     *
     * @param log
     *        the logger to set
     */
    void setLogger(Logger log);
}
