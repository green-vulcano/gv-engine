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
package it.greenvulcano.gvesb.datahandling.dbobuilder;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DHResult;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBO;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.datahandling.utils.AbstractRetriever;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>DBOBuilder</code> is the class that holds the creation logic of IDBO
 * objects. Its role is to invoke the DTE to make transformations and
 * initializing the IDBO objects that will physically manipulate the DB. It's
 * based on a connection requested to the <code>JdbcDataBaseConnection</code>
 * and released at the end of operations. This component, to handle the
 * concurrent behavior, is designed as thread-safe module.
 * 
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DBOBuilder implements IDBOBuilder
{
	private class MergeInfo {
        String source;
        String xpathSrc;
        String xpathDest;

        /**
         * @param source
         * @param xpathSrc
         * @param xpathDest
         */
        public MergeInfo(String source, String xpathSrc, String xpathDest) {
            this.source = source;
            this.xpathSrc = xpathSrc;
            this.xpathDest = xpathDest;
        }

        @Override
        public String toString() {
            return "Source: " + source + " - xpathSrc: " + xpathSrc + " - xpathDest: " + xpathDest;
        }
    }

    private Vector<IDBO>              dboList            = null;
    private Map<String, IDBO>         dboOutputMap       = null;

    /**
     * Configured properties to eventually overwrite in the service call.
     */
    private Map<String, String>       baseProps;

    private boolean                   resolveMetadata;

    private int                       internalIdx;

    private Node                      configurationNode  = null;

    private DTEController             dteController      = null;

    private String                    jdbcConnectionName = null;

    private boolean                   transacted         = true;
    private boolean                   isXA               = false;

    private String                    serviceName        = null;

    private String                    outputDataName     = null;
    private String                    statsDataName      = null;
    private final static String       ALL_STATS          = "ALL";

    private static final Logger       logger             = org.slf4j.LoggerFactory.getLogger(DBOBuilder.class);

    private final Map<String, Object> dataCache          = new HashMap<String, Object>();
    private List<MergeInfo>           mergeList          = new Vector<MergeInfo>();
    
    private int                       makeDump           = DUMP_TEXT;

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node builder) throws DataHandlerException
    {
        dboList = new Vector<IDBO>();
        dboOutputMap = new HashMap<String, IDBO>();
        baseProps = new HashMap<String, String>();
        String dboClassName = "";

        NMDC.push();
        try {
            NMDC.put("DH_SERVICE", "");
            logger.debug("DBOBuilder initialized with node\n[" + XMLUtils.serializeDOM_S(builder) + "].");
            String sDump = XMLConfig.get(builder, "@make-dump", "text");
            if (sDump.equals("none")) {
                makeDump = DUMP_NONE;
            }
            else if (sDump.equals("hex")) {
                makeDump = DUMP_HEX;
            }
            else {
                makeDump = DUMP_TEXT;
            }
            serviceName = XMLConfig.get(builder, "@name");
            NMDC.put("DH_SERVICE", serviceName);
            jdbcConnectionName = XMLConfig.get(builder, "@jdbc-connection-name");
            logger.debug("Connection = " + jdbcConnectionName);
            transacted = XMLConfig.getBoolean(builder, "@transacted", true);
            logger.debug("Execute in transaction: " + transacted + ".");
            isXA = XMLConfig.getBoolean(builder, "@isXA", false);
            logger.debug("Is XA: " + isXA + ".");

            logger.debug("Listing for DBOs.");
            NodeList dbosNodes = XMLConfig.getNodeList(builder, "*[@type='dbo']");
            IDBO idbo = null;
            for (int i = 0; i < dbosNodes.getLength(); i++) {
                Node dboNode = dbosNodes.item(i);
                dboClassName = XMLConfig.get(dboNode, "@class");

                idbo = (IDBO) Class.forName(dboClassName).newInstance();
                idbo.init(dboNode);
                idbo.setServiceName(serviceName);
                idbo.setTransacted(transacted);
                idbo.setJdbcConnectionName(jdbcConnectionName);
                dboList.add(idbo);
                dboOutputMap.put(idbo.getOutputDataName(), idbo);
                logger.debug("Added a IDBO class [" + dboClassName + "].");
            }

            outputDataName = XMLConfig.get(builder, "@output-data", idbo.getOutputDataName());
            // stats-data = ALL ensure to add all the statistics by DHResult
            statsDataName = XMLConfig.get(builder, "@output-stats", outputDataName);

            resolveMetadata = XMLConfig.getBoolean(builder, "DHVariables/@resolve-metadata-on-call", true);
            NodeList nlv = XMLConfig.getNodeList(builder, "DHVariables/DHVariable");
            if (nlv != null) {
                for (int i = 0; i < nlv.getLength(); i++) {
                    Node nv = nlv.item(i);
                    baseProps.put(XMLConfig.get(nv, "@name"),
                            XMLConfig.get(nv, "@value", XMLConfig.get(nv, ".", "")).trim());
                }
            }
            
            NodeList mergeNodes = XMLConfig.getNodeList(builder, "XMLMerge/MergeInfo");
            for (int i = 0; i < mergeNodes.getLength(); i++) {
                Node mergeNode = mergeNodes.item(i);
                MergeInfo mergeInfo = new MergeInfo(XMLConfig.get(mergeNode, "@source"), XMLConfig.get(mergeNode, "@xpath-source"),
                		XMLConfig.get(mergeNode, "@xpath-dest"));
                mergeList.add(mergeInfo);
                logger.debug("Added a MergeInfo[" + mergeInfo + "].");
            }
        }
        catch (XMLConfigException exc) {
            logger.error("Error reading configuration", exc);
            throw new DataHandlerException("Error reading configuration", exc);
        }
        catch (IllegalAccessException exc) {
            logger.error("Error accessing IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error accessing IDBO class '" + dboClassName + "'", exc);
        }
        catch (InstantiationException exc) {
            logger.error("Error instantiating IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error instantiating IDBO class '" + dboClassName + "'", exc);
        }
        catch (ClassNotFoundException exc) {
            logger.error("Error creating IDBO class '" + dboClassName + "'", exc);
            throw new DataHandlerException("Error creating IDBO class '" + dboClassName + "'", exc);
        }
        catch (Exception exc) {
            logger.error("Unhandled exception in IDBO initialization", exc);
            throw new DataHandlerException("Unhandled exception in IDBO initialization", exc);
        }
        finally {
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#XML2DB(java.lang.String,
     *      byte[], java.util.Map)
     */
    @Override
    public void XML2DB(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", serviceName);
        logger.debug("Start executing XML2DB [" + operation + "]\n\tParams    : " + localParams.toString());
        if (logger.isDebugEnabled() && (file != null) && (makeDump != DUMP_NONE)) {
            if (makeDump == DUMP_HEX) {
                logger.debug("Input data: [\n" + new Dump(file, -1) + "\n].");
            }
            else {
                logger.debug("Input data: [\n" + new String(file) + "\n].");
            }
        }
        internalIdx = 0;
        Connection conn = null;
        String intConnName = null;
        try {
            logger.debug("Searching for a new available connection named [" + jdbcConnectionName + "].");
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.debug("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = jdbcConnectionName;
            }

            conn = JDBCConnectionBuilder.getConnection(intConnName);
            if (transacted && !isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, configurationNode);

            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), serviceName, logger);

                IDBO idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    Object xmlFile = transform(idbo, file, localParams);
                    if (xmlFile != null) {
                        if (logger.isDebugEnabled() && (makeDump != DUMP_NONE)) {
                            if (xmlFile instanceof byte[]) {
                                if (makeDump == DUMP_HEX) {
                                    logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile) + "\n].");
                                }
                            }
                            else if (xmlFile instanceof Node) {
                                try {
                                    logger.debug("Transformation output: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                            }
                        }
                    }
                    logger.debug("Start executing of IDBO [" + idbo.toString() + "].");
                    idbo.execute(xmlFile, conn, localParams, null);
                    logger.debug("End executing of IDBO [" + idbo.toString() + "]. Execution time: "
                            + getPartialTime(start));
                }
                finally {
                    NMDC.pop();
                }
            }
            if (transacted && !isXA) {
                logger.debug("Committing XML2DB [" + operation + "].");
                conn.commit();
            }
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back XML2DB [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back XML2DB [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("End executing XML2DB [" + operation + "]. Execution time: " + getPartialTime(start));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#DB2XML(java.lang.String,
     *      byte[], java.util.Map)
     */
    @Override
    public byte[] DB2XML(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", serviceName);
        logger.debug("Start executing DB2XML [" + operation + "]\n\tParams    : " + localParams.toString());
        internalIdx = 0;
        Connection conn = null;
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        String intConnName = null;
        try {
            logger.debug("Searching for a new available connection named [" + jdbcConnectionName + "].");
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.debug("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = jdbcConnectionName;
            }
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            if (transacted) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, configurationNode);

            IDBO idbo = firstDBO();
            dataCache.put(idbo.getInputDataName(), file);
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    if (idbo.getForcedMode().equals(IDBO.MODE_XML2DB)) {
                        Object xmlFile = transform(idbo, dataCache.get(idbo.getInputDataName()), localParams);
                        if (xmlFile != null) {
                            dataCache.put(idbo.getOutputDataName(), xmlFile);
                            if (logger.isDebugEnabled() && (makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (makeDump == DUMP_HEX) {
                                        logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1)
                                                + "\n].");
                                    }
                                    else {
                                        logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile)
                                                + "\n].");
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced XML2DB mode.");
                        idbo.execute(in, conn, localParams, null);
                        logger.debug("End executing IDBO [" + idbo.toString()
                                + "] in forced XML2DB mode. Execution time: " + getPartialTime(start));
                    }
                    else {
                        out = new ByteArrayOutputStream();
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in normal mode.");
                        idbo.execute(out, conn, localParams);
                        logger.debug("End executing IDBO [" + idbo.toString() + "] in normal mode. Execution time: "
                                + getPartialTime(start));
                        byte[] output = out.toByteArray();
                        if (logger.isDebugEnabled() && (output != null) && (makeDump != DUMP_NONE)) {
                            if (makeDump == DUMP_HEX) {
                                logger.debug("Received data from DB: [\n" + new Dump(output, -1) + "\n].");
                            }
                            else {
                                logger.debug("Received data from DB: [\n" + new String(output) + "\n].");
                            }
                        }
                        Object xmlFile = transform(idbo, output, localParams);
                        dataCache.put(idbo.getOutputDataName(), xmlFile);
                    }
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException exc) {
                            // Nothing to do
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (IOException exc) {
                            // Nothing to do
                        }
                    }
                    NMDC.pop();
                }
            }
            if (transacted && !isXA) {
                logger.debug("Committing DB2XML [" + operation + "].");
                conn.commit();
            }
            byte[] xmlFile = null;
            
            if (mergeList.size() < 2) {
                xmlFile = (byte[]) dataCache.get(outputDataName);
            }
            else {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    MergeInfo mergeDest = mergeList.get(0);
                    Document dest = (Document) parser.parseObject(dataCache.get(mergeDest.source), false, true);
                    for (int i = 1; i < mergeList.size(); i++) {
                        MergeInfo mergeInfo = mergeList.get(i);
                        Object srcO = dataCache.get(mergeInfo.source);
                        if (srcO != null) {
                            Document src = (Document) parser.parseObject(srcO, false, true);
                            NodeList sources = parser.selectNodeList(src, mergeInfo.xpathSrc);
                            if (sources.getLength() > 0) {
                                Node destNode = parser.selectSingleNode(dest, mergeInfo.xpathDest);
                                for (int j = 0; j < sources.getLength(); ++j) {
                                    Node node = sources.item(j);
                                    node = dest.importNode(node, true);
                                    destNode.appendChild(node);
                                }
                            }
                        }
                    }

                    xmlFile = parser.serializeDOMToByteArray(dest);
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
            }
            
            if (logger.isDebugEnabled() && (xmlFile != null) && (makeDump != DUMP_NONE)) {
                if (makeDump == DUMP_HEX) {
                    logger.debug("Returning data: [\n" + new Dump(xmlFile, -1) + "\n].");
                }
                else {
                    logger.debug("Returning data: [\n" + new String(xmlFile) + "\n].");
                }
            }
            return xmlFile;
        }
        catch (SQLException exc) {
            logger.error("Unhandled SQL exception.", exc);
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back DB2XML [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back DB2XML [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.warn("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("End executing DB2XML [" + operation + "]. Execution time: " + getPartialTime(start));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#CALL(java.lang.String,
     *      byte[], java.util.Map)
     */
    @Override
    public byte[] CALL(String operation, byte[] file, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", serviceName);
        logger.debug("Start executing CALL [" + operation + "]\n\tParams    :" + localParams.toString());
        if (logger.isDebugEnabled() && (file != null) && (makeDump != DUMP_NONE)) {
            if (makeDump == DUMP_HEX) {
                logger.debug("Input data: [\n" + new Dump(file, -1) + "\n].");
            }
            else {
                logger.debug("Input data: [\n" + new String(file) + "\n].");
            }
        }
        internalIdx = 0;
        Connection conn = null;
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        String intConnName = null;
        try {
            logger.debug("Searching for a new available connection named [" + jdbcConnectionName + "].");
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.debug("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = jdbcConnectionName;
            }
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            if (transacted && !isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, configurationNode);

            IDBO idbo = firstDBO();
            dataCache.put(idbo.getInputDataName(), file);
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());

                    Object xmlFile = transform(idbo, file, localParams);
                    if (xmlFile != null) {
                        dataCache.put(idbo.getOutputDataName(), xmlFile);
                        if (logger.isDebugEnabled() && (makeDump != DUMP_NONE)) {
                            if (xmlFile instanceof byte[]) {
                                if (makeDump == DUMP_HEX) {
                                    logger.debug("Transformation output: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + new String((byte[]) xmlFile) + "\n].");
                                }
                            }
                            else if (xmlFile instanceof Node) {
                                try {
                                    logger.debug("Transformation output: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile)
                                            + "\n].");
                                }
                                catch (Exception exc) {
                                    logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                }
                            }
                            else {
                                logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                            }
                        }
                    }
                    out = new ByteArrayOutputStream();

                    logger.debug("Start executing IDBO [" + idbo.toString() + "].");
                    idbo.execute(in, out, conn, localParams);
                    logger.debug("End executing IDBO [" + idbo.toString() + "]. Execution time: "
                            + getPartialTime(start));

                    byte[] output = out.toByteArray();
                    xmlFile = output;
                    dataCache.put(idbo.getOutputDataName(), xmlFile);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException exc) {
                            // Nothing to do
                        }
                    }
                    NMDC.pop();
                }
            }
            if (transacted && !isXA) {
                logger.debug("Committing CALL [" + operation + "].");
                conn.commit();
            }
            byte[] xmlFile = (byte[]) dataCache.get(outputDataName);
            if (logger.isDebugEnabled() && (xmlFile != null) && (makeDump != DUMP_NONE)) {
                if (makeDump == DUMP_HEX) {
                    logger.debug("Returning data: [\n" + new Dump(xmlFile, -1) + "\n].");
                }
                else {
                    logger.debug("Returning data: [\n" + new String(xmlFile) + "\n].");
                }
            }
            return xmlFile;
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back CALL [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                        logger.warn("Rolling-back CALL [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("End executing CALL [" + operation + "]. Execution time: " + getPartialTime(start));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#EXECUTE(java.lang.String,
     *      java.lang.Object, java.util.Map)
     */
    @Override
    public DHResult EXECUTE(String operation, Object object, Map<String, Object> params) throws DataHandlerException,
            InterruptedException {
        long start = System.currentTimeMillis();
        Map<String, Object> localParams = buildProps(params);
        NMDC.push();
        NMDC.put("DH_SERVICE", serviceName);
        logger.debug("Start executing EXECUTE [" + operation + "]\n\tParams    : " + localParams.toString());
        if (logger.isDebugEnabled() && (object != null) && (makeDump != DUMP_NONE)) {
            if (object instanceof byte[]) {
                if (makeDump == DUMP_HEX) {
                    logger.debug("Input data: [\n" + new Dump((byte[]) object, -1) + "\n].");
                }
                else {
                    logger.debug("Input data: [\n" + new String((byte[]) object) + "\n].");
                }
            }
            else if (object instanceof Node) {
                try {
                    logger.debug("Input data: [\n" + XMLUtils.serializeDOM_S((Node) object) + "\n].");
                }
                catch (Exception exc) {
                    logger.debug("Input data: [\nDUMP ERROR!!!!!\n].", exc);
                }
            }
            else {
                logger.debug("Input data: [\n" + object.toString() + "\n].");
            }
        }
        internalIdx = 0;
        Connection conn = null;
        ByteArrayOutputStream out = null;
        String intConnName = null;
        try {
            logger.debug("Searching for a new available connection named [" + jdbcConnectionName + "].");
            intConnName = (String) localParams.get(DBO_JDBC_CONNECTION_NAME);
            if ((intConnName != null) && !"".equals(intConnName) && !"NULL".equals(intConnName)) {
                logger.debug("Overwriting default Connection with: " + intConnName);
            }
            else {
                intConnName = jdbcConnectionName;
            }
            conn = JDBCConnectionBuilder.getConnection(intConnName);
            if (transacted && !isXA) {
                conn.setAutoCommit(false);
            }

            // Static utility classes initialization
            AbstractRetriever.setAllConnection(conn, configurationNode);

            IDBO idbo = firstDBO();
            DHResult dhr = idbo.getExecutionResult();
            dhr.setData(object);
            dataCache.put(idbo.getInputDataName(), dhr);
            while (hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), serviceName, logger);
                idbo = nextDBO();
                NMDC.push();
                try {
                    NMDC.put("DH_DBO", idbo.getName());
                    if (idbo.getForcedMode().equals(IDBO.MODE_XML2DB)) {
                        dhr = (DHResult) dataCache.get(idbo.getInputDataName());
                        if (dhr == null) {
                            dhr = idbo.getExecutionResult();
                            dhr.setData(object);
                        }
                        Object xmlFile = transform(idbo, dhr.getData(), localParams);
                        dhr = idbo.getExecutionResult();
                        dhr.setData(xmlFile);
                        dataCache.put(idbo.getOutputDataName(), dhr);
                        if (xmlFile != null) {
                            if (logger.isDebugEnabled() && (makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (makeDump == DUMP_HEX) {
                                        logger.debug("Input data: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                    }
                                    else {
                                        logger.debug("Input data: [\n" + new String((byte[]) xmlFile) + "\n].");
                                    }
                                }
                                else if (xmlFile instanceof Node) {
                                    try {
                                        logger.debug("Transformation output: [\n"
                                                + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                                    }
                                    catch (Exception exc) {
                                        logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].");
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced XML2DB mode.");
                        idbo.execute(xmlFile, conn, localParams, object);
                        logger.debug("End executing IDBO [" + idbo.toString()
                                + "] in forced XML2DB mode. Execution time: " + getPartialTime(start));
                    }
                    else if (idbo.getForcedMode().equals(IDBO.MODE_DB2XML)) {
                        out = new ByteArrayOutputStream();
                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in forced DB2XML mode.");
                        idbo.execute(out, conn, localParams);
                        logger.debug("End executing IDBO [" + idbo.toString()
                                + "] in forced DB2XML mode. Execution time: " + getPartialTime(start));
                        byte[] output = out.toByteArray();
                        if (logger.isDebugEnabled() && (output != null) && (makeDump != DUMP_NONE)) {
                            if (makeDump == DUMP_HEX) {
                                logger.debug("Received data from DB: [\n" + new Dump(output, -1) + "\n].");
                            }
                            else {
                                logger.debug("Received data from DB: [\n" + new String(output) + "\n].");
                            }
                        }
                        Object xmlFile = transform(idbo, output, localParams);
                        dhr = idbo.getExecutionResult();
                        dhr.setData(xmlFile);
                        dataCache.put(idbo.getOutputDataName(), dhr);
                    }
                    else {
                        dhr = (DHResult) dataCache.get(idbo.getInputDataName());
                        if (dhr == null) {
                            dhr = idbo.getExecutionResult();
                            dhr.setData(object);
                        }
                        Object xmlFile = transform(idbo, dhr.getData(), localParams);
                        dhr = idbo.getExecutionResult();
                        dhr.setData(xmlFile);
                        dataCache.put(idbo.getOutputDataName(), dhr);
                        if (xmlFile != null) {
                            if (logger.isDebugEnabled() && (makeDump != DUMP_NONE)) {
                                if (xmlFile instanceof byte[]) {
                                    if (makeDump == DUMP_HEX) {
                                        logger.debug("Input data: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                                    }
                                    else {
                                        logger.debug("Input data: [\n" + new String((byte[]) xmlFile) + "\n].");
                                    }
                                }
                                else if (xmlFile instanceof Node) {
                                    try {
                                        logger.debug("Transformation output: [\n"
                                                + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                                    }
                                    catch (Exception exc) {
                                        logger.debug("Transformation output: [\nDUMP ERROR!!!!!\n].", exc);
                                    }
                                }
                                else {
                                    logger.debug("Transformation output: [\n" + xmlFile + "\n].");
                                }
                            }
                        }
                        out = new ByteArrayOutputStream();

                        logger.debug("Start executing IDBO [" + idbo.toString() + "] in normal mode.");
                        idbo.execute(xmlFile, out, conn, localParams);
                        logger.debug("End executing IDBO [" + idbo.toString() + "] in normal mode. Execution time: "
                                + getPartialTime(start));

                        byte[] output = out.toByteArray();
                        xmlFile = output;
                        dhr = idbo.getExecutionResult();
                        dhr.setData(xmlFile);
                        dataCache.put(idbo.getOutputDataName(), dhr);
                    }
                }
                finally {
                    if (out != null) {
                        try {
                            out.close();
                        }
                        catch (IOException exc) {
                            // Nothing to do
                        }
                    }
                    NMDC.pop();
                }
            }
            if (transacted && !isXA) {
                logger.debug("Committing EXECUTE [" + operation + "].");
                conn.commit();
            }
            dhr = (DHResult) dataCache.get(outputDataName);
            if (!dboOutputMap.get(outputDataName).isReturnData()) {
                dhr.setData(null);
            }
            Object xmlFile = dhr.getData();

            // Manage statistics (if the @output-stats attribute is set)
            if (statsDataName != null) {
                if (statsDataName.equals(ALL_STATS)) {

                    // statistics from all DBOs...
                    internalIdx = 0;

                    DHResult _dhr = null;
                    long readAll = 0;
                    long insertAll = 0;
                    long updateAll = 0;
                    long discardAll = 0;
                    List<DiscardCause> discardCause = new ArrayList<DiscardCause>();
                    long totalAll = 0;
                    while (hasNext()) {
                        idbo = nextDBO();
                        _dhr = (DHResult) dataCache.get(idbo.getOutputDataName());

                        readAll += _dhr.getRead();
                        insertAll += _dhr.getInsert();
                        updateAll += _dhr.getUpdate();
                        discardAll += _dhr.getDiscard();
                        discardCause.addAll(_dhr.getDiscardCauseList());
                        totalAll += _dhr.getTotal();
                    }
                    _dhr = null;
                    dhr.setRead(readAll);
                    dhr.setInsert(insertAll);
                    dhr.setUpdate(updateAll);
                    dhr.setDiscard(discardAll);
                    dhr.setDiscardCauseList(discardCause);
                    dhr.setTotal(totalAll);
                }
                else if (!statsDataName.equals(outputDataName)) {
                    // You need statistics from a different IDBO
                    DHResult _dhr = (DHResult) dataCache.get(statsDataName);

                    dhr.setRead(_dhr.getRead());
                    dhr.setInsert(_dhr.getInsert());
                    dhr.setUpdate(_dhr.getUpdate());
                    dhr.setTotal(_dhr.getTotal());

                    _dhr = null;

                }
            }

            if (mergeList.size() >1) {
                XMLUtils parser = null;
                try {
                    parser = XMLUtils.getParserInstance();
                    MergeInfo mergeDest = mergeList.get(0);
                    Document dest = (Document) parser.parseObject(((DHResult)dataCache.get(mergeDest.source)).getData(), false, true);
                    for (int i = 1; i < mergeList.size(); i++) {
                        MergeInfo mergeInfo = mergeList.get(i);
                        Object srcO = ((DHResult) dataCache.get(mergeInfo.source)).getData();
                        if (srcO != null) {
                            Document src = (Document) parser.parseObject(srcO, false, true);
                            NodeList sources = parser.selectNodeList(src, mergeInfo.xpathSrc);
                            if (sources.getLength() > 0) {
                                Node destNode = parser.selectSingleNode(dest, mergeInfo.xpathDest);
                                for (int j = 0; j < sources.getLength(); ++j) {
                                    Node node = sources.item(j);
                                    node = dest.importNode(node, true);
                                    destNode.appendChild(node);
                                }
                            }
                        }
                    }

                    xmlFile = dest;
                }
                finally {
                    XMLUtils.releaseParserInstance(parser);
                }
                dhr.setData(xmlFile);
            }
            
            if (logger.isDebugEnabled() && (xmlFile != null) && (makeDump != DUMP_NONE)) {
                if (xmlFile instanceof byte[]) {
                    if (makeDump == DUMP_HEX) {
                        logger.debug("Returning data: [\n" + new Dump((byte[]) xmlFile, -1) + "\n].");
                    }
                    else {
                        logger.debug("Returning data: [\n" + new String((byte[]) xmlFile) + "\n].");
                    }
                }
                else if (xmlFile instanceof Node) {
                    try {
                        logger.debug("Returning data: [\n" + XMLUtils.serializeDOM_S((Node) xmlFile) + "\n].");
                    }
                    catch (Exception exc) {
                        logger.debug("Returning data: [\nDUMP ERROR!!!!!\n].", exc);
                    }
                }
                else {
                    logger.debug("Returning data: [\n" + xmlFile + "\n].");
                }
            }
            return dhr;
        }
        catch (SQLException exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                    	logger.warn("Rolling-back EXECUTE [" + operation + "].");
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            OracleExceptionHandler.handleSQLException(exc);
            throw new DataHandlerException("SQL Exception: " + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            if (conn != null) {
                if (transacted && !isXA) {
                    try {
                    	logger.warn("Rolling-back EXECUTE [" + operation + "]."); 
                        conn.rollback();
                    }
                    catch (Exception ex) {
                        // Nothing to do
                    }
                }
            }
            logger.error("Unhandled Exception", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DataHandlerException("Unhandled Exception: " + exc.getMessage(), exc);
        }
        finally {
            cleanup();
            try {
                JDBCConnectionBuilder.releaseConnection(intConnName, conn);
            }
            catch (Exception exc) {
                // do nothing
            }
            logger.debug("End executing EXECUTE [" + operation + "]. Execution time: " + getPartialTime(start));
            NMDC.pop();
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#cleanup()
     */
    @Override
    public void cleanup()
    {
        dataCache.clear();
        internalIdx = 0;
        while (hasNext()) {
            IDBO idbo = nextDBO();
            try {
                idbo.cleanup();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        // Static utility classes reset
        AbstractRetriever.cleanupAll();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#destroy()
     */
    @Override
    public void destroy()
    {
        baseProps.clear();

        internalIdx = 0;
        while (hasNext()) {
            IDBO idbo = nextDBO();
            try {
                idbo.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        // Static utility classes reset
        AbstractRetriever.cleanupAll();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#getConfigurationNode()
     */
    @Override
    public Node getConfigurationNode()
    {
        return configurationNode;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setConfigurationNode(org.w3c.dom.Node)
     */
    @Override
    public void setConfigurationNode(Node configurationNode)
    {
        this.configurationNode = configurationNode;
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.IDBOBuilder#setDteController(it.greenvulcano.gvesb.gvdte.controller.DTEController)
     */
    @Override
    public void setDteController(DTEController dteController)
    {
        this.dteController = dteController;
    }

    private Object transform(IDBO idbo, Object input, Map<String, Object> params) throws Exception
    {
        Object output = input;
        String transformation = idbo.getTransformation();
        if ((transformation != null) && !transformation.equals("")) {
            logger.debug("Transformation [" + transformation + "] execution using DTE.");
            output = dteController.transform(transformation, input, params);
        }
        else {
            logger.debug("No transformation for this IDBO.");
        }
        return output;
    }

    private IDBO firstDBO()
    {
        return dboList.get(0);
    }

    private IDBO nextDBO()
    {
        return dboList.get(internalIdx++);
    }

    private boolean hasNext()
    {
        return (internalIdx < dboList.size());
    }

    private Map<String, Object> buildProps(Map<String, Object> props) throws DataHandlerException
    {
        try {
            Map<String, Object> allProps = new HashMap<String, Object>(baseProps);
            if (props != null) {
                allProps.putAll(props);
            }
            if (resolveMetadata) {
                boolean toDecode = true;
                while (toDecode) {
                    toDecode = false;
                    for (Entry<String, Object> entry : allProps.entrySet()) {
                        String name = entry.getKey();
                        String value = (String) entry.getValue();
                        String nValue = PropertiesHandler.expand(value, allProps);
                        if (!PropertiesHandler.isExpanded(nValue) && ((nValue != null) && (!nValue.equals(value)))) {
                            toDecode = true;
                        }
                        allProps.put(name, nValue);
                    }
                }
            }
            return allProps;
        }
        catch (Exception exc) {
            throw new DataHandlerException("Error building properties map", exc);
        }
    }

    private static String getPartialTime(long start)
    {

        long end = System.currentTimeMillis();
        long partial = end - start;
        int ms = (int) partial % 1000;
        String msec = Integer.toString(ms);
        if (ms < 10) {
            msec = "00" + ms;
        }
        else if (ms < 100) {
            msec = "0" + ms;
        }
        partial = (partial - ms) / 1000;

        int s = (int) partial % 60;
        String sec = Integer.toString(s);
        if (s < 10) {
            sec = "0" + s;
        }
        partial = (partial - s) / 60;

        int m = (int) partial % 3600;
        String min = Integer.toString(m);
        if (m < 10) {
            min = "0" + m;
        }
        return min + ":" + sec + "." + msec;
    }
}
