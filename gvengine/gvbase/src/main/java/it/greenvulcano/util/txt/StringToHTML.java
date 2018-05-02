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
package it.greenvulcano.util.txt;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
    
    public static String escapeToEntity(String text) {
    	try {
	    	byte[] original = text.getBytes("UTF-32");
	    	
	    	
	    	StringBuilder escaped = new StringBuilder();
	    	for (int index = 0; index<original.length;) {	    		
		    	
	    		byte[] character = Arrays.copyOfRange(original, index, index+=4);	    		
	    		
	    		int codepoint = ByteBuffer.wrap(character).getInt();
	    		
	    		if (codepoint>128 || codepoint==34 || codepoint==39|| codepoint==60|| codepoint==62) {
	    			escaped.append("&#"+codepoint+";");
	    		} else {
	    			escaped.append(new String(character, "UTF-32"));   	
	    		}				
	    	}
	    	return escaped.toString();
    	} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();			
		}
    	
    	return text;
    }
}
