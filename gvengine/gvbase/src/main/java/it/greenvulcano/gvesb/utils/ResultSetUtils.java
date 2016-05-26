/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */

package it.greenvulcano.gvesb.utils;

import it.greenvulcano.util.xml.XMLUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @version 3.0.0 Dec 29, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ResultSetUtils
{
    private ResultSetUtils()
    {

    }

    /**
     * Returns all values from the ResultSet as an XML.
     * For instance, if the ResultSet has 3 values, the returned XML will have following fields:
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
     * @param rs
     * @return
     * @throws Exception
     */
    public static Document getResultSetAsDOM(ResultSet rs) throws Exception
    {
        XMLUtils xml = XMLUtils.getParserInstance();
        try {
            Document doc = xml.newDocument("RowSet");
            Element docRoot = doc.getDocumentElement();

            if (rs != null) {
                try {
                    ResultSetMetaData metadata = rs.getMetaData();
                    Element data = null;
                    Element row = null;
                    Element col = null;
                    Text text = null;
                    String textVal = null;
                    while (rs.next()) {
                        boolean restartResultset = false;
                        for (int j = 1; j <= metadata.getColumnCount() && !restartResultset; j++) {
                            col = xml.createElement(doc, "col");
                            restartResultset = false;
                            switch (metadata.getColumnType(j)) {
                                case Types.CLOB :{
                                    Clob clob = rs.getClob(j);
                                    if (clob != null) {
                	                    Reader is = clob.getCharacterStream();
                                        StringWriter strW = new StringWriter();

                                        IOUtils.copy(is, strW);
                                        is.close();
                                        textVal = strW.toString();
                                    }
                                    else {
                                        textVal = "";
                                    }
                                }
                                    break;
                                case Types.BLOB :{
                                    Blob blob = rs.getBlob(j);
                                    if (blob != null) {
                                        InputStream is = blob.getBinaryStream();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        IOUtils.copy(is, baos);
                                        is.close();
                                        try {
                                            byte[] buffer = Arrays.copyOf(baos.toByteArray(), (int) blob.length());
                                            textVal = new String(Base64.getEncoder().encode(buffer));
                                        }
                                        catch (SQLFeatureNotSupportedException exc) {
                                            textVal = new String(Base64.getEncoder().encode(baos.toByteArray()));
                                        }
                                    }
                                    else {
                                        textVal = "";
                                    }
                                }
                                    break;
                                case -10 :{ // OracleTypes.CURSOR
                                    Object obj = rs.getObject(j);
                                    if (obj instanceof ResultSet) {
                                        rs = (ResultSet) obj;
                                        metadata = rs.getMetaData();
                                    }
                                    restartResultset = true;
                                }
                                    break;
                                default :{
                                    textVal = rs.getString(j);
                                    if (textVal == null) {
                                        textVal = "";
                                    }
                                }
                            }
                            if (restartResultset) {
                                continue;
                            }
                            if (row == null || j == 1) {
                                row = xml.createElement(doc, "row");
                            }
                            if (textVal != null) {
                                text = doc.createTextNode(textVal);
                                col.appendChild(text);
                            }
                            row.appendChild(col);
                        }
                        if (row != null) {
                            if (data == null) {
                                data = xml.createElement(doc, "data");
                            }
                            data.appendChild(row);
                        }
                    }
                    if (data != null) {
                        docRoot.appendChild(data);
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

            return doc;
        }
        finally {
            XMLUtils.releaseParserInstance(xml);
        }
    }

    /**
     * Returns all values from the ResultSet as an XML.
     * For instance, if the ResultSet has 3 values, the returned XML will have following fields:
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
     *
     * @param rs
     * @return
     * @throws Exception
     */
    public static String getResultSetAsXMLString(ResultSet rs) throws Exception
    {
        return XMLUtils.serializeDOM_S(getResultSetAsDOM(rs));
    }
}
