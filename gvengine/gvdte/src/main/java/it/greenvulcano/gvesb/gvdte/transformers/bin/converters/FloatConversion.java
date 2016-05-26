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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class implements the <code>encode</code> and <code>decode</code> methods
 * to read the value within the binary field (or within the string) as a
 * floating-point number.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class FloatConversion extends NumConversion
{

    /**
     * This method implements the encoding algorithm. It reads the input byte
     * array (containing a <tt>float</tt> variable) considering the first byte
     * as the most meaningful one and converts it to a string representation of
     * the variable's value.
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
        DataInputStream bIn = new DataInputStream(new ByteArrayInputStream(inField));
        float value = bIn.readFloat();
        return ("" + value);
    }

    /**
     * This method implements the decoding algorithm. It converts a string
     * representing a <tt>float</tt> variable into an 4-bytes array containing
     * the value of that variable (with the first byte as the most meaningful
     * one)
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
        float value;
        try {
            value = Float.parseFloat(inValue);
        }
        catch (NumberFormatException exc) {
            throw new IOException("" + exc);
        }
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DataOutputStream dOut = new DataOutputStream(bOut);

        // Write high byte first
        dOut.writeFloat(value);
        return bOut.toByteArray();
    }

}
