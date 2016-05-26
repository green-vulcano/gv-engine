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
package it.greenvulcano.gvesb.gvdte.transformers.bin.bin2bin;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class handle bytes overwriting.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class OverWriteBytesTransformer implements DTETransformer
{
    private static final Logger     logger  = org.slf4j.LoggerFactory.getLogger(OverWriteBytesTransformer.class);

    private String                  name;
    private int                     offset  = 0;
    private byte[]                  vBytes  = null;
    private List<TransformerHelper> helpers = new ArrayList<TransformerHelper>();

    public OverWriteBytesTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param nodo
     * @param dsf
     *
     * @throws DTETransfException
     *         if configuration parameters or conversion properties can't be
     *         accessed for any reason.
     */
    public void init(Node nodo, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        try {
            name = XMLConfig.get(nodo, "@name", "NO_NAME");
            offset = XMLConfig.getInteger(nodo, "@Offset");
            String inputBytesHex = XMLConfig.get(nodo, "@Bytes");
            logger.debug("Loaded parameters: offset = " + offset + " - bytes = " + inputBytesHex);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StringTokenizer st = new StringTokenizer(inputBytesHex, ",");
            while (st.hasMoreTokens()) {
                baos.write(Integer.parseInt(st.nextToken(), 16));
            }
            vBytes = baos.toByteArray();

            logger.debug("Init stop");
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * The <code>input</code> parameter is a byte. The return value is a byte array representing
     * the input whith some bytes overwritten.
     *
     * @param input
     *        the input data of the transformation (a byte array).
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return the input with some bytes overwritten.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        try {
            byte[] inputBuffer = (byte[]) input;
            byte[] intermediateBuffer = (byte[]) buffer;

            if (intermediateBuffer != null) {
                System.arraycopy(vBytes, 0, intermediateBuffer, offset, vBytes.length);
                logger.debug("Transform stop");
                return intermediateBuffer;
            }

            if (inputBuffer != null) {
                byte[] outputBuffer = new byte[inputBuffer.length];
                System.arraycopy(inputBuffer, 0, outputBuffer, 0, inputBuffer.length);
                System.arraycopy(vBytes, 0, outputBuffer, offset, vBytes.length);
                logger.debug("Transform stop");
                return outputBuffer;
            }
            throw new Exception("Input buffer and intermediate buffer can't be both null");
        }
        catch (ClassCastException exc) {
            logger.error("Input object is not a binary buffer", exc);
            throw new DTETransfException("GVDTE_CAST_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     *
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getMapName()
     */
    public String getMapName()
    {
        return "";
    }

    /**
     *
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#setValidate(java.lang.String)
     */
    public void setValidate(String validate)
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#validate()
     */
    public boolean validate()
    {
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    @Override
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    public void destroy()
    {
        vBytes = null;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getHelpers()
     */
    @Override
    public List<TransformerHelper> getHelpers()
    {
        return helpers;
    }
}
