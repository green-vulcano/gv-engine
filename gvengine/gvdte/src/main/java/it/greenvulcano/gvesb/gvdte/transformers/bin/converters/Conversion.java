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
package it.greenvulcano.gvesb.gvdte.transformers.bin.converters;

import it.greenvulcano.gvesb.gvdte.config.IConfigLoader;

/**
 * The interface defines the method to handle conversion of a binary field to a string
 * and vice versa.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public interface Conversion
{

    /**
     * This method performs initialization tasks for the <code>Conversion</code>
     * instance.
     *
     * @param base
     * @param cfg
     *
     * @throws ConversionException
     *         if any error occurs during initialization.
     *
     */
    public void init(String base, IConfigLoader cfg) throws ConversionException;

    /**
     * This method performs a conversion of the value of a binary buffer field
     * into a string value to be inserted into an XML element.
     *
     * @param buffer
     *        the byte array containing data to be converted;
     * @param offset
     *        the start position, within <code>buffer</code>, of the field whose
     *        value is to be converted;
     * @param value
     *        the output string, containing the (converted) value of binary
     *        field;
     *
     * @return the offset, within <code>buffer</code>, of the next binary buffer
     *         field to be converted.
     * @throws ConversionException
     *         if any error occurs during conversion.
     */
    public int convertFieldToXML(byte[] buffer, int offset, StringBuffer value) throws ConversionException;

    /**
     * This method performs the conversion of the value of an element (or
     * attribute) of the DOM representing the XML document to be converted, to a
     * field of a binary buffer.
     *
     * @param buffer
     *        the byte array in which the binary field, containing converted
     *        data, will be inserted;
     * @param offset
     *        the start position, within <code>buffer</code>, of the field
     *        containing the converted value;
     * @param value
     *        the string value, contained within the XML element (or attribute)
     *        to be converted.
     * @return the offset, within <code>buffer</code>, of the next binary field
     *         within the buffer.
     * @throws ConversionException
     *         if any error occurs during conversion.
     */
    public int convertFieldToBin(byte[] buffer, int offset, String value) throws ConversionException;
}
