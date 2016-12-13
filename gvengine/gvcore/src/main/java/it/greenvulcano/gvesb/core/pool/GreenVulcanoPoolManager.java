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
package it.greenvulcano.gvesb.core.pool;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.jmx.GreenVulcanoPoolInfo;
import it.greenvulcano.jmx.JMXEntryPoint;
import it.greenvulcano.script.util.BaseContextManager;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 * 
 */
public final class GreenVulcanoPoolManager implements ConfigurationListener
{
    private static final Logger            logger            = org.slf4j.LoggerFactory.getLogger(GreenVulcanoPoolManager.class);
    public static final String             CONF_FILE_NAME    = "GVPoolManager.xml";

    /**
     * The subsystem -> GreenVulcanoPool map.
     */
    private Map<String, GreenVulcanoPool>  greenVulcanoPools = new HashMap<String, GreenVulcanoPool>();
    /**
     * If true the configuration is valid.
     */
    private boolean                        initialized       = false;
    /**
     * The singleton instance.
     */
    private static GreenVulcanoPoolManager _instance         = null;

    /**
     * The singleton entry point.
     * 
     * @return the singleton instance
     */
    public static synchronized GreenVulcanoPoolManager instance()
    {
        if (_instance == null) {
            _instance = new GreenVulcanoPoolManager();
            XMLConfig.addConfigurationListener(_instance, CONF_FILE_NAME);
            XMLConfig.addConfigurationListener(_instance, BaseContextManager.CFG_FILE);
        }
        return _instance;
    }

    /**
     * Constructor.
     */
    private GreenVulcanoPoolManager()
    {
        // do nothing
    }

    /**
     * Configuration changed. When the configuration changes, the internal
     * chache is removed.
     * 
     * @param event
     *        The configuration event received
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) { 
            if (event.getFile().equals(CONF_FILE_NAME)) {
                initialized = false;
            }
            else if (event.getFile().equals(BaseContextManager.CFG_FILE)) {
                initialized = false;
                try {
                    initGreenVulcanoPool();
                }
                catch (Exception exc) {
                    logger.error("Error reinizializing GreenVulcanoPool configuration", exc);
                }
            }
        }
    }

    /**
     * Obtain a GreenVulcanoPool instance.
     * 
     * @param subsystem
     * 
     * @return the required instance
     * @throws Exception
     *         if error occurs
     */
    public Optional<GreenVulcanoPool> getGreenVulcanoPool(String subsystem)
    {
    	try {
    		initGreenVulcanoPool();
    	} catch (Exception e) {
			logger.error("Failed to initialize GreenVulcanoPool", e);
		}

        logger.debug("Requested GreenVulcanoPool(" + subsystem + ")");
        return Optional.ofNullable(greenVulcanoPools.get(subsystem));
    }
    
    public static GreenVulcanoPool getDefaultGreenVulcanoPool(){
    	
    	return instance().getGreenVulcanoPool(GreenVulcanoPool.DEFAULT_SUBSYSTEM).get();
    }

    /**
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        XMLConfig.removeConfigurationListener(this);
        destroyGreenVulcanoPools();
    }

    /**
     * Initialize the pool.
     * 
     * @throws Exception
     *         if error occurs
     */
    private void initGreenVulcanoPool() throws Exception
    {
        if (initialized) {
            return;
        }

        synchronized (_instance) {
            if (initialized) {
                return;
            }

            logger.debug("Initializing GreenVulcanoPoolManager");
            // no already initialized pools...
            if (greenVulcanoPools.isEmpty()) {
            	GreenVulcanoPool defaultPool = new GreenVulcanoPool(GreenVulcanoPool.DEFAULT_INITIAL_SIZE, GreenVulcanoPool.DEFAULT_MAXIMUM_SIZE, GreenVulcanoPool.DEFAULT_MAXIMUM_CREATION, GreenVulcanoPool.DEFAULT_SUBSYSTEM);
            	greenVulcanoPools.put(GreenVulcanoPool.DEFAULT_SUBSYSTEM, defaultPool);
            	
            	if (XMLConfig.exists(CONF_FILE_NAME, "//GreenVulcanoPool")) {
            		try {
            		
	            		NodeList nl = XMLConfig.getNodeList(CONF_FILE_NAME, "//GreenVulcanoPool");
		                // initialize all configured pools
		                          
		                for (int i = 0; i < nl.getLength(); i++) {
		                    GreenVulcanoPool pool = new GreenVulcanoPool(nl.item(i));
		                    logger.debug("Initialized GreenVulcanoPool(" + pool.getSubsystem() + ")");
		                    register(pool);
		                    greenVulcanoPools.put(pool.getSubsystem(), pool);
		                }
            		} catch (Exception e) {
            			logger.error("Failed to parse GreenVulcanoPool configurations", e);
					}  
            	} else {
            		logger.debug("No GreenVulcanoPool configurations found ");
            	}
                initialized = true;
                return;
            }

            // some pools already initialized...
            Map<String, GreenVulcanoPool> tmp = new HashMap<String, GreenVulcanoPool>();
            NodeList nl = XMLConfig.getNodeList(CONF_FILE_NAME, "//GreenVulcanoPool");
            for (int p = 0; p < nl.getLength(); p++) {
                GreenVulcanoPool pool = greenVulcanoPools.remove(XMLConfig.get(nl.item(p), "@subsystem"));
                if (pool == null) {
                    // add new configured pools...
                    pool = new GreenVulcanoPool(nl.item(p));
                }
                else {
                    // reinitialize already configured pools...
                    pool.init(nl.item(p));
                }
                logger.debug("Initialized GreenVulcanoPool(" + pool.getSubsystem() + ")");
                register(pool);
                // save already configured pools removed from configuration...
                tmp.put(pool.getSubsystem(), pool);
            }

            // destroy pools removed from configuration...
            for (GreenVulcanoPool pool : greenVulcanoPools.values()) {
                logger.debug("Destroying GreenVulcanoPool(" + pool.getSubsystem() + ")");
                deregister(pool, true);
                try {
                    pool.destroy(true);
                }
                catch (Exception exc) {
                    // do nothing
                }
            }

            greenVulcanoPools.putAll(tmp);

            initialized = true;
        }
    }

    /**
     * Destroy the pools.
     * 
     */
    private void destroyGreenVulcanoPools()
    {
        for (GreenVulcanoPool pool : greenVulcanoPools.values()) {
            try {
                logger.debug("Destroying GreenVulcanoPool(" + pool.getSubsystem() + ")");
                deregister(pool, true);
                pool.destroy(true);
            }
            catch (Exception exc) {
                // do nothing
            }
        }
        greenVulcanoPools.clear();
    }

    /**
     * Register the pool as MBean.
     * 
     * @param pool
     *        the instance to register.
     */
    private void register(GreenVulcanoPool pool)
    {
        logger.debug("Registering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")");
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            deregister(pool, false);
            JMXEntryPoint jmx = JMXEntryPoint.getInstance();
            jmx.registerObject(new GreenVulcanoPoolInfo(pool), GreenVulcanoPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            logger.warn("Error registering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")", exc);
        }
    }

    /**
     * Deregister the pool as MBean.
     * 
     * @param pool
     *        the instance to deregister.
     */
    private void deregister(GreenVulcanoPool pool, boolean showError)
    {
        logger.debug("Deregistering MBean for GreenVulcanoPool(" + pool.getSubsystem() + ")");
        Hashtable<String, String> properties = getMBeanProperties(pool);
        try {
            JMXEntryPoint jmx = JMXEntryPoint.getInstance();
            jmx.unregisterObject(new GreenVulcanoPoolInfo(pool), GreenVulcanoPoolInfo.DESCRIPTOR_NAME, properties);
        }
        catch (Exception exc) {
            if (showError) {
                logger.warn("Cannot de-register GreenVulcano ESB Pool Manager.", exc);
            }
        }
    }

    private Hashtable<String, String> getMBeanProperties(GreenVulcanoPool pool)
    {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("Subsystem", pool.getSubsystem());
        return properties;
    }
}