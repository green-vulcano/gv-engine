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
package it.greenvulcano.gvesb.utils;

import it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import it.greenvulcano.util.metadata.PropertyHandler;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/**
 * @version 3.0.0 10/giu/2010
 * @author GreenVulcano Developer Team
 */
public class GVESBPropertyHandler implements PropertyHandler {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GVESBPropertyHandler.class);
    private final static Set<String> managedTypes = new HashSet<>();
	
    static {   
       managedTypes.add("sql");
       managedTypes.add("sqllist");
       managedTypes.add("sqltable");
     
       Collections.unmodifiableSet(managedTypes);
    } 
    
    @Override
	public Set<String> getManagedTypes() {		
		return managedTypes;
	}   

    /**
     * This method insert the correct values for the dynamic parameter found in
     * the input string. The property value can be a combination of:
     * 
     * <pre>
     *  - sql{{[conn::]statement}}  : execute a select sql statement and return the value of
     *                                the first field of the first selected record.
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     *  - sqllist{{[conn::[::sep]]statement}}
     *                              : execute a select sql statement and return the value of
     *                                the first field of all selected records as a 'sep' (default to comma) separated list.
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     *  - sqltable{{[conn::]statement}}
     *                              : execute a select sql statement and return all values of returned cursor as an XML.
     *                                For instance, if the cursor has 3 values, the returned XML will have following fields:
     *                                <RowSet>
     *                                  <data>
     *                                    <row>
     *                                      <col>value1</col>
     *                                      <col>value2</col>
     *                                      <col>value3</col>
     *                                    </row>
     *                                    <row>
     *                                      <col>value4</col>
     *                                      <col>value5</col>
     *                                      <col>value6</col>
     *                                    </row>
     *                                  ..
     *                                    <row>
     *                                      <col>valuex</col>
     *                                      <col>valuey</col>
     *                                      <col>valuez</col>
     *                                    </row>
     *                                  </data>
     *                                </RowSet>
     *                                The 'conn' parameter is the name of a connection provided by
     *                                it.greenvulcano.gvesb.j2ee.db.connections.JDBCConnectionBuilder
     *                                If 'conn' isn't defined then 'extra' must be a java.sql.Connection instance.
     * </pre>
     * 
     * @param type
     * @param str
     * @param inProperties
     * @param object
     * @param extra
     */
    @Override
    public String expand(String type, String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        if (type.startsWith("sqllist")) {
            return expandSQLListProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("sqltable")) {
            return expandSQLTableProperties(str, inProperties, object, extra);
        }
        else if (type.startsWith("sql")) {
            return expandSQLProperties(str, inProperties, object, extra);
        }
        return str;
    }

    private String expandSQLProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
                conn = JDBCConnectionBuilder.getConnection(connName);
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sql' metadata '" + str
                        + "', Connection undefined.");
            }
            logger.debug("Executing SQL statement {" + sqlStatement + "} on connection [" + connName + "]");
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();

            String paramValue = null;

            if (rs.next()) {
                ResultSetMetaData rsmeta = rs.getMetaData();
                if (rsmeta.getColumnType(1) == Types.CLOB) {
                    Clob clob = rs.getClob(1);
                    if (clob != null) {
	                    Reader is = clob.getCharacterStream();
                        StringWriter strW = new StringWriter();

                        IOUtils.copy(is, strW);
                        is.close();
                        paramValue = strW.toString();
                    }
                }
                else {
                    paramValue = rs.getString(1);
                }
            }

            return (paramValue != null) ? paramValue.trim() : paramValue;
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sql' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sql' metadata '" + str + "'", exc);
            }
            return "sql" + PROP_START + str + PROP_END;
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null)) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private String expandSQLListProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        String separator = ",";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                int pIdx2 = str.indexOf("::", pIdx + 2);
                if (pIdx2 != -1) {
                    separator = str.substring(pIdx + 2, pIdx2);
                    sqlStatement = str.substring(pIdx2 + 2);
                }
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
                conn = JDBCConnectionBuilder.getConnection(connName);
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sqllist' metadata '" + str
                        + "', Connection undefined.");
            }
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();

            String paramValue = "";

            int type = rs.getMetaData().getColumnType(1);

            while (rs.next()) {
                if (type == Types.CLOB) {
                    Clob clob = rs.getClob(1);
                    if (clob != null) {
                        Reader is = clob.getCharacterStream();
                        StringWriter strW = new StringWriter();

                        IOUtils.copy(is, strW);
                        is.close();
                        paramValue += separator + strW.toString();
                    }
                    else {
                    	paramValue += separator + "null";
                    }
                }
                else {
                    paramValue += separator + rs.getString(1);
                }
            }

            if (!paramValue.equals("")) {
                paramValue = paramValue.substring(separator.length());
            }

            return (paramValue != null) ? paramValue.trim() : paramValue;
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sqllist' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sqllist' metadata '" + str + "'", exc);
            }
            return "sqllist" + PROP_START + str + PROP_END;
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null)) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }

    private String expandSQLTableProperties(String str, Map<String, Object> inProperties, Object object,
            Object extra) throws PropertiesHandlerException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStatement = null;
        Connection conn = null;
        String connName = "";
        boolean intConn = false;
        try {
            if (!PropertiesHandler.isExpanded(str)) {
                str = PropertiesHandler.expand(str, inProperties, object, extra);
            }
            int pIdx = str.indexOf("::");
            if (pIdx != -1) {
                connName = str.substring(0, pIdx);
                sqlStatement = str.substring(pIdx + 2);
                intConn = true;
            }
            else {
                sqlStatement = str;
            }
            if (intConn) {
                conn = JDBCConnectionBuilder.getConnection(connName);
            }
            else if ((extra != null) && (extra instanceof Connection)) {
                conn = (Connection) extra;
            }
            else {
                throw new PropertiesHandlerException("Error handling 'sqltable' metadata '" + str
                        + "', Connection undefined.");
            }
            logger.debug("Esecuzione select: " + sqlStatement + ".");
            ps = conn.prepareStatement(sqlStatement);
            rs = ps.executeQuery();
            return ResultSetUtils.getResultSetAsXMLString(rs);
        }
        catch (Exception exc) {
            logger.warn("Error handling 'sqltable' metadata '" + sqlStatement + "'", exc);
            if (PropertiesHandler.isExceptionOnErrors()) {
                if (exc instanceof PropertiesHandlerException) {
                    throw (PropertiesHandlerException) exc;
                }
                throw new PropertiesHandlerException("Error handling 'sqltable' metadata '" + str + "'", exc);
            }
            return "sqltable" + PROP_START + str + PROP_END;
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
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            if (intConn && (conn != null)) {
                try {
                    JDBCConnectionBuilder.releaseConnection(connName, conn);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
        }
    }
}
