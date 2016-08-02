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

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;

/**
 * This class implements the handler for the following conversions:
 *
 * - conversion from the string value (representing a sequence of bytes encoded
 * in some way) contained within an element (or attribute) of an XML document to
 * a binary buffer field containing the same string (as a char array).
 *
 * - conversion from a binary buffer field, containing a string (as a sequence
 * of chars), into a string value (representing the same string) contained into
 * an element (or attribute) of an XML document.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StringConversion implements Conversion
{
    private static Logger       logger          = org.slf4j.LoggerFactory.getLogger(StringConversion.class);

    /**
     * Constant indicating String value right alignment
     */
    private final static String ALIGNMENT_RIGHT = "Right";

    /**
     * Constant indicating String value left alignment
     */
    private final static String ALIGNMENT_LEFT  = "Left";

    /**
     * Field value character encoding
     */
    private String              encoding        = "";

    /**
     * Maximum field length
     */
    private int                 fieldLen        = 0;

    /**
     * String value alignment within binary field
     */
    private String              alignment       = ALIGNMENT_LEFT;

    /**
     * Boolean indicating if padding chars are needed within binary field
     */
    private boolean             paddingNeeded   = false;

    /**
     * Padding char value
     */
    private char                paddingChar;

    /**
     * Boolean indicating if string value needs a termination char
     */
    private boolean             isTerminated    = false;

    /**
     * Termination char value
     */
    private char                termChar;

    /**
     * Boolean indicating if string value represents a number
     */
    private boolean             isNumeric       = false;


    /**
     * This method performs initialization tasks for the <code>Conversion</code>
     * object which corresponds to the input value conversion that has to be
     * performed.
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

            String strIsTerminated = (String) cfg.getData(base + "/@Terminated");
            isTerminated = Boolean.valueOf(strIsTerminated.trim()).booleanValue();

            if (isTerminated) {
                String termCharValue = BinaryUtils.unescapeString((String) cfg.getData(base + "/@TerminationChar"));
                if (termCharValue.length() != 1) {
                    logger.error("A single character must be used as termination character");
                    throw new ConversionException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                            " A single character must be used as termination character"}});
                }
                termChar = termCharValue.charAt(0);
            }

            paddingNeeded = Boolean.valueOf((String) cfg.getData(base + "/@Padding")).booleanValue();

            if (paddingNeeded) {
                String paddingCharValue = BinaryUtils.unescapeString((String) cfg.getData(base + "/@PaddingChar"));
                if (paddingCharValue.length() != 1) {
                    logger.error("A single character must be used as padding character");
                    throw new ConversionException("GVDTE_CONFIGURATION_ERROR", new String[][]{{"cause",
                            " A single character must be used as padding character"}});
                }
                paddingChar = paddingCharValue.charAt(0);
                alignment = (String) cfg.getData(base + "/@Alignment");
            }

            if (isTerminated && paddingNeeded) {
                if (paddingChar == termChar) {
                    logger.error("Termination character and padding character must NOT be the same character");
                    throw new ConversionException("GVDTE_CONVERSION_ERROR", new String[][]{{"msg",
                            " Termination character and padding character must NOT be the same character"}});
                }
            }

            String strIsNumeric = (String) cfg.getData(base + "/@isNumeric");
            isNumeric = Boolean.valueOf(strIsNumeric.trim()).booleanValue();

            logger.debug("Loaded parameters: fieldLen: " + fieldLen + " - encoding: " + encoding + " - isTerminated: " + isTerminated
                    + " - termChar: '" + ((termChar==0)?"":termChar) + "' - paddingNeeded: " + paddingNeeded
                    + " - paddingChar: '" + ((paddingChar==0)?"":paddingChar) + "' - alignment: " + alignment + " - isNumeric: " + isNumeric);
            logger.debug("Init stop");
        }
        catch (ConversionException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Catched exception."}}, exc);
        }

    }

    /**
     * Extracts from the input binary buffer a string value and converts it into a String
     * with the specified encoding.
     *
     * Assumptions:<br>
     * - padding char and termination char can't be the same char;<br>
     * - padding char must be a single char;<br>
     * - termination char must be a single char;<br>
     * - if the string value represents a signed number, padding chars<br>
     * can't be interposed between sign and number;<br>
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
        boolean isNumericWithSign = false;
        boolean isNegative = false;

        logger.debug("Convert FieldToXML start");
        int enterValue = offset; // start index of the value, inclusive
        int exitValue = offset + fieldLen; // end index of the value, exclusive
        try {
            if (paddingNeeded) {
                // Skip padding chars
                // taking alignment into account
                if (alignment.equals(ALIGNMENT_RIGHT)) {
                    // Right alignment
                    for (int i = offset; i < offset + fieldLen; i++) {
                        // Skip padding chars
                        char currChar = (char) buffer[i];
                        if (currChar != paddingChar) {
                            if (isNumeric && (currChar == '+')) {
                                // Exclude sign if it's positive
                                enterValue = i + 1;
                            }
                            else {
                                enterValue = i;
                            }
                            break;
                        }
                    }

                    if (isTerminated) {
                        // Exclude termination char
                        exitValue = offset + fieldLen - 1;
                    }
                    else {
                        exitValue = offset + fieldLen;
                    }
                }
                else {
                    // Default: left alignment
                    for (int i = offset + fieldLen - 1; i >= offset; i--) {
                        // Skip padding chars
                        char currChar = (char) buffer[i];
                        if (currChar != paddingChar) {
                            if (isTerminated) {
                                // Exclude termination char
                                exitValue = i;
                            }
                            else {
                                exitValue = i + 1;
                            }
                            break;
                        }
                    }

                    // Find enterValue:
                    // If the string represents a number,
                    // the first character might be its sign
                    if (isNumeric) {
                        char firstChar = (char) buffer[offset];
                        if (firstChar == '+') {
                            isNumericWithSign = true;
                        }
                        else if (firstChar == '-') {
                            isNumericWithSign = true;
                            isNegative = true;
                        }
                    }

                    if (isNumericWithSign) {
                        // Exclude sign if it's positive
                        if (isNegative) {
                            enterValue = offset;
                        }
                        else {
                            enterValue = offset + 1;
                        }
                    }
                    else {
                        enterValue = offset;
                    }
                }
            }
            else {
                // No Padding:
                // If the string represents a number,
                // the first character might be its sign
                if (isNumeric) {
                    char firstChar = (char) buffer[offset];
                    if (firstChar == '+') {
                        isNumericWithSign = true;
                    }
                    else if (firstChar == '-') {
                        isNumericWithSign = true;
                        isNegative = true;
                    }
                }

                if (isNumericWithSign) {
                    // Exclude sign if it's positive
                    if (isNegative) {
                        enterValue = offset;
                    }
                    else {
                        enterValue = offset + 1;
                    }
                }
                else {
                    enterValue = offset;
                }

                if (isTerminated) {
                    // Exclude termination char
                    exitValue = offset + fieldLen - 1;
                }
                else {
                    exitValue = offset + fieldLen;
                }
            }

            for (int i = enterValue; i < exitValue; i++) {
                value.append((char) buffer[i]);
            }
            logger.debug("Result string: " + value.toString());
            logger.debug("Convert FieldToXML stop");
            return fieldLen;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

    /**
     * Converts the input String into an array of chars (according to the specified encoding)
     * and copies it into the output binary buffer, adding termination char and
     * padding char if needed.
     *
     * Assumptions:<br>
     * - padding char and termination char can't be the same char;<br>
     * - padding char must be a single char;<br>
     * - termination char must be a single char;<br>
     * - if the string value represents a signed number, padding chars<br>
     * can't be interposed between sign and number;<br>
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
        byte[] charArray = null;
        logger.debug("Convert FieldToBin start");
        logger.debug("Output buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(buffer));
        logger.debug("String value to be converted is: " + value);

        try {
            // Convert input string value to a char array
            // in the requested encoding;
            logger.debug("Encoding: " + encoding);
            charArray = value.getBytes(encoding);
            logger.debug("CharArray content is now: " + BinaryUtils.dumpByteArrayAsInts(charArray));

            // Before copying charArray bytes values into output buffer template
            // take alignment into account, if padding is needed.
            int enterValue = offset; // start index of the value, inclusive
            int exitField = offset + fieldLen; // end index of the field, exclusive
            int valueCharArrayLen = charArray.length; // Number of chars of the value (term. char not included)
            int termCharLen = (isTerminated ? 1 : 0); // Number of termination chars

            if (paddingNeeded && alignment.equals(ALIGNMENT_RIGHT)) {
                enterValue = exitField - valueCharArrayLen - termCharLen;
            }

            // Copy charArray bytes values into output buffer template
            // starting from the proper position
            System.arraycopy(charArray, 0, buffer, enterValue, charArray.length);

            // Add termination char if needed
            if (isTerminated) {
                buffer[enterValue + charArray.length] = (byte) termChar;
            }

            // Add padding chars if needed
            if (paddingNeeded) {
                int startPadding, endPadding;
                if (alignment.equals(ALIGNMENT_RIGHT)) {
                    // Right alignment
                    startPadding = offset;
                    endPadding = enterValue;
                }
                else {
                    // Default: left alignment
                    startPadding = offset + charArray.length + termCharLen;
                    endPadding = exitField;
                }
                for (int i = startPadding; i < endPadding; i++) {
                    buffer[i] = (byte) paddingChar;
                }
            }

            logger.debug("Output buffer content is now: " + BinaryUtils.dumpByteArrayAsInts(buffer));
            logger.debug("Convert FieldToBin stop");
            return (offset + fieldLen);
        }
        catch (UnsupportedEncodingException exc) {
            logger.error("Unsupported Encoding", exc);
            throw new ConversionException("GVDTE_ENCODING_ERROR", new String[][]{{"msg", "Unsupported Encoding."}}, exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new ConversionException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", "Unexpected error."}}, exc);
        }
    }

}
