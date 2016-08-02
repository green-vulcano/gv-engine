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
package it.greenvulcano.gvesb.gvdte.controller;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.config.ConfigException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransfException;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import it.greenvulcano.gvesb.gvdte.util.TransformerHelper;
import it.greenvulcano.util.bin.Dump;
import it.greenvulcano.util.xml.XMLUtils;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.apache.log4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Is the DTE entry point.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DTEController implements ConfigurationListener
{
    private static final Logger   logger                     = org.slf4j.LoggerFactory.getLogger(DTEController.class);

    private DTETransformerManager transformerManager         = null;

    private String                cfgFileName = null;

    /**
     * Flag used for check if the configuration file is changed.
     */
    private boolean               configChangeFlag           = false;

    private DataSourceFactory     dataSourceFactory          = null;
    /**
     * If true print a HEX dump of input and output buffers.
     */
    private boolean               hexDump   = true;

    /**
     * Public constructor.
     *
     * @param cfgFileName
     * @throws DTEException
     */
    public DTEController(String cfgFileName) throws DTEException
    {
        logger.debug("Constructor start");
        if ((cfgFileName == null) || (cfgFileName.trim().equals(""))) {
            throw new DTEException("GVDTE_INVALID_CONFIGURATION_FILE", new String[][]{{"cause",
                    " The parameter can't be null or empty."}});
        }
        try {
            this.cfgFileName = cfgFileName;
            XMLConfig.addConfigurationListener(this, cfgFileName);
            dataSourceFactory = new DataSourceFactory();
            dataSourceFactory.setMainConfigurationName(cfgFileName);
            transformerManager = new DTETransformerManager(dataSourceFactory);
            hexDump = XMLConfig.getBoolean(cfgFileName, "/GVDataTransformation/@make-hex-dump", true);
            loadTransformations("*");
        }
        catch (DTEException exc) {
            logger.error("Error while instantiating DTEController", exc);
            throw new DTEException("GVDTE_INSTANCE_ERROR", new String[][]{
                    {"className", "it.greenvulcano.gvesb.gvdte.controller"},
                    {"cause", "Error while instantiating DTEController."}}, exc);
        }
        logger.debug("Constructor stop");
    }

    /**
     * Performs the requested data transformation using the appropriate
     * transformer.
     *
     * @param name
     * @param input
     * @param mapParam
     * @return the transformation result
     * @throws DTEException
     */
    public Object transform(String name, Object input, Map<String, Object> mapParam) throws DTEException, InterruptedException
    {
        logger.debug("Transform start");

        DTETransformer theTransformer = null;
        List<TransformerHelper> helpers = null;
        try {
            if (configChangeFlag) {
                logger.debug("Configuration is changed, the internal chache is removed");
                configurationClean();
                logger.debug("Reload all transformations");
                loadTransformations("*");
            }

            logger.debug("BEGIN - Transformation '" + name + "'");
            theTransformer = transformerManager.getTransformer(name);

            if (logger.isDebugEnabled()) {
                if (input instanceof byte[]) {
                    if (hexDump) {
                        logger.debug("INPUT:\n" + new Dump((byte[]) input, -1));
                    }
                    else {
                        logger.debug("INPUT:\n" + new String((byte[]) input));
                    }
                }
                else if (input instanceof Node) {
                    try {
                        logger.debug("INPUT:\n" + XMLUtils.serializeDOM_S((Node) input));
                    }
                    catch (Exception exc) {
                        logger.debug("INPUT:\nDUMP ERROR!!!!!");
                    }
                }
                else {
                    logger.debug("INPUT:\n" + input);
                }
            }

            helpers = theTransformer.getHelpers();
            registerHelpers(helpers, mapParam);

            Object output = theTransformer.transform(input, null, mapParam);

            if (logger.isDebugEnabled()) {
                if (output instanceof byte[]) {
                    if (hexDump) {
                        logger.debug("OUTPUT:\n" + new Dump((byte[]) output, -1));
                    }
                    else {
                        logger.debug("OUTPUT:\n" + new String((byte[]) output));
                    }
                }
                else if (output instanceof Node) {
                    try {
                        logger.debug("OUTPUT:\n" + XMLUtils.serializeDOM_S((Node) output));
                    }
                    catch (Exception exc) {
                        logger.debug("OUTPUT:\nDUMP ERROR!!!!!");
                    }
                }
                else {
                    logger.debug("OUTPUT:\n" + output);
                }
            }

            logger.debug("END - Transformation '" + name + "'");

            return output;
        }
        catch (DTETransfException exc) {
            logger.error("Error while performing transformation", exc);
            throw new DTEException("GVDTE_TRASFORMATION_ERROR", new String[][]{{"action", "performing transformation"},
                    {"map", name}}, exc);
        }
        catch (DTEException exc) {
            logger.error("Error while getting requested transformer", exc);
            throw new DTEException("GVDTE_TRASFORMATION_ERROR", new String[][]{
                    {"action", "getting requested transformation"}, {"map", name}}, exc);
        }
        catch (InterruptedException exc) {
            logger.error("Transformation interrupted", exc);
            throw exc;
        }
        catch (Throwable exc) {
            logger.error("Unexpected error", exc);
            throw new DTEException("GVDTE_GENERIC_ERROR", new String[][]{{"msg", " Unexpected error"}}, exc);
        }
        finally {
            if (theTransformer != null) {
                theTransformer.clean();
            }
            unregisterHelpers(helpers, mapParam);
            MDC.remove("MAP-NAME");
        }
    }


    /**
     * Configuration changed.
     *
     * @param event
     *        The configuration event received
     */
    public void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && (event.getFile().equals(cfgFileName))) {
            configChangeFlag = true;
        }
    }

    /**
     * Configuration clean. Remove the internal cache .
     *
     * @throws ConfigException
     */
    public void configurationClean() throws ConfigException
    {
        try {
            logger.debug("BEGIN - Reload Configuration");
            dataSourceFactory.setMainConfigurationName(cfgFileName);
            transformerManager.clear();
            configChangeFlag = false;
            logger.debug("END - Reload Configuration");
        }
        catch (DTEException exc) {
            throw new ConfigException("GVDTE_RELOAD_ERROR", new String[][]{{"msg", "" + exc}}, exc);
        }
    }

    /**
     *
     */
    public void destroy()
    {
        logger.debug("BEGIN - DTEController destroy");
        XMLConfig.removeConfigurationListener(this);
        if (transformerManager != null) {
        	transformerManager.destroy();
        }
        transformerManager = null;
        if (dataSourceFactory != null) {
        	dataSourceFactory.configurationClean();
        }
        dataSourceFactory = null;
        logger.debug("END - DTEController destroy");
    }

    /**
     * @param list
     * @throws DTEException
     */
    public void loadTransformations(String list) throws DTEException
    {
        String trasfName = "";
        try {
            logger.debug("Load transformations '" + list + "'");

            if (list.length() == 0) {
                return;
            }

            transformerManager.clear();

            if (list.equals("*")) {
                NodeList tList = XMLConfig.getNodeList(cfgFileName, "/GVDataTransformation/Startup/ToPreload/*[@Transformation]");

                for (int i = 0; i < tList.getLength(); i++) {
                    trasfName = XMLConfig.get(tList.item(i), "@Transformation");
                    transformerManager.getTransformer(trasfName);
                }
            }
            else {
                StringTokenizer strTok = new StringTokenizer(list, ";");
                while (strTok.hasMoreTokens()) {
                    transformerManager.getTransformer(strTok.nextToken());
                }
            }
        }
        catch (Exception exc) {
            logger.error("Error while preloading transformation", exc);
            throw new DTEException("GVDTE_LOAD_TRASFORMATION_ERROR", new String[][]{{"name", trasfName}}, exc);

        }

    }

    /**
     * Converts GVDTE Output to a byte array.
     *
     * @param output
     * @return the result of conversion
     * @throws DTEException
     */
    public byte[] convertOutputToByteArray(Object output) throws DTEException
    {
        byte[] result = null;

        try {
            if (output instanceof byte[]) {
                result = (byte[]) output;
            }
            else if (output instanceof String) {
                result = ((String) output).getBytes();
            }
            else if (output instanceof Document) {
                result = XMLUtils.serializeDOMToByteArray_S((Document) output, "UTF-8");
            }
            else {
                logger.debug("Error converting the returned buffer: incompatible type [" + output.getClass() + "]");
                throw new DTEException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                        "Error converting to array the output buffer: incompatible type [" + output.getClass() + "]"}});
            }
        }
        catch (DTEException exc) {
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Convertion error", exc);
            throw new DTEException("GVDTE_GENERIC_ERROR", new String[][]{{"msg",
                    "Convertion error"}}, exc);
        }
        return result;
    }

    /**
     * @param helpers
     * @throws DTEException
     */
    private void registerHelpers(List<TransformerHelper> helpers, Map<String, Object> mapParam) throws DTEException
    {
        if (helpers == null) {
            return;
        }
        for (TransformerHelper helper: helpers) {
            helper.register(mapParam);
        }
    }

    /**
     * @param helpers
     * @throws DTEException
     */
    private void unregisterHelpers(List<TransformerHelper> helpers, Map<String, Object> mapParam)
    {
        if (helpers == null) {
            return;
        }
        for (TransformerHelper helper: helpers) {
            try {
                helper.unregister(mapParam);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

}
