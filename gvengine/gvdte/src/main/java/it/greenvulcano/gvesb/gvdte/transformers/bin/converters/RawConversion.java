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

import it.greenvulcano.gvesb.gvdte.config.IConfigLoader;
import it.greenvulcano.util.bin.BinaryUtils;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * This class implements the handler for the following conversions:
 *
 * - conversion from the string value (representing a sequence of bytes encoded
 * in some way) contained within an element (or attribute) of an XML document to
 * a binary buffer field containing the same sequence of bytes.
 *
 * - conversion from a binary buffer field, containing a sequence of bytes, into
 * a string value (representing the same sequence of bytes encoded in some way)
 * contained into an element (or attribute) of an XML document.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class RawConversion implements Conversion
{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RawConversion.class);

    /**
     * Maximum field length
     */
    private int           fieldLen;

    /**
     * Raw data field encoding
     */
    private String        encoding;

    /**
     * Temporary storage for raw data field value
     */
    private byte[]        tempField;

    /**
     * Initialize the instance.
     *
     * @param base
     * @param cfg
     *
     * @throws ConversionException
     *         if any error occurs during initialization.
     */
    public void init(String base, IConfigLoader cfg) throws ConversionException
    {
        logger.debug("Init start");
        try {
            String strFieldLen = (String) cfg.getData(base + "/@FieldLength");
            fieldLen = Integer.parseInt(strFieldLen.trim());
            encoding = (String) cfg.getData(base + "/@Encoding");
            tempField = new byte[fieldLen];
            logger.debug("Loaded parameters: fieldLen: " + fieldLen + " - encoding: " + encoding);
            logger.debug("Init stop");
        }
        catch (NumberFormatException exc) {
            logger.error("Error while reading fieldLen parameter", exc);
            throw new ConversionException("GVDTE_FORMAT_ERROR", new String[][]{{"msg",
                    " Error while reading fieldLen parameter."}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * This method, after copying into a temporary buffer the value of the field
     * to be converted, calls the <code>encode</code> method to obtain the
     * resulting string value.
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
    public int convertFieldToXML(byte[] buffer, int offset, StringBuffer value) throws ConversionException
    {

        logger.debug("Convert FieldToXML start");
        try {
            System.arraycopy(buffer, offset, tempField, 0, fieldLen);
            logger.debug("TempField buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(tempField));
            value.append(encode(tempField));
            logger.debug("Result string: " + value.toString());
            logger.debug("Convert FieldToXML stop");
            return fieldLen;
        }
        catch (IOException exc) {
            logger.error("Error while encoding byte array", exc);
            throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{"msg", "byte array."}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * This method calls the <tt>decode</tt> method on the string <tt>value</tt>
     * and copies the content of the resulting byte array in the output binary
     * buffer.
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
    public int convertFieldToBin(byte[] buffer, int offset, String value) throws ConversionException
    {
        logger.debug("Convert FieldToBin start");
        try {
            logger.debug("Output buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(buffer));
            int fieldMaxLen = fieldLen;
            logger.debug("FieldMaxLen: " + fieldMaxLen);

            byte[] fieldBuffer = null;
            fieldBuffer = decode(value);
            logger.debug("FieldBuffer content is now: " + BinaryUtils.dumpByteArrayAsInts(fieldBuffer));

            if (fieldBuffer.length > fieldMaxLen) {
                logger.error("Raw value '" + value
                        + "' is longer than its destination field into output binary buffer (" + fieldMaxLen + ")");
                throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{
                        "msg",
                        "Raw value '" + value
                                + "' is longer than its destination field into output binary buffer (" + fieldMaxLen
                                + ")"}});
            }

            System.arraycopy(fieldBuffer, 0, buffer, offset, fieldBuffer.length);
            logger.debug("Output buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(buffer));
            logger.debug("Convert FieldToBin stop");
            return (offset + fieldBuffer.length);
        }
        catch (ConversionException exc) {
            throw exc;
        }
        catch (IOException exc) {
            logger.error("Error while decoding string value", exc);
            throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{"msg", "string value "}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error while copying fieldBuffer into binary buffer", exc);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                    "Unexpected error while copying fieldBuffer into binary buffer."}}, exc);
        }
    }

    /**
     * This abstract method must implement the encoding algorithm.
     *
     * @param inBuffer
     *        the byte array containing data to be converted.
     * @return the result of the encoding as a string value.
     * @throws IOException
     *         if any error occurs during encoding.
     */
    protected abstract String encode(byte[] inBuffer) throws IOException;

    /**
     * This abstract method must implement the decoding algorithm.
     *
     * @param inValue
     *        the string value to be converted.
     * @return the result of the decoding as a byte array.
     * @throws IOException
     *         if any error occurs during decoding.
     */
    protected abstract byte[] decode(String inValue) throws IOException;
}
