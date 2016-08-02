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
package it.greenvulcano.gvesb.internal.cache.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.internal.cache.CacheDescriptor;
import it.greenvulcano.gvesb.internal.cache.CacheException;
import it.greenvulcano.gvesb.internal.cache.CacheHandler;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public abstract class BaseCacheHandler implements CacheHandler
{
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(BaseCacheHandler.class);

    private String                subSystem;

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#init(org.w3c.dom.Node)
     */
    public void init(Node node) throws CacheException
    {
        try {
            subSystem = XMLConfig.get(node, "@sub-system");
        }
        catch (Exception exc) {
            logger.error("Error initializing BaseCacheHandler", exc);
            throw new CacheException("Error initializing BaseCacheHandler", exc);
        }
        initInt(node);
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#put(CacheDescriptor)
     */
    public String put(CacheDescriptor descr) throws CacheException
    {
        checkCache();
        try {
            logger.debug("CacheHandler[" + subSystem + "]: Inserting CacheDescriptor: " + descr);
            String key = putInt(descr);

            logger.debug("CacheHandler[" + subSystem + "]: Inserted CacheDescriptor for key[" + key + "]");
            return key;
        }
        catch (CacheException exc) {
            logger.error("CacheHandler[" + subSystem + "]: Error inserting CacheDescriptor: " + descr, exc);
            throw exc;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#put(CacheDescriptor,
     *      String)
     */
    public String put(CacheDescriptor descr, String forceKey) throws CacheException
    {
        checkCache();
        try {
            logger.debug("CacheHandler[" + subSystem + "]: Inserting CacheDescriptor for key[" + forceKey + "]: "
                    + descr);
            String key = putInt(descr, forceKey);

            logger.debug("CacheHandler[" + subSystem + "]: Inserted CacheDescriptor for key[" + key + "]");
            return key;
        }
        catch (CacheException exc) {
            logger.error("CacheHandler[" + subSystem + "]: Error inserting CacheDescriptor for key[" + forceKey + "]: "
                    + descr, exc);
            throw exc;
        }
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#get(java.lang.String)
     */
    public CacheDescriptor get(String key) throws CacheException
    {
        logger.debug("CacheHandler[" + subSystem + "]: Extractiong CacheDescriptor for key[" + key + "]");
        CacheDescriptor descr = getInt(key);
        if (descr != null) {
            logger.debug("CacheHandler[" + subSystem + "]: Extracted CacheDescriptor for key[" + key + "]: " + descr);
        }
        return descr;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#getOptions(java.lang.String)
     */
    public Map<String, Object> getOptions(String key) throws CacheException
    {
        logger.debug("CacheHandler[" + subSystem + "]: Extractiong Options for key[" + key + "]");
        Map<String, Object> options = getOptionsInt(key);
        if (options != null) {
            logger.debug("CacheHandler[" + subSystem + "]: Extracted Options for key[" + key + "]: " + options);
        }
        return options;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#remove(java.lang.String)
     */
    public Map<String, Object> remove(String key) throws CacheException
    {
        logger.debug("CacheHandler[" + subSystem + "]: Removing key[" + key + "]");
        Map<String, Object> options = removeInt(key);
        if (options != null) {
            logger.debug("CacheHandler[" + subSystem + "]: Removed key[" + key + "]: " + options);
        }
        return options;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.CacheHandler#getIterator()
     */
    public Iterator<String> getIterator() throws CacheException
    {
        return getIteratorInt();
    }

    /**
     *
     * @return the subsystem
     */
    public String getSubSystem()
    {
        return subSystem;
    }

    /**
     *
     * @return the logger
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     *
     */
    protected abstract void checkCache();

    /**
     *
     * @param node
     * @throws CacheException
     */
    protected abstract void initInt(Node node) throws CacheException;

    /**
     *
     * @param descr
     * @return the identifier of the object inserted in cache
     * @throws CacheException
     */
    protected abstract String putInt(CacheDescriptor descr) throws CacheException;

    /**
     *
     * @param descr
     * @param forceKey
     * @return the identifier of the object inserted in cache
     * @throws CacheException
     */
    protected abstract String putInt(CacheDescriptor descr, String forceKey) throws CacheException;

    /**
     *
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    protected abstract Map<String, Object> getOptionsInt(String key) throws CacheException;

    /**
     *
     * @param key
     * @return the cache descriptor corresponding to the key
     * @throws CacheException
     */
    protected abstract CacheDescriptor getInt(String key) throws CacheException;

    /**
     *
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    protected abstract Map<String, Object> removeInt(String key) throws CacheException;

    /**
     *
     * @return an iterator containing keys of the cache
     * @throws CacheException
     */
    protected abstract Iterator<String> getIteratorInt() throws CacheException;
}
