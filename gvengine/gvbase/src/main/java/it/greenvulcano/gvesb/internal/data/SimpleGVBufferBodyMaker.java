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
package it.greenvulcano.gvesb.internal.data;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.internal.GVInternalException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xpath.XPathFinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.Node;

/**
 * 
 * @version 4.1.0 Jul 01, 2020
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class SimpleGVBufferBodyMaker implements GVBufferBodyMaker {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleGVBufferBodyMaker.class);

    /**
     * The data to copy in the Object field of GVBuffer.
     */
    private byte[] body = null;

    /**
     * The name of the file from which read the body.
     */
    private String fileName = null;
    
    /**
     * If true the file content can contains metadata to be resolved.
     */
    private boolean processMetadata = true;

    private String encoding = null;

    /**
     * Initialize the instance.
     * 
     * @param node
     * the initialization node.
     * @throws XMLConfigException
     * if error occurs.
     */
    @Override
    public final void init(Node node) throws Exception {

        encoding = XMLConfig.get(node, "@encoding", "UTF-8");
        fileName = XMLConfig.get(node, "@file-name");       
        processMetadata = XMLConfig.getBoolean(node, "@process-metadata", true);
        
        if (fileName == null) {
            try {
                body = XMLConfig.get(node, ".").getBytes(encoding);
            } catch (Exception exc) {
                throw new XMLConfigException("Bad encoding type '" + encoding + "' for node " + XPathFinder.buildXPath(node), exc);
            }
            logger.debug("Initialized SimpleGVBufferBodyMaker from Node");
        
        } else {            
            logger.debug("Initialized SimpleGVBufferBodyMaker from file: " + fileName);
        }

    }

    /**
     * @param currBuffer
     * the current GVBuffer value
     * @return the data to be used as body of the GVBuffer.
     */
    @Override
    public final byte[] getBuffer(GVBuffer currBuffer) throws GVException {

        try {

            currBuffer.setProperty("OBJECT_ENCODING", encoding);
            
            if (fileName != null) {
                
                Path filePath = Paths.get(PropertiesHandler.expand(fileName, currBuffer));
                logger.debug("Loading body from file from file: " + filePath.toString());
                body = Files.readAllBytes(filePath);
            }
            
            return processMetadata ? PropertiesHandler.expand(new String(body, encoding), currBuffer).getBytes() : body;
            
            
        } catch (Exception exc) {
            throw new GVInternalException("SIMPLE_GVBUFFER_ERROR", new String[][] { { "message", exc.getMessage() } }, exc);
        }
      
    }

    /**
     * readOnce
     * Perform cleanup operations. Is called after getData().
     */
    @Override
    public final void cleanUp() {
        // do nothing
    }

}