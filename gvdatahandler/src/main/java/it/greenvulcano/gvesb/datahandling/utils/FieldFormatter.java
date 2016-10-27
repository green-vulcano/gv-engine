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
package it.greenvulcano.gvesb.datahandling.utils;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

/**
 * FieldFormatter class
 * 
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class FieldFormatter
{
    public final String   DEFAULT_NUMBER_FORMAT   = "#,##0.###";
    public final String   DEFAULT_GRP_SEPARATOR   = ".";
    public final String   DEFAULT_DEC_SEPARATOR   = ",";
    public final String   DEFAULT_DATE_FORMAT     = "yyyyMMdd HH:mm:ss";
    public final int      DEFAULT_LENGTH          = 64;
    public final String   DEFAULT_FILLER_CHAR     = " ";
    public final String   DEFAULT_TERMINATOR_CHAR = "";
    public final String   DEFAULT_TRIM            = "none";
    public final String   DEFAULT_PADDING         = "none";
    private final String  DEFAULT_FILLER_STRING   = "                                                   ";
    private final int     DEFAULT_FILLER_LENGTH   = 50;

    private DecimalFormat numberFormatter         = new DecimalFormat();

    private String        fieldName;
    private String        fieldId;
    private String        numberFormat            = DEFAULT_NUMBER_FORMAT;
    private String        groupSeparator          = DEFAULT_GRP_SEPARATOR;
    private String        decSeparator            = DEFAULT_DEC_SEPARATOR;
    private String        dateFormat              = DEFAULT_DATE_FORMAT;
    private String        dateTZoneOut            = DateUtils.getDefaultTimeZone().getID();
    private int           fieldLength             = DEFAULT_LENGTH;
    private String        fillerChar              = DEFAULT_FILLER_CHAR;
    private String        terminatorChar          = DEFAULT_TERMINATOR_CHAR;
    private String        trim                    = DEFAULT_TRIM;
    private String        padding                 = DEFAULT_PADDING;
    private boolean       isDefaultFillerChar     = false;

    /**
     *
     */
    public FieldFormatter()
    {
        numberFormatter.setRoundingMode(RoundingMode.FLOOR);
    }

    /**
     * @param node
     * @throws XMLConfigException
     */
    public void init(Node node) throws XMLConfigException
    {
        fieldName = XMLConfig.get(node, "@field-name", "NO_FIELD").toUpperCase();
        fieldId = XMLConfig.get(node, "@field-id", "-1");
        numberFormat = XMLConfig.get(node, "@number-format", DEFAULT_NUMBER_FORMAT);
        groupSeparator = XMLConfig.get(node, "@grouping-separator", DEFAULT_GRP_SEPARATOR);
        decSeparator = XMLConfig.get(node, "@decimal-separator", DEFAULT_DEC_SEPARATOR);
        dateFormat = XMLConfig.get(node, "@date-format", DEFAULT_DATE_FORMAT);
        List<String> dateParts = TextUtils.splitByStringSeparator(dateFormat, "::");
        dateFormat = dateParts.get(0);
        if (dateParts.size() > 1) {
            dateTZoneOut = dateParts.get(1);
        }
        fieldLength = XMLConfig.getInteger(node, "@field-length", DEFAULT_LENGTH);
        fillerChar = XMLConfig.get(node, "@filler-char", DEFAULT_FILLER_CHAR);
        isDefaultFillerChar = DEFAULT_FILLER_CHAR.equals(fillerChar);
        terminatorChar = XMLConfig.get(node, "@terminator-char", DEFAULT_TERMINATOR_CHAR).replaceAll("\\\\n", "\n").replaceAll(
                "\\\\r", "\r").replaceAll("\\\\t", "\t");
        trim = XMLConfig.get(node, "@trim", DEFAULT_TRIM);
        padding = XMLConfig.get(node, "@padding", DEFAULT_PADDING);
    }

    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @return the fieldId
     */
    public String getFieldId()
    {
        return fieldId;
    }

    /**
     * @return the numberFormat
     */
    public String getNumberFormat()
    {
        return numberFormat;
    }

    /**
     * @return the groupSeparator
     */
    public String getGroupSeparator()
    {
        return groupSeparator;
    }

    /**
     * @return the decSeparator
     */
    public String getDecSeparator()
    {
        return decSeparator;
    }

    /**
     * @return the dateFormat
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * @return the dateTZoneOut
     */
    public String getDateTZoneOut()
    {
        return this.dateTZoneOut;
    }

    /**
     * @return the fieldLength
     */
    public int getFieldLength()
    {
        return this.fieldLength;
    }

    /**
     * @return the fillerChar
     */
    public String getFillerChar()
    {
        return this.fillerChar;
    }

    /**
     * @return the terminatorChar
     */
    public String getTerminatorChar()
    {
        return this.terminatorChar;
    }

    /**
     * @return the trim
     */
    public String getTrim()
    {
        return this.trim;
    }

    /**
     * @return the padding
     */
    public String getPadding()
    {
        return this.padding;
    }

    /**
     * 
     * @param number
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(BigDecimal number) throws Exception
    {
        return formatField(formatNumber(number, null, null, null));
    }

    /**
     * 
     * @param number
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(double number) throws Exception
    {
        return formatField(formatNumber(number, null, null, null));
    }

    /**
     * 
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(BigDecimal number, String currNumberFormat, String currGroupSeparator,
            String currDecSeparator) throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : decSeparator;

        String formattedNumber = null;

        DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        numberFormatter.setDecimalFormatSymbols(dfs);
        numberFormatter.applyPattern(currNumberFormat);
        formattedNumber = numberFormatter.format(number);

        return formattedNumber;
    }

    /**
     * 
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the formatted number
     * @throws Exception
     */
    public String formatNumber(double number, String currNumberFormat, String currGroupSeparator, String currDecSeparator)
            throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : decSeparator;

        String formattedNumber = null;

        DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        numberFormatter.setDecimalFormatSymbols(dfs);
        numberFormatter.applyPattern(currNumberFormat);
        formattedNumber = numberFormatter.format(number);

        return formattedNumber;
    }

    /**
     * 
     * @param number
     * @return the parsed String as {@link BigDecimal}
     * @throws Exception
     */
    public BigDecimal parseToBigDecimal(String number) throws Exception
    {
        return parseToBigDecimal(number, null, null, null);
    }

    /**
     * 
     * @param number
     * @param currNumberFormat
     * @param currGroupSeparator
     * @param currDecSeparator
     * @return the parsed String as {@link BigDecimal}
     * @throws Exception
     */
    public BigDecimal parseToBigDecimal(String number, String currNumberFormat, String currGroupSeparator,
            String currDecSeparator) throws Exception
    {
        currNumberFormat = (currNumberFormat != null) ? currNumberFormat : numberFormat;
        currGroupSeparator = (currGroupSeparator != null) ? currGroupSeparator : groupSeparator;
        currDecSeparator = (currDecSeparator != null) ? currDecSeparator : decSeparator;

        BigDecimal parsedNumber = null;

        DecimalFormatSymbols dfs = numberFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(currDecSeparator.charAt(0));
        dfs.setGroupingSeparator(currGroupSeparator.charAt(0));
        numberFormatter.setDecimalFormatSymbols(dfs);
        numberFormatter.applyPattern(currNumberFormat);
        boolean isBigDecimal = numberFormatter.isParseBigDecimal();
        try {
            numberFormatter.setParseBigDecimal(true);
            parsedNumber = (BigDecimal) numberFormatter.parse(number);
        }
        finally {
            numberFormatter.setParseBigDecimal(isBigDecimal);
        }
        return parsedNumber;
    }

    /**
     * 
     * @param dateTime
     * @return the formatted {@link Date}
     * @throws Exception
     */
    public String formatDate(Date dateTime) throws Exception
    {
        return formatField(formatDate(dateTime, null));
    }

    /**
     * 
     * @param dateTime
     * @param currDateFormat
     * @return the formatted {@link Date}
     * @throws Exception
     */
    public String formatDate(Date dateTime, String currDateFormat) throws Exception
    {
        currDateFormat = (currDateFormat != null) ? currDateFormat : dateFormat;

        String formattedDate = DateUtils.dateToString(dateTime, currDateFormat, dateTZoneOut);

        return formattedDate;
    }

    /**
     * 
     * @param dateTime
     * @return the parsed {@link Date}
     * @throws Exception
     */
    public Date parseDate(String dateTime) throws Exception
    {
        return parseDate(dateTime, null);
    }

    /**
     * 
     * @param dateTime
     * @param currDateFormat
     * @return the parsed {@link Date}
     * @throws Exception
     */
    public Date parseDate(String dateTime, String currDateFormat) throws Exception
    {
        currDateFormat = (currDateFormat != null) ? currDateFormat : dateFormat;

        Date parsedDate = DateUtils.stringToDate(dateTime, currDateFormat, dateTZoneOut);

        return parsedDate;
    }

    /**
     * 
     * @param field
     * @return
     */
    public String formatField(String field)
    {
        if (field != null) {
            field = trim(field);
            int l = field.length();
            if (l >= fieldLength) {
                field.substring(0, fieldLength);
            }
            field = pad(field);
        }
        else {
            field = "";
        }
        return field + terminatorChar;
    }

    /**
     * 
     * @param field
     * @return
     */
    private String trim(String field)
    {
        if (!"none".equals(trim)) {
            if ("both".equals(trim)) {
                field.trim();
            }
            if ("left".equals(trim)) {
                while (" ".equals(field.charAt(0))) {
                    field = field.substring(1, field.length());
                }
            }
            if ("right".equals(trim)) {
                while (" ".equals(field.charAt(field.length() - 1))) {
                    field = field.substring(0, field.length() - 1);
                }
            }
        }
        return field;
    }

    /**
     * 
     * @param field
     * @return
     */
    private String pad(String field)
    {
        if (!"none".equals(padding)) {
            StringBuffer pad = new StringBuffer();

            if (isDefaultFillerChar) {
                int l = fieldLength - field.length();
                while (pad.length() < l) {
                    pad.append(((pad.length() + DEFAULT_FILLER_LENGTH) < l)
                            ? DEFAULT_FILLER_STRING
                            : DEFAULT_FILLER_STRING.substring(0, l - pad.length()));
                }
            }
            else {
                int l = field.length();
                for (int i = l; i < fieldLength; i++) {
                    pad.append(fillerChar);
                }
            }
            if ("right".equals(padding)) {
                field = field + pad;
            }
            if ("left".equals(padding)) {
                field = pad + field;
            }
        }
        return field;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "FieldFormatter: fieldName[" + fieldName + "] fieldId[" + fieldId + "] numberFormat[" + numberFormat
                + "] groupSeparator[" + groupSeparator + "] decSeparator[" + decSeparator + "] dateFormat["
                + dateFormat + "] fieldLength[" + fieldLength + "] fillerChar[" + fillerChar + "] terminatorChar["
                + terminatorChar + "] padding[" + padding + "]";
    }
}
