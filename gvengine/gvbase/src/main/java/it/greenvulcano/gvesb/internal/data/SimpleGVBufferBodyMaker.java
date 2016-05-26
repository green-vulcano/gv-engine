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
package it.greenvulcano.gvesb.internal.data;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.w3c.dom.Node;

/**
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class SimpleGVBufferBodyMaker implements GVBufferBodyMaker
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(SimpleGVBufferBodyMaker.class);

    /**
     * The data to copy in the Object field of GVBuffer.
     */
    private byte[]        body            = null;

    /**
     * The name of the file from which read the body.
     */
    private String        fileName        = null;

    /**
     * If true the file content is cached.
     */
    private boolean       readOnce        = true;

    /**
     * If true the file content can contains metadata to be resolved.
     */
    private boolean       processMetadata = true;

    /**
     * Initialize the instance.
     * 
     * @param node
     *        the initialization node.
     * @throws XMLConfigException
     *         if error occurs.
     */
    @Override
    public final void init(Node node) throws Exception
    {
        fileName = XMLConfig.get(node, "@file-name");
        if (fileName == null) {
            String sBody = XMLConfig.get(node, ".");
            String encoding = XMLConfig.get(node, "@encoding", "ISO-8859-1");

            if (sBody != null) {
                try {
                    body = sBody.getBytes(encoding);
                }
                catch (Exception exc) {
                    throw new XMLConfigException("Bad encoding type '" + encoding + "' for node "
                            + XPathFinder.buildXPath(node), exc);
                }
            }
            logger.debug("Initialized SimpleGVBufferBodyMaker from Node");
        }
        else {
            fileName = PropertiesHandler.expand(XMLConfig.get(node, "@file-name"));
            readOnce = XMLConfig.getBoolean(node, "@read-once", true);
            setBody();
            logger.debug("Initialized SimpleGVBufferBodyMaker from file: " + fileName);
        }

        processMetadata = XMLConfig.getBoolean(node, "@process-metadata", false);
    }

    /**
     * @param currBuffer
     *        the current GVBuffer value
     * @return the data to be used as body of the GVBuffer.
     */
    @Override
    public final byte[] getBuffer(GVBuffer currBuffer)
    {
        if ((fileName != null) && !readOnce) {
            setBody();
        }
        if (processMetadata) {
            try {
                return PropertiesHandler.expand(new String(body),
                        GVBufferPropertiesHelper.getPropertiesMapSO(currBuffer, true), currBuffer).getBytes();
            }
            catch (Exception exc) {
                logger.error("SimpleGVDataBodyMaker - Cannot process buffer metadata", exc);
            }
            return null;
        }
        return body;
    }

    /**
     * Perform cleanup operations. Is called after getData().
     */
    @Override
    public final void cleanUp()
    {
        // do nothing
    }

    /**
     * Read the body content from file.
     * 
     */
    private void setBody()
    {
        try {
            InputStream is = null;
            if (fileName.startsWith("CP://") || fileName.startsWith("cp://")) {
                is = this.getClass().getClassLoader().getResourceAsStream(fileName.substring(5));
            }
            else {
                is = new FileInputStream(new File(fileName));
            }
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            int size;
            while ((size = is.read(buffer)) != -1) {
                baos.write(buffer, 0, size);
            }
            body = baos.toByteArray();
        }
        catch (Exception exc) {
            logger.error("SimpleGVDataBodyMaker - Cannot read the specified resource (" + fileName + ")", exc);
        }
    }

}
