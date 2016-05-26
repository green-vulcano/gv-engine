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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Used to execute a bytes array to Hex dump, or to recover the dumped data.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Dump
{
    public static final int UNBOUNDED       = -1;

    private byte[]          buffer;
    private int             maxBufferLength = 0;
    private BufferedReader  reader;

    /**
     * @param buffer
     */
    public Dump(byte[] buffer)
    {
        this.buffer = buffer;
    }

    /**
     * @param buffer
     * @param maxBufferLength
     */
    public Dump(byte[] buffer, int maxBufferLength)
    {
        this.buffer = buffer;
        this.maxBufferLength = maxBufferLength;
    }
    
    /**
     * 
     * @param reader used to read dumped lines to be converted as bytes
     */
    public Dump(Reader reader) {
        this.reader = new BufferedReader(reader);
    }
    
    /**
     * 
     * @param dump used to read dumped lines to be converted as bytes
     */
    public Dump(String dump) {
        this.reader = new BufferedReader(new StringReader(dump));
    }

    /**
     * @param maxBufferLength
     */
    public void setMaxBufferLength(int maxBufferLength)
    {
        this.maxBufferLength = maxBufferLength;
    }

    /**
     * Perform the dump into a String.
     */
    @Override
    public String toString()
    {
        try {
            StringWriter writer = new StringWriter();
            dump(writer);
            return writer.toString();
        }
        catch (IOException exc) {
            // Should never happen
            return "" + exc;
        }
    }

    /**
     * Perform the dump into an OutputStream
     *
     * @param stream
     * @throws IOException
     */
    public void dump(OutputStream stream) throws IOException
    {
        dump(new OutputStreamWriter(stream));
    }

    /**
     * Perform the dump into an Writer
     *
     * @param writer
     * @throws IOException
     */
    public void dump(Writer writer) throws IOException
    {
        int bufferLength = buffer.length;

        // No dump is required
        if (maxBufferLength == 0) {
            return;
        }

        if ((maxBufferLength > 0) && (maxBufferLength < buffer.length)) {
            bufferLength = maxBufferLength;
        }

        for (int start = 0; start < bufferLength; start += 16) {
            int end = start + 16;
            if (end > bufferLength) {
                end = bufferLength;
            }
            dump(start, end, writer);
        }
        writer.flush();
    }

    public byte[] recoverToBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        recoverToStream(buffer);
        return buffer.toByteArray();
    }

    public void recoverToStream(OutputStream stream) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String hex = splitRow(line);
            if (hex == null) {
                throw new IOException("Error: unsupported data format");
            }
            //stream.write(hex.getBytes());
            stream.write(BinaryUtils.dumpHexIntsAsByteArray(hex));
        }
    }

    private static final String spaces = "                                                ";

    private void dump(int start, int end, Writer writer) throws IOException
    {
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();

        writer.write(toHex(start, 8));
        writer.write(";");

        for (int i = start; i < end; ++i) {
            int b = buffer[i] & 0xFF;
            hex.append(" ").append(toHex(b, 2));
            if ((b >= 32) && (b < 128)) {
                ascii.append((char) b);
            }
            else {
                ascii.append(".");
            }
        }

        writer.write(hex.toString());
        int l = hex.length() / 3;
        if (l < 16) {
            writer.write(spaces.substring(0, (16 - l) * 3));
        }
        writer.write(" ; ");
        writer.write(ascii.toString());
        writer.write("\n");
    }

    private static final String zeros = "0000000000000000";

    private String toHex(int num, int width)
    {
        String ret = Integer.toHexString(num).toUpperCase();
        int z = width - ret.length();
        if (z > 0) {
            return zeros.substring(0, z) + ret;
        }
        return ret;
    }

    /**
     * Split the string into 3 blocks, separated by ';'
     *
     * @param row
     * @return the splitted row
     */
    private String splitRow(String row)
    {
        String output = null;
        int i = row.indexOf(";");
        int j = row.indexOf(";", i + 1);
        if ((i > 0) && (j > 0)) {
            output = row.substring(i + 1, j);
        }
        if ((i < 0) && (j < 0)) {
            output = row.trim();
        }
        if (output != null) {
            output = output.replaceAll("\\s", "");
        }
        return output;
    }

}
