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
package it.greenvulcano.gvesb.j2ee.db.connections.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 *
 * @version 3.1.0 Gen 29, 2011
 * @author GreenVulcano Developer Team
 *
 */
public class DriverConnectionBuilder implements ConnectionBuilder
{
    private static Logger logger        = org.slf4j.LoggerFactory.getLogger(DriverConnectionBuilder.class);

    private String        url           = null;
    private String        className     = null;
    private String        user          = null;
    private String        password      = null;
    private String        name          = null;
    private boolean       debugJDBCConn = false;
    private boolean       isFirst       = true;

    public void init(Node node) throws GVDBException
    {
        try {
            name = XMLConfig.get(node, "@name");
            className = XMLConfig.get(node, "@driver-class");
            user = XMLConfig.get(node, "@user", "");
            password = XMLConfig.getDecrypted(node, "@password", "");
            url = XMLConfig.get(node, "@url");

            Class.forName(className);
        }
        catch (Exception exc) {
            throw new GVDBException("DriverConnectionBuilder - Initialization error", exc);
        }
        try {
            debugJDBCConn = Boolean.getBoolean("it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder.debugJDBCConn");
        }
        catch (Exception exc) {
            debugJDBCConn = false;
        }
        logger.debug("Crated DriverConnectionBuilder(" + name + "). className: " + className + " - user: " + user
                + " - password: ********* - url: " + url);
    }

    public Connection getConnection() throws GVDBException
    {
        try {
            Connection conn = null;
            if (!user.equals("")) {
                conn = DriverManager.getConnection(url, user, password);
            }
            else {
                conn = DriverManager.getConnection(url);
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
            throw new GVDBException("DriverConnectionBuilder - Error while creating Connection[" + name + "]", exc);
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
                logger.error("DriverConnectionBuilder - Error while closing Connection[" + name + "]: [" + conn + "]",
                        exc);
            }
        }
    }

    public void destroy()
    {
        logger.debug("Destroyed DriverConnectionBuilder(" + name + ")");
    }
}
