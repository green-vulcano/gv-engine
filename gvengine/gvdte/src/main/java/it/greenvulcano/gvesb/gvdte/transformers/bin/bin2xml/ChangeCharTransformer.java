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
package it.greenvulcano.gvesb.gvdte.transformers.bin.bin2xml;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class handle characters substitution.
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ChangeCharTransformer implements DTETransformer
{
    private static Logger logger      = org.slf4j.LoggerFactory.getLogger(ChangeCharTransformer.class);

    private String        name;
    private Vector<Byte>  vInputChar  = null;
    private Vector<Byte>  vOutputChar = null;
    private List<TransformerHelper> helpers = new ArrayList<TransformerHelper>();

    public ChangeCharTransformer()
    {
        // do nothing
    }

    /**
     * Initialize the instance.
     *
     * @param node
     * @param dsf
     *
     * @throws DTETransfException
     *         if configuration parameters or conversion properties can't be
     *         accessed for any reason.
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        logger.debug("Init start");
        try {
            name = XMLConfig.get(node, "@name", "NO_NAME");
            vInputChar = new Vector<Byte>();
            vOutputChar = new Vector<Byte>();

            addCharConversion(node);

            NodeList tList = XMLConfig.getNodeList(node, "CharConversion");

            for (int i = 0; i < tList.getLength(); i++) {
                addCharConversion(tList.item(i));
            }
            logger.debug("Init stop");
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration", exc);
            throw new DTETransfException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (DTETransfException exc) {
            throw exc;
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

    private void addCharConversion(Node node) throws DTETransfException
    {
        String inputCharHex;
        String outputCharHex;
        byte inputChar;
        byte outputChar;

        try {
            inputCharHex = XMLConfig.get(node, "@InputChar");
            outputCharHex = XMLConfig.get(node, "@OutputChar");
            logger.debug("Loaded parameters: InputChar[" + inputCharHex + "] OutputChar[" + outputCharHex + "]");

            if (inputCharHex.length() != 2) {
                logger.error("Bad HEX Input character (not two digits): [" + inputCharHex + "]");
                throw new IOException("Bad HEX Input character (not two digits): [" + inputCharHex + "]");
            }

            if (outputCharHex.length() != 2) {
                logger.error("init - Bad HEX Output character (not two digits): [" + outputCharHex + "]");
                throw new IOException("Bad HEX Output character (not two digits): [" + outputCharHex + "]");
            }

            int digitValue = Integer.parseInt(inputCharHex, 16);
            inputChar = (byte) digitValue;

            digitValue = Integer.parseInt(outputCharHex, 16);
            outputChar = (byte) digitValue;

            vInputChar.add(new Byte(inputChar));
            vOutputChar.add(new Byte(outputChar));

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

    /**
     * The <code>input</code> parameter is a byte array. The <code>buffer</code> parameter,
     * if it's not <code>null</code>, is a byte array. The return value is an array of
     * bytes representing the result of the transformation.
     *
     * @param input
     *        the input data to the transformation (a byte array).
     * @param buffer
     *        the intermediate result of the transformation (if needed).
     * @param mapParam
     * @return a byte array with the transformed buffer content
     * @throws DTETransfException
     *         if any transformation error occurs.
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
            InterruptedException {
        logger.debug("Transform start");
        byte inputChar;
        byte outputChar;

        try {
            byte[] inputBuffer = (byte[]) input;
            byte[] intermediateBuffer = (byte[]) buffer;

            if (inputBuffer != null) {
                byte[] outputBuffer = new byte[inputBuffer.length];
                for (int i = 0; i < inputBuffer.length; i++) {
                    int c = 0;
                    while (c < vInputChar.size()) {
                        inputChar = (vInputChar.get(c)).byteValue();
                        outputChar = (vOutputChar.get(c)).byteValue();
                        if (inputBuffer[i] == inputChar) {
                            outputBuffer[i] = outputChar;
                            break;
                        }
                        c++;
                    }
                    if (c == vInputChar.size()) {
                        outputBuffer[i] = inputBuffer[i];
                    }
                }
                logger.debug("Transform stop");
                return outputBuffer;
            }
            if (intermediateBuffer != null) {
                for (int i = 0; i < intermediateBuffer.length; i++) {
                    int c = 0;
                    while (c < vInputChar.size()) {
                        inputChar = (vInputChar.get(c)).byteValue();
                        outputChar = (vOutputChar.get(c)).byteValue();
                        if (intermediateBuffer[i] == inputChar) {
                            intermediateBuffer[i] = outputChar;
                            break;
                        }
                        c++;
                    }
                }
                logger.debug("Transform stop");
                return intermediateBuffer;
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
        vInputChar.clear();
        vOutputChar.clear();
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
