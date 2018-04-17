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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.util.thread.ThreadMap;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class define a dataSource connection created with jndi-name value.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DataSourceConnection implements DataBaseConnection
{
    private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(DataSourceConnection.class);

    /**
     * The dataSource JNDIName
     */
    protected String      jndiName   = "";

    /**
     * The dataSource
     */
    protected DataSource  ds         = null;

    /**
     * the JNDI helper
     */
    protected JNDIHelper  jndiHelper = null;

    /**
     * Initialize the connection
     *
     * @param node
     *        The configuration node
     * @throws GVDBException
     *         if an error occurred
     */
    public void init(Node node) throws GVDBException
    {
        jndiHelper = new JNDIHelper();
        try {
            jndiName = XMLConfig.get(node, "@jndi-name");
            if (XMLConfig.exists(node, "JNDIHelper")) {
                jndiHelper = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
            }
            ds = initDS();
        }
        catch (XMLConfigException exc) {
            logger.error("INIT DataSourceConnection - Error while accessing configuration info : ", exc);
            throw new GVDBException("J2EE_DB_CONFIGURATION_CTX_ERROR", new String[][]{
                    {"properties", jndiHelper.toString()}, {"jndiName", jndiName}}, exc);
        }
        catch (GVDBException exc) {
            throw exc;
        }
    }

    /**
     * @return
     * @throws NamingException
     */
    private DataSource initDS() throws GVDBException
    {
        DataSource dS = null;
        try {
            dS = (DataSource) jndiHelper.lookup(jndiName);
        }
        catch (NamingException exc) {
            logger.error("INIT DataSourceConnection - Error during the DataSource creation : ", exc);
            throw new GVDBException("J2EE_DB_JNDI_NAME_ERROR", new String[][]{{"jndiName", jndiName}}, exc);
        }
        finally {
            try {
                jndiHelper.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return dS;
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
            conn = ds.getConnection();
        }
        catch (SQLException exc) {
            initDS();
            try {
                conn = ds.getConnection();
            }
            catch (SQLException exc2) {
                logger.error("GET CONNECTION - DataSourceConnection - Error creating the DataSource connection : ",
                        exc2);
                throw new GVDBException("J2EE_DB_CONNECTION_ERROR", exc2);
            }
        }

        return conn;
    }

    /**
     * Get the connection object from cache if not found get it from connection
     * pool.
     *
     * @param key
     *
     * @return The connection
     * @throws GVDBException
     *         if an error occurred getting connection
     */
    public synchronized Connection getConnection(String key) throws GVDBException
    {
        Connection conn = (Connection) ThreadMap.get(key);
        if (conn == null) {
            logger.debug("Create a new connection and insert it in pool.");
            try {
                conn = ds.getConnection();
            }
            catch (SQLException exc) {
                initDS();
                try {
                    conn = ds.getConnection();
                }
                catch (SQLException exc2) {
                    logger.error("GET CONNECTION - DataSourceConnection - Error creating the DataSource connection : ",
                            exc2);
                    throw new GVDBException("J2EE_DB_CONNECTION_ERROR", exc2);
                }
            }
            ThreadMap.put(key, conn);
        }

        return conn;
    }

    /**
     * Release the connection in cache
     *
     * @param connection
     *        the connection object to release
     * @throws GVDBException
     *         if an error occurred closing the given connection
     */
    public void releaseConnection(Connection connection) throws GVDBException
    {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (SQLException exc) {
            logger.error("RELEASE CONNECTION - DataSourceConnection - Error while closing connection : " + exc);
            throw new GVDBException("J2EE_DB_CLOSE_CONNECTION_ERROR", exc);
        }
    }

    /**
     * Release the connection
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
                connection.close();
            }
        }
        catch (SQLException exc) {
            logger.error("RELEASE CONNECTION - DataSourceConnection - Error while closing connection : ", exc);
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
        DataSourceConnection.logger = log;
    }
}
