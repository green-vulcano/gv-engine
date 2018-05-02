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
package it.greenvulcano.gvesb.gvdte.transformers.bin.converters;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * This class implements the <code>encode</code> and <code>decode</code> methods
 * to read the value within the binary field (or within the string) as a raw
 * data array.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Base64RawConversion extends RawConversion
{
    /**
     * This method implements the encoding algorithm. It reads the input byte
     * array and converts each byte into the corresponding base64-encoded
     * string, appending all the conversions to a single string.
     *
     * @param inField
     *        the byte array containing data to be converted.
     * @return the result of the encoding as a string value.
     * @throws UnsupportedEncodingException
     *         if any error occurs during encoding.
     */
    @Override
    protected String encode(byte[] inField) throws UnsupportedEncodingException
    {
        return new String(Base64.getEncoder().encode(inField), "ISO-88589-1");
    }

    /**
     * This method implements the decoding algorithm. It converts a
     * Base64-encoded string into the corresponding byte array.
     *
     * @param inValue
     *        the string value to be converted.
     * @return the result of the decoding as a byte array.
     * @throws UnsupportedEncodingException
     *         if any error occurs during decoding.
     */
    @Override
    protected byte[] decode(String inValue) throws UnsupportedEncodingException
    {
        return Base64.getDecoder().decode(inValue.getBytes("ISO-88589-1"));
    }
}
