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
package it.greenvulcano.gvesb.core.config;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.exc.GVCoreException;
import it.greenvulcano.gvesb.statistics.StatisticsDataManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;

/**
 * <code>ServiceConfigManager</code> is the class for the configuration
 * objects. When the core are called, GreenVulcano reads the
 * configuration for each workflow (defined by GVSERVICE + OPERATION)
 * from the configuration file. For each workflow a configuration object is
 * created and initialized. This class also caches the objects already created
 * to provide a faster access to the fields that control the internal
 * GreenVulcano algorithms.
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 */

public class ServiceConfigManager
{
    private static Logger              logger                = org.slf4j.LoggerFactory.getLogger(ServiceConfigManager.class);

    /**
     * The cache of previously used services.
     */
    private Map<String, GVServiceConf> gvServiceConfigMap    = new HashMap<String, GVServiceConf>();
    /**
     * The cache of aliased services.
     */
    private Map<String, GVServiceConf> gvServiceAliasMap     = new HashMap<String, GVServiceConf>();
    /**
     * The Statistics StatisticsDataManager to be used
     */
    private StatisticsDataManager      statisticsDataManager = null;

    /**
     * The default constructor.
     */
    public ServiceConfigManager()
    {
        logger.debug("Service Config Manager created");
    }

    /**
     * Obtain a cached GVServiceConf. If the object is not available then create
     * it.
     *
     * @param gvBuffer
     *        The GreenVulcano data coming from the client
     * @return The GVServiceConf containing the configuration for the given
     *         system::service
     * @throws GVCoreException
     *         if an error occurs creating or retrieving a configuration object
     */
    public GVServiceConf getGVSConfig(GVBuffer gvBuffer) throws GVCoreException
    {
        GVServiceConf gvServiceConfig = null;
        logger.debug("getGVSConfig: Start (" + gvBuffer.getService() + ")");

        String key = generateKey(gvBuffer);
        gvServiceConfig = gvServiceConfigMap.get(key);

        if (gvServiceConfig == null) {
            gvServiceConfig = gvServiceAliasMap.get(key);
        }
        if (gvServiceConfig == null) {
            gvServiceConfig = new GVServiceConf();
            gvServiceConfig.setStatisticsDataManager(statisticsDataManager);
            gvServiceConfig.init(gvBuffer);
            cache(gvServiceConfig);
        }
        logger.debug("getGVSConfig: End (" + gvBuffer.getService() + ")");
        return gvServiceConfig;
    }

    /**
     * Stores the object into cache for future uses.
     *
     * @param gvServiceConfig
     *        The configuration object to save in the cache
     */
    private void cache(GVServiceConf gvServiceConfig)
    {
        gvServiceConfigMap.put(gvServiceConfig.getKey(), gvServiceConfig);
        Vector<String> aliasList = gvServiceConfig.getAliasList();
        for (String alias : aliasList) {
            gvServiceAliasMap.put(alias, gvServiceConfig);
        }
    }

    /**
     * Generate the key to retrieve/store the object from/to the cache.
     *
     * @param gvBuffer
     *        The GVBuffer used to generate the key to store and retrieve the
     *        configuration objects
     * @return The key to use to create/retrieve the objects containing the
     *         configuration from the cache
     */
    private String generateKey(GVBuffer gvBuffer)
    {
        return gvBuffer.getService();
    }

    /**
     * @return The Statistics StatisticsDataManager to be used
     */
    public StatisticsDataManager getStatisticsDataManager()
    {
        return statisticsDataManager;
    }

    /**
     * @param manager
     *        Set the Statistics StatisticsDataManager to be used
     */
    public void setStatisticsDataManager(StatisticsDataManager manager)
    {
        statisticsDataManager = manager;
    }

    /**
     * Destroy the internal objects
     */
    public void destroy()
    {
        logger.debug("GVSConfig destroy: Start");
        for (GVServiceConf gvServiceConfig : gvServiceConfigMap.values()) {
            gvServiceConfig.destroy();
        }
        gvServiceConfigMap.clear();
        gvServiceAliasMap.clear();
        logger.debug("GVSConfig destroy: End");
    }
}