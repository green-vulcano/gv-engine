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

import it.greenvulcano.util.runtime.Environment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides an utility method to replace various types of
 * placeholders within a given text {@link java.lang.String <code>String</code>}
 * .<br>
 * The placeholders <i>must</i> adhere to the naming convention explained below.<br>
 *
 * The following placeholders are supported:
 *
 * <ul>
 * <li><code>%{env:VARNAME}</code> is replaced with the content of the
 * environment variable <code>VARNAME</code> or with an empty string (
 * <code>""</code>) if no such variable is defined;</li>
 * </ul>
 *
 * <ul>
 * <li><code>%{java:JAVAPROPNAME}</code> is replaced with the value returned by
 * a <code>System.getProperties("JAVAPROPNAME")</code> call or with an empty
 * string (<code>""</code>) if no such property is defined;</li>
 * </ul>
 *
 * <ul>
 * <li><code>%{timestamp}</code> is replaced with the value returned by a
 * <code>System.currentTimeMillis()</code> call;</li>
 * </ul>
 *
 * <ul>
 * <li><code>%{datetime}</code> is replaced with the value returned by a call to
 * the <code>toString</code> method of a newly created
 * <code>java.util.Date</code> object;</li>
 * </ul>
 *
 * <ul>
 * <li><code>%{datetime:DATEPATTERN}</code> is replaced with the value returned
 * by a call to the <code>format</code> method of a
 * <code>java.txt.SimpleDateFormat</code> object, to which a newly created
 * instance of <code>java.util.Date</code> has been passed;</li>
 * </ul>
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StringPlaceholderExpander
{

    /**
     * This method takes a text {@link java.lang.String <code>String</code>} as
     * input, possibly containing placeholders in the supported formats, and
     * returns a copy of the string with the placeholders replaced by the
     * corresponding values, or by an empty string if the value is not defined.
     *
     * @param input
     *        a text {@link java.lang.String <code>String</code>}, possibly
     *        containing placeholders in the supported formats.
     * @return a copy of the input {@link java.lang.String <code>String</code>}
     *         with the placeholders replaced by the corresponding values, or by
     *         an empty string if the value is not defined.
     * @throws Exception
     *         if any error occurs.
     */
    public String expand(String input) throws Exception
    {
        StringBuilder output = new StringBuilder(input.length());
        int currIdx = 0;
        while (currIdx < input.length()) {
            char currChar = input.charAt(currIdx);

            if (currChar == PLACEHOLDER_START_DELIM) {
                currIdx++;
                if (currIdx == input.length()) {
                    throw new IllegalArgumentException(
                            "Malformed input string (placeholder start delimiter at the end of the string): " + input);
                }
                if (input.charAt(currIdx) != PLACEHOLDER_VALUE_START_DELIM) {
                    throw new IllegalArgumentException(
                            "Malformed input string (placeholder start delimiter without placeholder value start delimiter): "
                                    + input);
                }
                currIdx++;
                if (currIdx == input.length()) {
                    throw new IllegalArgumentException(
                            "Malformed input string (placeholder value start delimiter at the end of the string): "
                                    + input);
                }

                int valueStartIdx = currIdx;
                int valueEndIdx = valueStartIdx;
                boolean endDelimFound = false;
                while (currIdx < input.length()) {
                    currChar = input.charAt(currIdx);
                    if (currChar == PLACEHOLDER_END_DELIM) {
                        valueEndIdx = currIdx;
                        endDelimFound = true;
                        break;
                    }
                    currIdx++;
                }

                if (endDelimFound == false) {
                    throw new IllegalArgumentException(
                            "Malformed input string (placeholder value start delimiter with no value end delimiter): "
                                    + input);
                }

                if (valueStartIdx == valueEndIdx) {
                    throw new IllegalArgumentException("Malformed input string (placeholder with no value): " + input);
                }
                String phName = input.substring(valueStartIdx, valueEndIdx);
                String phValue = getPlaceholderValue(phName);
                if (phValue == null) {
                    phValue = "";
                }
                output.append(phValue);
                currIdx++;
            }
            else {
                output.append(currChar);
                currIdx++;
            }
        }

        return output.toString();
    }

    /**
     * Returns <code>true</code> if the passed text string contains
     * placeholders, <code>false</code> otherwise.
     *
     * @param input
     *        a text string, possibly containing placeholders
     * @return <code>true</code> if the passed text string contains
     *         placeholders, <code>false</code> otherwise.
     */
    public boolean containsPlaceholders(String input)
    {
        if (input == null) {
            return false;
        }
        int startIdx = input.indexOf(PLACEHOLDER_START_DELIM_STR);
        int endIdx = input.indexOf(PLACEHOLDER_END_DELIM);
        return (startIdx != -1) && (endIdx != -1) && (endIdx > startIdx + 2);
    }

    /**
     * Given a placeholder 'full name', retrieves the corresponding value, if
     * available
     *
     * @param phName
     * @return the placeholder value
     * @throws Exception
     */
    protected synchronized String getPlaceholderValue(String phName) throws Exception
    {
        String phNamespace = null;
        String phLocalname = null;
        int idx = phName.indexOf(':');
        if (idx != -1) {
            phNamespace = phName.substring(0, idx);
            phLocalname = phName.substring(idx + 1);
        }
        else {
            phNamespace = phName;
            phLocalname = "";
        }

        PlaceHolderHandler phHandler = handlers.get(phNamespace);
        if (phHandler != null) {
            return phHandler.handlePlaceholder(phLocalname);
        }
        return "";
    }

    /**
     * Protected interface providing a method for expanding a placeholder of a
     * particular kind, given its 'local name'.
     */
    protected static interface PlaceHolderHandler
    {

        /**
         * Interface method. Given a placeholder 'local name' returns the
         * corresponding value to be used to expand the placeholder, or an empty
         * string if no such value is available.
         *
         * @param placeholder
         *        the name of the placeholder to be replaced
         * @return the corresponding value to be used to expand the placeholder,
         *         or an empty string if no such value is available.
         * @throws Exception
         *         if any error occurs
         */
        String handlePlaceholder(String placeholder) throws Exception;
    }

    /**
     * Implementation of the PlaceHolderHandler interface designed to handle
     * placeholders for environment variables values
     *
     * @version 3.0.0 Feb 27, 2010
     * @author nunzio
     *
     */
    protected static class EnvVarPlaceHolderHandler implements PlaceHolderHandler
    {

        /**
         * @see it.greenvulcano.util.txt.StringPlaceholderExpander.PlaceHolderHandler#handlePlaceholder(java.lang.String)
         */
        public String handlePlaceholder(String placeholder) throws Exception
        {
            String result = Environment.getVariable(placeholder);
            if (result == null) {
                result = "";
            }

            return result;
        }
    }

    // Implementation of the PlaceHolderHandler interface
    // designed to handle placeholders for Java system properties values
    private static class JavaPropsPlaceHolderHandler implements PlaceHolderHandler
    {

        public String handlePlaceholder(String placeholder) throws Exception
        {
            String result = System.getProperty(placeholder);
            if (result == null) {
                result = "";
            }

            return result;
        }
    }

    // Implementation of the PlaceHolderHandler interface
    // designed to handle placeholders to be replaced by
    // UNIX timestamps
    private static class TimestampPlaceHolderHandler implements PlaceHolderHandler
    {

        public String handlePlaceholder(String placeholder) throws Exception
        {
            return "" + System.currentTimeMillis();
        }
    }

    // Implementation of the PlaceHolderHandler interface
    // designed to handle placeholders to be replaced by
    // date-time string with Java standard or user-defined format
    private static class DatePlaceHolderHandler implements PlaceHolderHandler
    {

        public String handlePlaceholder(String placeholder) throws Exception
        {
            String result = null;
            if (placeholder.equals("")) {
                result = new Date().toString();

            }
            else {
                result = DateUtils.nowToString(placeholder);
            }

            if (result == null) {
                result = "";
            }

            return result;
        }
    }

    // Private constants related to placeholders
    private final static char                        PLACEHOLDER_START_DELIM       = '%';
    private final static char                        PLACEHOLDER_VALUE_START_DELIM = '{';
    private final static char                        PLACEHOLDER_END_DELIM         = '}';
    private final static String                      PLACEHOLDER_START_DELIM_STR   = "%{";

    /**
     *
     */
    protected static Map<String, PlaceHolderHandler> handlers                      = null;

    static {
        handlers = new HashMap<String, PlaceHolderHandler>();
        handlers.put("env", new EnvVarPlaceHolderHandler());
        handlers.put("java", new JavaPropsPlaceHolderHandler());
        handlers.put("timestamp", new TimestampPlaceHolderHandler());
        handlers.put("datetime", new DatePlaceHolderHandler());
    }
}
