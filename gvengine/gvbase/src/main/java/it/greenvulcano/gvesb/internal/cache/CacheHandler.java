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

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public interface CacheHandler
{

    /**
     *
     * @param node
     * @throws CacheException
     */
    public void init(Node node) throws CacheException;

    /**
     *
     * @param descr
     * @return the identifier of the object inserted in cache
     * @throws CacheException
     */
    public String put(CacheDescriptor descr) throws CacheException;

    /**
     *
     * @param descr
     * @param forceKey
     * @return the identifier of the object inserted in cache
     * @throws CacheException
     */
    public String put(CacheDescriptor descr, String forceKey) throws CacheException;

    /**
     *
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    public Map<String, Object> getOptions(String key) throws CacheException;

    /**
     *
     * @param key
     * @return the object corresponding to key in cache
     * @throws CacheException
     */
    public CacheDescriptor get(String key) throws CacheException;

    /**
     *
     * @param key
     * @return a map containing options corresponding to the key
     * @throws CacheException
     */
    public Map<String, Object> remove(String key) throws CacheException;

    /**
     *
     * @return an iterator to keys stored in the cache
     * @throws CacheException
     */
    public Iterator<String> getIterator() throws CacheException;

}
