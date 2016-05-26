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
package it.greenvulcano.gvesb.j2ee.db;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.j2ee.db.connections.DataBaseConnection;
import it.greenvulcano.gvesb.j2ee.db.formatter.FormatterFactory;
import it.greenvulcano.gvesb.j2ee.db.formatter.ResponseFormatter;
import it.greenvulcano.gvesb.j2ee.db.resolver.ParamResolver;
import it.greenvulcano.gvesb.j2ee.db.resolver.ResolverFactory;
import it.greenvulcano.gvesb.j2ee.db.utils.ResultSetEnumeration;
import it.greenvulcano.gvesb.log.GVBufferDump;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class creates the connection object requested.
 *
 * The dataSource connection is for all client using an application server. The
 * JDBC connection is a generally connection that all client can use also
 * without the application server support.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class GVDBOperation
{
    private static Logger       logger             = org.slf4j.LoggerFactory.getLogger(GVDBOperation.class);

    /**
     * The parameter resolvers within the list extract data from the GVBuffer in
     * various ways and populate the query's parameters.
     */
    private List<ParamResolver> paramResolverList  = null;

    /**
     * The response formatter extracts data from the result set and set the
     * GVBuffer's fields.
     */
    private ResponseFormatter   responseFormatter  = null;

    /**
     * The interface for the connection classes.
     */
    private DataBaseConnection  dataBaseConnection = null;

    /**
     * The statement type to use. Callable or Prepared.
     */
    private String              statementType      = "";

    /**
     * The result set type type.
     */
    private String              rsType             = "";

    /**
     * The result set concurrency.
     */
    private String              rsConcurrency      = "";

    /**
     * The result set holdability.
     */
    private String              rsHoldability      = "";

    /**
     * To use the same connection for different statement or not.
     */
    private boolean             cacheConnection    = false;

    /**
     * The statement value inserted on the configuration file
     */
    private String              statementValue     = "";

    /**
     * The string value as key to find connection in cache
     */
    private String              keyConnection      = "";

    /**
     * Initialization method
     *
     * @param node
     *        the configuration db-call node
     * @throws GVDBException
     *         if an error occurred reading configuration file or creating
     *         objects.
     */
    public final void init(Node node) throws GVDBException
    {
        logger.debug("INIT GVDBOperation");
        String connectionClassName = "";

        try {
            Node statementNode = XMLConfig.getNode(node, "DBOperations/Statement");

            statementType = XMLConfig.get(statementNode, "@statementType");
            statementValue = XMLConfig.get(statementNode, "StatementValue");
            logger.debug("Statement value " + statementValue);

            if (XMLConfig.exists(statementNode, "ParamResolvers")) {
                NodeList paramResolverNodes = XMLConfig.getNodeList(statementNode,
                        "ParamResolvers/ParamResolver/*[@type='resolver']");
                paramResolverList = new ArrayList<ParamResolver>(paramResolverNodes.getLength());
                for (int i = 0; i < paramResolverNodes.getLength(); i++) {
                    Node paramResolverNode = paramResolverNodes.item(i);
                    ParamResolver paramResolver = ResolverFactory.create(paramResolverNode);
                    paramResolverList.add(paramResolver);
                    logger.debug("ParamResolver = " + paramResolver);
                }
            }
            else {
                logger.debug("ParamResolver list NOT configured");
            }

            rsType = XMLConfig.get(node, "DBOperations/@rsType");
            rsConcurrency = XMLConfig.get(node, "DBOperations/@rsConcurrency");
            rsHoldability = XMLConfig.get(node, "DBOperations/@rsHoldability");

            Node connectionNode = XMLConfig.getNode(node, "DBOperations/*[@type='db-connection']");
            logger.debug("connectionNode = " + connectionNode.getNodeName());
            keyConnection = XMLConfig.get(connectionNode, "@keyConnection");
            if (keyConnection != null) {
                cacheConnection = true;
            }
            logger.debug("CACHE CONNECTION ?" + cacheConnection);
            connectionClassName = XMLConfig.get(connectionNode, "@class");

            Class<?> connectionClass = Class.forName(connectionClassName);
            dataBaseConnection = (DataBaseConnection) connectionClass.newInstance();
            dataBaseConnection.init(connectionNode);
            dataBaseConnection.setLogger(GVDBOperation.logger);

            Node outputFormatNode = XMLConfig.getNode(node, "DBOperations/ResultSet/*[@type='formatter']");
            responseFormatter = FormatterFactory.create(outputFormatNode);
            logger.debug("INIT GVDBOperation responseFormatter = " + responseFormatter);
        }
        catch (XMLConfigException exc) {
            logger.error("INIT DBOperations - Error while accessing configuration info : ", exc);
            throw new GVDBException("J2EE_DB_CONFIGURATION_ERROR", new String[][]{{"msg",
                    "Error accessing configuration : "}}, exc);
        }
        catch (ClassNotFoundException exc) {
            logger.error("INIT DBOperations - Error getting the class for the given className : ", exc);
            throw new GVDBException("J2EE_CLASS_NOT_FOUND_ERROR", new String[][]{{"className", connectionClassName}},
                    exc);
        }
        catch (InstantiationException exc) {
            logger.error("INIT DBOperations - Error instantiating the requested class : ", exc);
            throw new GVDBException("J2EE_INSTANTIATION_ERROR", new String[][]{{"className", connectionClassName}}, exc);
        }
        catch (IllegalAccessException exc) {
            logger.error("INIT DBOperations - Error accessing at requested class : ", exc);
            throw new GVDBException("GV_GENERIC_ERROR", exc);
        }
    }

    /**
     * Perform SQL statement.
     *
     * @param gvBuffer
     *        input buffer.
     * @return The GVBuffer object
     * @throws GVDBException
     *         if errors occur.
     */
    public final GVBuffer performSQL(GVBuffer gvBuffer) throws GVDBException
    {
        Connection conn = null;
        Statement stmt = null;
        GVBufferDump dump = null;

        try {
            logger.debug("BEGIN - <" + gvBuffer.getSystem() + "> - <" + gvBuffer.getService() + ">");

            if (logger.isDebugEnabled()) {
                dump = new GVBufferDump(gvBuffer, false);
                logger.debug("INPUT GVBuffer: "+dump);
                
                dump = null;
            }
            if (cacheConnection) {
                logger.debug("KEY for chache connection = " + keyConnection);
                conn = dataBaseConnection.getConnection(keyConnection);
            }
            else {
                conn = dataBaseConnection.getConnection();
            }
            stmt = prepareStatementCall(conn, statementValue);
            callParamResolver(conn, stmt, gvBuffer);

            responseFormatter.execute(stmt, gvBuffer);
        }
        catch (SQLException exc) {
            gvBuffer.setRetCode(-1);
            logger.error("performSQL - Error while performing SQL", exc);

            throw new GVDBException("GV_DB_SQL_ERROR", new String[][]{{"statement", ""}}, exc);
        }
        finally {
            freeResource(conn, stmt);
        }

        if (logger.isDebugEnabled()) {
            dump = new GVBufferDump(gvBuffer, false);
            logger.debug("OUTPUT GVBuffer: "+dump);
           
            dump = null;
        }

        logger.debug("END - <" + gvBuffer.getSystem() + "> - <" + gvBuffer.getService() + ">");
        return gvBuffer;
    }

    /**
     *
     */
    private void callParamResolver(Connection conn, Statement stmt, GVBuffer gvBuffer) throws GVDBException
    {
        if (paramResolverList != null) {
            logger.debug("Resolve parameters....");
            for (ParamResolver paramResolver : paramResolverList) {
                logger.debug("call resolving: " + paramResolver);
                paramResolver.resolve(stmt, gvBuffer);
            }
        }
        else {
            logger.debug("Resolve parameters not required....");
        }
    }

    /**
     * Release resource.
     *
     * @param connection
     *        the connection at database
     * @param stmt
     *        the statement
     * @throws GVDBException
     *         if an SQL error occurred
     */
    private final void freeResource(Connection connection, Statement stmt) throws GVDBException
    {
        logger.debug("free resources...");

        try {
            if (stmt != null) {
                logger.debug("close Statement....");
                stmt.close();
            }
        }
        catch (Exception exc) {
            logger.error("freeResource - Error releasing resources ", exc);
            throw new GVDBException("J2EE_DB_CLOSE_STATEMENT_ERROR", exc);
        }
        finally {
            if (!cacheConnection) {
                if (connection != null) {
                    logger.debug("close connection....");
                    dataBaseConnection.releaseConnection(connection);
                }
            }
        }
    }

    /**
     * This method isa call form GreenVulcano ESB core when the flow is
     * terminated. Only now the connection is closed and only if a cache
     * connection is requestes otherwise the connection is already closed by
     * this object in freeResource method.
     *
     * @throws GVDBException
     *
     */
    public void cleanUp() throws GVDBException
    {
        if (cacheConnection) {
            logger.debug("close connection....");
            dataBaseConnection.releaseConnection(keyConnection);
        }
    }

    /**
     * Manage the result set parameters.
     *
     * @param conn
     *        the connection object
     * @return the statement prepares
     * @throws SQLException
     *         if an error occurred
     */
    private PreparedStatement prepareStatementCall(Connection conn, String statementValue) throws SQLException
    {
        PreparedStatement stmt = null;
        if ((rsType != null) && (rsConcurrency != null)) {
            if (rsHoldability != null) {
                if (statementType != null) {
                    if (statementType.equals("CallableStatement")) {
                        stmt = conn.prepareCall(statementValue, ResultSetEnumeration.valueOf(rsType).getId(),
                                ResultSetEnumeration.valueOf(rsConcurrency).getId(), ResultSetEnumeration.valueOf(
                                        rsHoldability).getId());
                    }
                    else if (statementType.equals("PreparedStatement")) {
                        stmt = conn.prepareStatement(statementValue, ResultSetEnumeration.valueOf(rsType).getId(),
                                ResultSetEnumeration.valueOf(rsConcurrency).getId(), ResultSetEnumeration.valueOf(
                                        rsHoldability).getId());
                    }
                }
            }
            else {
                if (statementType != null) {
                    if (statementType.equals("CallableStatement")) {
                        stmt = conn.prepareCall(statementValue, ResultSetEnumeration.valueOf(rsType).getId(),
                                ResultSetEnumeration.valueOf(rsConcurrency).getId());
                    }
                    else if (statementType.equals("PreparedStatement")) {
                        stmt = conn.prepareStatement(statementValue, ResultSetEnumeration.valueOf(rsType).getId(),
                                ResultSetEnumeration.valueOf(rsConcurrency).getId());
                    }
                }
            }
        }
        else {
            if (statementType != null) {
                if (statementType.equals("CallableStatement")) {
                    stmt = conn.prepareCall(statementValue);
                }
                else if (statementType.equals("PreparedStatement")) {
                    stmt = conn.prepareStatement(statementValue);
                }
            }
        }

        return stmt;
    }

    /**
     * Set the logger.
     *
     * @param log
     *        the logger object
     */
    public final void setLogger(Logger log)
    {
        GVDBOperation.logger = log;
    }
}
