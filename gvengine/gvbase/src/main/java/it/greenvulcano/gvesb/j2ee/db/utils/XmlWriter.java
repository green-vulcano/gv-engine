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
package it.greenvulcano.gvesb.j2ee.db.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;

/**
 * The XML writer for a ResultSet object, which writes the rowset in XML format.
 * Writing a ResultSet object includes printing the rowset's data, metadata,
 * all with the appropriate XML tags.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 * ERVISION OK
 */
public interface XmlWriter
{
    /**
     * Only the data
     */
    int    ONLY_DATA         = 1;

    /**
     * The meta data and data
     */
    int    METADATA_AND_DATA = 2;

    /**
     * The default encoding
     */
    String DEFAULT_ENCODING  = "UTF-8";

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
    void writeXML(ResultSet resultSet, OutputStream outputStream, InputStream inputStream) throws XmlWriterException;

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
    void writeXML(ResultSet resultSet, OutputStream outputStream, String enc, InputStream inputStream)
            throws XmlWriterException;

    /**
     * Structure with data and metaData or only with data.
     *
     * @param structure
     */
    void setStructure(int structure);

    /**
     * @return if the ResultSet is empty
     */
    boolean getRsEmpty();

    /**
     * @return if the number of records
     */
    int getRecordCount();
}
