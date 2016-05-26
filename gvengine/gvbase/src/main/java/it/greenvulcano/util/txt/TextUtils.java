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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * This class contains static utility methods to manage text strings.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class TextUtils
{
    private static Random              rnd        = new Random(System.currentTimeMillis());

    /**
     * A string containing (some) invalid JS chars.
     */
    private static String              jsInvalid  = null;                                  // "&<>\"'\n";
    private static Map<String, String> jsReplacements;
    static {
        jsReplacements = new HashMap<String, String>();
        jsReplacements.put("\"", "\\\"");
        jsReplacements.put("'", "\\'");
        jsReplacements.put("\r", "");
        jsReplacements.put("\n", "\\n");
        jsReplacements.put(">", "&gt;");
        jsReplacements.put("<", "&lt;");
        jsReplacements.put("\u00E0", "\\u00E0"); // &agrave;
        jsReplacements.put("\u00E8", "\\u00E8"); // &egrave;
        jsReplacements.put("\u00E9", "\\u00E9"); // &eacute;
        jsReplacements.put("\u00EC", "\\u00EC"); // &igrave;
        jsReplacements.put("\u00F2", "\\u00F2"); // &ograve;
        jsReplacements.put("\u00F9", "\\u00F9"); // &ugrave;

        StringBuffer sbuf = new StringBuffer(jsReplacements.size());
        Iterator<String> it = jsReplacements.keySet().iterator();
        while (it.hasNext()) {
            sbuf.append(it.next().charAt(0));
        }
        jsInvalid = sbuf.toString();
    }

    /**
     * A string containing (some) invalid SQL chars.
     */

    private static String              sqlInvalid = null;                                  // "'";
    private static Map<String, String> sqlReplacements;
    static {
        sqlReplacements = new HashMap<String, String>();
        sqlReplacements.put("'", "''");

        StringBuffer sbuf = new StringBuffer(sqlReplacements.size());
        Iterator<String> it = sqlReplacements.keySet().iterator();
        while (it.hasNext()) {
            sbuf.append(it.next().charAt(0));
        }
        sqlInvalid = sbuf.toString();
    }

    /**
     * Simply calls
     * {@link java.util.regex.Pattern#matches(String, CharSequence)}.
     * 
     * @param pattern
     * @param text
     * @return
     */
    public static boolean matches(String pattern, String text)
    {
        return Pattern.matches(pattern, text);
    }


    /**
     * Replaces within <tt>input</tt> string all the occurrences of the
     * substring <tt>toBeReplaced</tt> with occurrences of the substring
     * <tt>replacement</tt>.
     * 
     * @param input
     *        the input <tt>String</tt>.
     * @param toBeReplaced
     *        the substring to be replaced within <tt>input</tt> string.
     * @param replacement
     *        the string used to replace substring <tt>toBeReplaced</tt> within
     *        <tt>input</tt> string.
     * @return the input string, with all occurrences of the substring
     *         <tt>toBeReplaced</tt> replaced by occurrences of the substring
     *         <tt>replacement</tt>.
     */
    public static String replaceSubstring(String input, String toBeReplaced, String replacement)
    {
        if (input == null) {
            return null;
        }
        if (input.equals("")) {
            return "";
        }
        if (toBeReplaced == null) {
            toBeReplaced = "";
        }
        if (replacement == null) {
            replacement = "";
        }
        if (toBeReplaced.equals(replacement)) {
            return input;
        }
        if (input.indexOf(toBeReplaced) == -1) {
            return input;
        }

        int startNextToken = 0;
        int endNextToken = 0;
        int maxPosition = input.length();

        StringBuilder buf = new StringBuilder("");
        while (startNextToken < maxPosition) {
            endNextToken = input.indexOf(toBeReplaced, startNextToken);
            if (endNextToken != -1) {
                String currToken = input.substring(startNextToken, endNextToken);
                if (currToken.length() > 0) {
                    buf.append(currToken).append(replacement);
                }
                else {
                    buf.append(replacement);
                }
                startNextToken = endNextToken + toBeReplaced.length();
            }
            else {
                if (startNextToken <= (maxPosition - 1)) {
                    String currToken = input.substring(startNextToken);
                    buf.append(currToken);
                    startNextToken = maxPosition;
                }
            }
        }
        return buf.toString();
    }

    /**
     * @param input
     * @param phPrefix
     * @param phSuffix
     * @param phValues
     * @return the string with placeholders replaced
     * 
     */
    public static String replacePlaceholder(String input, String phPrefix, String phSuffix,
            Hashtable<String, String> phValues)
    {
        if (input == null) {
            return null;
        }
        if (input.equals("")) {
            return "";
        }
        if (phValues == null) {
            return input;
        }
        if (phValues.isEmpty()) {
            return input;
        }
        if ((phPrefix == null) || (phSuffix == null)) {
            return input;
        }
        if ((phPrefix.equals("")) || (phSuffix.equals(""))) {
            return input;
        }

        int phPlen = phPrefix.length();
        int phSlen = phSuffix.length();
        int startNextToken = 0;
        int endNextToken = 0;
        int maxPosition = input.length();

        StringBuilder buf = new StringBuilder("");
        while (startNextToken < maxPosition) {
            endNextToken = input.indexOf(phPrefix, startNextToken);
            if (endNextToken != -1) {
                String currToken = input.substring(startNextToken, endNextToken);
                int endPH = input.indexOf(phSuffix, endNextToken + phPlen);
                String phName = input.substring(endNextToken + phPlen, endPH);
                String replacement = phPrefix + phName + phSuffix;
                Object phValue = phValues.get(phName);
                if (phValue != null) {
                    replacement = "" + phValue;
                }
                if (currToken.length() > 0) {
                    buf.append(currToken).append(replacement);
                }
                else {
                    buf.append(replacement);
                }
                startNextToken = endPH + phSlen;
            }
            else {
                String currToken = input.substring(startNextToken);
                buf.append(currToken);
                startNextToken = maxPosition;
            }
        }
        return buf.toString();
    }

    /**
     * This method tokenizes a given string using another given string as
     * separator and returns a <tt>List</tt> containing found tokens. The
     * returned <tt>List</tt> is NEVER null (it may have zero elements, anyway).
     * 
     * @param theString
     *        the <tt>String</tt> to be tokenized.
     * @param separatorString
     *        the <tt>String</tt> separator between tokens.
     * @return a <tt>List</tt> containing found tokens.
     */
    public static List<String> splitByStringSeparator(String theString, String separatorString)
    {
        List<String> tokenList = new ArrayList<String>();

        if ((theString == null) || theString.equals("")) {
            return tokenList;
        }
        if (theString.equals(separatorString)) {
            return tokenList;
        }
        if ((separatorString == null) || separatorString.equals("")) {
            tokenList.add(theString);
            return tokenList;
        }
        if (theString.indexOf(separatorString) == -1) {
            tokenList.add(theString);
            return tokenList;
        }

        /*if (separatorString.length() == 1) {
            StringTokenizer st = new StringTokenizer(theString, separatorString);
            while (st.hasMoreTokens()) {
                String currToken = st.nextToken();
                tokenList.add(currToken);
            }
            return tokenList;
        }*/

        int startNextToken = 0;
        int endNextToken = 0;
        int maxPosition = theString.length();

        while (startNextToken < maxPosition) {
            endNextToken = theString.indexOf(separatorString, startNextToken);
            if (endNextToken != -1) {
                if (endNextToken > startNextToken) {
                    String currToken = theString.substring(startNextToken, endNextToken);
                    tokenList.add(currToken);
                }
                else if (endNextToken == startNextToken) {
                    tokenList.add("");
                }
                startNextToken = endNextToken + separatorString.length();
            }
            else if (startNextToken <= (maxPosition - 1)) {
                String currToken = theString.substring(startNextToken);
                tokenList.add(currToken);
                startNextToken = maxPosition;
            }
        }
        return tokenList;
    }

    /**
     * Replaces JS invalid chars within <tt>input</tt> String with the
     * corresponding entities.
     * 
     * @param input
     *        the input <tt>String</tt>.
     * @return the input string, with JS invalid chars replaced by the
     *         corresponding entities.
     */
    public static String replaceJSInvalidChars(String input)
    {
        // Shortcuts for efficiency
        final int len;
        if ((input == null) || ((len = input.length()) == 0)) {
            return input;
        }

        StringBuffer result = new StringBuffer((int) (len * 1.5));
        StringTokenizer tokenizer = new StringTokenizer(input, jsInvalid, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            Object repl = jsReplacements.get(token);
            if (repl == null) {
                repl = token;
            }
            result.append((String) repl);
        }
        return result.toString();
    }

    /**
     * Replaces SQL invalid chars within <tt>input</tt> String with the
     * corresponding entities.
     * 
     * @param input
     *        the input <tt>String</tt>.
     * @return the input string, with SQL invalid chars replaced by the
     *         corresponding entities.
     */
    public static String replaceSQLInvalidChars(String input)
    {
        // Shortcuts for efficiency
        final int len;
        if ((input == null) || ((len = input.length()) == 0)) {
            return input;
        }

        StringBuffer result = new StringBuffer((int) (len * 1.5));
        StringTokenizer tokenizer = new StringTokenizer(input, sqlInvalid, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            Object repl = sqlReplacements.get(token);
            if (repl == null) {
                repl = token;
            }
            result.append((String) repl);
        }
        return result.toString();
    }
    
    public static String urlEncode(String value) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(value, "UTF-8");
        return encoded;
    }
    
    public static String urlDecode(String value) throws UnsupportedEncodingException {
        String encoded = URLDecoder.decode(value, "UTF-8");
        return encoded;
    }

    /**
     * Convert the following definitions to the corresponding EOL char:
     * - \n or LF : line feed
     * - \r or CR : carriage return
     * - \r\n or CR-LF : carriage return and line feed
     * - native : OS native EOL
     * 
     * @param def
     *        the definition to convert
     * @return the EOL char or null
     */
    public static String getEOL(String def)
    {
        String eol = null;
        if ((def == null) || ("".equals(eol))) {
            return null;
        }

        if (def.equals("\\n") || def.equals("LF")) {
            eol = "\n";
        }
        else if (def.equals("\\r\\n") || def.equals("CR-LF")) {
            eol = "\r\n";
        }
        else if (def.equals("\\r") || def.equals("CR")) {
            eol = "\r";
        }
        else if (def.equals("native")) {
            eol = System.getProperty("line.separator");
        }
        return eol;
    }

    /**
     * Opens and loads the content of a text file into a String
     * 
     * @param filename
     *        the name of the file
     * @return The content of the text file as a String
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(String filename) throws FileNotFoundException, IOException
    {
        BufferedReader reader = null;
        try {
            filename = adjustPath(filename);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            return IOUtils.toString(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }


    /**
     * Opens and loads the content of a text file into a String
     * 
     * @param file
     * @return The content of the text file as a String
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(File file) throws FileNotFoundException, IOException
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            return IOUtils.toString(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Opens and loads the content of a text file (read from URL) into a
     * String
     * 
     * @param url
     *        the url of the file
     * @return The content of the text file as a String
     * @throws IOException
     */
    public static String readFileFromURL(URL url) throws IOException
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return IOUtils.toString(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Opens and loads the content of a text file into a list of String
     * 
     * @param filename
     *        the name of the file
     * @return The content of the text file as a list of String
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> readFileAsLines(String filename) throws FileNotFoundException, IOException
    {
        BufferedReader reader = null;
        try {
            filename = adjustPath(filename);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            return IOUtils.readLines(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Opens and loads the content of a text file (read from URL) into a
     * list of String
     * 
     * @param url
     *        the url of the file
     * @return The content of the text file as a list of String
     * @throws IOException
     */
    public static List<String> readFileAsLinesFromURL(URL url) throws IOException
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            return IOUtils.readLines(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentString
     *        The String to be written into the file
     * @param filename
     *        The name of the file
     * @throws IOException
     */
    public static void writeFile(String contentString, String filename) throws IOException
    {
        writeFile(contentString, filename, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentString
     *        The String to be written into the file
     * @param filename
     *        The name of the file
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(String contentString, String filename, boolean append) throws IOException
    {
        PrintWriter out = null;
        try {
            filename = adjustPath(filename);
            out = new PrintWriter(new FileWriter(filename, append));
            out.print(contentString);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentString
     *        The StringBuffer to be written into the file
     * @param filename
     *        The name of the file
     * @throws IOException
     */
    public static void writeFile(StringBuffer contentString, String filename) throws IOException
    {
        writeFile(contentString, filename, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentString
     *        The StringBuffer to be written into the file
     * @param filename
     *        The name of the file
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(StringBuffer contentString, String filename, boolean append) throws IOException
    {
        PrintWriter out = null;
        try {
            filename = adjustPath(filename);
            out = new PrintWriter(new FileWriter(filename, append));
            IOUtils.write(contentString, out);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentStrings
     *        The list of strings to be written into the file
     * @param filename
     *        The name of the file
     * @param endline
     *        The line terminator, if null or empty is used '\n'
     * @throws IOException
     */
    public static void writeFile(List<String> contentStrings, String filename, String endline) throws IOException
    {
        writeFile(contentStrings, filename, endline, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentStrings
     *        The list of strings to be written into the file
     * @param filename
     *        The name of the file
     * @param endline
     *        The line terminator, if null or empty is used '\n'
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(List<String> contentStrings, String filename, String endline, boolean append)
            throws IOException
    {
        FileOutputStream out = null;
        try {
            filename = adjustPath(filename);
            if ((endline == null) || endline.equals("")) {
                endline = "\n";
            }
            out = new FileOutputStream(filename, append);
            IOUtils.writeLines(contentStrings, endline, out);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentString
     *        The String to be written into the file
     * @param file
     *        The destination file
     * @throws IOException
     */
    public static void writeFile(String contentString, File file) throws IOException
    {
        writeFile(contentString, file, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentString
     *        The String to be written into the file
     * @param file
     *        The destination file
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(String contentString, File file, boolean append) throws IOException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file, append));
            out.print(contentString);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentString
     *        The StringBuffer to be written into the file
     * @param file
     *        The destination file
     * @throws IOException
     */
    public static void writeFile(StringBuffer contentString, File file) throws IOException
    {
        writeFile(contentString, file, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentString
     *        The StringBuffer to be written into the file
     * @param file
     *        The destination file
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(StringBuffer contentString, File file, boolean append) throws IOException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file, append));
            IOUtils.write(contentString, out);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a text String into a new file
     * 
     * @param contentStrings
     *        The StringBuffer to be written into the file
     * @param file
     *        The destination file
     * @param endline
     *        The line terminator, if null or empty is used '\n'
     * @throws IOException
     */
    public static void writeFile(List<String> contentStrings, File file, String endline) throws IOException
    {
        writeFile(contentStrings, file, endline, false);
    }

    /**
     * Writes a text String into a file
     * 
     * @param contentStrings
     *        The StringBuffer to be written into the file
     * @param file
     *        The destination file
     * @param endline
     *        The line terminator, if null or empty is used '\n'
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     */
    public static void writeFile(List<String> contentStrings, File file, String endline, boolean append)
            throws IOException
    {
        FileOutputStream out = null;
        try {
            if ((endline == null) || endline.equals("")) {
                endline = "\n";
            }
            out = new FileOutputStream(file, append);
            IOUtils.writeLines(contentStrings, endline, out);
            out.flush();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Dumps the content of a byte array as chars
     * 
     * @param arr
     *        a byte array
     * @return A representation of the given byte array as a sequence of chars
     */
    public static String dumpByteArrayAsChars(byte[] arr)
    {
        StringBuilder buf = new StringBuilder("");
        for (byte element : arr) {
            buf.append((char) element);
        }
        return buf.toString();
    }

    /**
     * Generate random chars string of given <code>length</code>.
     * 
     * @param length
     * @return a random string of given <code>length</code>.
     */
    public static String generateRandomString(int length)
    {
        int blen = ((length + 3) / 4) * 3; // base 64: 3 bytes = 4 chars
        byte[] bval = new byte[blen];
        rnd.nextBytes(bval);
        // change '/' and '\' with '$' in case the string is used as file name
        return new String(Base64.getEncoder().encode(bval), 0, length).replace('/', '$');
    }

    
    /**
     * Check if a given string is null and if so returns an empty string.
     * 
     * @param value
     * @return the input value or an empty string if input is null
     */
    public static String nullToEmpty(String value)
    {
        if (value == null) {
            return "";
        }
        return value;
    }
    
    public static void checkNull(String message, Object value) throws NullPointerException
    {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * @param throwable
     * @return riturn the stack-trace
     */
    public static String getStackTrace(Throwable throwable)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream(baos);
        String stack = null;
        throwable.printStackTrace(pstream);
        pstream.flush();
        stack = baos.toString();
        return stack;
    }

    /**
     * @param filename
     * @return
     */
    public static String adjustPath(String filename) {
        if (File.separator.equals("\\")) {
            filename = filename.replace("\\", File.separator);
        }
        else {
            filename = filename.replace("/", File.separator);
        }
        return filename;
    }


    /**
     * Parse the following string (case insensitive) into boolean values:
     * boolean true : 'true', 'yes', 'si', 'on', 'ok', '1'
     * boolean false: all other values, empty or null
     * 
     * @param s
     * @return boolean value
     */
    public static boolean parseBoolean(String s) {
        if (s == null) return false;
        return (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("si")
                || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("ok") || s.equalsIgnoreCase("1"));
    }

}
