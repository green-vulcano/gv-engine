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
package it.greenvulcano.util.txt;

import java.util.StringTokenizer;

/**
 *
 * StringToHTML class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StringToHTML
{
    /**
     * @param str
     * @return transformed string to HTML
     */
    public static String toHTML(String str)
    {
        StringTokenizer st = new StringTokenizer(str, "\n", true);
        StringBuilder ret = new StringBuilder();
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if (tk.equals("\n")) {
                ret.append("<br>\n");
            }
            else {
                ret.append(quote(tk));
            }
        }
        return ret.toString();
    }

    /**
     * @param str
     * @return the quoted string
     */
    public static String quote(String str)
    {
        StringTokenizer st = new StringTokenizer(str, "<>&\"\n", true);
        StringBuilder ret = new StringBuilder();
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if (tk.equals("<")) {
                ret.append("&lt;");
            }
            else if (tk.equals(">")) {
                ret.append("&gt;");
            }
            else if (tk.equals("&")) {
                ret.append("&amp;");
            }
            else if (tk.equals("\"")) {
                ret.append("&quot;");
            }
            else if (tk.equals("\n")) {
                ret.append("<br/>\n");
            }
            else {
                ret.append(tk);
            }
        }
        return ret.toString();
    }
}
