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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class JdbcConnectionPool implements ConfigurationListener
{
    private static final Logger              logger             = org.slf4j.LoggerFactory.getLogger(JdbcConnectionPool.class);

    /**
     * Configuration file for the JDBC connections
     */
    private static final String              CONFIGURATION_FILE = "GVJdbc.xml";

    /**
     * Flag to know if the configuration is changed
     */
    private boolean                          confChangedFlag    = false;

    /**
     * The hashMap containing connectionId/jdbcConnections
     */
    private Map<String, JdbcConnections>     connectionsMap     = new HashMap<String, JdbcConnections>();

    /**
     * The map containing the connection in use connection/jdbcConnections
     */
    private Map<Connection, JdbcConnections> usedConnections    = new HashMap<Connection, JdbcConnections>();

    /**
     * Unique instance.
     */
    private static JdbcConnectionPool        singletonInstance  = null;

    /**
     * the object JMX descriptor
     */
    public static final String               DESCRIPTOR_NAME    = "JdbcConnectionPool";

    /**
     * The connection id useful for the jmx mechanism
     */
    private String                           connectionIdField  = "";

    /**
     *
     * @return the unique instance of the class.
     * @throws GVDBException
     *         if an error occurs
     */
    public static synchronized JdbcConnectionPool instance() throws GVDBException
    {
        if (singletonInstance == null) {
            singletonInstance = new JdbcConnectionPool();
        }
        return singletonInstance;
    }

    /**
     * Private constructor, so no instance can be created other than the
     * singleton.
     *
     * @throws GVDBException
     *         if an error occurs
     */
    private JdbcConnectionPool() throws GVDBException
    {
        init();
        XMLConfig.addConfigurationListener(this, CONFIGURATION_FILE);
    }

    /**
     * Initialize the connection object info
     *
     * @throws GVDBException
     *         if an error occurred reading the configuration file
     */
    private void init() throws GVDBException
    {
        logger.debug("JDBCConnectionPool - BEGIN INIT");

        confChangedFlag = false;

        try {
            NodeList nodeList = XMLConfig.getNodeList(CONFIGURATION_FILE,
                    "/GVJdbc/Drivers/*[@type='driver' and @enabled='Yes']");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node driverNode = nodeList.item(i);
                String className = XMLConfig.get(driverNode, "@class");
                logger.debug("JdbcConnectionPool className = " + className);

                try {
                    Class<?> classDriver = Class.forName(className);
                    String initType = XMLConfig.get(driverNode, "@initType", "static");
                    logger.debug("JdbcConnectionPool initTYpe = " + initType);
                    if (initType.equals("instance")) {
                        classDriver.newInstance();
                    }
                }
                catch (ClassNotFoundException exc) {
                    logger.error("INIT JdbcConnectionPool - Error loading the jdbc Driver: ", exc);
                    throw new GVDBException("J2EE_CLASS_NOT_FOUND_ERROR", new String[][]{{"className", className}}, exc);
                }
                catch (InstantiationException exc) {
                    logger.error("INIT JdbcConnectionPool - Error loading the jdbc Driver: ", exc);
                    throw new GVDBException("J2EE_INSTANTIATION_ERROR", new String[][]{{"className", className}}, exc);
                }
                catch (IllegalAccessException exc) {
                    logger.error("INIT JdbcConnectionPool - Error loading the jdbc Driver: " + exc);
                    throw new GVDBException("J2EE_INSTANTIATION_ERROR", new String[][]{{"className", className}}, exc);
                }
            }

            nodeList = XMLConfig.getNodeList(CONFIGURATION_FILE,
                    "/GVJdbc/Connections/*[@type='connection' and @enabled='Yes']");
            String connectionId = "";

            logger.debug("Connection nodeList = " + nodeList);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node connectionNode = nodeList.item(i);
                connectionId = XMLConfig.get(connectionNode, "@id");
                setConnectionId(connectionId);
                logger.debug("JdbcConnectionPool connectionId = " + connectionId);

                JdbcConnections jdbcConnections = new JdbcConnections();
                jdbcConnections.init(connectionNode);
                connectionsMap.put(connectionId, jdbcConnections);
                logger.debug("Connection Maps: connectionId = " + connectionId);
            }
        }
        catch (XMLConfigException exc) {
            logger.error("INIT JdbcConnectionPool - Error while accessing configuration info : ", exc);
            throw new GVDBException("J2EE_DB_CONFIGURATION_ERROR", exc);
        }
    }

    /**
     * The unic connection identification
     *
     * @param connectionId
     *        The connection identification
     */
    private void setConnectionId(String connectionId)
    {
        connectionIdField = connectionId;
    }

    /**
     * Get the connection object contained in the connection Pool
     *
     * @param connectionName
     *        The connection name to identify it in the pool
     * @return The connection
     * @throws GVDBException
     *         If an error occurred getting connection
     */
    public synchronized Connection getConnection(String connectionName) throws GVDBException
    {
        logger.debug("JdbcConnectionPool - BEGIN GET CONNECTION");

        if (confChangedFlag) {
            reset();
        }

        Connection connection = null;
        JdbcConnections jdbcConnections = connectionsMap.get(connectionName);
        logger.debug("JdbcConnectionPool connectionName = " + connectionName);

        if (jdbcConnections != null) {
            connection = jdbcConnections.getConnection();
            usedConnections.put(connection, jdbcConnections);
        }
        else {
            throw new GVDBException("J2EE_DB_CONNECTION_NOT_FOUND", new String[][]{{"connection", connectionName}});
        }

        return connection;
    }

    /**
     * Release the connection object. The connection is searched in the
     * usedConnection vector. If the connection is not found the connection is
     * closed directly. If the connection is found release the connection in the
     * pool.
     *
     * @param connection
     *        The connection object to release
     * @throws GVDBException
     *         If an error occurred
     */
    public synchronized void releaseConnection(Connection connection) throws GVDBException
    {
        logger.debug("Release connection");

        if (confChangedFlag) {
            reset();
        }
        if (connection != null) {
            JdbcConnections jdbcConnections = usedConnections.get(connection);

            if (jdbcConnections == null) {
                try {
                    connection.close();
                    return;
                }
                catch (SQLException exc) {
                    logger.error("RELEASE CONNECTION - JdbcConnectionPool - Error closing connection", exc);
                }
            }
            else {
                usedConnections.remove(connection);
                jdbcConnections.releaseConnection(connection);
            }
        }
        else {
            throw new GVDBException("J2EE_DB_INVALID_CONNECTION", new String[][]{{"reason", "null"}});
        }
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && (event.getFile().equals(CONFIGURATION_FILE))) {
            logger.debug("BEGIN - Operation(reload Configuration)");
            confChangedFlag = true;
            logger.debug("END - Operation(reload Configuration)");
        }
    }

    /**
     * This method clear the map object from connections and close the open
     * connection in case of configuration changed.
     *
     * @throws GVDBException
     *         If an error occurred
     */
    public synchronized void reset() throws GVDBException
    {
        usedConnections.clear();

        for (JdbcConnections jdbcConnections : connectionsMap.values()) {
            jdbcConnections.closeAllConnections();
        }
        connectionsMap.clear();
        init();
        confChangedFlag = false;
    }

    /**
     * @return Returns the descriptorName.
     */
    public static String getDescriptorName()
    {
        return DESCRIPTOR_NAME;
    }

    /**
     * Return the available connections created in the pool
     *
     * @return the available connection in the pool
     */
    public Integer[] getAvailableConnections()
    {
        Integer[] availableConnections = null;

        try {
            availableConnections = new Integer[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                availableConnections[i++] = Integer.valueOf(jdbcConnections.getAvailablesConnections().size());
            }
        }
        catch (Throwable exc) {
            logger.error("GET AVAILABLE CONNECTION ERROR", exc);
        }

        return availableConnections;
    }

    /**
     * Return the connections used in the pool
     *
     * @return the connection used
     */
    public int getUsedConnections()
    {
        if (usedConnections != null) {
            return usedConnections.size();
        }

        return 0;
    }

    /**
     * Return the connections string to get connection
     *
     * @return the connection string
     */
    public String[] getConnectionString()
    {
        String[] connectionString = null;

        try {
            connectionString = new String[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                connectionString[i++] = jdbcConnections.getConnectionString();
            }
        }
        catch (Throwable exc) {
            logger.error("GET CONNECTION STRING ERROR", exc);
        }

        return connectionString;
    }

    /**
     * Return the connections identification
     *
     * @return the connection name identification
     */
    public String getConnectionName()
    {
        return connectionIdField;
    }

    /**
     * Return the auto commit for the connection
     *
     * @return the auto commit
     */
    public String[] getAutoCommit()
    {
        String[] autoCommit = null;

        try {
            autoCommit = new String[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                if (jdbcConnections.getAutoCommit()) {
                    autoCommit[i++] = "true";
                }
                else {
                    autoCommit[i++] = "false";
                }
            }
        }
        catch (Throwable exc) {
            logger.error("GET AUTO COMMIT ERROR", exc);
        }

        return autoCommit;
    }

    /**
     * Return the transaction isolation name for the connection
     *
     * @return the txIsolation
     */
    public String[] getTxIsolation()
    {
        String[] txIsolation = null;

        try {
            txIsolation = new String[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                txIsolation[i++] = jdbcConnections.getTxIsolation();
            }
        }
        catch (Throwable exc) {
            logger.error("GET TX ISOLATION ERROR", exc);
        }

        return txIsolation;
    }

    /**
     * Return the maximum connection number in the pool
     *
     * @return the maximum connection count
     */
    public Integer[] getMaxConnections()
    {
        Integer[] maxConnection = null;

        try {
            maxConnection = new Integer[connectionsMap.size()];

            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                maxConnection[i++] = Integer.valueOf(jdbcConnections.getMaxConnectionCount());
            }
        }
        catch (Throwable exc) {
            logger.error("MAX CONNECTION ERROR", exc);
        }

        return maxConnection;
    }

    /**
     * Return the initial connection number in the pool
     *
     * @return the initial connection count
     */
    public Integer[] getInitialConnections()
    {
        Integer[] initialConnection = null;

        try {
            initialConnection = new Integer[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                initialConnection[i++] = Integer.valueOf(jdbcConnections.getInitialConnectionCount());
            }
        }
        catch (Throwable exc) {
            logger.error("GET INITIAL CONNECTION ERROR", exc);
        }

        return initialConnection;
    }

    /**
     * Return the user to connect at data base
     *
     * @return the user
     */
    public String[] getUser()
    {
        String[] user = null;

        try {
            user = new String[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                user[i++] = jdbcConnections.getUser();
            }
        }
        catch (Throwable exc) {
            logger.error("GET USER ERROR", exc);
        }
        return user;
    }

    /**
     * Return the password to connect at data base
     *
     * @return the password
     */
    public String[] getPassword()
    {
        String[] password = null;

        try {
            password = new String[connectionsMap.size()];
            int i = 0;

            for (JdbcConnections jdbcConnections : connectionsMap.values()) {
                password[i++] = jdbcConnections.getPassword();
            }
        }
        catch (Throwable exc) {
            logger.error("GET PASSWORD ERROR", exc);
        }

        return password;
    }
}
