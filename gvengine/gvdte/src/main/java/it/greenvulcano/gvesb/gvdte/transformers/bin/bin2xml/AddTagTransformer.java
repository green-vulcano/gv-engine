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
package it.greenvulcano.gvesb.gvdte.transformers.bin.bin2xml;

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

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * This class handle data transformations from generic string to XML string.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AddTagTransformer implements DTETransformer
{
    private static Logger       logger           = org.slf4j.LoggerFactory.getLogger(AddTagTransformer.class);

    private static final String XML_START        = "<?xml version=\"1.0\" encoding=\"";
    private static final String XML_END          = "\" ?>";
    private static final String DEFAULT_ENCODING = "ISO-8859-1";

    private String              name;
    private String              tagName          = "";
    private String              encoding         = "";
    private boolean             useCDATA         = false;
    private List<TransformerHelper> helpers      = new ArrayList<TransformerHelper>();

    public AddTagTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param node
     * @param dsf
     * @throws DTETransfException
     *         if configuration parameters or conversion properties can't be
     *         accessed for any reason.
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        try {
            name = XMLConfig.get(node, "@name", "NO_NAME");
            tagName = XMLConfig.get(node, "@TagName");
            encoding = XMLConfig.get(node, "@Encoding", DEFAULT_ENCODING);
            useCDATA = XMLConfig.getBoolean(node, "@UseCDATA", false);
            logger.debug("Loaded parameters: tagName = " + tagName + " - encoding = " + encoding
                    + " - useCDATA = " + useCDATA);
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
     * The <code>input</code> parameter is a byte array. The return value is a byte array
     * containing the XML string.
     *
     * @param input
     *        the input data of the transformation (a byte array).
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a XML string representing the result of the data transformation.
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        try {
            byte[] inputBuffer = null;
            if (input instanceof byte[]) {
                inputBuffer = (byte[]) input;
            }
            else if (input instanceof String) {
                inputBuffer = ((String) input).getBytes();
            }
            else {
                throw new ClassCastException("Input object is not a binary buffer or String: " + input.getClass());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream(inputBuffer.length + 100);
            baos.write(XML_START.getBytes());
            baos.write(encoding.getBytes());
            baos.write(XML_END.getBytes());
            baos.write(("<" + tagName + ">").getBytes());
            if (useCDATA) {
                baos.write(("<![CDATA[").getBytes());
            }
            baos.write(inputBuffer);
            if (useCDATA) {
                baos.write(("]]>").getBytes());
            }
            baos.write(("</" + tagName + ">").getBytes());
            logger.debug("Transform stop");
            return baos.toByteArray();
        }
        catch (ClassCastException exc) {
            logger.error("Input object is not a binary buffer or String", exc);
            throw new DTETransfException("GVDTE_CAST_ERROR", exc);
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTETransfException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error."}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getMapName()
     */
    public String getMapName()
    {
        return "";
    }

    /**
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
        // do nothing
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    public void clean()
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    public void destroy()
    {
        // do nothing
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
