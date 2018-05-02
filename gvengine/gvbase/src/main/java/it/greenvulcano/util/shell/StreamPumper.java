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
package it.greenvulcano.util.shell;

import it.greenvulcano.util.thread.BaseThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

/**
 * This class is used for read a stream.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class StreamPumper extends BaseThread
{

    private static final Logger logger      = org.slf4j.LoggerFactory.getLogger(StreamPumper.class);

    /**
     * The stream to be read.
     */
    private BufferedReader      stream;

    /**
     * If true the end of stream has been reached.
     */
    private boolean             endOfStream = false;

    /**
     * Sleep time.
     */
    private static final int    SLEEP_TIME  = 5;

    /**
     * Buffer containing the stream.
     */
    private StringBuffer        buffer      = null;

    /**
     * The line separator.
     */
    private String              crlf        = "";

    /**
     * Default constructor.
     *
     * @param is
     *        the stream to be read.
     */
    public StreamPumper(InputStream is)
    {
        super("StreamPumper");
        stream = new BufferedReader(new InputStreamReader(is));
        buffer = new StringBuffer(4096);
        crlf = System.getProperty("line.separator");
    }

    /**
     * Reads the stream and flushes into a buffer.
     *
     * @throws IOException
     *         if an error occurs.
     */
    private void pumpStream() throws IOException
    {

        if (!endOfStream) {
            String line = stream.readLine();
            if (line != null) {
                buffer.append(line);
                buffer.append(crlf);
            }
            else {
                endOfStream = true;
            }
        }
    }

    /**
     * Starts the thread reading the stream.
     */
    @Override
    public void run()
    {
        try {
            try {
                while (!endOfStream) {
                    pumpStream();
                    sleep(SLEEP_TIME);
                }
            }
            catch (InterruptedException ie) {
                logger.warn("An error occurred: " + ie.getMessage(), ie);
            }
            stream.close();
        }
        catch (IOException ioe) {
            logger.warn("An error occurred: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns the stream.
     *
     * @return the stream in String format.
     */
    public String getOutput()
    {
        return buffer.toString();
    }
}
