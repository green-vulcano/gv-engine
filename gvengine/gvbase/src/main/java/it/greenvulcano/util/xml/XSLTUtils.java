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
package it.greenvulcano.util.xml;

import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

import org.w3c.dom.Node;

/**
 * This class provides the implementation for some utility methods which can be
 * called by a Java XSLT engine while applying a XSL stylesheet.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class XSLTUtils
{

    /**
     * @param str
     * @return the string in upper case
     */
    public static synchronized String upperCase(String str)
    {
        return str.toUpperCase();
    }

    /**
     * @param str
     * @return the string in lower case
     */
    public static synchronized String lowerCase(String str)
    {
        return str.toLowerCase();
    }

    /**
     * @param str
     * @param width
     * @param padding
     * @return the string left filled with padding character passed
     */
    public static synchronized String leftPad(String str, int width, char padding)
    {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < width; ++i) {
            buff.append(padding);
        }
        buff.append(str);
        return buff.substring(buff.length() - width);
    }

    /**
     * @param str
     * @param width
     * @param padding
     * @return the string right filled with padding character passed
     */
    public static synchronized String rightPad(String str, int width, char padding)
    {
        StringBuilder buff = new StringBuilder(str);
        for (int i = 0; i < width; ++i) {
            buff.append(padding);
        }
        return buff.substring(0, width);
    }

    /**
     * @param str
     * @return the trimmed string
     * 
     * @see java.lang.String#trim()
     */
    public static synchronized String trim(String str)
    {
        return str.trim();
    }


    /**
     * @param str
     * @param find
     * @param replace
     * @return the replaced string
     */
    public static String replace(String str, String find, String replace)
    {
        return TextUtils.replaceSubstring(str, find, replace);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#serializeDOM_S(Node, String)
     */
    public static String serializeNode(Node node, String encoding)
    {
        try {
            return XMLUtils.serializeDOM_S(node, encoding);
        }
        catch (XMLUtilsException exc) {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#convertDate(String, String,
     *      String)
     */
    public static String convertDate(String date, String formatIn, String formatOut)
    {
        return DateUtils.convertString(date, formatIn, formatOut);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#convertDate(String, String,
     *      String, String, String)
     */
    public static String convertDate(String date, String formatIn, String tZoneIn, String formatOut, String tZoneOut)
    {
        return DateUtils.convertString(date, formatIn, tZoneIn, formatOut, tZoneOut);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#convertDate(String, String,
     *      String, String, String, String, String)
     */
    public static String convertDate(String date, String formatIn, String tZoneIn, String langIn, String formatOut,
            String tZoneOut, String langOut)
    {
        return DateUtils.convertString(date, formatIn, tZoneIn, langIn, formatOut, tZoneOut, langOut);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#nowToString(String)
     */
    public static String nowToString(String formatOut)
    {
        return DateUtils.nowToString(formatOut);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#nowToString(String, String)
     */
    public static String nowToString(String formatOut, String tZoneOut)
    {
        return DateUtils.nowToString(formatOut, tZoneOut);
    }

    /**
     * @see it.greenvulcano.util.txt.DateUtils#nowToString(String, String,
     *      String)
     */
    public static String nowToString(String formatOut, String tZoneOut, String langOut)
    {
        return DateUtils.nowToString(formatOut, tZoneOut, langOut);
    }
}
