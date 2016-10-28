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
package it.greenvulcano.gvesb.datahandling.dbo.utils;

import it.greenvulcano.gvesb.datahandling.dbo.AbstractDBO;
import it.greenvulcano.gvesb.datahandling.utils.FieldFormatter;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * 
 * @version 3.4.0 06/ago/2013
 * @author GreenVulcano Developer Team
 * 
 */
public class StandardRowSetBuilder implements RowSetBuilder
{
    private String           name;
    private Logger           logger;
    private String           numberFormat;
    private String           groupSeparator;
    private String           decSeparator;
    private XMLUtils         parser;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private DecimalFormat    numberFormatter;

    public Document createDocument(XMLUtils parser) throws NullPointerException {
        if (parser == null) {
            parser = this.parser;
        }
        if (parser == null) {
            throw new NullPointerException("Parser not set");
        }
        Document doc = parser.newDocument(AbstractDBO.ROWSET_NAME);
        return doc;
    }

    public int build(Document doc, String id, ResultSet rs, Set<Integer> keyField,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        if (rs == null) {
            return 0;
        }
        int rowCounter = 0;
        Element docRoot = doc.getDocumentElement();
        ResultSetMetaData metadata = rs.getMetaData();
        FieldFormatter[] fFormatters = buildFormatterArray(metadata, fieldNameToFormatter, fieldIdToFormatter);

        boolean noKey = ((keyField == null) || keyField.isEmpty());

        //boolean isNull = false;
        Element data = null;
        Element row = null;
        Element col = null;
        Text text = null;
        String textVal = null;
        String precKey = null;
        String colKey = null;
        Map<String, String> keyAttr = new HashMap<String, String>();
        while (rs.next()) {
            if (rowCounter % 10 == 0) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), name, logger);
            }
            row = parser.createElement(doc, AbstractDBO.ROW_NAME);

            parser.setAttribute(row, AbstractDBO.ID_NAME, id);
            for (int j = 1; j <= metadata.getColumnCount(); j++) {
                FieldFormatter fF = fFormatters[j];

                //isNull = false;
                col = parser.createElement(doc, AbstractDBO.COL_NAME);
                switch (metadata.getColumnType(j)) {
                    case Types.DATE : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DATE_TYPE);
                        java.sql.Date dateVal = rs.getDate(j);
                        textVal = processDateTime(col, fF, dateVal, AbstractDBO.DEFAULT_DATE_FORMAT);
                    }
                        break;
                    case Types.TIME : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.TIME_TYPE);
                        java.sql.Time dateVal = rs.getTime(j);
                        textVal = processDateTime(col, fF, dateVal, AbstractDBO.DEFAULT_TIME_FORMAT);
                    }
                        break;
                    case Types.TIMESTAMP : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.TIMESTAMP_TYPE);
                        Timestamp dateVal = rs.getTimestamp(j);
                        textVal = processDateTime(col, fF, dateVal, AbstractDBO.DEFAULT_DATE_FORMAT);
                    }
                        break;
                    case Types.DOUBLE : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                        double numVal = rs.getDouble(j);
                        textVal = processDouble(col, fF, numVal);
                    }
                        break;
                    case Types.FLOAT :
                    case Types.REAL : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                        float numVal = rs.getFloat(j);
                        textVal = processDouble(col, fF, numVal);
                    }
                        break;
                    case Types.BIGINT : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BIGINT_TYPE);
                        long numVal = rs.getLong(j);
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        textVal = String.valueOf(numVal);
                    }
                        break;
                    case Types.INTEGER : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.INTEGER_TYPE);
                        int numVal = rs.getInt(j);
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        textVal = String.valueOf(numVal);
                    }
                        break;
                    case Types.SMALLINT :
                    case Types.TINYINT : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.SMALLINT_TYPE);
                        short numVal = rs.getShort(j);
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        textVal = String.valueOf(numVal);
                    }
                        break; 
                    case Types.NUMERIC :
                    case Types.DECIMAL : {
                        BigDecimal bigdecimal = rs.getBigDecimal(j);
                        boolean isNull = bigdecimal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DECIMAL_TYPE);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                            }
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DECIMAL_TYPE);
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                                parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                textVal = fF.formatNumber(bigdecimal);
                            }
                            else if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DECIMAL_TYPE);
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, numberFormat);
                                parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, groupSeparator);
                                parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, decSeparator);
                                textVal = numberFormatter.format(bigdecimal);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                                textVal = bigdecimal.toString();
                            }
                        }
                    }
                        break;
                    case Types.BOOLEAN : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BOOLEAN_TYPE);
                        boolean bVal = rs.getBoolean(j);
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
                        textVal = String.valueOf(bVal);
                    }
                        break;
                    case Types.SQLXML : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.XML_TYPE);
                        SQLXML xml = rs.getSQLXML(j);
                        boolean isNull = xml == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = ""; 
                        }
                        else {
                            textVal = xml.getString();
                        }
                    }
                        break;
                    case Types.NCHAR :
                    case Types.NVARCHAR : {
                    	parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NSTRING_TYPE);
                        textVal = rs.getNString(j);
                        if (textVal == null) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.CHAR :
                    case Types.VARCHAR : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.STRING_TYPE);
                        textVal = rs.getString(j);
                        boolean isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.NCLOB : {
                    	parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_NSTRING_TYPE);
                        NClob clob = rs.getNClob(j);
                        if (clob != null) {
                            Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                        else {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.CLOB : {
                    	parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_STRING_TYPE);
                        Clob clob = rs.getClob(j);
                        if (clob != null) {
                        	Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                        else {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.BLOB : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BASE64_TYPE);
                        Blob blob = rs.getBlob(j);
                        boolean isNull = blob == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                            InputStream is = blob.getBinaryStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.copy(is, baos);
                            is.close();
                            try {
                                byte[] buffer = Arrays.copyOf(baos.toByteArray(), (int) blob.length());
                                textVal = Base64.getEncoder().encodeToString(buffer);
                            }
                            catch (SQLFeatureNotSupportedException exc) {
                                textVal = Base64.getEncoder().encodeToString(baos.toByteArray());
                            }
                        }
                    }
                        break;
                    default : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.DEFAULT_TYPE);
                        textVal = rs.getString(j);
                        boolean isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                }
                if (textVal != null) {
                    text = doc.createTextNode(textVal);
                    col.appendChild(text);
                }
                if (!noKey && keyField.contains(new Integer(j))) {
                    if (textVal != null) {
                        if (colKey == null) {
                            colKey = textVal;
                        }
                        else {
                            colKey += "##" + textVal;
                        }
                        keyAttr.put("key_" + j, textVal);
                    }
                }
                else {
                    row.appendChild(col);
                }
            }
            if (noKey) {
                if (data == null) {
                    data = parser.createElement(doc, AbstractDBO.DATA_NAME);
                    parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                }
            }
            else if ((colKey != null) && !colKey.equals(precKey)) {
                if (data != null) {
                    docRoot.appendChild(data);
                }
                data = parser.createElement(doc, AbstractDBO.DATA_NAME);
                parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                for (Entry<String, String> keyAttrEntry : keyAttr.entrySet()) {
                    parser.setAttribute(data, keyAttrEntry.getKey(), keyAttrEntry.getValue());
                }
                keyAttr.clear();
                precKey = colKey;
            }
            colKey = null;
            data.appendChild(row);
            rowCounter++;
        }
        if (data != null) {
            docRoot.appendChild(data);
        }

        return rowCounter;
    }

    /**
     * @param col
     * @param fF
     * @param numVal
     * @return
     * @throws NullPointerException
     * @throws Exception
     */
    private String processDouble(Element col, FieldFormatter fF, double numVal) throws NullPointerException, Exception {
        String textVal;
        parser.setAttribute(col, AbstractDBO.NULL_NAME, "false");
        if (fF != null) {
            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
            parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
            parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
            textVal = fF.formatNumber(numVal);
        }
        else {
            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, numberFormat);
            parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, groupSeparator);
            parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, decSeparator);
            textVal = numberFormatter.format(numVal);
        }
        return textVal;
    }

    /**
     * @param col
     * @param fF
     * @param dateVal
     * @return
     * @throws Exception
     */
    private String processDateTime(Element col, FieldFormatter fF, Date dateVal, String format)
            throws Exception {
        String textVal;
        boolean isNull = dateVal == null;
        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
        if (isNull) {
            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, format);
            textVal = "";
        }
        else {
            if (fF != null) {
                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getDateFormat());
                textVal = fF.formatDate(dateVal);
            }
            else {
                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, format);
                if (dateVal instanceof Time) {
                    textVal = timeFormatter.format(dateVal);
                }
                else {
                    textVal = dateFormatter.format(dateVal);
                }
            }
        }
        return textVal;
    }

    public void cleanup() {
        numberFormat = null;
        groupSeparator = null;
        decSeparator = null;
        parser = null;
        dateFormatter = null;
        timeFormatter = null;
        numberFormatter = null;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setDateFormatter(SimpleDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }
    
    public void setTimeFormatter(SimpleDateFormat timeFormatter) {
        this.timeFormatter = timeFormatter;
    }

    public void setNumberFormatter(DecimalFormat numberFormatter) {
        this.numberFormatter = numberFormatter;
    }

    public void setDecSeparator(String decSeparator) {
        this.decSeparator = decSeparator;
    }

    public void setGroupSeparator(String groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void setXMLUtils(XMLUtils parser) {
        this.parser = parser;
    }

    @Override
    public RowSetBuilder getCopy() {
        StandardRowSetBuilder copy = new StandardRowSetBuilder();
        
        copy.name = this.name;
        copy.logger = this.logger;
        copy.numberFormat = this.numberFormat;
        copy.groupSeparator = this.groupSeparator;
        copy.decSeparator = this.decSeparator;
        copy.parser = this.parser;
        copy.dateFormatter = this.dateFormatter;
        copy.numberFormatter = this.numberFormatter;

        return copy;
    }

    private FieldFormatter[] buildFormatterArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        FieldFormatter[] fFA = new FieldFormatter[rsm.getColumnCount() + 1];

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
