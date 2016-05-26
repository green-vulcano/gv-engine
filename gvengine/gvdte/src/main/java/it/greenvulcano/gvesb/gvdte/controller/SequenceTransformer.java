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
package it.greenvulcano.gvesb.gvdte.controller;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.config.ConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.thread.ThreadUtils;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handle a sequence of transformers to perform complex transformation.
 * The first transformer use the caller provided input, following transformers can use:
 * <ul>
 * <li> the client input
 * <li> preeceding transformers output
 * <ul>
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class SequenceTransformer implements DTETransformer
{
    private static final Logger   logger    = org.slf4j.LoggerFactory.getLogger(SequenceTransformer.class);

    /**
     * Conventional buffer name.
     */
    private static final String   NO_BUFFER = "no_buffer";

    private String                name;
    /**
     * List of SequenceElement.
     */
    private List<SequenceElement> transformerSequence;

    /**
     * Cache the data buffers used by the transformers.
     */
    private Map<String, Object>   buffersMap;

    /**
     * The name of the final output buffer
     */
    private String                outputBufName;

    /**
     * An instance of DTETransformerManager
     */
    private DTETransformerManager manager   = null;

    private boolean               dumpInOut = false;

    /**
     * Public constructor
     *
     * @throws DTEException
     */
    public SequenceTransformer() throws DTEException
    {
        transformerSequence = new ArrayList<SequenceElement>();
        buffersMap = new HashMap<String, Object>();
    }

    /**
     * Initialize the instance.
     *
     * @param node
     * @param dsf
     * @throws DTETransfException
     */
    public void init(Node node, DataSourceFactory dsf) throws DTETransfException
    {
        name = XMLConfig.get(node, "@name", "NO_NAME");
        transformerSequence = loadSequenceFromXPath(node);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Perform the sequence transformation.
     *
     * @param input
     * @param buffer
     * @param mapParam
     * @return the transformation's result
     * @throws DTETransfException
     * @throws InterruptedException 
     */
    public Object transform(Object input, Object buffer, Map<String, Object> mapParam) throws DTETransfException, 
             InterruptedException
    {
        logger.debug("Transform start");

        SequenceElement first = transformerSequence.get(0);
        buffersMap.put(first.getInputName(), input);
        buffersMap.put(first.getBufferName(), buffer);

        try {
            int i = 0;
            for (SequenceElement currElem : transformerSequence) {
                ThreadUtils.checkInterrupted("SequenceTransformer", getName(), logger);
                ++i;
                logger.debug("Performing sequence step n. " + i);
                logger.debug("Reading infos for sequence step n. " + i);
                String inputName = currElem.getInputName();
                String outputName = currElem.getOutputName();
                String bufferName = currElem.getBufferName();
                String transformerName = currElem.getTransformerName();

                logger.debug("InputName  = '" + inputName + "'");
                logger.debug("OutName = '" + outputName + "'");
                logger.debug("BufferName = '" + bufferName + "'");
                logger.debug("TransformerName = '" + transformerName + "'");

                logger.debug("Buffers for sequence step n. " + i);
                Object currInput = null;
                Object currBuffer = null;
                if (!inputName.equals(NO_BUFFER)) {
                    currInput = buffersMap.get(inputName);
                }
                else {
                    logger.error("Input buffer can't be set to null");
                    throw new DTETransfException("GVDTE_SEQUENCE_TRASFORMER_ERROR", new String[][]{{"cause",
                            "Input buffer can't be set to null"}});
                }
                currBuffer = buffersMap.get(bufferName);

                logger.debug("Getting transformer for sequence step n. " + i);
                DTETransformer transformer = manager.getTransformer(transformerName);

                logger.debug("Doing transformation for sequence step n. " + i);
                if (dumpInOut) {
                    if (currInput instanceof byte[]) {
                        logger.debug("Input[" + i + "]: [\n" + new Dump((byte[]) currInput, -1) + "\n].");
                    }
                    else if (currInput instanceof Node) {
                        try {
                            logger.debug("Input[" + i + "]: [\n" + XMLUtils.serializeDOM_S((Node) currInput) + "\n].");
                        }
                        catch (Exception exc) {
                            logger.debug("Input[" + i + "]: [\nDUMP ERROR!!!!!\n].");
                        }
                    }
                    else {
                        logger.debug("Input[" + i + "]: [\n" + currInput + "\n].");
                    }
                }
                Object currOutput = transformer.transform(currInput, currBuffer, mapParam);

                if (!outputName.equals(NO_BUFFER)) {
                    buffersMap.put(outputName, currOutput);
                }
                else {
                    logger.error("Output buffer can't be set to null");
                    throw new DTETransfException("GVDTE_SEQUENCE_TRASFORMER_ERROR", new String[][]{{"cause",
                            "Output buffer can't be set to null"}});
                }
                if (dumpInOut) {
                    if (currOutput instanceof byte[]) {
                        logger.debug("Output[" + i + "]: [\n" + new Dump((byte[]) currOutput, -1) + "\n].");
                    }
                    else if (currOutput instanceof Node) {
                        try {
                            logger.debug("Output[" + i + "]: [\n" + XMLUtils.serializeDOM_S((Node) currOutput) + "\n].");
                        }
                        catch (Exception exc) {
                            logger.debug("Output[" + i + "]: [\nDUMP ERROR!!!!!\n].");
                        }
                    }
                    else {
                        logger.debug("Output[" + i + "]: [\n" + currOutput + "\n].");
                    }
                }
            }

            Object finalOutput = buffersMap.get(outputBufName);
            logger.debug("Transform stop");
            return finalOutput;
        }
        catch (InterruptedException exc) {
            throw exc;
        }
        catch (DTETransfException exc) {
            throw exc;
        }
        catch (DTEException exc) {
            logger.error("Sequence Transformation Error", exc);
            throw new DTETransfException("GVDTE_SEQUENCE_TRASFORMER_ERROR", new String[][]{{"cause",
                    "Sequence Transformation Error"}}, exc);
        }
    }

    /**
     * Initialize the transformation sequence.
     */
    private List<SequenceElement> loadSequenceFromXPath(Node node) throws DTETransfException
    {
        logger.debug("Load Sequence start");
        List<SequenceElement> theList = new ArrayList<SequenceElement>();
        try {
            outputBufName = XMLConfig.get(node, "@Output");
            dumpInOut = XMLConfig.getBoolean(node, "@DumpInOut", false);
            logger.debug("Loaded outputBufName : " + outputBufName);
            logger.debug("Loading SequenceElement");
            NodeList eList = XMLConfig.getNodeList(node, "SequenceElement");
            if (eList.getLength() == 0) {
                throw new ConfigException("Section : SequenceElement not found");
            }
            for (int i = 0; i < eList.getLength(); i++) {
                Node se = eList.item(i);
                logger.debug("Loaded sequence element configuration at: " + se);
                String trasfName = XMLConfig.get(se, "@Transformer");
                String inputName = XMLConfig.get(se, "@Input");
                String outputName = XMLConfig.get(se, "@Output");
                String bufferName = XMLConfig.get(se, "@Buffer", NO_BUFFER);

                SequenceElement currElem = new SequenceElement(trasfName, inputName, bufferName, outputName);
                theList.add(currElem);
            }
            logger.debug("Load Sequence stop");
            return theList;
        }
        catch (Exception exc) {
            logger.error("Error initializing Sequence transformation", exc);
            throw new DTETransfException("GVDTE_SEQUENCE_TRASFORMER_ERROR", new String[][]{{"cause",
                    "Error initializing Sequence transformation"}}, exc);

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
     * @param trfManager
     * @throws DTEException
     * @throws InterruptedException 
     */
    public void setTransformerManager(DTETransformerManager trfManager) throws DTEException, InterruptedException
    {
        manager = trfManager;
        reload();
    }

    /**
     * @throws DTEException
     * @throws InterruptedException 
     */
    public void reload() throws DTEException, InterruptedException
    {
        for (int i = 0; i < transformerSequence.size(); i++) {
            logger.debug("Reloading sequence step n. " + (i + 1));
            SequenceElement currElem = transformerSequence.get(i);
            manager.getTransformer(currElem.getTransformerName());
        }
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
        return false;
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#clean()
     */
    public void clean()
    {
        buffersMap.clear();
        for (int i = 0; i < transformerSequence.size(); i++) {
            SequenceElement currElem = transformerSequence.get(i);
            try {
                manager.getTransformer(currElem.getTransformerName()).clean();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#destroy()
     */
    public void destroy()
    {
        manager = null;
        transformerSequence.clear();
        buffersMap.clear();
    }

    /**
     * @see it.greenvulcano.gvesb.gvdte.transformers.DTETransformer#getHelpers()
     */
    @Override
    public List<TransformerHelper> getHelpers()
    {
        List<TransformerHelper> helpers = new ArrayList<TransformerHelper>();
        for (int i = 0; i < transformerSequence.size(); i++) {
            SequenceElement currElem = transformerSequence.get(i);
            try {
                helpers.addAll(manager.getTransformer(currElem.getTransformerName()).getHelpers());
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        return helpers;
    }
}
