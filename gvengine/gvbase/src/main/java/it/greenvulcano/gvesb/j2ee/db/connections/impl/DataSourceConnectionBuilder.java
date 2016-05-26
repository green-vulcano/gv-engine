/*
 * Copyright (c) 2009-2011 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.gvesb.j2ee.db.connections.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.JNDIHelper;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DataSourceConnectionBuilder implements ConnectionBuilder
{
    private static Logger logger        = org.slf4j.LoggerFactory.getLogger(DataSourceConnectionBuilder.class);

    private DataSource    ds            = null;
    private JNDIHelper    context       = null;
    private String        dsJNDI        = null;
    private String        name          = null;
    private boolean       debugJDBCConn = false;
    private boolean       isFirst       = true;

    /**
     * Initializes the object using node passed to retrieve the JNDI name to use.
     *
     * @param node
     * @throws GVDBException
     */
    public void init(Node node) throws GVDBException
    {
        try {
            name = XMLConfig.get(node, "@name");
            dsJNDI = XMLConfig.get(node, "@data-source-jndi");
            context = new JNDIHelper(XMLConfig.getNode(node, "JNDIHelper"));
        }
        catch (Exception exc) {
            throw new GVDBException("DataSourceConnectionBuilder - Initialization error", exc);
        }
        try {
            debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
        }
        catch (Exception exc) {
            debugJDBCConn = false;
        }
        logger.debug("Crated DataSourceConnectionBuilder(" + name + "). dsJNDI: " + dsJNDI);
    }

    /**
     * Initializes the object using data source JNDI name passed.
     *
     * @param dsJNDI
     * @throws GVDBException
     */
    public void init(String dsJNDI) throws GVDBException
    {
        this.name = dsJNDI;
        this.dsJNDI = dsJNDI;
        context = new JNDIHelper();
        try {
            debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
        }
        catch (Exception exc) {
            debugJDBCConn = false;
        }
        logger.debug("Crated DataSourceConnectionBuilder(" + name + "). dsJNDI: " + dsJNDI);
    }

    /**
     * Returns a connection retrieved from the data source.
     *
     * @throws GVDBException
     */
    public Connection getConnection() throws GVDBException
    {
        try {
            if (ds == null) {
                ds = (DataSource) context.lookup(dsJNDI);
            }
            Connection conn = null;
            try {
                conn = ds.getConnection();
            }
            catch (Exception exc) {
                // re-execute jndi lookup
                ds = null;
                ds = (DataSource) context.lookup(dsJNDI);
                conn = ds.getConnection();
            }
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
            ds = null;
            throw new GVDBException("DataSourceConnectionBuilder - Error while creating Connection[" + name + "]", exc);
        }
        finally {
            try {
                context.close();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * Release the connection to the data source.
     *
     * @param conn the connection to release.
     * @throws GVDBException
     */
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
                logger.error("DataSourceConnectionBuilder - Error while closing Connection[" + name + "]: [" + conn
                        + "]", exc);
            }
        }
    }

    /**
     * Cleans used resources before this <i>DataSourceConnectionBuilder</i> object is destroyed.
     *
     */
    public void destroy()
    {
        this.context = null;
        this.ds = null;
        logger.debug("Destroyed DataSourceConnectionBuilder(" + name + ")");
    }
}
