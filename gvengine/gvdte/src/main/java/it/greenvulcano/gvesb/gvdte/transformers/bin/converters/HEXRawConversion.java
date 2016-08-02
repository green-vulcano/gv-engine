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

import java.io.IOException;

import org.slf4j.Logger;

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
public class HEXRawConversion extends RawConversion
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(HEXRawConversion.class);

    /**
     * This method implements the encoding algorithm. It reads the input byte
     * array and converts each byte (considered as an unsigned integer between 0
     * and 255) into the corresponding Hex-encoded string (2 Hex digits),
     * appending all the conversions to a single string.
     *
     * @param inField
     *        the byte array containing data to be converted.
     * @return the result of the encoding as a string value.
     * @throws IOException
     *         if any error occurs during encoding.
     */
    @Override
    protected String encode(byte[] inField) throws IOException
    {
        try {
            StringBuilder result = new StringBuilder("");
            for (byte current : inField) {
                String currString = Integer.toHexString(current);
                if (currString.length() < 2) {
                    result.append("0").append(currString);
                }
                else {
                    result.append(currString.substring(currString.length() - 2));
                }
            }
            return result.toString().toUpperCase();
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new IOException("Unexpected error", exc);
        }
    }

    /**
     * This method implements the decoding algorithm. It converts a string
     * representing a sequence of values Hex-encoded (2 hex digits) into the
     * corresponding byte array (each byte containing an unsigned integer
     * between 0 and 255).
     *
     * @param inValue
     *        the string value to be converted.
     * @return the result of the decoding as a byte array.
     * @throws IOException
     *         if any error occurs during decoding.
     */
    @Override
    protected byte[] decode(String inValue) throws IOException
    {
        if (inValue.length() % 2 != 0) {
            logger.error("Bad HEX String (odd number of characters): [" + inValue + "]");
            throw new IOException("Bad HEX String (odd number of characters)");
        }

        try {
            byte[] result = new byte[inValue.length() / 2];
            int i = 0;
            int idx = 0;
            while (i < inValue.length() - 1) {
                String currString;
                if ((i + 2) < inValue.length()) {
                    currString = inValue.substring(i, i + 2);
                }
                else {
                    currString = inValue.substring(i);
                }
                int currValue = Integer.parseInt(currString, 16);
                result[idx++] = (byte) currValue;
                i += 2;
            }
            return result;

        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new IOException("Unexpected error", exc);
        }
    }

}
