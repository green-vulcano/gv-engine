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

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.gvesb.j2ee.db.GVDBException;
import it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder;
import it.greenvulcano.gvesb.j2ee.db.connections.impl.DataSourceConnectionBuilder;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.thread.ThreadMap;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class JDBCConnectionBuilder implements ConfigurationListener, ShutdownEventListener
{
    private static final Logger            logger             = org.slf4j.LoggerFactory.getLogger(JDBCConnectionBuilder.class);

    private static JDBCConnectionBuilder   instance           = null;
    /**
     * Configuration file for the JDBC connections
     */
    private static final String            CONFIGURATION_FILE = "GVJDBCConnection.xml";

    private static final String            THMAP_KEY          = "JDBCConnectionBuilder_THMAP_KEY";

    private Map<String, ConnectionBuilder> connBuilders       = new ConcurrentHashMap<String, ConnectionBuilder>();

    private JDBCConnectionBuilder() throws GVDBException
    {
        init();
    }

    private void init() throws GVDBException
    {
        logger.debug("JDBCConnectionBuilder - BEGIN initialization");
        connBuilders.clear();

        try {
            Document docConfig = XMLConfig.getDocument(CONFIGURATION_FILE);
            if (docConfig != null) {
                try {
                    NodeList nodeList = XMLConfig.getNodeList(docConfig, "//*[@type='jdbc-connection-builder']");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String className = XMLConfig.get(node, "@class");
                        String name = XMLConfig.get(node, "@name");
                        ConnectionBuilder cBuilder = (ConnectionBuilder) Class.forName(className).newInstance();
                        cBuilder.init(node);
                        connBuilders.put(name, cBuilder);
                    }
                }
                catch (Exception exc) {
                    logger.error("Error initializing JDBCConnectionBuilder", exc);
                    throw new GVDBException("Error initializing JDBCConnectionBuilder", exc);
                }
            }
        }
        catch (XMLConfigException exc) {
            logger.warn("Error reading JDBCConnectionBuilder configuration from file: " + CONFIGURATION_FILE, exc);
        }


        logger.debug("JDBCConnectionBuilder - END initialization");
    }

    private static synchronized JDBCConnectionBuilder instance() throws GVDBException
    {
        if (instance == null) {
            instance = new JDBCConnectionBuilder();
            XMLConfig.addConfigurationListener(instance, CONFIGURATION_FILE);
            ShutdownEventLauncher.addEventListener(instance);
        }
        return instance;
    }

    /**
     * @param name
     * @return the connection object
     * @throws GVDBException
     */
    public static Connection getConnection(String name) throws GVDBException
    {
        return getConnection(name, false);
    }

    /**
     * @param name
     * @return the connection object
     * @throws GVDBException
     */
    public static Connection getConnection(String name, boolean useThreadMap) throws GVDBException
    {
        return JDBCConnectionBuilder.instance().intGetConnection(name, useThreadMap);
    }

    /**
     * @param name
     * @param conn
     * @throws GVDBException
     */
    public static void releaseConnection(String name, Connection conn) throws GVDBException
    {
        releaseConnection(name, conn, false);
    }

    /**
     * @param name
     * @param conn
     * @throws GVDBException
     */
    public static void releaseConnection(String name, Connection conn, boolean useThreadMap) throws GVDBException
    {
        JDBCConnectionBuilder.instance().intReleaseConnection(name, conn, useThreadMap);
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if (event.getFile().equals(CONFIGURATION_FILE) && (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED)) {
            destroy();
            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    try {
                        init();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            };

            BaseThread bt = new BaseThread(rr, "Config reloader for JDBCConnectionBuilder");
            bt.setDaemon(true);
            bt.start();
        }
    }

    /**
     * @see it.greenvulcano.event.util.shutdown.ShutdownEventListener#shutdownStarted(it.greenvulcano.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public synchronized void shutdownStarted(ShutdownEvent event)
    {
        destroy();
    }

    /**
     * 
     */
    private void destroy() {
        for (Iterator<ConnectionBuilder> iterator = connBuilders.values().iterator(); iterator.hasNext();) {
            ConnectionBuilder cBuilder = iterator.next();
            cBuilder.destroy();
        }
        connBuilders.clear();
    }

    private Connection intGetConnection(String name, boolean useThreadMap) throws GVDBException
    {
        try {
            Map<String, Connection> thConns = null;
            if (useThreadMap) {
                thConns = (Map<String, Connection>) ThreadMap.get(THMAP_KEY);
                if (thConns == null) {
                    thConns = new HashMap<String, Connection>();
                    ThreadMap.put(THMAP_KEY, thConns);
                }
                Connection conn = thConns.get(name);
                //if ((conn != null) && conn.isValid(0)) {
                if (conn != null) {
                    return conn;
                }
            }
            ConnectionBuilder cBuilder = connBuilders.get(name);
            if (cBuilder == null) {
                synchronized (this) {
                    if (cBuilder == null) {
                        logger.warn("ConnectionBuilder[" + name
                                + "] not found. Creating an automatic local DataSourceConnectionBuilder");
                        cBuilder = new DataSourceConnectionBuilder();
                        ((DataSourceConnectionBuilder) cBuilder).init(name);
                        connBuilders.put(name, cBuilder);
                    }
                }
            }
            Connection conn = cBuilder.getConnection();
            if (useThreadMap) {
                thConns.put(name, conn);
            }
            return conn;
        }
        catch (GVDBException exc) {
            logger.error("JDBCConnectionBuilder - Error while creating Connection[" + name + "]", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("JDBCConnectionBuilder - Error while creating Connection[" + name + "]", exc);
            throw new GVDBException("Error while creating Connection[" + name + "]", exc);
        }
    }

    private void intReleaseConnection(String name, Connection conn, boolean useThreadMap) throws GVDBException
    {
        Map<String, Connection> thConns = null;
        boolean foundInMap = false;
        if (useThreadMap) {
            thConns = (Map<String, Connection>) ThreadMap.get(THMAP_KEY);
            if (thConns != null) {
                foundInMap = thConns.remove(name) != null;
                if (thConns.isEmpty()) {
                    ThreadMap.remove(THMAP_KEY);
                }
            }
        }
        ConnectionBuilder cBuilder = connBuilders.get(name);
        if (cBuilder == null) {
            logger.error("ConnectionBuilder not found for [" + name + "] - Forced close connection");
            if (conn != null) {
                try {
                    conn.close();
                }
                catch (Exception exc) {
                    if (!useThreadMap || foundInMap) {
                        logger.error("JDBCConnectionBuilder - Error while closing Connection[" + name + "]: [" + conn
                                + "]", exc);
                    }
                }
            }
            return;
        }
        cBuilder.releaseConnection(conn);
    }

}
