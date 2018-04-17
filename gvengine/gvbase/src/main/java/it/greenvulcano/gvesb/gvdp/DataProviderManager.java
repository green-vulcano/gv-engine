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
package it.greenvulcano.gvesb.gvdp;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.gvdp.impl.ArrayDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.CollectionDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.GVBufferChangeDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.InputStreamDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.MapDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.ObjectDataProvider;
import it.greenvulcano.gvesb.gvdp.impl.StringDataProvider;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.w3c.dom.Node;

/**
 * DataProviderManager is a class that initialize and manages
 * {@link IDataProvider} objects.
 * 
 * @version 3.0.0 Mar 2, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class DataProviderManager implements ConfigurationListener
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(DataProviderManager.class);

    public static final String                DP_CONFIGURATION_ERROR = "DP_CONFIGURATION_ERROR";
    public static final String                DP_CREATION_ERROR      = "DP_CREATION_ERROR";

    /**
     * The configuration file name of this DataProviderManager.
     */
    private static final String               CONFIG_FILE            = "GVDataProviderManager.xml";

    private static DataProviderManager        instance;

    /**
     * Keeps status of configuration reload.
     */
    private boolean                           reloadConfiguration    = false;

    /**
     * Cache for the Data Provider objects.
     */
    private Map<String, Stack<IDataProvider>> iDataProviders         = null;

    private Vector<IDataProvider>             currentlyInUseDP       = null;

    private Node                              dataProvidersConfigNode;
    
    private static final ConcurrentMap<String, Supplier<IDataProvider>> suppliers = new ConcurrentHashMap<>();
    
    public static void registerSupplier(String name, Supplier<IDataProvider> supplier) {
    	suppliers.put(name, supplier);
    }
    
    public static void unregisterSupplier(String name) {
    	suppliers.remove(name);
    }
    
    static {
    	registerSupplier("ArrayDataProvider",ArrayDataProvider::new);
    	registerSupplier("CollectionDataProvider",CollectionDataProvider::new);
    	registerSupplier("GVBufferChangeDataProvider",GVBufferChangeDataProvider::new);
    	registerSupplier("GVBufferChangeDataProvider",InputStreamDataProvider::new);
    	registerSupplier("MapDataProvider",MapDataProvider::new);
    	registerSupplier("ObjectDataProvider",ObjectDataProvider::new);
    	registerSupplier("StringDataProvider",StringDataProvider::new);
    }
    
    /**
     * @return the singleton instance of DataProviderManager
     * @throws DataProviderException
     */
    public static synchronized DataProviderManager instance() throws DataProviderException
    {
        if (instance == null) {
            instance = new DataProviderManager();
        }
        return instance;
    }

    private DataProviderManager() throws DataProviderException
    {
        logger.debug("BEGIN DataProviderManager()");
        XMLConfig.addConfigurationListener(this, CONFIG_FILE);

        try {
            init();
        }
        catch (XMLConfigException exc) {
            logger.error("Configuration error: '" + exc.getMessage() + "'. To verify the configuration.");
            throw new DataProviderException(DP_CONFIGURATION_ERROR, new String[][]{{"className", getClass().getName()},
                    {"method", "DataProviderManager()"}, {"cause", exc.getMessage()}}, exc);
        }

        logger.debug("END DataProviderManager()");
    }

    /**
     * The initialization method.
     * 
     * @throws XMLConfigException
     *         if an configuration error occurred.
     */
    private void init() throws XMLConfigException
    {
        logger.debug("BEGIN init()");
        iDataProviders = new ConcurrentHashMap<String, Stack<IDataProvider>>();
        currentlyInUseDP = new Vector<IDataProvider>();

        dataProvidersConfigNode = XMLConfig.getNode(CONFIG_FILE, "/GVDataProviderManager/DataProviders");

        logger.debug("END init()");
    }

    /**
     * Remember to call <i>releaseDataProvider</i> after using the
     * <code>DataProvider</code>.
     * 
     * @param dpName
     *        name of the data provider
     * @return the {@link IDataProvider}
     * @throws DataProviderException
     *         If the error occurred.
     */
    public IDataProvider getDataProvider(String dpName) throws DataProviderException
    {
        logger.debug("BEGIN getDataProvider(String dpName)");

        if (reloadConfiguration) {
            configurationReload();
        }

        if ((dpName == null) || dpName.equals("")) {
            logger.error("The data provider name is not valid. Verify the configuration.");
            throw new DataProviderException(DP_CONFIGURATION_ERROR, new String[][]{{"className", getClass().getName()},
                    {"method", "getDataProvider(String dpName)"},
                    {"cause", "The key is not valid. Verify the configuration."}});
        }

        logger.debug("The data provider name is '" + dpName + "'.");

        IDataProvider iDataProvider = null;
        Stack<IDataProvider> iDataProviderStack = iDataProviders.get(dpName);

        if (iDataProviderStack == null) {
            iDataProviderStack = new Stack<IDataProvider>();
            iDataProviders.put(dpName, iDataProviderStack);
        }
        else if (!iDataProviderStack.isEmpty()) {
            iDataProvider = iDataProviderStack.pop();
        }

        if (iDataProvider == null) {
            logger.debug("Data provider with key '" + dpName + "' not found in the cache. Create it.");

            Node dpConfigNode = null;
            String dataProviderXPath = "*[@name='" + dpName + "' and @type='dataProvider']";
            try {
                dpConfigNode = XMLConfig.getNode(dataProvidersConfigNode, dataProviderXPath);
            }
            catch (Exception exc) {
                throw new DataProviderException(DP_CONFIGURATION_ERROR, new String[][]{
                        {"className", getClass().getName()}, {"method", "getDataProvider(String dpName)"},
                        {"cause", "The XPATH: " + dataProviderXPath}}, exc);
            }
           
            try {
                           
                iDataProvider = Optional.ofNullable(suppliers.get(dpConfigNode.getNodeName()))
                						.orElseThrow(NoSuchElementException::new)
                						.get();
                		
                iDataProvider.init(dpConfigNode);
                logger.debug("New data provider created: " + dpConfigNode.getNodeName());
            } catch (NoSuchElementException exc) {
            	logger.error("Missing supplier " + dpConfigNode.getNodeName());
                throw new DataProviderException(DP_CONFIGURATION_ERROR, 
                		  new String[][]{
                    {"className", getClass().getName()},
                    {"method", "getDataProvider(String dpName)"},
                    {"dpname", dpName},
                    {"cause", "Supplier not found for "+ dpConfigNode.getNodeName()}}, exc);            
                        
            } catch (Exception exc) {
                logger.error("Creation error: '" + exc.getMessage() + "' - DP class in '" 
                		+ dpConfigNode.getNodeName() + "'");
                throw new DataProviderException(DP_CREATION_ERROR,
                        new String[][]{
                                {"className", getClass().getName()},
                                {"method", "getDataProvider(String dpName)"},
                                {"dpname", dpName},
                                {"cause", "Creation error: '" + exc.getMessage() + "'"}}, exc);
            }
        }
        else {
            logger.debug("Data provider with name <" + dpName + "> found in the cache. Returning it.");
        }
        logger.debug("END getDataProvider(String dpName)");
        currentlyInUseDP.add(iDataProvider);
        return iDataProvider;
    }

    /**
     * @param dpName
     * @param dataProvider
     */
    public void releaseDataProvider(String dpName, IDataProvider dataProvider)
    {
        if (dataProvider == null) {
            return;
        }
        if (currentlyInUseDP.contains(dataProvider)) {
            currentlyInUseDP.remove(dataProvider);
        }
        Stack<IDataProvider> dpStack = iDataProviders.get(dpName);
        if (dpStack != null) {
            dpStack.push(dataProvider);
        }
    }

    private void configurationReload() throws DataProviderException
    {
        try {
            init();
            reloadConfiguration = false;
        }
        catch (XMLConfigException exc) {
            logger.error("Configuration error: '" + exc.getMessage() + "'. To verify the configuration.");
            throw new DataProviderException(DP_CONFIGURATION_ERROR, new String[][]{{"className", getClass().getName()},
                    {"method", "configurationReload()"}, {"cause", exc.getMessage()}}, exc);
        }
    }

    /**
     * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(it.greenvulcano.configuration.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt)
    {
        if ((evt.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && (evt.getFile().equals(CONFIG_FILE))) {
            reloadConfiguration = true;
        }
    }

}
