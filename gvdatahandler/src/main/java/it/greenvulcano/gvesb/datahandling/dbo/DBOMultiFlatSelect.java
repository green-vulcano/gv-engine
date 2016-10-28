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
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.thread.ThreadUtils;

import java.io.FileWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * IDBO Class specialized in selecting multiple set of data from the DB.
 * The selected data are formatted as CSV text.
 *
 * @version 3.2.0 01/10/2011
 * @author GreenVulcano Developer Team
 */
public class DBOMultiFlatSelect extends AbstractDBO
{
    private String                                   endLine                = DEFAULT_END_LINE;

    private String                                   encoding               = DEFAULT_ENCODING;

    private int                                      sbRowLength            = 100;

    private Map<String, Map<String, FieldFormatter>> statIdToNameFormatters = new HashMap<String, Map<String, FieldFormatter>>();
    private Map<String, Map<String, FieldFormatter>> statIdToIdFormatters   = new HashMap<String, Map<String, FieldFormatter>>();

    private List<Integer>                            statIDs                = new ArrayList<Integer>();

    private static final Logger                      logger                 = org.slf4j.LoggerFactory.getLogger(DBOMultiFlatSelect.class);
    
    private String                                   directFilePath         = null;

    public DBOMultiFlatSelect()
    {
        super();
    }

    @Override
    public void init(Node config) throws DBOException
    {
        super.init(config);
        try {
            endLine = XMLConfig.get(config, "@end-line", DEFAULT_END_LINE);
            encoding = XMLConfig.get(config, "@encoding", DEFAULT_ENCODING);
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_DB2XML);
            isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            directFilePath = XMLConfig.get(config, "@direct-file-path", null);
            NodeList stmts = XMLConfig.getNodeList(config, "statement[@type='select']");
            String id = null;
            Node stmt;
            for (int i = 0; i < stmts.getLength(); i++) {
                stmt = stmts.item(i);
                id = XMLConfig.get(stmt, "@id");
                if (id == null) {
                    id = Integer.toString(i);
                }
                statements.put(id, XMLConfig.getNodeValue(stmt));
                statIDs.add(new Integer(id));
            }

            Collections.sort(statIDs);

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
        throw new DBOException("Unsupported method - DBOFlatSelect::execute(Object, Connection, Map)");
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream dataOut, Connection conn, Map<String, Object> props) throws DBOException
    {
        FileWriter fw = null;
        try {
            prepare();
            rowCounter = 0;
            logger.debug("Begin execution of DB data read through " + dboclass);

            Map<String, Object> localProps = buildProps(props);
            logProps(localProps);

            String localDirectFilePath = PropertiesHandler.expand(directFilePath, localProps, null, conn);
            if (localDirectFilePath != null) {
                fw = new FileWriter(localDirectFilePath);
            }
            
            StringBuilder sb = new StringBuilder(sbRowLength);

            Iterator<Integer> itr = statIDs.iterator();
            while (itr.hasNext()) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                String id = itr.next().toString();
                String stmt = statements.get(id);
                Map<String, FieldFormatter> fieldNameToFormatter = statIdToNameFormatters.get(id);
                Map<String, FieldFormatter> fieldIdToFormatter = statIdToIdFormatters.get(id);

                if (stmt != null) {
                    String expandedSQL = PropertiesHandler.expand(stmt, localProps, null, conn);
                    Statement sqlStatement = null;
                    try {
                        sqlStatement = getInternalConn(conn).createStatement();
                        logger.debug("Executing select:\n" + expandedSQL);
                        ResultSet rs = sqlStatement.executeQuery(expandedSQL);
                        if (rs != null) {
                            try {
                                ResultSetMetaData metadata = rs.getMetaData();
                                FieldFormatter[] fFormatters = buildFormatterArray(metadata, fieldNameToFormatter,
                                        fieldIdToFormatter);
                                String textVal = null;
                                while (rs.next()) {
                                    if (rowCounter % 10 == 0) {
                                        ThreadUtils.checkInterrupted(getClass().getSimpleName(), getName(), logger);
                                    }
                                    for (int j = 1; j <= metadata.getColumnCount(); j++) {
                                        FieldFormatter fF = fFormatters[j];
                                        if (fF == null) {
                                            fF = fFormatters[0];
                                        }
                                        switch (metadata.getColumnType(j)) {
                                            case Types.DATE :
                                        	case Types.TIME : 
                                            case Types.TIMESTAMP :{
                                                Timestamp dateVal = rs.getTimestamp(j);
                                                if (dateVal == null) {
                                                    textVal = fF.formatField("");
                                                }
                                                else {
                                                    textVal = fF.formatDate(dateVal);
                                                }
                                            }
                                                break;
                                            case Types.DOUBLE :
                                            case Types.FLOAT :
                                            case Types.REAL : {
                                                float numVal = rs.getFloat(j);
                                                textVal = fF.formatNumber(numVal);
                                            }
                                                break;
                                            case Types.BIGINT :
                                            case Types.INTEGER :
                                            case Types.NUMERIC :
                                            case Types.SMALLINT : 
                                            case Types.TINYINT : {
                                                BigDecimal bigdecimal = rs.getBigDecimal(j);
                                                if (bigdecimal == null) {
                                                    textVal = fF.formatField("");
                                                }
                                                else if (metadata.getScale(j) > 0) {
                                                    textVal = fF.formatNumber(bigdecimal);
                                                }
                                                else {
                                                    textVal = fF.formatField(bigdecimal.toString());
                                                }
                                            }
                                                break;
                                            case Types.NCHAR :
                                            case Types.NVARCHAR :{
                                                String val = rs.getNString(j);
                                                if (val == null) {
                                                    textVal = fF.formatField("");
                                                }
                                                else {
                                                    textVal = fF.formatField(val);
                                                }
                                            }
                                                break;
                                            case Types.CHAR :
                                            case Types.VARCHAR :{
                                                String val = rs.getString(j);
                                                if (val == null) {
                                                    textVal = fF.formatField("");
                                                }
                                                else {
                                                    textVal = fF.formatField(val);
                                                }
                                            }
                                                break;
                                            case Types.NCLOB :{
                                                textVal = "";
                                            }
                                                break;
                                            case Types.CLOB :{
                                                textVal = "";
                                            }
                                                break;
                                            case Types.BLOB :{
                                                textVal = "";
                                            }
                                                break;
                                            default :{
                                                textVal = fF.formatField(rs.getString(j));
                                            }
                                        }
                                        sb.append(textVal);
                                    }
                                    rowCounter++;
                                    if (fw != null) {
                                        fw.append(sb).append(endLine);
                                        sb.delete(0, sb.length());
                                    }
                                    else {
                                        sb.append(endLine);
                                    }
                                }
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
                        sbRowLength = Math.max(sbRowLength, sb.length());
                    }
                    finally {
                        if (sqlStatement != null) {
                            try {
                                sqlStatement.close();
                            }
                            catch (Exception exc) {
                                // do nothing
                            }
                            sqlStatement = null;
                        }
                    }
                }
            }
            if (fw == null) {
                Charset cs = Charset.forName(encoding);
                ByteBuffer bb = cs.encode(CharBuffer.wrap(sb));
                //dataOut.write(bb.array());
                dataOut.write(bb.array(), 0, sb.length()); // da verificare!!!
                dataOut.flush();
            }

            dhr.setRead(rowCounter);
            dhr.setTotal(rowCounter);

            logger.debug("End execution of DB data read through " + dboclass);
        }
        catch (SQLException exc) {
            OracleExceptionHandler.handleSQLException(exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        catch (Exception exc) {
            logger.error("Error on execution of " + dboclass + " with name [" + getName() + "]", exc);
            throw new DBOException("Error on execution of " + dboclass + " with name [" + getName() + "]: "
                        + exc.getMessage(), exc);
        }
        finally {
            //cleanup();
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                }
                catch (Exception exc2) {
                    // do nothing
                }
            }
        }
    }

    private FieldFormatter[] buildFormatterArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception
    {
        FieldFormatter[] fFA = new FieldFormatter[rsm.getColumnCount() + 1];
        fFA[0] = fieldIdToFormatter.get("0");

        for (int i = 1; i < fFA.length; i++) {
            FieldFormatter fF = fieldNameToFormatter.get(rsm.getColumnName(i));
            if (fF == null) {
                fF = fieldIdToFormatter.get("" + i);
            }
            fFA[i] = fF;
        }
        return fFA;
    }
}
