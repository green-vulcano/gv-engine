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
import it.greenvulcano.gvesb.j2ee.db.utils.TxIsolationEnumeration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class JdbcConnections
{
    private static final Logger    logger                 = org.slf4j.LoggerFactory.getLogger(JdbcDataBaseConnection.class);
    /**
     * Number of initial connections to make.
     */
    private int                    initialConnectionCount = 0;

    /**
     * The initial maximum connection number
     */
    private int                    connectionMax          = 0;

    /**
     * The connection string to connect to database
     */
    private String                 connectionString       = "";

    /**
     * The user to connect to database
     */
    private String                 user                   = "";

    /**
     * The password to connect to database
     */
    private String                 password               = "";

    /**
     * The transaction isolation
     */
    private TxIsolationEnumeration txIsolation            = TxIsolationEnumeration.TRANSACTION_NONE;

    /**
     * The autoCommit value
     */
    private boolean                autoCommit             = true;

    /**
     * The Available connections pool
     */
    private LinkedList<Connection> availableConnections   = new LinkedList<Connection>();

    /**
     * Initialize the connection
     *
     * @param node
     *        The configuration node
     * @throws GVDBException
     *         if an error occurred reading the configuration file
     */
    public void init(Node node) throws GVDBException
    {
        logger.debug("JdbcConnections - INIT BEGIN");

        try {
            initialConnectionCount = XMLConfig.getInteger(node, "@connectionMin");
            connectionMax = XMLConfig.getInteger(node, "@connectionMax");
            autoCommit = XMLConfig.getBoolean(node, "@autoCommit");
            txIsolation = TxIsolationEnumeration.valueOf(XMLConfig.get(node, "@txIsolation"));
            connectionString = XMLConfig.get(node, "@connectionString");
            user = XMLConfig.get(node, "@user", "");
            password = XMLConfig.getDecrypted(node, "@password", "");
        }
        catch (XMLConfigException exc) {
            logger.error("INIT JdbcConnections - Error while accessing configuration info : ", exc);
            throw new GVDBException("J2EE_DB_CONFIGURATION_ERROR", exc);
        }

        try {
            for (int i = 0; i < initialConnectionCount; i++) {
                availableConnections.add(createConnection());
            }
        }
        catch (GVDBException exc) {
            logger.debug("JdbcConnections Init exception closeAllConnections");
            closeAllConnections();
            throw exc;
        }

    }

    /**
     * Close all the connection created
     */
    public void closeAllConnections()
    {
        logger.debug("JdbcConnections Close All Connections");
        while (availableConnections.size() > 0) {
            Connection conn = availableConnections.removeFirst();
            try {
                conn.close();
            }
            catch (SQLException exc) {
                logger.warn("Error closing connection.");
            }
        }
    }

    /**
     * Create the connection object
     *
     * @return The connection
     * @throws GVDBException
     *         if an error occurred getting connection
     */
    private Connection createConnection() throws GVDBException
    {
        logger.debug("JdbcConnections BEGIN CREATE CONNECTION");

        try {
            if ((!user.equals("")) && (!password.equals(""))) {
                return DriverManager.getConnection(connectionString, user, password);
            }
            return DriverManager.getConnection(connectionString);
        }
        catch (SQLException exc) {
            logger.error("GetConnection JdbcConnections - Error creating jdbc connection: ", exc);
            throw new GVDBException("J2EE_DB_CONNECTION_ERROR", exc);
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
        Connection connection = null;

        logger.debug("getConnection - CheckOut the connection from Pool");

        if (availableConnections.size() == 0) {
            logger.debug("creating new connection");
            connection = createConnection();
        }
        else {
            logger.debug("extracting connection from pool");
            connection = availableConnections.removeLast();
            logger.debug("getConnection - availableConnections size = " + availableConnections.size());
        }

        try {
            connection.setAutoCommit(autoCommit);
            if (txIsolation != TxIsolationEnumeration.TRANSACTION_NONE) {
                connection.setTransactionIsolation(txIsolation.getId());
            }
        }
        catch (SQLException exc) {
            logger.error("GetConnection JdbcConnections - Error setting the autoCommit/tx isolation parameters:", exc);
            throw new GVDBException("J2EE_DB_CONNECTION_ERROR", exc);
        }

        return connection;
    }

    /**
     * Release the connection object
     *
     * @param connection
     *        the connection object to release
     * @throws GVDBException
     *         If an error occurred
     */
    public synchronized void releaseConnection(Connection connection) throws GVDBException
    {
        logger.debug("Release connection");

        if (connection != null) {
            if (availableConnections.size() >= connectionMax) {
                try {
                    connection.close();
                }
                catch (SQLException exc) {
                    logger.warn("ReleaseConnection JdbcConnections - Error releasing connection in pool: " + exc);
                }
            }
            else {
                availableConnections.addFirst(connection);
            }
            logger.debug("releaseConnection - availableConnections size = " + availableConnections.size());
        }
        else {
            throw new GVDBException("J2EE_DB_INVALID_CONNECTION", new String[][]{{"reason", "null"}});
        }
    }

    /**
     * Return the availables connection
     *
     * @return availableConnections
     */
    public LinkedList<Connection> getAvailablesConnections()
    {
        return availableConnections;
    }

    /**
     * Return the connection String
     *
     * @return connectionString
     */
    public String getConnectionString()
    {
        return connectionString;
    }

    /**
     * Return the user to connect at database
     *
     * @return user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Return the password to connect at database
     *
     * @return password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Return the initial number of connections requested
     *
     * @return The initial connection number
     */
    public int getInitialConnectionCount()
    {
        return initialConnectionCount;
    }

    /**
     * Return the maximum number of connections requested
     *
     * @return The maximum connection number
     */
    public int getMaxConnectionCount()
    {
        return connectionMax;
    }

    /**
     * Return the auto commit for the connection
     *
     * @return autoCommit
     */
    public boolean getAutoCommit()
    {
        return autoCommit;
    }

    /**
     * Return the transaction isolation name for the connection
     *
     * @return txIsolation
     */
    public String getTxIsolation()
    {
        return txIsolation.toString();
    }
}
