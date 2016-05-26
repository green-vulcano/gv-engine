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
package it.greenvulcano.gvesb.j2ee.db.connections;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.util.thread.ThreadMap;

import java.sql.Connection;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class define a generic JDBC connection.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JdbcDataBaseConnection implements DataBaseConnection
{
    private static Logger logger         = org.slf4j.LoggerFactory.getLogger(JdbcDataBaseConnection.class);

    /**
     * The connection name to manage the connection pool
     */
    private String        connectionName = "";

    /**
     * Initialize the connection object info
     *
     * @param node
     *        The configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        try {
            connectionName = XMLConfig.get(node, "@connectionName");
        }
        catch (XMLConfigException exc) {
            logger.error("INIT JdbcConnection - Error while accessing configuration info : ", exc);
            throw new GVDBException("J2EE_MISSED_CFG_PARAM_ERROR", new String[][]{{"name", "connectionName"},
                    {"node", "JdbcConnection"}}, exc);
        }
    }

    /**
     * Get the connection object
     *
     * @return The connection
     * @throws GVDBException
     *         if an error occurred getting connection
     */
    public synchronized Connection getConnection() throws GVDBException
    {
        Connection conn = null;

        try {
            conn = JdbcConnectionPool.instance().getConnection(connectionName);
        }
        catch (GVDBException exc) {
            logger.error("GET CONNECTION - JdbcConnection - Error getting connection : ", exc);
            throw new GVDBException("J2EE_DB_CONNECTION_ERROR", new String[][]{{"name", connectionName}}, exc);
        }

        return conn;
    }

    /**
     * Get the connection object from cache
     *
     * @param key
     *
     * @return The connection
     * @throws GVDBException
     *         if an error occurred getting connection
     */
    public synchronized Connection getConnection(String key) throws GVDBException
    {
        Connection conn = null;

        try {
            conn = (Connection) ThreadMap.get(key);
            if (conn == null) {
                conn = JdbcConnectionPool.instance().getConnection(connectionName);
                ThreadMap.put(key, conn);
            }
        }
        catch (GVDBException exc) {
            logger.error("GET CONNECTION - JdbcConnection - Error getting connection : ", exc);
            throw new GVDBException("J2EE_DB_CONNECTION_ERROR", new String[][]{{"name", connectionName}}, exc);
        }

        return conn;
    }

    /**
     * Release the connection
     *
     * @param connection
     *        the connection object to release
     * @throws GVDBException
     *         if an error occurred closing the given connection
     */
    public void releaseConnection(Connection connection) throws GVDBException
    {
        try {
            JdbcConnectionPool.instance().releaseConnection(connection);
        }
        catch (GVDBException exc) {
            logger.error("RELEASE CONNECTION - JdbcConnection - Error while closing connection : ", exc);
            throw new GVDBException("J2EE_DB_CLOSE_CONNECTION_ERROR", exc);
        }
    }

    /**
     * Release the connection in cache
     *
     * @param key
     *        the key to remove the connection in cache
     * @throws GVDBException
     *         if an error occurred closing the given connection
     */
    public void releaseConnection(String key) throws GVDBException
    {
        try {
            Connection connection = (Connection) ThreadMap.remove(key);
            if (connection != null) {
                JdbcConnectionPool.instance().releaseConnection(connection);
            }
        }
        catch (GVDBException exc) {
            logger.error("RELEASE CONNECTION - JdbcConnection - Error while closing connection : ", exc);
            throw new GVDBException("J2EE_DB_CLOSE_CONNECTION_ERROR", exc);
        }
    }

    /**
     * Set the logger
     *
     * @param log
     *        the logger to set
     */
    public void setLogger(Logger log)
    {
        JdbcDataBaseConnection.logger = log;
    }
}
