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
package it.greenvulcano.gvesb.utils;

/**
 * Utility class for String
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class StringUtility
{
    /**
     * @param str
     * @return if string is null or empty
     */
    public static boolean isEmpty(String str)
    {
        if (str == null || str.equals(""))
            return true;
        else
            return false;
    }

    /**
     * @param str
     * @return an empty string if null or empty, otherwise the same string
     */
    public static String addIfNotEmpty(String str)
    {
        String result = "";
        if (!isEmpty(str))
            result = str;

        return result;
    }

    /**
     * @param str
     * @param param
     * @return an empty string if null or empty, otherwise the same string
     *         concatenated with the parameter <code>param</code> passed
     */
    public static String addIfNotEmpty(String str, String param)
    {
        String result = "";
        if (!isEmpty(str))
            result = str + param;

        return result;
    }
}
