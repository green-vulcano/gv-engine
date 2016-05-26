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
package it.greenvulcano.util.bin;

import it.greenvulcano.util.txt.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * This class contains static utility methods to manage binary buffers
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 **/
public final class BinaryUtils
{

    /**
     * Dumps the content of a byte array as a sequence of integers
     * 
     * @param arr
     *        the array to convert
     * @return the converted value
     */
    public static String dumpByteArrayAsInts(byte[] arr)
    {
        StringBuilder buf = new StringBuilder("");
        for (int i = 0; i < arr.length; i++) {
            buf.append(arr[i]);
            if (i != (arr.length - 1)) {
                buf.append(" ");
            }
        }
        return buf.toString();
    }

    /**
     * Dumps the content of a byte array as a sequence of integers (Hex format)
     * 
     * @param arr
     *        The byte array
     * @return The String conversion of the buffer bytes as integer values in
     *         2-Hex-digits output format
     */
    public static String dumpByteArrayAsHexInts(byte[] arr)
    {
        StringBuilder buf = new StringBuilder("");
        for (byte element : arr) {
            String byteValueAsHex = Integer.toHexString(element);
            if (byteValueAsHex.length() < 2) {
                buf.append("0").append(byteValueAsHex);
            }
            else {
                buf.append(byteValueAsHex.substring(byteValueAsHex.length() - 2));
            }
        }
        return buf.toString().toUpperCase();
    }

    /**
     * Dumps the Hex formatted sequence as a byte array
     * 
     * @param hexArr
     *        The 2-Hex-digits string to convert
     * @return the byte array, or an empy array if input is malformed
     */
    public static byte[] dumpHexIntsAsByteArray(String hexArr)
    {
        if ((hexArr.length() % 2) != 0) {
            return new byte[0];
        }
        byte[] byteArr = new byte[hexArr.length() / 2];
        int eb = 0;
        int ih = 0;
        while (eb < byteArr.length) {
            byteArr[eb] = (byte) Integer.parseInt(hexArr.substring(ih, ih + 2), 16);
            eb++;
            ih += 2;
        }
        return byteArr;
    }

    /**
     * Convert sequences of characters (into an input string) representing an
     * escape sequence into the corresponding escape sequences
     * 
     * @param input
     *        the input string
     * @return the converted string
     */
    public static String unescapeString(String input)
    {
        StringBuilder buf = new StringBuilder("");
        int i = 0;
        while (i < input.length()) {
            char currChar = input.charAt(i);
            if ((currChar == '\\') && (i < (input.length() - 1))) {
                char toAdd = 0;
                char nextChar = input.charAt(++i);
                if (nextChar == '0') {
                    // End-of-string escape sequence
                    toAdd = '\0';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == 'b') {
                    // backspace escape sequence
                    toAdd = '\b';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == 't') {
                    // tab escape sequence
                    toAdd = '\t';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == 'n') {
                    // linefeed escape sequence
                    toAdd = '\n';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == 'f') {
                    // formfeed escape sequence
                    toAdd = '\f';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == 'r') {
                    // carriage return escape sequence
                    toAdd = '\r';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == '\"') {
                    // double quote escape sequence
                    toAdd = '\"';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == '\'') {
                    // single quote escape sequence
                    toAdd = '\'';
                    buf.append(toAdd);
                    i++;
                }
                else if (nextChar == '\\') {
                    // backslash escape sequence
                    String strToAdd = "\\\\";
                    buf.append(strToAdd);
                    i++;
                }
                else {
                    // It's not an escape sequence
                    buf.append(currChar).append(nextChar);
                    i++;
                }
            }
            else {
                buf.append(currChar);
                i++;
            }
        }
        return buf.toString();
    }

    /**
     * This method tokenizes a given string using a given byte sequence as
     * separator and returns a <tt>List</tt> containing found tokens. The
     * returned <tt>List</tt> is NEVER null (it may have zero components,
     * anyway).
     * 
     * @param theString
     *        the <tt>String</tt> to be tokenized.
     * @param separatorByteSequence
     *        the byte sequence separator between tokens.
     * @return a <tt>List</tt> containing found tokens.
     */
    public static List<String> splitByByteSequenceSeparator(String theString, byte[] separatorByteSequence)
    {
        List<String> tokenList = new ArrayList<String>();

        if ((theString == null) || theString.equals("")) {
            return tokenList;
        }

        byte[] input = theString.getBytes();

        if (Arrays.equals(input, separatorByteSequence)) {
            return tokenList;
        }

        if ((separatorByteSequence == null) || (separatorByteSequence.length == 0)) {
            tokenList.add(theString);
            return tokenList;
        }

        if (findByteSequenceWithinByteArray(input, separatorByteSequence) == -1) {
            tokenList.add(theString);
            return tokenList;
        }

        int startNextToken = 0;
        int endNextToken = 0;
        int maxPosition = input.length;

        while (startNextToken < maxPosition) {
            endNextToken = findByteSequenceWithinByteArray(input, separatorByteSequence, startNextToken);
            if (endNextToken != -1) {

                if (endNextToken > startNextToken) {

                    byte[] buffer = new byte[endNextToken - startNextToken];
                    System.arraycopy(input, startNextToken, buffer, 0, buffer.length);
                    String currToken = new String(buffer);
                    tokenList.add(currToken);
                }
                startNextToken = endNextToken + separatorByteSequence.length;
            }
            else {
                if (startNextToken != (maxPosition - 1)) {
                    byte[] buffer = new byte[maxPosition - startNextToken];
                    System.arraycopy(input, startNextToken, buffer, 0, buffer.length);
                    String currToken = new String(buffer);
                    tokenList.add(currToken);
                    startNextToken = maxPosition;
                }
            }
        }
        return tokenList;
    }

    /**
     * Loads the full content of a file.<br>
     * The file must be reachable via the <i>parent classloader</i>, or at least
     * via the <i>system classloader</i>.<br>
     * The full content of a file is then stored into a <code>byte</code> array.<br>
     * 
     * @param filename
     *        the name of the file to be read
     * @return the full content of the file, stored into a <code>byte</code>
     *         array
     * @throws IOException
     *         if any I/O error occurs
     */
    public static byte[] readFileAsBytesFromCP(String filename) throws IOException
    {
        filename = TextUtils.adjustPath(filename);
        URL url = ClassLoader.getSystemResource(filename);
        if (url == null) {
            throw new IOException("File " + filename + " not found in classpath");
        }

        InputStream in = null;
        try {
            in = url.openStream();
            return inputStreamToBytes(in);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Loads the full content of a file into a <code>byte</code> array.<br>
     * 
     * @param filename
     *        the name of the file to be read
     * @return the full content of the file, stored into a <code>byte</code>
     *         array
     * @throws IOException
     *         if any I/O error occurs
     */
    public static byte[] readFileAsBytes(String filename) throws IOException
    {
        FileInputStream in = null;
        try {
            filename = TextUtils.adjustPath(filename);
            in = new FileInputStream(filename);
            return inputStreamToBytes(in);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Loads the full content of a file into a <code>byte</code> array.<br>
     * 
     * @param file
     *        the file to be read
     * @return the full content of the file, stored into a <code>byte</code>
     *         array
     * @throws IOException
     *         if any I/O error occurs
     */
    public static byte[] readFileAsBytes(File file) throws IOException
    {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return inputStreamToBytes(in);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Read all data from a source <code>InputStream</code> and stores them into
     * a <code>byte</code> array.<br>
     * It is assumed that the source <code>InputStream</code> will be closed by
     * the caller of this method.
     * 
     * @param in
     *        the <code>InputStream</code> to read from
     * @return the read bytes, stored into a <code>byte</code> array
     * @throws IOException
     *         if any I/O error occurs
     */
    public static byte[] inputStreamToBytes(InputStream in) throws IOException
    {
        return IOUtils.toByteArray(in);
    }

    /**
     * Read all data from a source <code>InputStream</code> and stores them into
     * a file on the local filesystem.<br>
     * It is assumed that the source <code>InputStream</code> will be closed by
     * the caller of this method.
     * 
     * @param in
     *        the <code>InputStream</code> to read from
     * @param filename
     *        the name of the file to be written to
     * @throws IOException
     *         if any I/O error occurs
     */
    public static void inputStreamToFile(InputStream in, String filename) throws IOException
    {
        OutputStream fOut = null;
        try {
            filename = TextUtils.adjustPath(filename);
            fOut = new FileOutputStream(filename);
            IOUtils.copyLarge(in, fOut);
        }
        finally {
            if (fOut != null) {
                fOut.close();
            }
        }
    }

    /**
     * Write the content of a <code>byte</code> array into a new file on the
     * local filesystem.
     * 
     * @param contentArray
     *        the <code>byte</code> array to be written to file
     * @param filename
     *        the name of the file to be written to
     * @throws IOException
     *         if any I/O error occurs
     */
    public static void writeBytesToFile(byte[] contentArray, String filename) throws IOException
    {
        writeBytesToFile(contentArray, filename, false);
    }

    /**
     * Write the content of a <code>byte</code> array into a file on the local
     * filesystem.
     * 
     * @param contentArray
     *        the <code>byte</code> array to be written to file
     * @param filename
     *        the name of the file to be written to
     * @param append
     *        If true the data are appended to existent file
     * @throws IOException
     *         if any I/O error occurs
     */
    public static void writeBytesToFile(byte[] contentArray, String filename, boolean append) throws IOException
    {
        BufferedOutputStream bufOut = null;
        try {
            filename = TextUtils.adjustPath(filename);
            bufOut = new BufferedOutputStream(new FileOutputStream(filename, append), 10240);
            IOUtils.write(contentArray, bufOut);
            bufOut.flush();
        }
        finally {
            if (bufOut != null) {
                bufOut.close();
            }
        }
    }

    /**
     * Write the content of a <code>byte</code> array into a new file on the
     * local filesystem.
     * 
     * @param contentArray
     *        the <code>byte</code> array to be written to file
     * @param file
     *        the file to be written to
     * @throws IOException
     *         if any I/O error occurs
     */
    public static void writeBytesToFile(byte[] contentArray, File file) throws IOException
    {
        writeBytesToFile(contentArray, file, false);
    }

    /**
     * Write the content of a <code>byte</code> array into a file on the local
     * filesystem.
     * 
     * @param contentArray
     *        the <code>byte</code> array to be written to file
     * @param file
     *        the file to be written to
     * @throws IOException
     *         if any I/O error occurs
     */
    public static void writeBytesToFile(byte[] contentArray, File file, boolean append) throws IOException
    {
        BufferedOutputStream bufOut = null;
        try {
            bufOut = new BufferedOutputStream(new FileOutputStream(file, append), 10240);
            IOUtils.write(contentArray, bufOut);
            bufOut.flush();
        }
        finally {
            if (bufOut != null) {
                bufOut.close();
            }
        }
    }

    /**
     * Returns <code>true</code> if the passed <code>byte</code> value is an
     * ASCII printable character, <code>false</code> otherwise.
     * 
     * @param b
     *        the passed <code>byte</code> value
     * @return <code>true</code> if the passed <code>byte</code> value is an
     *         ASCII printable character, <code>false</code> otherwise.
     */
    public static boolean isASCIIPrintableChar(byte b)
    {
        return ((b >= 0x20) && (b <= 0x7E));
    }

    /**
     * Returns the index of the first occurrence of the given byte sequence
     * within a given byte array starting at the specified index, or -1 if the
     * byte sequence does not occur.
     * 
     * @param input
     *        A given byte array.
     * @param byteSequence
     *        A given byte sequence.
     * @param fromIndex
     *        The index to start the search from.
     * @return the index of the first occurrence of the given byte sequence
     *         within a given byte array starting at the specified index, or -1
     *         if the byte sequence does not occur.
     */
    private static int findByteSequenceWithinByteArray(byte[] input, byte[] byteSequence, int fromIndex)
    {
        int result = -1;

        if ((input == null) || (byteSequence == null)) {
            return result;
        }
        if ((fromIndex < 0) || (fromIndex > (input.length - 1))) {
            return result;
        }
        if ((input.length - fromIndex) < byteSequence.length) {
            return result;
        }

        byte[] buffer = new byte[byteSequence.length];

        for (int i = fromIndex; i <= (input.length - byteSequence.length); i++) {
            System.arraycopy(input, i, buffer, 0, buffer.length);
            if (Arrays.equals(buffer, byteSequence)) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the index of the first occurrence of the given byte sequence into
     * a byte array obtained from the given string, or -1 if the byte sequence
     * does not occur.
     * 
     * @param input
     *        A given string.
     * @param byteSequence
     *        A given byte sequence.
     * @return the index of the first occurrence of the given byte sequence into
     *         a byte array obtained from the given string, or -1 if the byte
     *         sequence does not occur.
     * 
     */
    private static int findByteSequenceWithinByteArray(byte[] input, byte[] byteSequence)
    {
        return findByteSequenceWithinByteArray(input, byteSequence, 0);
    }

    /**
     * The following might seem useless but it's just a trick to have something
     * on which call the getClass().getClassloader().getResource() non-static
     * method calls chain.
     */
    private static BinaryUtils theInstance = null;

    /**
     * Private method to retrieve the one-and-only-private instance of
     * BinaryUtils (which does nothing).
     */
    @SuppressWarnings("unused")
	private static BinaryUtils getPrivateInstance()
    {
        if (theInstance != null) {
            theInstance = new BinaryUtils();
        }
        return theInstance;
    }

    /**
     * Private constructor
     */
    private BinaryUtils()
    {
        // do nothing
    }
}
