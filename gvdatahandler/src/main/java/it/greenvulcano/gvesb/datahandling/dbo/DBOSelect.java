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

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * IDBO Class specialized in selecting data from the DB.
 * The selected data are formatted as RowSet XML document.
 * 
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOSelect extends AbstractDBO
{
    private final Map<String, Set<Integer>>          keysMap;

    private String                                   numberFormat           = DEFAULT_NUMBER_FORMAT;
    private String                                   groupSeparator         = DEFAULT_GRP_SEPARATOR;
    private String                                   decSeparator           = DEFAULT_DEC_SEPARATOR;
    private Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private static final Logger                      logger                 = org.slf4j.LoggerFactory.getLogger(DBOSelect.class);
    
    private RowSetBuilder                            rowSetBuilder          = null;

    /**
     *
     */
    public DBOSelect()
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
    public void execute(Object input, Connection conn, Map<String, Object> props) throws DBOException
    {
        prepare();
        throw new DBOException("Unsupported method - DBOSelect::execute(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
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
            rowSetBuilder.setTimeFormatter(timeFormatter);
            rowSetBuilder.setNumberFormatter(numberFormatter);
            rowSetBuilder.setDecSeparator(decSeparator);
            rowSetBuilder.setGroupSeparator(groupSeparator);
            rowSetBuilder.setNumberFormat(numberFormat);
            
            for (Entry<String, String> entry : statements.entrySet()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                Object key = entry.getKey();
                String stmt = entry.getValue();
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
                    String expandedSQL = PropertiesHandler.expand(stmt, localProps, null, conn);
                    Statement statement = null;
                    try {
                        statement = getInternalConn(conn).createStatement();
                        logger.debug("Executing select:\n" + expandedSQL);
                        ResultSet rs = statement.executeQuery(expandedSQL);
                        if (rs != null) {
                            try {
                                rowCounter += rowSetBuilder.build(doc, "" + key, rs, keyField, fieldNameToFormatter, 
                                                                  fieldIdToFormatter);
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
                        }
                    }
                    finally {
                        if (statement != null) {
                            try {
                                statement.close();
                            }
                            catch (Exception exc) {
                                // do nothing
                            }
                            statement = null;
                        }
                    }
                }
            }
            byte[] dataDOM = parser.serializeDOMToByteArray(doc);
            dataOut.write(dataDOM);

            dhr.setRead(rowCounter);
            dhr.setTotal(rowCounter);

            logger.debug("End execution of DB data read through " + dboclass);
        }
        catch (SQLException exc) {
            OracleExceptionHandler.handleSQLException(exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        catch (InterruptedException exc) {
            logger.error("DBO[" + getName() + "] interrupted", exc);
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            // cleanup();
            if (parser != null) {
                XMLUtils.releaseParserInstance(parser);
            }
            rowSetBuilder.cleanup();
        }
    }
}
