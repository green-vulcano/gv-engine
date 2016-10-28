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
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
public class ExtendedRowSetBuilder implements RowSetBuilder
{
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
    private static final String NS          = "http://www.greenvulcano.com/database";
    private String           name;
    private Logger           logger;
    private String           numberFormat;
    private String           groupSeparator;
    private String           decSeparator;
    private XMLUtils         parser;
    private SimpleDateFormat dateFormatter;
    @SuppressWarnings("unused")
	private SimpleDateFormat timeFormatter;
    private DecimalFormat    numberFormatter;
    private FieldFormatter[] fFormatters; 
    private String[]         colNames; 

    public Document createDocument(XMLUtils parser) throws NullPointerException {
        if (parser == null) {
            parser = this.parser;
        }
        if (parser == null) {
            throw new NullPointerException("Parser not set");
        }
        Document doc = parser.newDocument(AbstractDBO.ROWSET_NAME, NS);
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
        buildFormatterAndNamesArray(metadata, fieldNameToFormatter, fieldIdToFormatter);

        boolean noKey = ((keyField == null) || keyField.isEmpty());
        boolean isKeyCol = false;

        boolean isNull = false;
        Element data = null;
        Element row = null;
        Element col = null;
        Text text = null;
        String textVal = null;
        String precKey = null;
        String colKey = null;
        Map<String, Element> keyCols = new TreeMap<String, Element>();
        while (rs.next()) {
            if (rowCounter % 10 == 0) {
                ThreadUtils.checkInterrupted(getClass().getSimpleName(), name, logger);
            }
            row = parser.createElementNS(doc, AbstractDBO.ROW_NAME, NS);

            parser.setAttribute(row, AbstractDBO.ID_NAME, id);
            for (int j = 1; j <= metadata.getColumnCount(); j++) {
                FieldFormatter fF = fFormatters[j];
                String colName = colNames[j];

                isKeyCol =  (!noKey && keyField.contains(new Integer(j)));
                isNull = false;
                col = parser.createElementNS(doc, colName, NS);
                if (isKeyCol) {
                    parser.setAttribute(col, AbstractDBO.ID_NAME, String.valueOf(j));
                }
                switch (metadata.getColumnType(j)) {
                    case Types.DATE :
                    case Types.TIME :
                    case Types.TIMESTAMP : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.TIMESTAMP_TYPE);
                        Timestamp dateVal = rs.getTimestamp(j);
                        isNull = dateVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getDateFormat());
                                textVal = fF.formatDate(dateVal);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, AbstractDBO.DEFAULT_DATE_FORMAT);
                                textVal = dateFormatter.format(dateVal);
                            }
                        }
                    }
                        break;
                    case Types.DOUBLE :
                    case Types.FLOAT :
                    case Types.REAL : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                        float numVal = rs.getFloat(j);
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
                    }
                        break;
                    case Types.BIGINT :
                    case Types.INTEGER :
                    case Types.NUMERIC :
                    case Types.SMALLINT :
                    case Types.TINYINT : {
                        BigDecimal bigdecimal = rs.getBigDecimal(j);
                        isNull = bigdecimal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                            }
                            else {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NUMERIC_TYPE);
                            }
                            textVal = "";
                        }
                        else {
                            if (fF != null) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
                                parser.setAttribute(col, AbstractDBO.FORMAT_NAME, fF.getNumberFormat());
                                parser.setAttribute(col, AbstractDBO.GRP_SEPARATOR_NAME, fF.getGroupSeparator());
                                parser.setAttribute(col, AbstractDBO.DEC_SEPARATOR_NAME, fF.getDecSeparator());
                                textVal = fF.formatNumber(bigdecimal);
                            }
                            else if (metadata.getScale(j) > 0) {
                                parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.FLOAT_TYPE);
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
                    case Types.NCHAR :
                    case Types.NVARCHAR : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.NSTRING_TYPE);
                        textVal = rs.getNString(j);
                        isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.CHAR :
                    case Types.VARCHAR : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.STRING_TYPE);
                        textVal = rs.getString(j);
                        isNull = textVal == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                    }
                        break;
                    case Types.NCLOB : {
                    	parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_NSTRING_TYPE);
                        NClob clob = rs.getNClob(j);
                        isNull = clob == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                            Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                    }
                        break;
                    case Types.CLOB : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.LONG_STRING_TYPE);
                        Clob clob = rs.getClob(j);
                        isNull = clob == null;
                        parser.setAttribute(col, AbstractDBO.NULL_NAME, String.valueOf(isNull));
                        if (isNull) {
                            textVal = "";
                        }
                        else {
                        	Reader is = clob.getCharacterStream();
                            StringWriter str = new StringWriter();

                            IOUtils.copy(is, str);
                            is.close();
                            textVal = str.toString();
                        }
                    }
                        break;
                    case Types.BLOB : {
                        parser.setAttribute(col, AbstractDBO.TYPE_NAME, AbstractDBO.BASE64_TYPE);
                        Blob blob = rs.getBlob(j);
                        isNull = blob == null;
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
                        isNull = textVal == null;
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
                if (isKeyCol) {
                    if (textVal != null) {
                        if (colKey == null) {
                            colKey = textVal;
                        }
                        else {
                            colKey += "##" + textVal;
                        }
                        keyCols.put(String.valueOf(j), col);
                    }
                }
                else {
                    row.appendChild(col);
                }
            }
            if (noKey) {
                if (data == null) {
                    data = parser.createElementNS(doc, AbstractDBO.DATA_NAME, NS);
                    parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                }
            }
            else if ((colKey != null) && !colKey.equals(precKey)) {
                if (data != null) {
                    docRoot.appendChild(data);
                }
                data = parser.createElementNS(doc, AbstractDBO.DATA_NAME, NS);
                parser.setAttribute(data, AbstractDBO.ID_NAME, id);
                Element key = parser.createElementNS(doc, AbstractDBO.KEY_NAME, NS);
                data.appendChild(key);
                for (Entry<String, Element> keyColsEntry : keyCols.entrySet()) {
                    key.appendChild(keyColsEntry.getValue());
                }
                keyCols.clear();
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

    public void cleanup() {
        numberFormat = null;
        groupSeparator = null;
        decSeparator = null;
        parser = null;
        dateFormatter = null;
        numberFormatter = null;
        fFormatters = null; 
        colNames = null;
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
        ExtendedRowSetBuilder copy = new ExtendedRowSetBuilder();
        
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

    private void buildFormatterAndNamesArray(ResultSetMetaData rsm,
            Map<String, FieldFormatter> fieldNameToFormatter, Map<String, FieldFormatter> fieldIdToFormatter)
            throws Exception {
        fFormatters = new FieldFormatter[rsm.getColumnCount() + 1];
        colNames = new String[rsm.getColumnCount() + 1];

        for (int i = 1; i < fFormatters.length; i++) {
            String cName = rsm.getColumnLabel(i);
            if (cName == null) {
                cName = rsm.getColumnName(i);
            }
            colNames[i] = adaptName(cName);
            FieldFormatter fF = fieldNameToFormatter.get(cName);
            if (fF == null) {
                fF = fieldIdToFormatter.get("" + i);
            }
            fFormatters[i] = fF;
        }
    }
    
    private String adaptName(String cName) {
        String res = "";
        for (int i = 0; i < cName.length(); i++) {
            String c = cName.substring(i, i+1);
            if (VALID_CHARS.indexOf(c) != -1) {
                res += c;
            }
            else {
                res += "_";
            }
        }
        return res;
    }
}
