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
package it.greenvulcano.gvesb.datahandling.dbo;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.dbo.utils.ExtendedRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.RowSetBuilder;
import it.greenvulcano.gvesb.datahandling.dbo.utils.StandardRowSetBuilder;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * IDBO Class specialized in selecting data from the DB using multiple Threads.
 * The selected data are formatted as RowSet XML document.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOThreadSelect extends AbstractDBO
{

    private class ThreadSelect implements Runnable
    {
        private String              stmt             = null;
        private Document            doc              = null;
        private Object              key              = null;
        private RowSetBuilder       rowSetBuilder    = null;
        private Map<String, Object> props            = null;

        private final static int    NEW              = 0;
        private final static int    RUNNING          = 1;
        private final static int    TERM             = 2;
        private final static int    ERROR            = 3;

        private int                 state            = NEW;
        private Map<Object, Object> context          = null;
        private long                rowThreadCounter = 0;

        private ThreadSelect(Map<Object, Object> ctx)
        {
            context = ctx;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            MDC.getContext().putAll(context);
            Thread thd = Thread.currentThread();
            logger.debug("Thread " + thd.getName() + " started.");
            state = RUNNING;
            Connection conn = null;
            Statement sqlStatement = null;
            ResultSet rs = null;
            try {
                Set<Integer> keyField = keysMap.get(key);
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                if (statIdToNameFormatters.containsKey(key)) {
                    fieldNameToFormatter = statIdToNameFormatters.get(key);
                }
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();
                if (statIdToIdFormatters.containsKey(key)) {
                    fieldIdToFormatter = statIdToIdFormatters.get(key);
                }

                if (stmt != null) {
                    conn = getConnection();
                    String expandedSQL = PropertiesHandler.expand(stmt, props, null, conn);
                    sqlStatement = conn.createStatement();
                    logger.debug("Executing select statement: " + expandedSQL + ".");
                    rs = sqlStatement.executeQuery(expandedSQL);
                    if (rs != null) {
                        Document localDoc = rowSetBuilder.createDocument(null);
                        try {
                            rowThreadCounter += rowSetBuilder.build(localDoc, "" + key, rs, keyField, 
                                    fieldNameToFormatter, fieldIdToFormatter);
                        }
                        finally {
                            if (rs != null) {
                                try {
                                    rs.close();
                                }
                                catch (Exception exc) {
                                    // do nothing
                                }
                                rs = null;
                            }
                        }
                        if (!thd.isInterrupted()) {
                            synchronized (doc) {
                                Element docRoot = doc.getDocumentElement();
                                Element localDocRoot = localDoc.getDocumentElement();
                                NodeList nodes = localDocRoot.getChildNodes();
                                for (int i = 0; i < nodes.getLength(); i++) {
                                    Node dataNode = doc.importNode(nodes.item(i), true);
                                    docRoot.appendChild(dataNode);
                                }
                            }
                        }
                    }
                }
            }
            catch (SQLException exc) {
                OracleExceptionHandler.handleSQLException(exc).printLoggerInfo();
                state = ERROR;
            }
            catch (Throwable exc) {
                logger.error("Thread " + thd.getName() + " terminated with error.", exc);
                state = ERROR;
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                if (sqlStatement != null) {
                    try {
                        sqlStatement.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                if (conn != null) {
                    try {
                        releaseConnection(conn);
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                logger.debug("Thread " + thd.getName() + " terminated.");
                if (state != ERROR) {
                    state = TERM;
                }
                synchronized (synchObj) {
                    synchObj.notify();
                }
            }
        }

        private void setKey(Object key)
        {
            this.key = key;
        }

        private void setDocument(Document doc)
        {
            this.doc = doc;
        }

        private void setProps(Map<String, Object> props)
        {
            this.props = props;
        }

        private void setStatement(String stmt)
        {
            this.stmt = stmt;
        }
        
        private void setRowSetBuilder(RowSetBuilder rowSetBuilder)
        {
            this.rowSetBuilder = rowSetBuilder;
        }

        private Connection getConnection() throws Exception
        {
            return JDBCConnectionBuilder.getConnection(getJdbcConnectionName());
        }

        private void releaseConnection(Connection conn) throws Exception
        {
            JDBCConnectionBuilder.releaseConnection(getJdbcConnectionName(), conn);
        }

        /**
         * @return the rowThreadCounter
         */
        public long getRowThreadCounter()
        {
            return rowThreadCounter;
        }
    }

    private final Map<String, Set<Integer>>          keysMap;

    private String                                   numberFormat           = DEFAULT_NUMBER_FORMAT;
    private String                                   groupSeparator         = DEFAULT_GRP_SEPARATOR;
    private String                                   decSeparator           = DEFAULT_DEC_SEPARATOR;
    private Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private static final Logger                      logger                 = org.slf4j.LoggerFactory.getLogger(DBOThreadSelect.class);

    private RowSetBuilder                            rowSetBuilder          = null;
    private final Object                             synchObj               = new Object();

    /**
     *
     */
    public DBOThreadSelect()
    {
        super();
        keysMap = new HashMap<String, Set<Integer>>();
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            String rsBuilder = XMLConfig.get(config, "@rowset-builder", "standard");
            if (rsBuilder.equals("extended")) {
                rowSetBuilder = new ExtendedRowSetBuilder();
            }
            else {
                rowSetBuilder = new StandardRowSetBuilder();
            }
            rowSetBuilder.setName(getName());
            rowSetBuilder.setLogger(logger);

            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='select']");
            String id = null;
            String keys = null;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                keys = XMLConfig.get(stmt, "@keys");
                if (id == null) {
                    id = Integer.toString(i);
                }
                statements.put(id, XMLConfig.getNodeValue(stmt));
                if (keys != null) {
                    Set<Integer> s = new HashSet<Integer>();
                    StringTokenizer sTok = new StringTokenizer(keys, ",");
                    while (sTok.hasMoreTokens()) {
                        String str = sTok.nextToken();
                        s.add(new Integer(str.trim()));
                    }
                    keysMap.put(id, s);
                }
            }

            NodeList fFrmsList = XMLConfig.getNodeList(config, "FieldFormatters");
            for (int i = 0; i < fFrmsList.getLength(); i++) {
                Node fFrmsL = fFrmsList.item(i);
                id = XMLConfig.get(fFrmsL, "@id");
                if (id == null) {
                    id = Integer.toString(i);
                }
                Map<String, FieldFormatter> fieldNameToFormatter = new HashMap<String, FieldFormatter>();
                Map<String, FieldFormatter> fieldIdToFormatter = new HashMap<String, FieldFormatter>();

                NodeList fFrms = XMLConfig.getNodeList(fFrmsL, "*[@type='field-formatter']");
                for (int j = 0; j < fFrms.getLength(); j++) {
                    Node fF = fFrms.item(j);
                    FieldFormatter fForm = new FieldFormatter();
                    fForm.init(fF);
                    String fName = fForm.getFieldName();
                    if (fName != null) {
                        if (fName.indexOf(",") != -1) {
                            StringTokenizer st = new StringTokenizer(fName, " ,");
                            while (st.hasMoreTokens()) {
                                fieldNameToFormatter.put(st.nextToken().toUpperCase().trim(), fForm);
                            }
                        }
                        else {
                            fieldNameToFormatter.put(fForm.getFieldName().toUpperCase().trim(), fForm);
                        }
                    }
                    String fId = fForm.getFieldId();
                    if (fId != null) {
                        if (fId.indexOf(",") != -1) {
                            StringTokenizer st = new StringTokenizer(fId, " ,");
                            while (st.hasMoreTokens()) {
                                fieldIdToFormatter.put(st.nextToken().toUpperCase().trim(), fForm);
                            }
                        }
                        else {
                            fieldIdToFormatter.put(fForm.getFieldId().toUpperCase().trim(), fForm);
                        }
                    }
                }
                statIdToNameFormatters.put(id, fieldNameToFormatter);
                statIdToIdFormatters.put(id, fieldIdToFormatter);
            }

            if (statements.isEmpty()) {
                throw new DBOException("Empty/misconfigured statements list for [" + getName() + "/" + dboclass + "]");
            }
        }
        catch (DBOException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + getName() + "/" + dboclass + "]", exc);
        }
    }

    /**
     * Unsupported method for this IDBO.
     *
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object input, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOSelect::execute(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(OutputStream dataOut, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        XMLUtils parser = null;
        try {
            prepare();
            rowCounter = 0;
            logger.debug("Begin execution of DB data read through " + dboclass);

            Map<String, Object> localProps = buildProps(props);
            logProps(localProps);

            numberFormat = (String) localProps.get(FORMAT_NAME);
            if (numberFormat == null) {
                numberFormat = DEFAULT_NUMBER_FORMAT;
            }
            groupSeparator = (String) localProps.get(GRP_SEPARATOR_NAME);
            if (groupSeparator == null) {
                groupSeparator = DEFAULT_GRP_SEPARATOR;
            }
            decSeparator = (String) localProps.get(DEC_SEPARATOR_NAME);
            if (decSeparator == null) {
                decSeparator = DEFAULT_DEC_SEPARATOR;
            }
            DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
            dfs.setDecimalSeparator(decSeparator.charAt(0));
            dfs.setGroupingSeparator(groupSeparator.charAt(0));
            numberFormatter.setDecimalFormatSymbols(dfs);
            numberFormatter.applyPattern(numberFormat);

            parser = XMLUtils.getParserInstance();
            Document doc = rowSetBuilder.createDocument(parser);
            
            rowSetBuilder.setXMLUtils(parser);
            rowSetBuilder.setDateFormatter(dateFormatter);
            rowSetBuilder.setNumberFormatter(numberFormatter);
            rowSetBuilder.setDecSeparator(decSeparator);
            rowSetBuilder.setGroupSeparator(groupSeparator);
            rowSetBuilder.setNumberFormat(numberFormat);
            

            Vector<ThreadSelect> thrSelVector = new Vector<ThreadSelect>();
            Vector<Thread> thrVector = new Vector<Thread>();
            for (Entry<String, String> entry : statements.entrySet()) {
                Object key = entry.getKey();
                String stmt = entry.getValue();
                ThreadSelect ts = new ThreadSelect(MDC.getContext());
                ts.setDocument(doc);
                ts.setStatement(stmt);
                ts.setKey(key);
                ts.setProps(localProps);
                ts.setRowSetBuilder(rowSetBuilder.getCopy());
                thrSelVector.add(ts);

                Thread t = new Thread(ts);
                thrVector.add(t);
                t.start();
            }

            try {
                Thread thd = Thread.currentThread();
                // wait for all threads are terminated
                boolean finished = false;
                boolean error = false;
                while (!finished && !thd.isInterrupted()) {
                    int s = thrSelVector.size();
                    int idx = 0;
                    for (int i = 0; i < s; i++) {
                        ThreadSelect to = thrSelVector.get(idx);
                        if (error) {
                            thrVector.elementAt(i).interrupt();
                        }
                        switch (to.state) {
                            case ThreadSelect.TERM :{
                                thrSelVector.remove(idx);
                                rowCounter += to.getRowThreadCounter();
                            }
                                break;
                            case ThreadSelect.ERROR :{
                                thrSelVector.remove(idx);
                                rowCounter += to.getRowThreadCounter();
                                error = true;
                                idx = 0;
                            }
                                break;
                            default :
                                idx++;
                        }
                    }
                    if (thrSelVector.size() == 0) {
                        finished = true;
                    }
                    else {
                        try {
                            synchronized (synchObj) {
                                synchObj.wait(1000);
                            }
                        }
                        catch (InterruptedException exc) {
                            logger.error("DBOThreadSelect[" + getName() + "] interrupted", exc);
                            throw exc;
                        }
                    }
                }
            }
            finally {
                thrSelVector.clear();
                for (Thread thread : thrVector) {
                    thread.interrupt();
                }
                thrVector.clear();
            }

            byte[] dataDOM = parser.serializeDOMToByteArray(doc);
            dataOut.write(dataDOM);

            dhr.setRead(rowCounter);
            dhr.setTotal(rowCounter);

            logger.debug("End execution of DB data read through " + dboclass);
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
            ThreadUtils.checkInterrupted(exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            XMLUtils.releaseParserInstance(parser);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#destroy()
     */
    @Override
    public void destroy()
    {
        super.destroy();
    }

}
