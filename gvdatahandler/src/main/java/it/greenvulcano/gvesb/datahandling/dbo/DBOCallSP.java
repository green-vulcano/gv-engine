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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DBOException;
import it.greenvulcano.gvesb.datahandling.utils.DiscardCause;
import it.greenvulcano.gvesb.datahandling.utils.ParameterType;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleError;
import it.greenvulcano.gvesb.datahandling.utils.exchandler.oracle.OracleExceptionHandler;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xml.XMLUtilsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * IDBO Class specialized to parse the input RowSet document and in
 * calling stored procedures in the DB.
 * 
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 */
public class DBOCallSP extends AbstractDBO
{
    /**
     * @version 3.0.0 Mar 30, 2010
     * @author GreenVulcano Developer Team
     */
    public class SPCallDescriptor
    {
        private final String ROWSET_NAME = "RowSet";

        private final String DATA_NAME   = "data";

        /**
         * @version 3.0.0 Mar 30, 2010
         * @author GreenVulcano Developer Team
         * 
         */
        public class SPOutputParam
        {
            /**
             * Type of parameter.
             */
            private String  dbType             = null;

            /**
             * Type of parameter.
             */
            private String  javaType           = null;

            /**
             * Type of parameter.
             */
            private String  javaTypeFormat     = null;

            /**
             * Position of parameter in the SQL statement.
             */
            private int     position           = 0;

            /**
             * Precision of numeric parameter.
             */
            private int     precision          = 0;

            /**
             * Return the value in properties map.
             */
            private boolean returnInProperties = false;

            /**
             * Type property name.
             */
            private String  propName           = null;

            private boolean returnInUUID;

            private String  paramName;

            /**
             * SPOutputParam Constructor
             * 
             * @param node
             *        the configuration node
             * @throws DBOException
             *         if an error occurred
             */
            public SPOutputParam(Node node) throws DBOException
            {
                try {
                    dbType = XMLConfig.get(node, "@db-type");
                    javaType = XMLConfig.get(node, "@java-type");
                    javaTypeFormat = XMLConfig.get(node, "@java-type-format", "");
                    position = XMLConfig.getInteger(node, "@position");
                    precision = XMLConfig.getInteger(node, "@precision", 0);
                    returnInProperties = XMLConfig.getBoolean(node, "@return-in-prop", false);
                    returnInUUID = XMLConfig.getBoolean(node, "@return-in-uuid", false);
                    propName = XMLConfig.get(node, "@prop-name", "" + position);
                    paramName = XMLConfig.get(node, "@param-name", "");
                }
                catch (XMLConfigException exc) {
                    throw new DBOException("Error configuring the output parameter: " + exc, exc);
                }
                catch (Throwable exc) {
                    throw new DBOException("Error initializing the output parameter: " + exc, exc);
                }
            }

            /**
             * Set the dbType.<br/>
             * <br/>
             * 
             * @param dbType
             *        The value to set.
             */
            public void setDBType(String dbType)
            {
                this.dbType = dbType;
            }

            /**
             * Set the javaType.<br/>
             * <br/>
             * 
             * @param javaType
             *        The value to set.
             */
            public void setJavaType(String javaType)
            {
                this.javaType = javaType;
            }

            /**
             * Set the javaTypeFormat.<br/>
             * <br/>
             * 
             * @param javaTypeFormat
             *        The value to set.
             */
            public void setJavaTypeFormat(String javaTypeFormat)
            {
                this.javaTypeFormat = javaTypeFormat;
            }

            /**
             * Set the position of parameter into the SQL Statement.<br/>
             * <br/>
             * 
             * @param position
             *        The value to set.
             */
            public void setPosition(int position)
            {
                this.position = position;
            }

            /**
             * Set the numeric precision of parameter.<br/>
             * <br/>
             * 
             * @param precision
             *        The value to set.
             */
            public void setPrecision(int precision)
            {
                this.precision = precision;
            }

            /**
             * Get the dbtype attribute.<br/>
             * <br/>
             * 
             * @return the dbtype
             */
            public String getDBType()
            {
                return dbType;
            }

            /**
             * Get the java type attribute.<br/>
             * <br/>
             * 
             * @return the java type
             */
            public String getJavaType()
            {
                return javaType;
            }

            /**
             * Get the java type format attribute.<br/>
             * <br/>
             * 
             * @return the java type format
             */
            public String getJavaTypeFormat()
            {
                return javaTypeFormat;
            }

            /**
             * Get the precision of numeric parameter.<br/>
             * <br/>
             * 
             * @return the precision
             */
            public int getPrecision()
            {
                return precision;
            }

            /**
             * Get the position of parameter into the SQL Statement.<br/>
             * <br/>
             * 
             * @return the position
             */
            public int getPosition()
            {
                return position;
            }

            /**
             * @return if return object should be set as property
             */
            public boolean isReturnInProperties()
            {
                return returnInProperties;
            }

            /**
             * @return if return object should be set associated to UUID
             *         specified as row attribute.
             */
            public boolean isReturnInUUID()
            {
                return returnInUUID;
            }

            /**
             * Get the propName attribute.<br/>
             * <br/>
             * 
             * @return the propName
             */
            public String getPropName()
            {
                return propName;
            }

            /**
             * @param paramName
             *        the paramName to set
             */
            public void setParamName(String paramName)
            {
                this.paramName = paramName;
            }

            /**
             * @return the paramName
             */
            public String getParamName()
            {
                return paramName;
            }

        }

        private final List<SPOutputParam> spOutputParams = new ArrayList<SPOutputParam>();
        private String                    statement      = "";

        private boolean                   namedParameterMode;

        /**
         * @param node
         * @throws DBOException
         */
        public SPCallDescriptor(Node node) throws DBOException
        {
            try {
                statement = XMLConfig.get(node, "statement[@type='callsp']", "");
                // Reading stored procedure output parameters
                NodeList nlParameters = XMLConfig.getNodeList(node, "SPOutputParameters/SPOutputParameter");
                int iNumParam = nlParameters.getLength();

                namedParameterMode = XMLConfig.getBoolean(node, "@named-parameter-mode", false);

                for (int i = 0; i < iNumParam; i++) {
                    spOutputParams.add(new SPOutputParam(nlParameters.item(i)));
                }

                if (statement.equals("")) {
                    throw new DBOException("Empty/misconfigured statements list for stored procedure call descriptor");
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new DBOException("Error initializing the stored procedure call descriptor", exc);
            }
        }

        /**
         * @return the statement
         */
        public String getStatement()
        {
            return statement;
        }

        /**
         * Specify the output parameter from stored procedure
         * 
         * @param callStmt
         *        the statement
         * @throws DBOException
         *         if an error occurred
         */
        public void specifyOutputParameter(CallableStatement callStmt) throws DBOException
        {
            try {
                for (int i = 0; i < spOutputParams.size(); i++) {
                    SPOutputParam outp = spOutputParams.get(i);
                    String type = outp.getDBType().trim();
                    int iPos = outp.getPosition();
                    int iPrec = outp.getPrecision();
                    logger.debug("Parameter Output[" + (i + 1) + "] Type [" + type + "] Position [" + iPos
                            + "] Precision [" + iPrec + "]...");

                    if (type.equalsIgnoreCase(ParameterType.ORACLE_STRING)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.VARCHAR);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_INT)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.INTEGER);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_LONG)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.BIGINT);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_NUM)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.NUMERIC, iPrec);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.DATE);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_LONG_RAW)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.LONGVARBINARY);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.CLOB);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                        callStmt.registerOutParameter(iPos, java.sql.Types.BLOB);
                    }
                    else if (type.equalsIgnoreCase(ParameterType.ORACLE_CURSOR)) {
                        callStmt.registerOutParameter(iPos, -10); // OracleTypes.CURSOR
                    }
                    else {
                        logger.error("specifyOutputParameter - "
                                + "Error while registring parameters for CallableStatement: "
                                + "parameter type not supported " + type);

                        throw new DBOException(
                                "Error while registring output parameters for CallableStatement: parameter type not supported "
                                        + type);
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (SQLException exc) {
                throw new DBOException("Error while registering output parameters: " + exc, exc);
            }
        }

        /**
         * Specify the output parameter from store procedure
         * 
         * @param callStmt
         *        the statement
         * @param props
         * @throws DBOException
         *         if an error occurred
         */
        public void setOutputParameterValuesInMap(CallableStatement callStmt, Map<String, Object> props)
                throws DBOException
        {
            try {
                for (int i = 0; i < spOutputParams.size(); i++) {
                    SPOutputParam outp = spOutputParams.get(i);
                    if (outp.isReturnInProperties() || outp.isReturnInUUID()) {
                        String dbType = outp.getDBType().trim();
                        String javaType = outp.getJavaType().trim();
                        String propName = outp.getPropName();
                        int iPos = outp.getPosition();
                        String paramName = outp.getParamName();
                        Object value = null;

                        if (javaType.equalsIgnoreCase(ParameterType.JAVA_STRING)) {
                            if (dbType.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                                String format = outp.getJavaTypeFormat();
                                if (format.equals("")) {
                                    format = DEFAULT_DATE_FORMAT;
                                }
                                Timestamp ts = namedParameterMode
                                        ? callStmt.getTimestamp(paramName)
                                        : callStmt.getTimestamp(iPos);
                                value = DateUtils.dateToString(new Date(ts.getTime()), format);
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                            	Clob clob = namedParameterMode
                                        ? callStmt.getClob(paramName)
                                        : callStmt.getClob(iPos);
                                if (clob != null) {
	                                InputStream is = clob.getAsciiStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                	value = new String(baos.toByteArray(), 0, (int) clob.length());
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = baos.toString();
	                                }
	                            }
                                else {
                                	value = "";
                                }
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                            	Blob blob = namedParameterMode
                                        ? callStmt.getBlob(paramName)
                                        : callStmt.getBlob(iPos);
                            	if (blob != null) {
	                                InputStream is = blob.getBinaryStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                    byte[] buffer = Arrays.copyOf(baos.toByteArray(),
	                                            (int) blob.length());
	                                    value = Base64.getEncoder().encodeToString(buffer);
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = Base64.getEncoder().encodeToString(baos.toByteArray());
	                                }
	                            }
                            	else {
                            		value = "";
                            	}
                            }
                            else {
                                value = namedParameterMode ? callStmt.getString(paramName) : callStmt.getString(iPos);
                            }
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_INT)) {
                            int v = namedParameterMode ? callStmt.getInt(paramName) : callStmt.getInt(iPos);
                            value = new Integer(v);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_LONG)) {
                            long v = namedParameterMode ? callStmt.getLong(paramName) : callStmt.getLong(iPos);
                            value = new Long(v);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_DATE)) {
                            Timestamp ts = namedParameterMode
                                    ? callStmt.getTimestamp(paramName)
                                    : callStmt.getTimestamp(iPos);
                            value = new Date(ts.getTime());
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                            // ResultSet not returned in properties
                        }
                        else {
                            throw new DBOException(
                                    "Error while extracting the CallableStatement output parameters: parameter type not supported ("
                                            + javaType + ")");
                        }
                        if (!javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                            if (outp.isReturnInProperties()) {
                                logger.debug("Setting out parameter " + propName + " = " + value + " in properties");
                                props.put(propName, value);
                            }
                            if (outp.isReturnInUUID()) {
                                Object uuid = currentRowFields.get(iPos);
                                if (uuid != null) {
                                    String uuidStr = uuid.toString();
                                    logger.debug("Setting out parameter " + propName + " = " + value + " in UUID "
                                            + uuidStr);
                                    uuids.put(uuidStr, value.toString());
                                }
                            }
                        }
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (IOException exc) {
                throw new DBOException("Error while extracting the CallableStatement output parameters: " + exc, exc);
            }
            catch (SQLException exc) {
                throw new DBOException("Error while extracting the CallableStatement output parameters: " + exc, exc);
            }
        }

        /**
         * Specifies the output parameters from store procedure
         * 
         * @param callStmt
         *        the statement
         * @param xmlOut
         * @param statementId
         * @throws DBOException
         *         if an error occurred
         */
        public void buildOutXml(CallableStatement callStmt, Document xmlOut, String statementId) throws DBOException
        {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                Element docRoot = xmlOut.getDocumentElement();
                if (docRoot == null) {
                    xmlOut.appendChild(xml.createElement(xmlOut, ROWSET_NAME));
                    docRoot = xmlOut.getDocumentElement();
                }
                Element data = xml.createElement(xmlOut, DATA_NAME);
                xml.setAttribute(data, ID_NAME, statementId);
                docRoot.appendChild(data);

                Element row = xml.createElement(xmlOut, ROW_NAME);
                xml.setAttribute(row, ID_NAME, SP_RESULT);
                data.appendChild(row);
                for (int i = 0; i < spOutputParams.size(); i++) {
                    SPOutputParam outp = spOutputParams.get(i);
                    String dbType = outp.getDBType().trim();
                    String javaType = outp.getJavaType().trim();
                    String propName = outp.getPropName();
                    String paramName = outp.getParamName();
                    int iPos = outp.getPosition();
                    String value = null;
                    ResultSet resultSet = null;

                    if (javaType.equalsIgnoreCase(ParameterType.JAVA_RESULTSET)) {
                        Object obj = null;
                        try {
                        	obj = namedParameterMode ? callStmt.getObject(paramName) : callStmt.getObject(iPos);
                        }
                        catch (SQLException exc) {
							// closed cursor?
                        	logger.warn("Error reading Cursor output parameter... Closed cursor?", exc);
						}
                        value = null;
                        if ((obj != null) && (obj instanceof ResultSet)) {
                            resultSet = (ResultSet) obj;
                        }
                    }
                    else {
                        Element col = xml.createElement(xmlOut, COL_NAME);
                        xml.setAttribute(col, ID_NAME, propName);
                        if (javaType.equalsIgnoreCase(ParameterType.JAVA_STRING)) {
                            if (dbType.equalsIgnoreCase(ParameterType.ORACLE_DATE)) {
                                String format = outp.getJavaTypeFormat();
                                if (format.equals("")) {
                                    format = DEFAULT_DATE_FORMAT;
                                }
                                Timestamp ts = namedParameterMode
                                        ? callStmt.getTimestamp(paramName)
                                        : callStmt.getTimestamp(iPos);
                                value = DateUtils.dateToString(new Date(ts.getTime()), format);
                                xml.setAttribute(col, TYPE_NAME, TIMESTAMP_TYPE);
                                xml.setAttribute(col, FORMAT_NAME, format);
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_CLOB)) {
                            	xml.setAttribute(col, TYPE_NAME, LONG_STRING_TYPE);
                            	Clob clob = namedParameterMode
                                        ? callStmt.getClob(paramName)
                                        : callStmt.getClob(iPos);
                                if (clob != null) {
	                                InputStream is = clob.getAsciiStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                	value = new String(baos.toByteArray(), 0, (int) clob.length());
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = baos.toString();
	                                }
	                            }
                                else {
                                	value = "";
                                }
                            }
                            else if (dbType.equalsIgnoreCase(ParameterType.ORACLE_BLOB)) {
                            	xml.setAttribute(col, TYPE_NAME, BASE64_TYPE);
                            	Blob blob = namedParameterMode
                                        ? callStmt.getBlob(paramName)
                                        : callStmt.getBlob(iPos);
                            	if (blob != null) {
	                                InputStream is = blob.getBinaryStream();
	                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                                IOUtils.copy(is, baos);
	                                is.close();
	                                try {
	                                    byte[] buffer = Arrays.copyOf(baos.toByteArray(),
	                                            (int) blob.length());
	                                    value = Base64.getEncoder().encodeToString(buffer);
	                                }
	                                catch (SQLFeatureNotSupportedException exc) {
	                                	value = Base64.getEncoder().encodeToString(baos.toByteArray());
	                                }
	                            }
                            	else {
                            		value = "";
                            	}
                            }
                            else {
                                xml.setAttribute(col, TYPE_NAME, STRING_TYPE);
                                value = namedParameterMode ? callStmt.getString(paramName) : callStmt.getString(iPos);
                            }
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_INT)) {
                            int v = namedParameterMode ? callStmt.getInt(paramName) : callStmt.getInt(iPos);
                            value = Integer.toString(v);
                            xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_LONG)) {
                            long v = namedParameterMode ? callStmt.getLong(paramName) : callStmt.getLong(iPos);
                            value = Long.toString(v);
                            xml.setAttribute(col, TYPE_NAME, NUMERIC_TYPE);
                        }
                        else if (javaType.equalsIgnoreCase(ParameterType.JAVA_DATE)) {
                            String format = outp.getJavaTypeFormat();
                            if (format.equals("")) {
                                format = DEFAULT_DATE_FORMAT;
                            }
                            Timestamp ts = namedParameterMode
                                    ? callStmt.getTimestamp(paramName)
                                    : callStmt.getTimestamp(iPos);
                            value = DateUtils.dateToString(new Date(ts.getTime()), format);
                            xml.setAttribute(col, TYPE_NAME, TIMESTAMP_TYPE);
                            xml.setAttribute(col, FORMAT_NAME, format);
                        }
                        else {
                            throw new DBOException(
                                    "Error while extracting the CallableStatement output parameters: parameter type not supported ("
                                            + javaType + ")");
                        }
                        if (value != null) {
                            Text text = xmlOut.createTextNode(value);
                            col.appendChild(text);
                        }
                        row.appendChild(col);
                    }
                    if (resultSet != null) {
                        try {
                            ResultSetMetaData metadata = resultSet.getMetaData();
                            boolean firstItr = true;
                            Set<Integer> keyField = keysMap.get(statementId);
                            boolean noKey = ((keyField == null) || keyField.isEmpty());
                            Map<String, String> keyAttr = new HashMap<String, String>();
                            String colKey = null;
                            String precKey = null;
                            while (resultSet.next()) {
                                if (!firstItr) {
                                    row = xml.createElement(xmlOut, ROW_NAME);
                                    xml.setAttribute(row, ID_NAME, SP_RESULT);
                                }
                                for (int j = 1; j <= metadata.getColumnCount(); j++) {
                                    Element col = xml.createElement(xmlOut, "col");
                                    xml.setAttribute(col, ID_NAME, propName + "[" + j + "]");
                                    switch (metadata.getColumnType(j)) {
                                        case Types.CLOB : {
                                            Clob clob = resultSet.getClob(j);
                                            if (clob != null) {
                                                InputStream is = clob.getAsciiStream();
                                                byte[] buffer = new byte[2048];
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                                                int size;
                                                while ((size = is.read(buffer)) != -1) {
                                                    baos.write(buffer, 0, size);
                                                }
                                                is.close();
                                                value = baos.toString();
                                            }
                                        }
                                            break;
                                        case Types.BLOB : {
                                            Blob blob = resultSet.getBlob(j);
                                            if (blob != null) {
                                                InputStream is = blob.getBinaryStream();
                                                byte[] buffer = new byte[2048];
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                                                int size;
                                                while ((size = is.read(buffer)) != -1) {
                                                    baos.write(buffer, 0, size);
                                                }
                                                is.close();
                                                value = Base64.getEncoder().encodeToString(baos.toByteArray());
                                            }
                                        }
                                            break;
                                        default : {
                                            value = resultSet.getString(j);
                                            if (value == null) {
                                                value = "";
                                            }
                                        }
                                    }
                                    if (value != null) {
                                        col.appendChild(xmlOut.createTextNode(value));
                                    }
                                    if (!noKey && keyField.contains(new Integer(j))) {
                                        if (value != null) {
                                            if (colKey == null) {
                                                colKey = value;
                                            }
                                            else {
                                                colKey += "##" + value;
                                            }
                                            keyAttr.put("key_" + j, value);
                                        }
                                    }
                                    else {
                                        row.appendChild(col);
                                    }
                                }
                                if (!noKey && (colKey != null) && !colKey.equals(precKey)) {
                                    if (!firstItr) {
                                        data = xml.createElement(xmlOut, DATA_NAME);
                                        xml.setAttribute(data, ID_NAME, statementId);
                                        docRoot.appendChild(data);
                                    }
                                    for (Entry<String, String> keyAttrEntry : keyAttr.entrySet()) {
                                        xml.setAttribute(data, keyAttrEntry.getKey(), keyAttrEntry.getValue());
                                    }
                                    keyAttr.clear();
                                    precKey = colKey;
                                }
                                if (firstItr) {
                                    firstItr = false;
                                }
                                colKey = null;
                                data.appendChild(row);
                            }
                        }
                        finally {
                            resultSet.close();
                        }
                    }
                }
            }
            catch (DBOException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new DBOException("Generic error", exc);
            }
            finally {
                XMLUtils.releaseParserInstance(xml);
            }
        }
    }

    /**
     * Current <code>SPCallDescriptor</code> to invoke the stored procedure.
     */
    SPCallDescriptor                            spCallDescriptor;

    /**
     * Call descriptors cache of stored procedure calls configured and
     * identified by an ID.
     */
    private final Map<String, SPCallDescriptor> spCallDescriptors;

    private Document                            xmlOut;

    /**
     * Private <i>logger</i> instance.
     */
    private static final Logger                 logger = org.slf4j.LoggerFactory.getLogger(DBOCallSP.class);

    private final Map<String, Set<Integer>>     keysMap;

    /**
     * Default constructor.
     * 
     */
    public DBOCallSP()
    {
        super();
        spCallDescriptors = new HashMap<String, SPCallDescriptor>();
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
            forcedMode = XMLConfig.get(config, "@force-mode", MODE_CALL);
            isReturnData = XMLConfig.getBoolean(config, "@return-data", true);
            NodeList callds = XMLConfig.getNodeList(config, "CallDescriptor");
            String id = null;
            String keys = null;
            for (int i = 0; i < callds.getLength(); i++) {
                Node calld = callds.item(i);
                id = XMLConfig.get(calld, "@id", Integer.toString(i));
                keys = XMLConfig.get(calld, "statement[@id='" + id + "']/@keys");
                spCallDescriptors.put(id, new SPCallDescriptor(calld));
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
        }
        catch (Exception exc) {
            logger.error("Error reading configuration of [" + dboclass + "]", exc);
            throw new DBOException("Error reading configuration of [" + dboclass + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object input, Connection conn, Map<String, Object> props) throws DBOException, 
            InterruptedException {
        dataOut = new ByteArrayOutputStream();
        try {
            createOutXML();
            super.execute(input, conn, props);
            storeResult();
        }
        finally {
            dhr.setRead(0);
            dhr.setTotal(0);
            dhr.setInsert(0);
            dhr.setUpdate(0);
            dhr.setDiscard(0);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.io.OutputStream,
     *      java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(OutputStream data, Connection conn, Map<String, Object> props) throws DBOException, 
            InterruptedException {
        dataOut = data;
        try {
            createOutXML();
            super.execute((OutputStream) null, conn, props);
            storeResult();
        }
        finally {
            dhr.setRead(0);
            dhr.setTotal(0);
            dhr.setInsert(0);
            dhr.setUpdate(0);
            dhr.setDiscard(0);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#execute(java.lang.Object,
     *      java.io.OutputStream, java.sql.Connection, java.util.Map)
     */
    @Override
    public void execute(Object dataIn, OutputStream dataOut, Connection conn, Map<String, Object> props)
            throws DBOException, InterruptedException {
        this.dataOut = dataOut;
        try {
            createOutXML();
            super.execute(dataIn, conn, props);
            storeResult();
        }
        finally {
            dhr.setRead(0);
            dhr.setTotal(0);
            dhr.setInsert(0);
            dhr.setUpdate(0);
            dhr.setDiscard(0);
        }
    }

    /**
     * @throws DBOException
     * 
     */
    private void storeResult() throws DBOException
    {
        if (dataOut != null) {
            XMLUtils xml = null;
            try {
                xml = XMLUtils.getParserInstance();
                byte[] dataDOM = xml.serializeDOMToByteArray(xmlOut);
                dataOut.write(dataDOM);
            }
            catch (Exception exc) {
                throw new DBOException("Cannot store DBOCallSP result.", exc);
            }
            finally {
                XMLUtils.releaseParserInstance(xml);
            }
        }
        xmlOut = null;
    }

    /**
     * @throws DBOException
     * 
     */
    private void createOutXML() throws DBOException
    {
        XMLUtils xml = null;
        try {
            xml = XMLUtils.getParserInstance();
            this.xmlOut = xml.newDocument();
        }
        catch (XMLUtilsException exc) {
            throw new DBOException("Cannot instantiate XMLUtils.", exc);
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    private void handleOutput() throws DBOException
    {
        try {
            CallableStatement sqlStatement = (CallableStatement) sqlStatementInfo.getStatement();
            spCallDescriptor.setOutputParameterValuesInMap(sqlStatement, getCurrentProps());
            spCallDescriptor.buildOutXml(sqlStatement, xmlOut, sqlStatementInfo.getId());
        }
        catch (DBOException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new DBOException("Error processing output parameters: " + exc.getMessage(), exc);
        }
    }

    private final String          SP_RESULT    = "sp_result";

    /**
     *
     */
    protected static final String OUTONLY_ATTR = "out-only";

    private int                   colIdx       = 0;

    private String                currType;

    private String                currDateFormat;

    private String                currNumberFormat;

    private String                currGroupSeparator;

    private String                currDecSeparator;

    private StringBuffer          textBuffer;

    private boolean               colDataExpecting;

    private OutputStream          dataOut;

    private String                currentUUID;

    private boolean               outOnly;

    private String                currName;

    private boolean               useName;

    /**
     * @see it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO#getStatement(java.lang.String)
     */
    @Override
    protected void getStatement(String id) throws SAXException
    {
        if (id == null) {
            id = "0";
        }
        if ((sqlStatementInfo == null) || !getCurrentId().equals(id)) {
            try {
                if (sqlStatementInfo != null) {
                    sqlStatementInfo.close();
                }
                spCallDescriptor = spCallDescriptors.get(id);
                if (spCallDescriptor == null) {
                    logger.error("SQL Call descriptor with id " + id + " not found.");
                    throw new SAXException("SQL Call descriptor with id " + id + " not found.");
                }
                String expandedSQL = PropertiesHandler.expand(spCallDescriptor.getStatement(), getCurrentProps(),
                        null, getInternalConn());
                logger.debug("expandedSQL stmt: " + expandedSQL);
                Statement statement = getInternalConn().prepareCall(expandedSQL);
                sqlStatementInfo = new StatementInfo(id, expandedSQL, statement);
                spCallDescriptor.specifyOutputParameter((CallableStatement) statement);
                setCurrentId(id);
            }
            catch (SAXException exc) {
                throw exc;
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
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (ROW_NAME.equals(localName)) {
            currentRowFields.clear();
            currentRowFields.add(null);
            colDataExpecting = false;
            colIdx = 0;
            String id = attributes.getValue(uri, ID_NAME);
            getStatement(id);
            currentXSLMessage = attributes.getValue(uri, XSL_MSG_NAME);
            String statsOn = attributes.getValue(uri, STATS_ON_NAME);
            statsOnInsert = !(STATS_UPD_MODE.equals(statsOn));
            String incrDisc = attributes.getValue(uri, INCR_DISC_NAME);
            incrDiscIfUpdKO = !(INCR_DISC_N_MODE.equals(incrDisc));
            currCriticalError = "true".equalsIgnoreCase(attributes.getValue(uri, CRITICAL_ERROR));
        }
        else if (COL_NAME.equals(localName)) {
            currType = attributes.getValue(uri, TYPE_NAME);
            currName = attributes.getValue(uri, NAME_ATTR);
            useName = (currName != null) && (currName.trim().length() > 0);
            currentUUID = attributes.getValue(uri, UUID_NAME);
            String outOnlyStr = attributes.getValue(uri, OUTONLY_ATTR);
            outOnly = outOnlyStr != null ? outOnlyStr.equalsIgnoreCase("true") : false;
            if (!outOnly) {
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
                textBuffer = new StringBuffer();
            }
            colIdx++;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (colDataExpecting) {
            textBuffer.append(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
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
                resultMessage.append("SQL Statement Informations:\n").append(sqlStatementInfo);
                resultMessage.append("Record parameters:\n").append(dumpCurrentRowFields());
                resultStatus = STATUS_PARTIAL;
            }
        }
        else if (COL_NAME.equals(localName)) {
            CallableStatement cs = (CallableStatement) sqlStatementInfo.getStatement();
            try {
                if (!outOnly) {
                    colDataExpecting = false;
                    String text = textBuffer.toString();
                    if ((currentUUID != null) && (currentUUID.trim().length() > 0) && (text.length() == 0)) {
                        text = uuids.get(currentUUID);
                        if (text == null) {
                            text = currentUUID;
                        }
                    }
                    if (TIMESTAMP_TYPE.equals(currType) || DATE_TYPE.equals(currType) || TIME_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            if (TIMESTAMP_TYPE.equals(currType)) setNull(cs, Types.TIMESTAMP);
                            else if (DATE_TYPE.equals(currType)) setNull(cs, Types.DATE);
                            else setNull(cs, Types.TIME);
                            currentRowFields.add(null);
                        }
                        else {
                            dateFormatter.applyPattern(currDateFormat);
                            Date formattedDate = dateFormatter.parse(text);
                            if (TIMESTAMP_TYPE.equals(currType)) {
                                Timestamp ts = new Timestamp(formattedDate.getTime());
                                setTimestamp(cs, ts);
                                currentRowFields.add(ts);
                            }
                            else if (DATE_TYPE.equals(currType)) {
                                java.sql.Date d = new java.sql.Date(formattedDate.getTime());
                                setDate(cs, d);
                                currentRowFields.add(d);
                            }
                            else {
                                java.sql.Time t = new java.sql.Time(formattedDate.getTime());
                                setTime(cs, t);
                                currentRowFields.add(t);
                            } 
                        }
                    }
                    else if (INTEGER_TYPE.equals(currType) || SMALLINT_TYPE.equals(currType) || BIGINT_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            if (INTEGER_TYPE.equals(currType)) setNull(cs, Types.INTEGER); 
                            else if (SMALLINT_TYPE.equals(currType)) setNull(cs, Types.SMALLINT);
                            else setNull(cs, Types.BIGINT);
                            currentRowFields.add(null);
                        }
                        else {
                            if (INTEGER_TYPE.equals(currType)) setInt(cs, Integer.parseInt(text, 10)); 
                            else if (SMALLINT_TYPE.equals(currType)) setShort(cs, Short.parseShort(text, 10));
                            else setLong(cs, Long.parseLong(text, 10));
                            currentRowFields.add(text);
                        }
                    }
                    else if (FLOAT_TYPE.equals(currType) || DOUBLE_TYPE.equals(currType) || DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            if (DECIMAL_TYPE.equals(currType) || NUMERIC_TYPE.equals(currType)) setNull(cs, Types.NUMERIC);
                            else if (FLOAT_TYPE.equals(currType)) setNull(cs, Types.FLOAT);
                            else setNull(cs, Types.DOUBLE);
                            currentRowFields.add(null);
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
                                    setBigDecimal(cs, formattedNumber);
                                    currentRowFields.add(formattedNumber);
                                }
                                else if (FLOAT_TYPE.equals(currType)) {
                                    setFloat(cs, formattedNumber.floatValue());
                                    currentRowFields.add(formattedNumber.floatValue());
                                }
                                else {
                                    setDouble(cs, formattedNumber.doubleValue());
                                    currentRowFields.add(formattedNumber.doubleValue());
                                }
                            }
                            finally {
                                numberFormatter.setParseBigDecimal(isBigDecimal);
                            }
                        }
                    }
                    else if (LONG_STRING_TYPE.equals(currType) || LONG_NSTRING_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            if (LONG_STRING_TYPE.equals(currType)) setNull(cs, Types.CLOB);
                            else setNull(cs, Types.NCLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            if (LONG_STRING_TYPE.equals(currType)) {
                                setCharacterStream(cs, new StringReader(text));
                                currentRowFields.add(text);
                            }
                            else {
                                setNCharacterStream(cs, new StringReader(text));
                                currentRowFields.add(text);
                            }
                        }
                    }
                    else if (BASE64_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.BLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            data = Base64.getDecoder().decode(data);
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            setBinaryStream(cs, bais, data.length);
                            currentRowFields.add(text);
                        }
                    }
                    else if (BINARY_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.BLOB);
                            currentRowFields.add(null);
                        }
                        else {
                            byte[] data = text.getBytes();
                            ByteArrayInputStream bais = new ByteArrayInputStream(data);
                            setBinaryStream(cs, bais, data.length);
                            currentRowFields.add(text);
                        }
                    }
                    else if (BOOLEAN_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.BOOLEAN);
                            currentRowFields.add(null);
                        }
                        else {
                            setBoolean(cs, TextUtils.parseBoolean(text));
                            currentRowFields.add(text);
                        }
                    }
                    else if (XML_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.SQLXML);
                            currentRowFields.add(null);
                        }
                        else {
                            SQLXML xml = cs.getConnection().createSQLXML();
                            xml.setString(text);
                            setSQLXML(cs, xml);
                            currentRowFields.add(text);
                        }
                    }
                    else if (NSTRING_TYPE.equals(currType)) {
                        if (text.equals("")) {
                            setNull(cs, Types.NVARCHAR);
                            currentRowFields.add(null);
                        }
                        else {
                            setNString(cs, text);
                            currentRowFields.add(text);
                        }
                    }
                    else {
                        if (text.equals("")) {
                            setNull(cs, Types.VARCHAR);
                            currentRowFields.add(null);
                        }
                        else {
                            setString(cs, text);
                            currentRowFields.add(text);
                        }
                    }
                }
                else {
                    currentRowFields.add(currentUUID);
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
     * @param cs
     * @param ts
     * @throws SQLException
     */
    private void setTimestamp(CallableStatement cs, Timestamp ts) throws SQLException
    {
        if (useName) {
            cs.setTimestamp(currName, ts);
        }
        else {
            cs.setTimestamp(colIdx, ts);
        }
    }

    private void setTime(CallableStatement cs, Time t) throws SQLException {
        if (useName) {
            cs.setTime(currName, t);
        }
        else {
            cs.setTime(colIdx, t);
        }
    }

    private void setDate(CallableStatement cs, java.sql.Date d) throws SQLException {
        if (useName) {
            cs.setDate(currName, d);
        }
        else {
            cs.setDate(colIdx, d);
        }
    }

    /**
     * @param cs
     * @param num
     * @throws SQLException
     */
    private void setInt(CallableStatement cs, int num) throws SQLException
    {
        if (useName) {
            cs.setInt(currName, num);
        }
        else {
            cs.setInt(colIdx, num);
        }
    }

    private void setLong(CallableStatement cs, long num) throws SQLException {
        if (useName) {
            cs.setLong(currName, num);
        }
        else {
            cs.setLong(colIdx, num);
        }
    }

    private void setShort(CallableStatement cs, short num) throws SQLException {
        if (useName) {
            cs.setShort(currName, num);
        }
        else {
            cs.setShort(colIdx, num);
        }
    }

    /**
     * @param cs
     * @param num
     * @throws SQLException
     */
    private void setFloat(CallableStatement cs, float num) throws SQLException
    {
        if (useName) {
            cs.setFloat(currName, num);
        }
        else {
            cs.setFloat(colIdx, num);
        }
    }

    private void setDouble(CallableStatement cs, double num) throws SQLException {
        if (useName) {
            cs.setDouble(currName, num);
        }
        else {
            cs.setDouble(colIdx, num);
        }
    }

    private void setBigDecimal(CallableStatement cs, BigDecimal num) throws SQLException {
        if (useName) {
            cs.setBigDecimal(currName, num);
        }
        else {
            cs.setBigDecimal(colIdx, num);
        }
    }

    /**
     * @param cs
     * @param bais
     * @param length
     * @throws SQLException
     */
    private void setBinaryStream(CallableStatement cs, ByteArrayInputStream bais, int length) throws SQLException
    {
        if (useName) {
            cs.setBinaryStream(currName, bais, length);
        }
        else {
            cs.setBinaryStream(colIdx, bais, length);
        }
    }

    private void setNCharacterStream(CallableStatement cs, StringReader sr) throws SQLException {
        if (useName) {
            cs.setNCharacterStream(currName, sr);
        }
        else {
            cs.setNCharacterStream(colIdx, sr);
        }
    }

    private void setCharacterStream(CallableStatement cs, StringReader sr) throws SQLException {
        if (useName) {
            cs.setCharacterStream(currName, sr);
        }
        else {
            cs.setCharacterStream(colIdx, sr);
        }
    }

    private void setSQLXML(CallableStatement cs, SQLXML xml) throws SQLException {
        if (useName) {
            cs.setSQLXML(currName, xml);
        }
        else {
            cs.setSQLXML(colIdx, xml);
        }
    }

    private void setBoolean(CallableStatement cs, boolean b) throws SQLException {
        if (useName) {
            cs.setBoolean(currName, b);
        }
        else {
            cs.setBoolean(colIdx, b);
        }
    }

    private void setNString(CallableStatement cs, String text) throws SQLException {
        if (useName) {
            cs.setNString(currName, text);
        }
        else {
            cs.setNString(colIdx, text);
        }
    }

    /**
     * @param cs
     * @param text
     * @throws SQLException
     */
    private void setString(CallableStatement cs, String text) throws SQLException
    {
        if (useName) {
            cs.setString(currName, text);
        }
        else {
            cs.setString(colIdx, text);
        }
    }

    /**
     * @param cs
     * @throws SQLException
     */
    private void setNull(CallableStatement cs, int type) throws SQLException
    {
        if (useName) {
            cs.setNull(currName, type);
        }
        else {
            cs.setNull(colIdx, type);
        }
    }

    /**
     * @throws SAXException
     */
    @Override
    protected void executeStatement() throws SAXException
    {
        try {
            if (sqlStatementInfo != null) {
                super.executeStatement();
                handleOutput();
            }
        }
        catch (DBOException exc) {
            logger.error("Record parameters:\n" + dumpCurrentRowFields());
            logger.error("SQL Statement Informations:\n" + sqlStatementInfo);
            logger.error("DBOException error on row " + getRowCounter() + ": " + exc.getMessage());
            throw new SAXException(exc);
        }
    }
}
