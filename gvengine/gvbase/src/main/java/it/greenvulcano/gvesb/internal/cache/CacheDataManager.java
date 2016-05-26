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
package it.greenvulcano.gvesb.internal.cache;

import it.greenvulcano.configuration.XMLConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public final class CacheDataManager
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(CacheDataManager.class);

    private static CacheDataManager              instance      = null;
    private static String                        CFG_FILE_NAME = "GVCacheDataManager.xml";

    private static HashMap<String, CacheHandler> cacheHandlers = null;

    private CacheDataManager() throws CacheException
    {
        cacheHandlers = new HashMap<String, CacheHandler>();
        try {
            NodeList chNl = XMLConfig.getNodeList(CFG_FILE_NAME,
                    "/CacheDataManager/CacheHandlers/*[@type='cache-handler']");

            for (int i = 0; i < chNl.getLength(); ++i) {
                Node chN = chNl.item(i);
                String subSystem = XMLConfig.get(chN, "@sub-system");
                String clazz = XMLConfig.get(chN, "@class");
                CacheHandler cacheHandler = (CacheHandler) Class.forName(clazz).newInstance();
                cacheHandler.init(chN);

                cacheHandlers.put(subSystem, cacheHandler);
                logger.debug("Initialized CacheHandler for SubSyste[" + subSystem + "]");
            }
        }
        catch (CacheException exc) {
            logger.error("Error initializing CacheDataManager", exc);
            cacheHandlers.clear();
            throw exc;
        }
        catch (Exception exc) {
            logger.error("Error initializing CacheDataManager", exc);
            cacheHandlers.clear();
            throw new CacheException("Error initializing CacheDataManager", exc);
        }
    }

    /**
     * @return the singleton instance
     * @throws CacheException
     */
    public static synchronized CacheDataManager getInstance() throws CacheException
    {
        if (instance == null) {
            instance = new CacheDataManager();
        }
        return instance;
    }

    /**
     * @param subSystem
     * @param descr
     * @return an identifier that references the object in the cache
     * @throws CacheException
     */
    public String put(String subSystem, CacheDescriptor descr) throws CacheException
    {
        String key = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                key = cache.put(descr);
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return key;
    }

    /**
     * @param subSystem
     * @param descr
     * @param forceKey
     * @return an identifier that references the object in the cache
     * @throws CacheException
     */
    public String put(String subSystem, CacheDescriptor descr, String forceKey) throws CacheException
    {
        String key = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                key = cache.put(descr, forceKey);
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return key;
    }

    /**
     * @param subSystem
     * @param key
     * @return the object returned from the cache corresponding to the key
     * @throws CacheException
     */
    public CacheDescriptor get(String subSystem, String key) throws CacheException
    {
        CacheDescriptor descr = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                descr = cache.get(key);
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return descr;
    }

    /**
     * @param subSystem
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    public Map<String, Object> getOptions(String subSystem, String key) throws CacheException
    {
        Map<String, Object> options = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                options = cache.getOptions(key);
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return options;
    }

    /**
     * @param subSystem
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    public Map<String, Object> remove(String subSystem, String key) throws CacheException
    {
        Map<String, Object> options = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                options = cache.remove(key);
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return options;
    }

    /**
     * @param subSystem
     * @return an iterator to keys stored in the cache
     * @throws CacheException
     */
    public Iterator<String> getIterator(String subSystem) throws CacheException
    {
        Iterator<String> iter = null;

        CacheHandler cache = cacheHandlers.get(subSystem);
        if (cache != null) {
            synchronized (cache) {
                iter = cache.getIterator();
            }
        }
        else {
            logger.error("Cache SubSystem[" + subSystem + "] not configured!");
            throw new CacheException("Cache SubSystem[" + subSystem + "] not configured!");
        }

        return iter;
    }
}
