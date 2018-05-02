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
package it.greenvulcano.gvesb.j2ee.db.connections.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DriverPoolConnectionBuilder implements ConnectionBuilder
{
    private static Logger     logger          = org.slf4j.LoggerFactory.getLogger(DriverPoolConnectionBuilder.class);

    private String            url             = null;
   
    private String            user            = null;
    private String            password        = null;
    private String            name            = null;
    private String            validationQuery = null;
    private PoolingDataSource dataSource      = null;
    private GenericObjectPool connectionPool  = null;
    private boolean           debugJDBCConn   = false;
    private boolean           isFirst         = true;

    public void init(Node node) throws GVDBException
    {
        try {
            name = XMLConfig.get(node, "@name");
          
            user = XMLConfig.get(node, "@user", null);
            password = XMLConfig.getDecrypted(node, "@password", null);
            url = XMLConfig.get(node, "@url");
            try {
                debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
            }
            catch (Exception exc) {
                debugJDBCConn = false;
            }
           

            Node poolNode = XMLConfig.getNode(node, "PoolParameters");
            connectionPool = new GenericObjectPool(null);
            connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
            connectionPool.setMaxWait(XMLConfig.getLong(poolNode, "@maxWait", 30) * 1000);

            connectionPool.setMinIdle(XMLConfig.getInteger(poolNode, "@minIdle", 5));
            connectionPool.setMaxIdle(XMLConfig.getInteger(poolNode, "@maxIdle", 10));
            connectionPool.setMaxActive(XMLConfig.getInteger(poolNode, "@maxActive", 15));
            connectionPool.setTimeBetweenEvictionRunsMillis(XMLConfig.getLong(poolNode,
                    "@timeBetweenEvictionRuns", 300) * 1000);
            connectionPool.setMinEvictableIdleTimeMillis(XMLConfig.getLong(poolNode, "@minEvictableIdleTime",
                    300) * 1000);
            connectionPool.setNumTestsPerEvictionRun(XMLConfig.getInteger(poolNode, "@numTestsPerEvictionRun", 3));
            if (XMLConfig.exists(poolNode, "validationQuery")) {
                validationQuery = XMLConfig.get(poolNode, "validationQuery");
            }
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Initialization error", exc);
        }

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);
        
        dataSource = new PoolingDataSource(connectionPool);

        logger.debug("Crated DriverPoolConnectionBuilder(" + name + "). - user: " + user
                + " - password: ********* - url: " + url + " - Pool: [" + connectionPool.getMinIdle() + "/"
                + connectionPool.getMaxIdle() + "/" + connectionPool.getMaxActive() + "]");
    }

    public Connection getConnection() throws GVDBException
    {
        try {
            Connection conn = dataSource.getConnection();
            if (debugJDBCConn) {
                logger.debug("Created JDBC Connection [" + name + "]: [" + conn + "]");
                if (isFirst) {
                    isFirst = false;
                    DatabaseMetaData dbmd = conn.getMetaData();  
                    
                    logger.debug("=====  Database info =====");  
                    logger.debug("DatabaseProductName: " + dbmd.getDatabaseProductName() );  
                    logger.debug("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion() );  
                    logger.debug("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion() );  
                    logger.debug("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion() );  
                    logger.debug("=====  Driver info =====");  
                    logger.debug("DriverName: " + dbmd.getDriverName() );  
                    logger.debug("DriverVersion: " + dbmd.getDriverVersion() );  
                    logger.debug("DriverMajorVersion: " + dbmd.getDriverMajorVersion() );  
                    logger.debug("DriverMinorVersion: " + dbmd.getDriverMinorVersion() );  
                    logger.debug("=====  JDBC/DB attributes =====");  
                    if (dbmd.supportsGetGeneratedKeys() )  
                        logger.debug("Supports getGeneratedKeys(): true");  
                    else  
                        logger.debug("Supports getGeneratedKeys(): false");  
                }
            }

            return conn;
        }
        catch (Exception exc) {
            throw new GVDBException("DriverPoolConnectionBuilder - Error while creating Connection[" + name + "]", exc);
        }
    }

    public void releaseConnection(Connection conn) throws GVDBException
    {
        if (debugJDBCConn) {
            logger.debug("Closed JDBC Connection [" + name + "]: [" + conn + "]");
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (Exception exc) {
                logger.error("DriverPoolConnectionBuilder - Error while closing Connection[" + name + "]: [" + conn
                        + "]", exc);
            }
        }
    }

    public void destroy()
    {
        if (connectionPool != null) {
            try {
                connectionPool.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        logger.debug("Destroyed DriverPoolConnectionBuilder(" + name + ")");
    }
}
