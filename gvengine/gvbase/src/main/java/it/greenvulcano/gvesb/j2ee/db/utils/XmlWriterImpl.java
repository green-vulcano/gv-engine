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
package it.greenvulcano.gvesb.j2ee.db.utils;

import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.xpath.XPathDOMBuilder;
import it.greenvulcano.util.xpath.XPathUtilsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.w3c.dom.Document;

/**
 * An implementation of the XmlWriter interface, which writes a ResultSet object
 * to an output stream as an XML document
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XmlWriterImpl implements XmlWriter
{
    private static final Logger logger          = org.slf4j.LoggerFactory.getLogger(XmlWriterImpl.class);

    /**
     * The XPath DOM builder
     */
    private XPathDOMBuilder     xpathDOMBuilder = null;

    /**
     * The document
     */
    private Document            document        = null;

    /**
     * The DTD
     */
    private String              strPublicDtdID  = "";

    /**
     * The system identification
     */
    private String              strSystemID     = "";

    /**
     * The structure
     */
    private int                 iStructure      = XmlWriter.METADATA_AND_DATA;

    /**
     * If the result set is empty or not
     */
    private boolean             rsEmpty         = true;

    /**
     * The number of record read
     */
    private int                 recordCount     = 0;

    /**
     * Get the DTD identification
     *
     * @return the identification DTD string
     */
    public String getPublicDtdID()
    {
        return strPublicDtdID;
    }

    /**
     * The system identification
     *
     * @return the system identification
     */
    public String getSystemID()
    {
        return strSystemID;
    }

    /**
     * get the structure
     *
     * @return the structure
     */
    public int getStructure()
    {
        return iStructure;
    }

    /**
     * set the identification DTD
     *
     * @param strPublicDtdID
     *        the DTD identification
     */
    public void setPublicDtdID(String strPublicDtdID)
    {
        this.strPublicDtdID = strPublicDtdID;
    }

    /**
     * The system identification
     *
     * @param strSystemID
     *        the system identification
     */
    public void setSystemID(String strSystemID)
    {
        this.strSystemID = strSystemID;
    }

    /**
     * set the structure
     *
     * @param iStructure
     *        the structure
     */
    @Override
    public void setStructure(int iStructure)
    {
        this.iStructure = iStructure;
    }

    /**
     * Writes the given ResultSet object to the specified java.io.OutputStream
     * as an XML document.
     *
     * @param resultSet
     *        input ResultSet.
     * @param outputStream
     *        output stream.
     * @param enc
     *        encoding used for output stream.
     * @param inputStream
     *        transformation to apply.
     * @throws XmlWriterException
     *         if errors occur.
     */
    @Override
    public void writeXML(ResultSet resultSet, OutputStream outputStream, String enc, InputStream inputStream)
            throws XmlWriterException
    {
        Transformer transformer = null;

        try {
            xpathDOMBuilder = new XPathDOMBuilder();
            document = xpathDOMBuilder.createNewDocument();

            writeResultSet(resultSet);

            if (inputStream == null) {
                transformer = TransformerFactory.newInstance().newTransformer();
            }
            else {
                transformer = (TransformerFactory.newInstance()).newTransformer(new StreamSource(inputStream));
            }

            transformer.setOutputProperty(OutputKeys.ENCODING, enc);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, new StreamResult(outputStream));
        }
        catch (TransformerException exc) {
            logger.error("writeXML - Error while transforming Document: ", exc);

            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while transforming Document: " + exc}}, exc);
        }
        catch (Exception exc) {
            logger.error("writeXML - Error while creating Document: ", exc);

            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while creating Document: " + exc}}, exc);
        }
    }

    /**
     * Writes the given ResultSet object to the specified java.io.OutputStream
     * as an XML document.
     *
     * @param resultSet
     *        input ResultSet.
     * @param outputStream
     *        output stream.
     * @param inputStream
     *        transformation to apply.
     * @throws XmlWriterException
     *         if errors occur.
     */
    @Override
    public void writeXML(ResultSet resultSet, OutputStream outputStream, InputStream inputStream)
            throws XmlWriterException
    {

        writeXML(resultSet, outputStream, XmlWriter.DEFAULT_ENCODING, inputStream);
    }

    /**
     * Add at document the given string
     *
     * @param string
     *        the string to add at document
     * @param flag
     *        the flag
     * @throws XPathUtilsException
     *         if an error occurred
     */
    private void propBoolean(String string, boolean flag) throws XPathUtilsException
    {
        xpathDOMBuilder.addElement(document, string);
        xpathDOMBuilder.addTextElement(document, string, String.valueOf(flag));
    }

    /**
     * Add at document the given string
     *
     * @param string
     *        the string to add
     * @param integer
     *        the integer
     * @throws XPathUtilsException
     *         if an error occurred
     */
    private void propInteger(String string, int integer) throws XPathUtilsException
    {
        xpathDOMBuilder.addElement(document, string);
        xpathDOMBuilder.addTextElement(document, string, Integer.toString(integer));
    }

    /**
     *
     * @param string
     *        the string to add
     * @param string1
     *        the string to add
     * @throws XPathUtilsException
     *         if an error occurred
     */
    private void propString(String string, String string1) throws XPathUtilsException
    {
        xpathDOMBuilder.addElement(document, string);
        xpathDOMBuilder.addTextElement(document, string, string1);
    }

    /**
     * Write data
     *
     * @param resultsetmetadata
     *        the result set meta data object
     * @param resultSet
     *        the resultSet object
     * @throws XmlWriterException
     *         if an error occurred
     */
    private void writeData(ResultSetMetaData resultsetmetadata, ResultSet resultSet) throws XmlWriterException
    {
        try {
            int i = resultsetmetadata.getColumnCount();
            logger.debug("columns number of cursor:[" + i + "]");
            xpathDOMBuilder.addElement(document, "/RowSet[1]/data[1]");

            int iCountRow = 1;
            if (resultSet.next()) {
                rsEmpty = false;
                do {
                    xpathDOMBuilder.addElement(document, "/RowSet[1]/data[1]/row[" + iCountRow + "]");
                    for (int j = 1; j <= i; j++) {
                        xpathDOMBuilder.addElement(document, "/RowSet[1]/data[1]/row[" + iCountRow + "]/col[" + j + "]");
                        writeValue(j, resultSet, "/RowSet[1]/data[1]/row[" + iCountRow + "]/col[" + j + "]");
                    }
                    iCountRow++;
                }
                while (resultSet.next());
            }
            else {
                rsEmpty = true;
            }

            recordCount = iCountRow - 1;
        }
        catch (SQLException exc) {
            logger.error("writeData - Error while writing Data section: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing Data section: " + exc}}, exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("writeData - Error while writing Data section: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing Data section:" + exc}}, exc);
        }
    }

    /**
     * Write meta data
     *
     * @param resultSet
     *        the result set
     * @throws XmlWriterException
     *         if an error occurred
     */
    private void writeMetaData(ResultSetMetaData resultsetmetadata) throws XmlWriterException
    {
        try {
            xpathDOMBuilder.addElement(document, "/RowSet[1]/metadata[1]");
            int i = resultsetmetadata.getColumnCount();
            propInteger("/RowSet[1]/metadata[1]/column-count[1]", i);
            for (int j = 1; j <= i; j++) {
                xpathDOMBuilder.addElement(document, "/RowSet[1]/metadata[1]/column-definition[" + j + "]");
                propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-index[1]", j);
                propBoolean("/RowSet[1]/metadata[1]/column-definition[" + j + "]/auto-increment[1]",
                        resultsetmetadata.isAutoIncrement(j));
                propBoolean("/RowSet[1]/metadata[1]/column-definition[" + j + "]/case-sensitive[1]",
                        resultsetmetadata.isCaseSensitive(j));
                propBoolean("/RowSet[1]/metadata[1]/column-definition[" + j + "]/currency[1]",
                        resultsetmetadata.isCurrency(j));
                propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/nullable[1]",
                        resultsetmetadata.isNullable(j));
                propBoolean("/RowSet[1]/metadata[1]/column-definition[" + j + "]/signed[1]",
                        resultsetmetadata.isSigned(j));
                propBoolean("/RowSet[1]/metadata[1]/column-definition[" + j + "]/searchable[1]",
                        resultsetmetadata.isSearchable(j));
                propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-display-size[1]",
                        resultsetmetadata.getColumnDisplaySize(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-label[1]",
                        resultsetmetadata.getColumnLabel(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-name[1]",
                        resultsetmetadata.getColumnName(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/schema-name[1]",
                        resultsetmetadata.getSchemaName(j));

                int type = resultsetmetadata.getColumnType(j);
                if ((type == Types.DECIMAL) || (type == Types.FLOAT) || (type == Types.DOUBLE)
                        || (type == Types.NUMERIC)) {
                    propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-precision[1]",
                            resultsetmetadata.getPrecision(j));
                }

                propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-scale[1]",
                        resultsetmetadata.getScale(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/table-name[1]",
                        resultsetmetadata.getTableName(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/catalog-name[1]",
                        resultsetmetadata.getCatalogName(j));
                propInteger("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-type[1]",
                        resultsetmetadata.getColumnType(j));
                propString("/RowSet[1]/metadata[1]/column-definition[" + j + "]/column-type-name[1]",
                        resultsetmetadata.getColumnTypeName(j));
            }
        }
        catch (SQLException exc) {
            logger.error("writeMetaData - Error while writing MetaData section: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing MetaData section: " + exc}}, exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("writeMetaData - Error while writing MetaData : ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing MetaData :" + exc}}, exc);
        }
    }

    /**
     * Write the result set
     *
     * @param resultSet
     *        the result set
     * @throws XmlWriterException
     *         if an error occurred
     */
    private void writeResultSet(ResultSet resultSet) throws XmlWriterException
    {
        recordCount = 0;

        try {
            xpathDOMBuilder.addElement(document, "/RowSet[1]");
            ResultSetMetaData resultsetmetadata = resultSet.getMetaData();
            if (iStructure == XmlWriter.METADATA_AND_DATA) {
                writeMetaData(resultsetmetadata);
            }

            writeData(resultsetmetadata, resultSet);
        }
        catch (XPathUtilsException exc) {
            logger.error("writeResultSet - Error while creating root node: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while creating root node:" + exc}}, exc);
        }
        catch (SQLException exc) {
            logger.error("writeResultSet - Error while get result: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR",
                    new String[][]{{"message", "Error while get result:" + exc}}, exc);
        }
    }

    /**
     * Write value
     *
     * @param i
     *        the column in the result set
     * @param resultSet
     *        the result set
     * @param xpath
     *        the XPath
     * @throws XmlWriterException
     *         if an error occurred
     */
    private void writeValue(int i, ResultSet resultSet, String xpath) throws XmlWriterException
    {
        try {
            int j = resultSet.getMetaData().getColumnType(i);

            switch (j) {
                case Types.BIT :
                    boolean flag = resultSet.getBoolean(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(flag));
                    }
                    break;
                case Types.LONGVARBINARY :
                case Types.VARBINARY :
                case Types.BINARY :
                    break;
                case Types.SMALLINT :
                    short word0 = resultSet.getShort(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, Short.toString(word0));
                    }
                    break;
                case Types.INTEGER :
                    int k = resultSet.getInt(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(k));
                    }
                    break;
                case Types.BIGINT :
                    long l = resultSet.getLong(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(l));
                    }
                    break;
                case Types.FLOAT :
                case Types.REAL :
                    float f = resultSet.getFloat(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(f));
                    }
                    break;
                case Types.DOUBLE :
                    double d = resultSet.getDouble(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(d));
                    }
                    break;
                case Types.NUMERIC :
                case Types.DECIMAL :
                    BigDecimal bigdecimal = resultSet.getBigDecimal(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, String.valueOf(bigdecimal));
                    }
                    break;
                case Types.DATE :
                    java.sql.Date date = resultSet.getDate(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath,
                                DateUtils.dateToString(date, DateUtils.FORMAT_ISO_DATETIME_S));
                    }
                    break;
                case Types.TIME :
                    java.sql.Time time = resultSet.getTime(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath,
                                DateUtils.dateToString(time, DateUtils.FORMAT_ISO_TIME_S));
                    }
                    break;
                case Types.TIMESTAMP :
                    java.sql.Timestamp timestamp = resultSet.getTimestamp(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath,
                                DateUtils.dateToString(timestamp, DateUtils.FORMAT_ISO_TIMESTAMP_S));
                    }
                    break;
                case Types.LONGNVARCHAR :
                case Types.CHAR :
                case Types.VARCHAR :
                    String value = resultSet.getString(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "null");
                    }
                    else {
                        xpathDOMBuilder.addTextElement(document, xpath, value);
                    }
                    break;
                case Types.BLOB :
                    Blob blob = resultSet.getBlob(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "");
                    }
                    else {
                        byte[] arrBytes = Base64.getEncoder().encode(BinaryUtils.inputStreamToBytes(blob.getBinaryStream()));
                        String value64 = new String(arrBytes, "US-ASCII");
                        xpathDOMBuilder.addTextElement(document, xpath, value64);
                    }
                    break;
                case Types.CLOB :
                    Clob clob = resultSet.getClob(i);
                    if (resultSet.wasNull()) {
                        xpathDOMBuilder.addTextElement(document, xpath, "");
                    }
                    else {
                        byte[] arrBytes = BinaryUtils.inputStreamToBytes(clob.getAsciiStream());
                        String valueS = new String(arrBytes);
                        xpathDOMBuilder.addTextElement(document, xpath, valueS);
                    }
                    break;
                default :
                    break;
            }
        }
        catch (SQLException exc) {
            logger.error("writeValue - Error while writing tag value: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing tag value: " + exc}}, exc);
        }
        catch (XPathUtilsException exc) {
            logger.error("writeValue - Error while writing tag value: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing tag value:" + exc}}, exc);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("writeValue - Error while writing tag value: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing tag value:" + exc}}, exc);
        }
        catch (IOException exc) {
            logger.error("writeValue - Error while writing tag value: ", exc);
            throw new XmlWriterException("GV_GENERIC_ERROR", new String[][]{{"message",
                    "Error while writing tag value:" + exc}}, exc);
        }
    }


    /**
     * @see it.greenvulcano.gvesb.j2ee.db.utils.XmlWriter#getRsEmpty()
     */
    @Override
    public boolean getRsEmpty()
    {
        return rsEmpty;
    }

    /**
     * @see it.greenvulcano.gvesb.j2ee.db.utils.XmlWriter#getRecordCount()
     */
    @Override
    public int getRecordCount()
    {
        return recordCount;
    }
}
