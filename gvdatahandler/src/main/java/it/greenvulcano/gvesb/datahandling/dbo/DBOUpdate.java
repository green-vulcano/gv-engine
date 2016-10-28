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
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * updating data to DB.
 * Updated to use named parameters in statement.
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOUpdate extends AbstractDBO
{

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DBOUpdate.class);

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        isInsert = false;
        try {
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_XML2DB);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='update']");
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
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException,
            InterruptedException {
        prepare();
        throw new DBOException("Unsupported method - DBOUpdate::execute(OutputStream, Connection, Map)");
    }

    private int          colIdx = 0;
    
    private List<Integer> colIdxs = new ArrayList<Integer>(1);

    private String       currType;

    private String       currDateFormat;

    private String       currNumberFormat;

    private String       currGroupSeparator;

    private String       currDecSeparator;

    private StringBuffer textBuffer;

    private boolean      colDataExpecting;

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        boolean processParams = false;

        if (ROW_NAME.equals(localName)) {
            currentRowFields.clear();
            colDataExpecting = false;
            colIdx = 0;
            String id = attributes.getValue(ID_NAME);
            getStatement(id);
            if (sqlStatementInfo.usesNamedParams()) {
                currentRowFields.setSize(sqlStatementInfo.getSqlStatementParamCount()+1);
            }
            else {
                colIdxs.clear();
                colIdxs.add(-1);
                currentRowFields.add(null);
            }
            currentXSLMessage = attributes.getValue(XSL_MSG_NAME);
            currCriticalError = "true".equalsIgnoreCase(attributes.getValue(CRITICAL_ERROR));
            generatedKeyID = attributes.getValue("generate-key");
            resetGeneratedKeyID = attributes.getValue("reset-generate-key");
            readGeneratedKey = autogenerateKeys && (generatedKeyID != null);
        }
        else if (sqlStatementInfo.usesNamedParams()) {
            processParams = sqlStatementInfo.getSqlStatementParams().containsKey(localName);
        }
        else {
            processParams = COL_NAME.equals(localName);
        } 
            
        if (processParams) {
            currType = attributes.getValue(TYPE_NAME);
            if (TIMESTAMP_TYPE.equals(currType) || DATE_TYPE.equals(currType) || TIME_TYPE.equals(currType)) {
                currDateFormat = attributes.getValue(FORMAT_NAME);
                if (currDateFormat == null) {
                    currDateFormat = DEFAULT_DATE_FORMAT;
                }
            }
            else if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType) || FLOAT_TYPE.equals(currType) || DOUBLE_TYPE.equals(currType)) {
                currNumberFormat = attributes.getValue(FORMAT_NAME);
                if (currNumberFormat == null) {
                    currNumberFormat = call_DEFAULT_NUMBER_FORMAT;
                }
                currGroupSeparator = attributes.getValue(GRP_SEPARATOR_NAME);
                if (currGroupSeparator == null) {
                    currGroupSeparator = call_DEFAULT_GRP_SEPARATOR;
                }
                currDecSeparator = attributes.getValue(DEC_SEPARATOR_NAME);
                if (currDecSeparator == null) {
                    currDecSeparator = call_DEFAULT_DEC_SEPARATOR;
                }
            }
            colDataExpecting = true;
            colIdx++;
            textBuffer = new StringBuffer();
            if (!sqlStatementInfo.usesNamedParams()) {
                currentRowFields.add(null);
            }
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
        boolean processParams = false;

        if (ROW_NAME.equals(localName)) {
            if (!currCriticalError) {
                executeStatement();
            }
            else {
                rowDisc++;
                // aggiunta DiscardCause al dhr...
                String msg = currentXSLMessage;

                dhr.addDiscardCause(new DiscardCause(rowCounter, msg));

                resultMessage.append("Data error on row ").append(rowCounter).append(": ").append(msg);
                resultMessage.append(" SQL Statement Informations:\n").append(sqlStatementInfo);
                resultMessage.append(" Record parameters:\n").append(dumpCurrentRowFields());
                resultStatus = STATUS_PARTIAL;
            }
        }
        else if (sqlStatementInfo.usesNamedParams()) {
            processParams = sqlStatementInfo.getSqlStatementParams().containsKey(localName);
        }
        else {
            processParams = COL_NAME.equals(localName);
        }
        
        if (processParams) {
            PreparedStatement ps = (PreparedStatement) sqlStatementInfo.getStatement();

            try {
                if (sqlStatementInfo.usesNamedParams()) {
                    colIdxs = sqlStatementInfo.getSqlStatementParams().get(localName);
                }
                else {
                    colIdxs.set(0, colIdx);
                }
                colDataExpecting = false;
                boolean autoKeySet = false;
                String text = textBuffer.toString();
                if (autogenerateKeys) {
                    if (text.startsWith(GENERATED_KEY_ID)) {
                        Object key = generatedKeys.get(text);
                        for (Integer idx : colIdxs) {
                            ps.setObject(idx, key);
                            currentRowFields.set(idx, key);
                        }
                        autoKeySet = true;
                    }
                }
                if (!autoKeySet) {
                    if (TIMESTAMP_TYPE.equals(currType) || DATE_TYPE.equals(currType) || TIME_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                if (TIMESTAMP_TYPE.equals(currType)) ps.setNull(idx, Types.TIMESTAMP);
                                else if (DATE_TYPE.equals(currType)) ps.setNull(idx, Types.DATE);
                                else ps.setNull(idx, Types.TIME);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            dateFormatter.applyPattern(currDateFormat);
                            Date formattedDate = dateFormatter.parse(text);
                            if (TIMESTAMP_TYPE.equals(currType)) {
                                Timestamp ts = new Timestamp(formattedDate.getTime());
                                for (Integer idx : colIdxs) {
                                    ps.setTimestamp(idx, ts);
                                    currentRowFields.set(idx, ts);
                                }
                            }
                            else if (DATE_TYPE.equals(currType)) {
                                java.sql.Date d = new java.sql.Date(formattedDate.getTime());
                                for (Integer idx : colIdxs) {
                                    ps.setDate(idx, d);
                                    currentRowFields.set(idx, d);
                                }
                            }
                            else {
                                java.sql.Time t = new java.sql.Time(formattedDate.getTime());
                                for (Integer idx : colIdxs) {
                                    ps.setTime(idx, t);
                                    currentRowFields.set(idx, t);
                                }
                            } 
                        }
                    }
                    else if (INTEGER_TYPE.equals(currType) || SMALLINT_TYPE.equals(currType) || BIGINT_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                if (INTEGER_TYPE.equals(currType)) ps.setNull(idx, Types.INTEGER); 
                                else if (SMALLINT_TYPE.equals(currType)) ps.setNull(idx, Types.SMALLINT);
                                else ps.setNull(idx, Types.BIGINT);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            for (Integer idx : colIdxs) {
                                if (INTEGER_TYPE.equals(currType)) ps.setInt(idx, Integer.parseInt(text, 10)); 
                                else if (SMALLINT_TYPE.equals(currType)) ps.setShort(idx, Short.parseShort(text, 10));
                                else ps.setLong(idx, Long.parseLong(text, 10));
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else if (FLOAT_TYPE.equals(currType) || DOUBLE_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) ps.setNull(idx, Types.NUMERIC);
                                else if (FLOAT_TYPE.equals(currType)) ps.setNull(idx, Types.FLOAT);
                                else ps.setNull(idx, Types.DOUBLE);
                                currentRowFields.set(idx, null);
                            }
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
                                for (Integer idx : colIdxs) {
                                    if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) {
                                        ps.setBigDecimal(idx, formattedNumber);
                                        currentRowFields.set(idx, formattedNumber);
                                    }
                                    else if (FLOAT_TYPE.equals(currType)) {
                                        ps.setFloat(idx, formattedNumber.floatValue());
                                        currentRowFields.set(idx, formattedNumber.floatValue());
                                    }
                                    else {
                                        ps.setDouble(idx, formattedNumber.doubleValue());
                                        currentRowFields.set(idx, formattedNumber.doubleValue());
                                    }
                                }
                            }
                            finally {
                                numberFormatter.setParseBigDecimal(isBigDecimal);
                            }
                        }
                    }
                    else if (LONG_STRING_TYPE.equals(currType) || LONG_NSTRING_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                if (LONG_STRING_TYPE.equals(currType)) ps.setNull(idx, Types.CLOB);
                                else ps.setNull(idx, Types.NCLOB);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            for (Integer idx : colIdxs) {
                                if (LONG_STRING_TYPE.equals(currType)) {
                                    ps.setCharacterStream(idx, new StringReader(text));
                                    currentRowFields.set(idx, text);
                                }
                                else {
                                    ps.setNCharacterStream(idx, new StringReader(text));
                                    currentRowFields.set(idx, text);
                                }
                            }
                        }
                    }
                    else if (BASE64_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.BLOB);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            byte[] data = text.getBytes();
                            data = Base64.getDecoder().decode(data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            for (Integer idx : colIdxs) {
                                ps.setBinaryStream(idx, bais, data.length);
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else if (BINARY_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.BLOB);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            for (Integer idx : colIdxs) {
                                ps.setBinaryStream(idx, bais, data.length);
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else if (BOOLEAN_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.BOOLEAN);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            for (Integer idx : colIdxs) {
                                ps.setBoolean(idx, TextUtils.parseBoolean(text));
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else if (XML_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.SQLXML);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            SQLXML xml = ps.getConnection().createSQLXML();
                            xml.setString(text);
                            for (Integer idx : colIdxs) {
                                ps.setSQLXML(idx, xml);
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else if (NSTRING_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.NVARCHAR);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            for (Integer idx : colIdxs) {
                                ps.setNString(idx, text);
                                currentRowFields.set(idx, text);
                            }
                        }
                    }
                    else {
                        if (text.equals("")) {
                            for (Integer idx : colIdxs) {
                                ps.setNull(idx, Types.VARCHAR);
                                currentRowFields.set(idx, null);
                            }
                        }
                        else {
                            for (Integer idx : colIdxs) {
                                ps.setString(idx, text);
                                currentRowFields.set(idx, text);
                            }
                        }
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
}
