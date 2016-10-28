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
package it.greenvulcano.gvesb.datahandling.utils;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadMap;
import it.greenvulcano.util.txt.TextUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Mar 30, 2010
 * @author nunzio
 *
 *
 */
public abstract class AbstractRetriever
{
    private static final Logger                    logger            = org.slf4j.LoggerFactory.getLogger(AbstractRetriever.class);

    private PreparedStatement                      internalStmt      = null;

    private String                                 lastMethod        = null;

    private Connection                             internalConn      = null;

    private Node                                   configurationNode = null;

    private final Map<String, String>              method2DataRetr   = new HashMap<String, String>();
    private final Map<String, List<String>>        method2Signature  = new HashMap<String, List<String>>();
    private final Map<String, Map<String, String>> method2Cache      = new HashMap<String, Map<String, String>>();

    /**
     * @return the data retriever function
     */
    protected abstract String getDataRetriever();

    /**
     * @param param
     * @return the data retriever function
     * @see #getDataRetriever()
     */
    protected String getDataRetriever(String param)
    {
        return getDataRetriever();
    }

    /**
     * @param method
     * @param params
     * @return the data retriever function
     * @throws Exception
     */
    protected String getDataRetriever(String method, Map<String, Object> params) throws Exception
    {
        if (method2DataRetr.isEmpty()) {
            initStatementMap();
        }
        String expandedFunction = PropertiesHandler.expand(method2DataRetr.get(method), params, null, getConnection());
        logger.debug("Expanded Function [" + method + "]: " + expandedFunction);
        return expandedFunction;
    }

    /**
     * @param method
     * @param paramList
     * @return the data retriever function
     * @throws Exception
     */
    protected String getDataRetriever(String method, List<String> paramList) throws Exception
    {
        if (method2DataRetr.isEmpty()) {
            initStatementMap();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> signature = method2Signature.get(method);
        if (signature == null) {
            throw new IllegalArgumentException("DataRetriever method <" + method + "> not configured.");
        }
        for (int i = 0; i < paramList.size(); i++) {
            params.put(signature.get(i), paramList.get(i));
        }
        String expandedFunction = PropertiesHandler.expand(method2DataRetr.get(method), params, null, getConnection());
        logger.debug("Expanded Function [" + method + "]: " + expandedFunction);
        return expandedFunction;
    }

    /**
     * @param method
     * @param paramList
     * @return the method parameters map
     * @throws Exception
     */
    protected Map<String, Object> getMethodParamMap(String method, List<String> paramList) throws Exception
    {
        if (method2DataRetr.isEmpty()) {
            initStatementMap();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> signature = method2Signature.get(method);
        for (int i = 0; i < paramList.size(); i++) {
            params.put(signature.get(i), paramList.get(i));
        }
        return params;
    }

    /**
     * @param method
     * @return
     */
    protected Map<String, String> getMethodCache(String method)
    {
        return method2Cache.get(method);
    }

    private static String THRMAP_KEY = "ABSTRACT_RETRIEVER_INSTANCES";

    /**
     * @return the generic retriever instance
     */
    public static GenericRetriever genericRetrieverInstance()
    {
        return (GenericRetriever) getRetrieverMap(null).get("GENERICRETRIEVER");
    }

    /**
     * @return the table retriever instance
     */
    public static TableRetriever tableRetrieverInstance()
    {
        return (TableRetriever) getRetrieverMap(null).get("TABLERETRIEVER");
    }

    /**
     * @return a JavaScript retriever instance
     */
    public static JavaScriptRetriever javaScriptRetrieverInstance()
    {
        return (JavaScriptRetriever) getRetrieverMap(null).get("JAVASCRIPTRETRIEVER");
    }

    /**
     * @param conn
     * @param configurationNode
     * @throws SQLException
     */
    public static void setAllConnection(Connection conn, Node configurationNode) throws SQLException
    {
        Map<String, AbstractRetriever> instances = getRetrieverMap(configurationNode);
        for (AbstractRetriever retr : instances.values()) {
            if (retr != null) {
                retr.setConnection(conn);
            }
        }
    }

    /**
     *
     */
    public static void cleanupAll()
    {
        Map<String, AbstractRetriever> instances = getRetrieverMap(null);
        for (AbstractRetriever retr : instances.values()) {
            if (retr != null) {
                retr.cleanup();
            }
        }
        ThreadMap.remove(THRMAP_KEY);
    }

    /**
     * @param conn
     * @throws SQLException
     */
    public void setConnection(Connection conn) throws SQLException
    {
        if (conn != null) {
            internalConn = conn;
            if (internalStmt != null) {
                try {
                    internalStmt.close();
                }
                catch (SQLException exc) {
                    // do nothing
                }
            }
            String stmt = getDataRetriever();
            if (stmt != null) {
                internalStmt = conn.prepareStatement(stmt);
            }
        }
    }

    /**
     * @return the internal connection
     */
    protected Connection getConnection()
    {
        return internalConn;
    }

    /**
     * @return the internal SQL {@link PreparedStatement}
     * @throws SQLException
     */
    protected PreparedStatement getInternalStmt() throws SQLException
    {
        lastMethod = null;
        if (internalConn == null) {
            throw new SQLException("Invalid Connection");
        }
        if (internalStmt == null) {
            internalStmt = internalConn.prepareStatement(getDataRetriever());
        }
        return internalStmt;
    }

    /**
     * @param param
     * @return the internal SQL {@link PreparedStatement}
     * @throws Exception
     */
    protected PreparedStatement getInternalStmt(String param) throws Exception
    {
        lastMethod = null;
        if (internalConn == null) {
            throw new SQLException("Connessione non valida!");
        }
        if (internalStmt == null) {
            internalStmt = internalConn.prepareStatement(getDataRetriever(param));
        }
        return internalStmt;
    }

    /**
     * @param method
     * @param params
     * @return the internal SQL {@link PreparedStatement}
     * @throws Exception
     */
    protected PreparedStatement getInternalStmt(String method, Map<String, Object> params) throws Exception
    {
        if (internalConn == null) {
            throw new SQLException("Invalid connection!");
        }
        if ((internalStmt != null) && (!method.equals(lastMethod))) {
            try {
                internalStmt.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            internalStmt = null;
        }
        if (internalStmt == null) {
            internalStmt = internalConn.prepareStatement(getDataRetriever(method, params));
        }
        return internalStmt;
    }

    /**
     * @param method
     * @param paramList
     * @return the internal SQL {@link PreparedStatement}
     * @throws Exception
     */
    protected PreparedStatement getInternalStmt(String method, List<String> paramList) throws Exception
    {
        if (internalConn == null) {
            throw new SQLException("Invalid connection!");
        }
        if ((internalStmt != null) && (!method.equals(lastMethod))) {
            try {
                internalStmt.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            internalStmt = null;
        }
        if (internalStmt == null) {
            internalStmt = internalConn.prepareStatement(getDataRetriever(method, paramList));
        }
        return internalStmt;
    }

    /**
     *
     */
    protected void cleanup()
    {
        if (internalStmt != null) {
            try {
                internalStmt.close();
            }
            catch (SQLException exc) {
                // do nothing
            }
        }
        internalConn = null;
        internalStmt = null;
        for (Map<String, String> cache : method2Cache.values()) {
            cache.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, AbstractRetriever> getRetrieverMap(Node configurationNode)
    {
        Map<String, AbstractRetriever> instances = (Map<String, AbstractRetriever>) ThreadMap.get(THRMAP_KEY);

        if (instances == null) {
            instances = new HashMap<String, AbstractRetriever>();
            AbstractRetriever retriever = new GenericRetriever();
            retriever.setConfigurationNode(configurationNode);
            instances.put("GENERICRETRIEVER", retriever);
            retriever = new JavaScriptRetriever();
            retriever.setConfigurationNode(configurationNode);
            instances.put("JAVASCRIPTRETRIEVER", retriever);
            retriever = new TableRetriever();
            retriever.setConfigurationNode(configurationNode);
            instances.put("TABLERETRIEVER", retriever);

            ThreadMap.put(THRMAP_KEY, instances);
        }

        return instances;
    }

    /**
     * @return the configuration node
     */
    protected Node getConfigurationNode()
    {
        return configurationNode;
    }

    /**
     * @param configurationNode
     *        the configuration node to set
     */
    protected void setConfigurationNode(Node configurationNode)
    {
        this.configurationNode = configurationNode;
    }

    private void initStatementMap() throws Exception
    {
        try {
            logger.debug("Listing DataRetriever BEGIN.");
            NodeList methNodes = XMLConfig.getNodeList(getConfigurationNode(),
                    "RetrieverConfig/*[@type='retriever' and @class='" + this.getClass().getName() + "']/DataRetriever");
            for (int i = 0; i < methNodes.getLength(); i++) {
                Node methNode = methNodes.item(i);
                String method = XMLConfig.get(methNode, "@method");
                List<String> signature = TextUtils.splitByStringSeparator(
                        XMLConfig.get(methNode, "@signature", "UNDEFINED"), ",");
                String dataR = XMLConfig.get(methNode, ".").trim();
                boolean cacheable = XMLConfig.getBoolean(methNode, "@cacheable", false);
                logger.debug("Adding DataRetriever for method [" + method + "]: [" + dataR + "]");
                method2DataRetr.put(method, dataR);
                logger.debug("Adding signature for method [" + method + "]: [" + signature + "]");
                method2Signature.put(method, signature);
                logger.debug("Setting cacheable for method [" + method + "]: [" + cacheable + "]");
                if (cacheable) {
                    method2Cache.put(method, new HashMap<String, String>());
                }
            }
            logger.debug("Listing DataRetriever END.");
        }
        catch (Exception exc) {
            method2DataRetr.clear();
            method2Signature.clear();
            throw exc;
        }
    }

}
