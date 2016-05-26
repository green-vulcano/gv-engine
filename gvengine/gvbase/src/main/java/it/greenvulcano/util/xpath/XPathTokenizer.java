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
package it.greenvulcano.util.xpath;

import java.util.StringTokenizer;

/**
 * This class tokenizes an XPath string to its components.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class XPathTokenizer
{

    private static final String VALID_NAME_SYMBOLS         = "_-";
    private static final String VALID_SEPARATOR_SYMBOLS    = "./@*[]()=";
    private static final String VALID_EXPRESSION_DELIMITER = "[]";

    private String              xpath                      = "";

    private String              lastTokenValue             = "";

    private int                 lastTokenType              = 0;

    private int                 index                      = 0;

    private int                 oldIndex                   = 0;

    /**
     * Class Constructor
     *
     * @param xPath
     *        String representation of XPath
     */
    public XPathTokenizer(String xPath)
    {
        xpath = deleteBlank(xPath);
    }

    /**
     * This method analyzes the input string and returns, at each invocation, a
     * new token
     *
     * @return The next token extracted
     */
    public String nextToken()
    {
        char currChar = '.';
        lastTokenType = 0;
        oldIndex = index;
        lastTokenValue = "";

        if ((index + 1) == xpath.length()) {
            // read the string's last char
            currChar = xpath.charAt(index);
            if (isValidSeparatorChar(currChar)) {
                // if it is a symbol then save it (es. *, ])
                lastTokenValue += currChar;
            }
            index++;
        }
        else {
            // there are othe chars to analize
            while (((index + 1) < xpath.length()) && isValidSeparatorChar(currChar)) {
                // Extract cha at position index
                currChar = xpath.charAt(index);
                if (isValidSeparatorChar(currChar)) {
                    // the char is a simbol
                    if (isValidExprDelimiterChar(currChar)) {
                        // the is an expression delimiter
                        if (lastTokenValue.length() != 0) {
                            // the char must be insert in the next token
                            break;
                        }
                        // the chus is inserted in the current token
                        lastTokenValue += currChar;
                        index++;
                        break;
                    }
                    if (currChar == '*') {
                        if (lastTokenValue.length() != 0) {
                            break;
                        }
                        lastTokenValue += currChar;
                        index++;
                        break;
                    }
                    lastTokenValue += currChar;
                    index++;
                    if (currChar == '/') {
                        // the current token in complete (es. /, ./, ../)
                        break;
                    }
                }
            }
        }

        if (lastTokenValue.length() != 0) {
            if (isValidExprDelimiterChar(lastTokenValue.charAt(0))) {
                lastTokenType = 2;
            }
            else {
                lastTokenType = 1;
            }
            // return a token composed of a simbol or expression delimiter
            return lastTokenValue;
        }

        currChar = 'a'; // Dummy value
        if ((index + 1) == xpath.length()) {
            currChar = xpath.charAt(index);
            if (isValidNameChar(currChar)) {
                lastTokenValue += currChar;
                index++;
            }
        }
        else {
            while (((index + 1) < xpath.length()) && isValidNameChar(currChar)) {
                currChar = xpath.charAt(index);
                if (isValidNameChar(currChar)) {
                    lastTokenValue += currChar;
                    index++;
                }
            }
            if ((index + 1) == xpath.length()) {
                currChar = xpath.charAt(index);
                if (isValidNameChar(currChar)) {
                    lastTokenValue += currChar;
                    index++;
                }
            }
        }
        if (lastTokenValue.length() != 0) {
            lastTokenType = 3;
        }
        // return a token representing a name
        return lastTokenValue;
    }

    /**
     * This method reverts the last call to nextToken()
     */
    public void putBack()
    {
        index = oldIndex;
    }

    /**
     * This method returns the last token extracted by nextToken()
     *
     * @return Last token extracted
     */
    public String lastToken()
    {
        return lastTokenValue;
    }

    /**
     * This method returns the type of the last token extracted by nextToken()
     *
     * @return Type of last token extracted
     */
    public int lastTokenType()
    {
        return lastTokenType;
    }

    /**
     * This method returns true if there are tokens left to extract from the
     * string
     *
     * @return True if there are tokens left to extract
     */
    public boolean hasMoreTokens()
    {
        return (index < xpath.length());
    }

    /**
     * This method deletes ALL spaces present in the input string
     *
     * @return String without spaces
     */
    private String deleteBlank(String strIn)
    {
        String strOut = "";
        StringTokenizer st = new StringTokenizer(strIn, " ", false);
        while (st.hasMoreTokens()) {
            strOut += st.nextToken();
        }
        return strOut;
    }

    /**
     * This method checks if the character passed is a valid identifier
     *
     * @return True if the character passed is a valid identifier
     */
    private boolean isValidNameChar(char currChar)
    {
        return (Character.isLetterOrDigit(currChar) || (VALID_NAME_SYMBOLS.indexOf(currChar) != -1));
    }

    /**
     * This method checks if the separator is valid
     *
     * @return True if the separator is valid
     */
    private boolean isValidSeparatorChar(char currChar)
    {
        return (VALID_SEPARATOR_SYMBOLS.indexOf(currChar) != -1);
    }

    /**
     * This method checks if the expression delimiter is valid
     *
     * @return True expression delimiter is valid
     */
    private boolean isValidExprDelimiterChar(char currChar)
    {
        return (VALID_EXPRESSION_DELIMITER.indexOf(currChar) != -1);
    }

}
