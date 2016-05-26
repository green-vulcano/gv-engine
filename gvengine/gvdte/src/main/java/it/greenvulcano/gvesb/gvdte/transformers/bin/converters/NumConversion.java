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
import it.greenvulcano.util.bin.BinaryUtils;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * This base class implements the handler for the following conversions:
 *
 * - conversion of the string value (representing a number) contained within an
 * element (or attribute) of an XML document to a binary buffer numeric field.
 *
 * - conversion from a binary buffer field, containing a number, into a string
 * value (representing the same number) contained into an element (or attribute)
 * of an XML document.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public abstract class NumConversion implements Conversion
{
    private static Logger       logger           = org.slf4j.LoggerFactory.getLogger(NumConversion.class);

    /**
     * String constant indicating Little Endian byte ordering
     */
    private final static String BYTEORDER_LITTLE = "LittleEndian";

    /**
     * Maximum field length
     */
    private int                 fieldLen;

    /**
     * Numeric field encoding
     */
    private String              encoding;

    /**
     * Numeric field byte ordering
     */
    private String              byteOrder;

    /**
     * Temporary storage for numeric field value in Big Endian encoding
     */
    private byte[]              tempBE;

    /**
     * Temporary storage for numeric field value in Little Endian encoding
     */
    private byte[]              tempLE;


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
            byteOrder = (String) cfg.getData(base + "/@ByteOrder");
            tempBE = new byte[fieldLen];
            tempLE = new byte[fieldLen];
            logger.debug("Loaded parameters: fieldLen: " + fieldLen + " - encoding: " + encoding + " - byteOrder: " + byteOrder);
            logger.debug("Init stop");
        }
        catch (NumberFormatException ex) {
            logger.error("init - Error while reading fieldLen parameter: ", ex);
            throw new ConversionException("GVDTE_FORMAT_ERROR", new String[][]{{"msg",
                    " Error while reading fieldLen parameter."}}, ex);
        }
        catch (Throwable ex) {
            logger.error("init - Unexpected error: ", ex);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, ex);

        }
    }

    /**
     * Copies into a temporary buffer the value of the field to be converted, performs
     * byte-ordering inversion if neeeded, then calls the <code>encode</code> method
     * to obtain the resulting string value.
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
            System.arraycopy(buffer, offset, tempBE, 0, fieldLen);
            logger.debug("tempBE buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(tempBE));

            // Now the field is in tempBE:
            // Must handle complementation and byte ordering

            // Handle encoding
            // TODO : handle encoding

            // Handle byte ordering
            if (encoding.equals(BYTEORDER_LITTLE)) {
                // Little endian means that the first byte is
                // the least significant: is the inverse of the
                // internal byte ordering of GVDTE Engine, so
                // the byte array must be inverted before passing it
                // to the encode method
                for (int i = 0; i < tempBE.length; i++) {
                    tempLE[tempBE.length - 1 - i] = tempBE[i];
                }
                value.append(encode(tempLE));
            }
            else {
                // Big endian means that the first byte is
                // the most significant: is the same of the
                // internal byte ordering of GVDTE Engine, so
                // the byte array can be directly passed
                // to the encode method
                value.append(encode(tempBE));
            }
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
     * Calls the <tt>decode</tt> method on the string <tt>value</tt> and copies the
     * content of the resulting byte array (after performing byte-ordering inversion
     * if needed) in the output binary buffer.
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
        logger.debug("Output buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(buffer));
        // Obtain, from the string value, an equivalent byte array
        // with the GVDTE internal byte ordering (high byte first)
        byte[] fieldBuffer = null;
        try {
            fieldBuffer = decode(value);
            logger.debug("FieldBuffer content is now: "
                    + BinaryUtils.dumpByteArrayAsInts(fieldBuffer));

            // Get current binary field max lenght
            int fieldMaxLen = fieldLen;
            logger.debug("fieldMaxLen: " + fieldMaxLen);
            // Must handle complementation and byte ordering
            logger.debug("encoding: " + encoding);
            logger.debug("byteOrder: " + byteOrder);

            // Handle encoding
            // TODO handle encoding

            // Handle byte ordering
            if (encoding.equals(BYTEORDER_LITTLE)) {
                // Little endian means that the first byte is
                // the least significant: is the inverse of the
                // internal byte ordering of GVDTE Engine, so
                // the byte array fieldBuffer must be inverted
                // before copying it into the binary buffer
                for (int i = 0; i < fieldBuffer.length; i++) {
                    tempLE[fieldBuffer.length - 1 - i] = fieldBuffer[i];
                }

                if (tempLE.length > fieldMaxLen) {
                    logger.error("Numeric value '" + value
                            + "' is longer than its destination field into output binary buffer (" + fieldMaxLen + ")");
                    throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{
                            "msg",
                            "Numeric value '" + value
                                    + "' is longer than its destination field into output binary buffer ("
                                    + fieldMaxLen + ")"}});

                }

                System.arraycopy(tempLE, 0, buffer, offset, tempLE.length);
                logger.debug("Output buffer content is now: "
                        + BinaryUtils.dumpByteArrayAsInts(buffer));
            }
            else {
                // Big endian means that the first byte is
                // the most significant: is the same of the
                // internal byte ordering of GVDTE Engine, so
                // the byte array can be directly copied
                // into the binary buffer
                if (fieldBuffer.length > fieldMaxLen) {
                    logger.error("Numeric value '" + value
                            + "' is longer than its destination field into output binary buffer (" + fieldMaxLen + ")");
                    throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{
                            "msg",
                            "Numeric value '" + value
                                    + "' is longer than its destination field into output binary buffer ("
                                    + fieldMaxLen + ")"}});
                }

                System.arraycopy(fieldBuffer, 0, buffer, offset, fieldBuffer.length);
                logger.debug("Output buffer content is now: "
                        + BinaryUtils.dumpByteArrayAsInts(buffer));
            }
            logger.debug("Convert FieldToBin stop");
            return (offset + fieldBuffer.length);
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
     * @param inField
     *        the byte array containing data to be converted.
     * @return the result of the encoding as a string value.
     * @throws IOException
     *         if any error occurs during encoding.
     */
    protected abstract String encode(byte[] inField) throws IOException;

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
