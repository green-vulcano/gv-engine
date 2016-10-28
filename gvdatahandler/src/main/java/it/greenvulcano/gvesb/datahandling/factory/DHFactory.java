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
package it.greenvulcano.gvesb.datahandling.factory;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.datahandling.DataHandlerException;
import it.greenvulcano.gvesb.datahandling.IDBOBuilder;
import it.greenvulcano.gvesb.gvdte.controller.DTEController;
import it.greenvulcano.util.thread.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * DHFactory class is a factory whose work is to return the object
 * <code>DBOBuilder</code> corresponding to the file type to process. Whenever
 * this object doesn't exist in the cache (first invocation for that file type),
 * the factory has the role to create the <code>DBOBuilder</code> and to store
 * it in its internal cache (dboBuilders).
 *
 * @version 3.0.0 Mar 31, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class DHFactory
{

    /**
     * Internal cache containing IDBO builders instances.
     */
    private Map<String, IDBOBuilder> dboBuilders        = null;

    /**
     * Private logger instance.
     */
    private static final Logger      logger             = org.slf4j.LoggerFactory.getLogger(DHFactory.class);

    public static final String       DH_CONFIG_FILENAME = "GVDataHandler-Configuration.xml";

    /**
     * Flag indicating this object has been initialized.
     */
    private boolean                  initialized        = false;

    /**
     * Configuration node for the data handler.
     */
    private Node                     configurationNode;

    /**
     * DataHandler global configuration node.
     */
    private Node                     globalConfigurationNode;

    private DTEController            dteController      = null;

    /**
     * Private constructor. Initializes the private IDBO builders cache.
     *
     */
    public DHFactory()
    {
        dboBuilders = new HashMap<String, IDBOBuilder>();
    }

    /**
     * Initializes the <code>DHFactory</code> instance.
     *
     * @param node
     *        configuration node.
     * @throws DataHandlerException
     *         if the node is null.
     */
    public void initialize(Node node) throws DataHandlerException
    {
        if (!initialized) {
            logger.debug("Initializing the Data Handler Factory.");
            try {
                Document globalConfig = XMLConfig.getDocument(DH_CONFIG_FILENAME);
                if (globalConfig != null) {
                    globalConfigurationNode = XMLConfig.getNode(globalConfig, "//GVDataHandlerConfiguration");
                }
            }
            catch (XMLConfigException exc) {
                logger.warn("Error reading DH configuration from file: " + DH_CONFIG_FILENAME, exc);
            }
            String dteConfFileName = null;
            if (node != null) {
                dteConfFileName = XMLConfig.get(node, "@dte-conf-file", "GVDataTransformation.xml");
            }
            else {
                dteConfFileName = "GVDataTransformation.xml";
            }
            logger.debug("DTE configuration file: " + dteConfFileName + ".");
            try {
                dteController = new DTEController(dteConfFileName);
            }
            catch (Exception exc) {
                logger.error("Error initializing DTEController from file: " + dteConfFileName, exc);
                throw new DataHandlerException("Error initializing DTEController from file: " + dteConfFileName, exc);
            }
            configurationNode = node;
            initialized = true;
        }
    }

    /**
     * Returns an instance of IDBOBuilder class implementation, specialized to
     * handle the file type passed as parameter.
     *
     * @param name
     *        file type the returned <code>IDBOBuilder</code> implementation
     *        class is specialized for.
     * @return an implementation of interface <code>IDBOBuilder</code>
     *         specialized in handling the requested file type.
     * @throws DataHandlerException
     *         if wrong configuration or no specialized <code>IDBOBuilder</code>
     *         implementation found for requested file type.
     */
    public IDBOBuilder getDBOBuilder(String name) throws DataHandlerException, InterruptedException
    {
        ThreadUtils.checkInterrupted("DHFactory", name, logger);
        if (!initialized) {
            throw new DataHandlerException("DHFactory instance not initialized. Call 'initialize' method before.");
        }
        if (name == null) {
            throw new DataHandlerException("Passed name parameter for IDBO Builder is null.");
        }
        IDBOBuilder builder = null;
        String dboBldClassName = null;
        if (dboBuilders.containsKey(name)) {
            builder = dboBuilders.get(name);
        }
        else {
            try {
                logger.debug("DBOBuilder " + name + " not configured, creating a new one.");
                Node localConfigNode = configurationNode;
                String xpath = "*[@type='dbobuilder' and (@name='" + name + "' or DHAliasList/DHAlias[@alias='" + name
                        + "'])]";
                Node builderNode = null;
                if (configurationNode != null) {
                    builderNode = XMLConfig.getNode(configurationNode, xpath);
                }
                if (builderNode == null && globalConfigurationNode != null) {
                    localConfigNode = globalConfigurationNode;
                    builderNode = XMLConfig.getNode(globalConfigurationNode, xpath);
                }
                if (builderNode == null) {
                    logger.error("IDBO Builder " + name + " not configured.");
                    throw new DataHandlerException("IDBO Builder [" + name + "] not configured.");
                }
                dboBldClassName = XMLConfig.get(builderNode, "@class");

                builder = (IDBOBuilder) Class.forName(dboBldClassName).newInstance();
                builder.init(builderNode);
                builder.setConfigurationNode(localConfigNode);
                builder.setDteController(dteController);
                logger.debug("DBOBuilder initialized: [" + name + "]");
                dboBuilders.put(name, builder);
            }
            catch (XMLConfigException exc) {
                logger.error("Error configuring the DBOBuilder[" + name +"]", exc);
                throw new DataHandlerException("Error configuring the DBOBuilder[" + name +"]", exc);
            }
            catch (IllegalAccessException exc) {
                logger.error("Error accessing DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
                throw new DataHandlerException("Error accessing DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
            }
            catch (InstantiationException exc) {
                logger.error("Error instantiating DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
                throw new DataHandlerException("Error instantiating DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
            }
            catch (ClassNotFoundException exc) {
                logger.error("Error creating DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
                throw new DataHandlerException("Error creating DBOBuilder[" + name +"] class '" + dboBldClassName + "'", exc);
            }
            catch (Exception exc) {
                logger.error("Unhandled exception in DBOBuilder[" + name +"] initialization", exc);
                throw new DataHandlerException("Unhandled exception in DBOBuilder[" + name +"] initialization", exc);
            }
        }
        return builder;
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized()
    {
        return this.initialized;
    }

    /**
     *
     */
    public void destroy()
    {
        for (IDBOBuilder dbo : dboBuilders.values()) {
            try {
                dbo.destroy();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        dboBuilders.clear();

        if (dteController != null) {
            dteController.destroy();
        }

        initialized = false;
    }
}