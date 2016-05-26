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
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdte.DTEException;
import it.greenvulcano.gvesb.gvdte.config.DataSourceFactory;
import it.greenvulcano.gvesb.gvdte.transformers.DTETransformer;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Initialize the Transformers.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DTETransformerFactory
{
    private static final Logger          logger   = org.slf4j.LoggerFactory.getLogger(DTETransformerFactory.class);

    private static DTETransformerFactory instance = null;

    private DTETransformerFactory() throws DTEException
    {
        // do nothing
    }

    /**
     * Singleton entry point.
     *
     * @return an instance of <code>DTETransformerFactory</code>
     * @throws DTEException
     */
    public static DTETransformerFactory instance() throws DTEException
    {
        if (instance == null) {
            try {
                instance = new DTETransformerFactory();
            }
            catch (DTEException exc) {
                logger.error("Error while instantiating DTETransformerFactory", exc);
                throw exc;
            }
        }
        return instance;
    }

    /**
     * Instantiate and initialize the requested transformer.
     *
     * @param name
     * @param dsf
     * @return the requested <code>DTETransformer</code>
     * @throws DTEException
     */
    public synchronized DTETransformer newTransformer(String name, DataSourceFactory dsf) throws DTEException
    {
        DTETransformer transf = null;
        String className = "";

        try {
            String cfgFileName = dsf.getMainConfigurationName();

            logger.debug("Configuration File : " + cfgFileName);
            logger.info("Loading : " + name);
            Node node = XMLConfig.getNode(cfgFileName, "/GVDataTransformation/Transformations/*[@name='" + name + "' and @type='transformation']");
            className = XMLConfig.get(node, "@class");
            logger.debug("Loading Transformation : '" + name + "' of Class '" + className + "'");
            transf = (DTETransformer) Class.forName(className).newInstance();
            transf.init(node, dsf);

            return transf;
        }
        catch (XMLConfigException exc) {
            logger.error("Error while accessing configuration informations", exc);
            throw new DTEException("GVDTE_XML_CONFIG_ERROR", exc);
        }
        catch (Exception exc) {
            logger.error("Error while instantiating new transformer", exc);
            throw new DTEException("GVDTE_INSTANCE_ERROR", new String[][]{{"className", className},
                    {"cause", "Error while instantiating new transformer's Java class"}}, exc);
        }
    }

    public String[] getTransformerAlias(String name, DataSourceFactory dsf) throws DTEException
    {
        try {
            String cfgFileName = dsf.getMainConfigurationName();

            Node tNode = XMLConfig.getNode(cfgFileName, "/GVDataTransformation/Transformations/*[@name='" + name + "' and @type='transformation']");
            NodeList tList = XMLConfig.getNodeList(tNode, "TransformationAlias");

            String[] aliases = new String[tList.getLength()];
            for (int i = 0; i < aliases.length; i++) {
                aliases[i] = XMLConfig.get(tList.item(i), "@name");
            }
            return aliases;
        }
        catch (XMLConfigException exc) {
            return new String[0];
        }
    }

    public static synchronized void destroy()
    {
        logger.info("BEGIN  - cleanup");
        instance = null;
        logger.info("END - cleanup");
    }
}
