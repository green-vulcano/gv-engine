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
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.txt.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * updating or inserting data to DB.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOUpdateOrInsert extends AbstractDBO
{

    StatementInfo                     sqlStatementInfoUpdate;

    private final Map<String, String> statements_update;

    /**
     * Statement parameter values, reported when error occurs.
     */
    protected Vector<Object>          localCurrentRowFields = null;
    /**
     *
     */
    protected Vector<Object>          currentInsertRowFields;
    /**
     *
     */
    protected Vector<Object>          currentUpdateRowFields;

    private static final Logger       logger                = org.slf4j.LoggerFactory.getLogger(DBOUpdateOrInsert.class);

    /**
     *
     */
    public DBOUpdateOrInsert()
    {
        super();
        statements_update = new HashMap<String, String>();
        currentInsertRowFields = new Vector<Object>(10);
        currentUpdateRowFields = new Vector<Object>(10);
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='insert']");
            String id;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    statements.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    statements.put(id, XMLConfig.getNodeValue(stmt));
                }
            }
            stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    statements_update.put(Integer.toString(i), XMLConfig.getNodeValue(stmt));
                }
                else {
                    statements_update.put(id, XMLConfig.getNodeValue(stmt));
                }
            }

            if (statements.isEmpty() || statements_update.isEmpty()) {
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
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOUpdateOrInsert::execute(OutputStream, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#cleanup()
     */
    @Override
    public void cleanup()
    {
        super.cleanup();
        localCurrentRowFields = null;
        currentInsertRowFields.clear();
        currentUpdateRowFields.clear();
        if (sqlStatementInfoUpdate != null) {
            try {
                sqlStatementInfoUpdate.close();
            }
            catch (Exception exc) {
                // do nothing
            }
            sqlStatementInfoUpdate = null;
        }
    }

    private int          colIdx    = 0;

    private int          colUpdIdx = 0;

    private String       currType;

    private String       currDateFormat;

    private String       currNumberFormat;

    private String       currGroupSeparator;

    private String       currDecSeparator;

    private StringBuffer textBuffer;

    private boolean      colDataExpecting;

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#getStatement(java.lang.String)
     */
    @Override
    protected void getStatement(String id) throws SAXException
    {
        if (id == null) {
            id = "0";
        }
        if ((sqlStatementInfo == null) || (sqlStatementInfoUpdate == null) || !getCurrentId().equals(id)) {
            try {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                if (sqlStatementInfo != null) {
                    sqlStatementInfo.close();
                    sqlStatementInfo = null;
                }
                if (sqlStatementInfoUpdate != null) {
                    sqlStatementInfoUpdate.close();
                    sqlStatementInfoUpdate = null;
                }
                String expandedSQL = PropertiesHandler.expand(statements.get(id), getCurrentProps(), null,
                        getInternalConn());
                Statement statement = getInternalConn().prepareStatement(expandedSQL);
                sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
                expandedSQL = PropertiesHandler.expand(statements_update.get(id), getCurrentProps(), getInternalConn(),
                        null);
                if (expandedSQL != null) {
                    statement = getInternalConn().prepareStatement(expandedSQL);
                    sqlStatementInfoUpdate = new StatementInfo(id, expandedSQL, statement);
                }
                setCurrentId(id);
            }
            catch (SQLException exc) {
                OracleError oerr = OracleExceptionHandler.handleSQLException(exc);
                oerr.printLoggerInfo();
                throw new SAXException(exc);
            }
            catch (Exception exc) {
                throw new SAXException(exc);
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            currentRowFields.clear();
            currentRowFields.add(null);
            currentInsertRowFields.clear();
            currentInsertRowFields.add(null);
            currentUpdateRowFields.clear();
            currentUpdateRowFields.add(null);
            colDataExpecting = false;
            colIdx = 0;
            colUpdIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            currType = attributes.getValue(uri, TYPE_NAME);
            if (TIMESTAMP_TYPE.equals(currType) || DATE_TYPE.equals(currType) || TIME_TYPE.equals(currType)) {
                currDateFormat = attributes.getValue(uri, FORMAT_NAME);
                if (currDateFormat == null) {
                    currDateFormat = DEFAULT_DATE_FORMAT;
                }
            }
            else if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType) || FLOAT_TYPE.equals(currType)
                    || DOUBLE_TYPE.equals(currType)) {
                currNumberFormat = attributes.getValue(uri, FORMAT_NAME);
                if (currNumberFormat == null) {
                    currNumberFormat = call_DEFAULT_NUMBER_FORMAT;
                }
                currGroupSeparator = attributes.getValue(uri, GRP_SEPARATOR_NAME);
                if (currGroupSeparator == null) {
                    currGroupSeparator = call_DEFAULT_GRP_SEPARATOR;
                }
                currDecSeparator = attributes.getValue(uri, DEC_SEPARATOR_NAME);
                if (currDecSeparator == null) {
                    currDecSeparator = call_DEFAULT_DEC_SEPARATOR;
                }
            }
            colDataExpecting = true;
            if (COL_NAME.equals(localName)) {
                colIdx++;
            }
            else {
                colUpdIdx++;
            }
            textBuffer = new StringBuffer();
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (colDataExpecting) {
            textBuffer.append(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            rowCounter++;
            int updated = -1;
            if (currCriticalError) {
                rowDisc++;
                // aggiunta DiscardCause al dhr...
                String msg = currentXSLMessage;

                dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                resultMessage.append("Data error on row ").append(rowCounter).append(": ").append(msg).append('\n');
                resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                resultStatus = STATUS_PARTIAL;
                return;
            }
            PreparedStatement sqlStatement_update = (PreparedStatement) sqlStatementInfoUpdate.getStatement();
            if (sqlStatement_update != null) {
                localCurrentRowFields = currentUpdateRowFields;
                try {
                    updated = sqlStatement_update.executeUpdate();
                }
                catch (SQLException exc) {
                    rowDisc++;
                    if (isTransacted()) {
                        logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfoUpdate);
                        resultStatus = STATUS_KO;
                        throw new SAXException(new DBOException("SQLException error on update for row " + rowCounter
                                + ": " + exc.getMessage(), exc));
                    }

                    OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
                    if (isBlockingError(oraerr.getErrorType())) {
                        resultStatus = STATUS_KO;
                        logger.error("Record update parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfoUpdate);
                        logger.error("SQLException configured as blocking error for service '" + serviceName
                                + "' on row " + Long.toString(rowCounter) + ".", exc);
                        throw new SAXException(new DBOException(
                                "SQLException configured as blocking error class on row " + rowCounter + ": "
                                        + exc.getMessage(), exc));
                    }

                    resultMessage.append("SQLException error on update for row ").append(rowCounter).append(": ").append(
                            exc.getMessage()).append('\n');
                    resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfoUpdate);
                    resultMessage.append("Record update parameters:\n").append(dumpCurrentRowFields());
                    resultStatus = STATUS_PARTIAL;

                    String msg = "";
                    if (onlyXSLErrorMsg && (currentXSLMessage != null)) {
                        msg += currentXSLMessage;
                    }
                    else {
                        msg += exc + " - XSL Message: " + currentXSLMessage;
                    }
                    dhr.addDiscardCause(new DiscardCause(rowCounter, msg));
                }
            }
            if ((updated < 1) && (sqlStatementInfo != null)) {
                try {
                    int actualOk = 0;
                    localCurrentRowFields = currentInsertRowFields;
                    actualOk = ((PreparedStatement) sqlStatementInfo.getStatement()).executeUpdate();
                    rowInsOk += actualOk;
                }
                catch (SQLException exc) {
                    rowDisc++;
                    if (isTransacted()) {
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
                        resultStatus = STATUS_KO;
                        throw new SAXException(new DBOException("SQLException error on insert for row " + rowCounter
                                + ": " + exc.getMessage(), exc));
                    }
                    OracleError oraerr = OracleExceptionHandler.handleSQLException(exc);
                    if (isBlockingError(oraerr.getErrorType())) {
                        resultStatus = STATUS_KO;
                        logger.error("Record insert parameters:\n" + dumpCurrentRowFields());
                        logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
                        logger.error("SQLException configured as blocking error for service '" + serviceName
                                + "' alla riga " + Long.toString(rowCounter) + ".", exc);
                        throw new SAXException(new DBOException(
                                "SQLException configured as blocking error class on row " + rowCounter + ": "
                                        + exc.getMessage(), exc));
                    }

                    resultMessage.append("SQLException error on insert for row ").append(rowCounter).append(": ").append(
                            exc.getMessage());
                    resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                    resultMessage.append("Record insert parameters:\n").append(dumpCurrentRowFields());
                    resultStatus = STATUS_PARTIAL;

                    String msg = "";
                    if (onlyXSLErrorMsg && (currentXSLMessage != null)) {
                        msg += currentXSLMessage;
                    }
                    else {
                        msg += exc + " - XSL Message: " + currentXSLMessage;
                    }
                    dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                }
            }
            else {
                rowUpdOk += updated;
            }
        }
        else if (COL_NAME.equals(localName) || COL_UPDATE_NAME.equals(localName)) {
            PreparedStatement stmt = null;
            int idx = 0;
            if (COL_NAME.equals(localName)) {
                stmt = (PreparedStatement) sqlStatementInfo.getStatement();
                idx = colIdx;
                localCurrentRowFields = currentInsertRowFields;
            }
            else {
                stmt = (PreparedStatement) sqlStatementInfoUpdate.getStatement();
                idx = colUpdIdx;
                localCurrentRowFields = currentUpdateRowFields;
            }
            try {
                colDataExpecting = false;
                String text = textBuffer.toString();
                if (TIMESTAMP_TYPE.equals(currType) || DATE_TYPE.equals(currType) || TIME_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        if (TIMESTAMP_TYPE.equals(currType)) stmt.setNull(idx, Types.TIMESTAMP);
                        else if (DATE_TYPE.equals(currType)) stmt.setNull(idx, Types.DATE);
                        else stmt.setNull(idx, Types.TIME);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        dateFormatter.applyPattern(currDateFormat);
                        Date formattedDate = dateFormatter.parse(text);
                        if (TIMESTAMP_TYPE.equals(currType)) {
                            Timestamp ts = new Timestamp(formattedDate.getTime());
                            stmt.setTimestamp(idx, ts);
                            localCurrentRowFields.add(ts);
                        }
                        else if (DATE_TYPE.equals(currType)) {
                            java.sql.Date d = new java.sql.Date(formattedDate.getTime());
                            stmt.setDate(idx, d);
                            localCurrentRowFields.add(d);
                        }
                        else {
                            java.sql.Time t = new java.sql.Time(formattedDate.getTime());
                            stmt.setTime(idx, t);
                            localCurrentRowFields.add(t);
                        } 
                    }
                }
                else if (INTEGER_TYPE.equals(currType) || SMALLINT_TYPE.equals(currType) || BIGINT_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        if (INTEGER_TYPE.equals(currType)) stmt.setNull(idx, Types.INTEGER); 
                        else if (SMALLINT_TYPE.equals(currType)) stmt.setNull(idx, Types.SMALLINT);
                        else stmt.setNull(idx, Types.BIGINT);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        if (INTEGER_TYPE.equals(currType)) stmt.setInt(idx, Integer.parseInt(text, 10)); 
                        else if (SMALLINT_TYPE.equals(currType)) stmt.setShort(idx, Short.parseShort(text, 10));
                        else stmt.setLong(idx, Long.parseLong(text, 10));
                        localCurrentRowFields.add(text);
                    }
                }
                else if (FLOAT_TYPE.equals(currType) || DOUBLE_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) stmt.setNull(idx, Types.NUMERIC);
                        else if (FLOAT_TYPE.equals(currType)) stmt.setNull(idx, Types.FLOAT);
                        else stmt.setNull(idx, Types.DOUBLE);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
                        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
                        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
                        numberFormatter.setDecimalFormatSymbols(dfs);
                        numberFormatter.applyPattern(currNumberFormat);
                        boolean isBigDecimal = numberFormatter.isParseBigDecimal();
                        try {
                            numberFormatter.setParseBigDecimal(true);
                            BigDecimal formattedNumber = (BigDecimal) numberFormatter.parse(text);
                            if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) {
                                stmt.setBigDecimal(idx, formattedNumber);
                                localCurrentRowFields.add(formattedNumber);
                            }
                            else if (FLOAT_TYPE.equals(currType)) {
                                stmt.setFloat(idx, formattedNumber.floatValue());
                                localCurrentRowFields.add(formattedNumber.floatValue());
                            }
                            else {
                                stmt.setDouble(idx, formattedNumber.doubleValue());
                                localCurrentRowFields.add(formattedNumber.doubleValue());
                            }
                        }
                        finally {
                            numberFormatter.setParseBigDecimal(isBigDecimal);
                        }
                    }
                }
                else if (LONG_STRING_TYPE.equals(currType) || LONG_NSTRING_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        if (LONG_STRING_TYPE.equals(currType)) stmt.setNull(idx, Types.CLOB);
                        else stmt.setNull(idx, Types.NCLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        if (LONG_STRING_TYPE.equals(currType)) {
                            stmt.setCharacterStream(idx, new StringReader(text));
                        }
                        else {
                            stmt.setNCharacterStream(idx, new StringReader(text));
                        }
                        localCurrentRowFields.add(text);
                    }
                }
                else if (BASE64_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        data = Base64.getDecoder().decode(data);
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        localCurrentRowFields.add(text);
                    }
                }
                else if (BINARY_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BLOB);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        byte[] data = text.getBytes();
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        stmt.setBinaryStream(idx, bais, data.length);
                        localCurrentRowFields.add(text);
                    }
                }
                else if (BOOLEAN_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.BOOLEAN);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setBoolean(idx, TextUtils.parseBoolean(text));
                        localCurrentRowFields.add(text);
                    }
                }
                else if (XML_TYPE.equals(currType)) {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.SQLXML);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        SQLXML xml = stmt.getConnection().createSQLXML();
                        xml.setString(text);
                        stmt.setSQLXML(idx, xml);
                        localCurrentRowFields.add(text);
                    }
                }
                else if (NSTRING_TYPE.equals(currType)) {
                    if (text.equals("")) {
                    	stmt.setNull(colIdx, Types.NVARCHAR);
                    	localCurrentRowFields.add(null);
                    }
                    else {
                    	stmt.setNString(colIdx, text);
                    	localCurrentRowFields.add(text);
                    }
                }
                else {
                    if (text.equals("")) {
                        stmt.setNull(idx, Types.VARCHAR);
                        localCurrentRowFields.add(null);
                    }
                    else {
                        stmt.setString(idx, text);
                        localCurrentRowFields.add(text);
                    }
                }
            }
            catch (ParseException exc) {
                throw new SAXException(exc);
            }
            catch (SQLException exc) {
                OracleExceptionHandler.handleSQLException(exc);
                throw new SAXException(exc);
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#dumpCurrentRowFields()
     */
    @Override
    protected String dumpCurrentRowFields()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < localCurrentRowFields.size(); i++) {
            sb.append("Field(").append(i).append(") value: [").append(localCurrentRowFields.elementAt(i)).append(
                    "]\n");
        }
        sb.append("XSL Message: ").append(currentXSLMessage).append("\n\n");
        return sb.toString();
    }
}
