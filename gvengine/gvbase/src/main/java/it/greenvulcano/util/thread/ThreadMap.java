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
package it.greenvulcano.util.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * ThreadMap class
 *
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public final class ThreadMap
{
    /**
     * Map(Thread, Map)
     */
    private static Map<Thread, HashMap<Object, Object>> threadsMap = new WeakHashMap<Thread, HashMap<Object, Object>>();

    /**
     * Constructor
     */
    private ThreadMap()
    {
        // do nothing
    }

    /**
     * Retrieve the object associated to 'key' in the current Thread map
     *
     * @param key
     *        the key to search for
     * @return the found object, or null
     */
    public static Object get(Object key)
    {
        return get(Thread.currentThread(), key);
    }

    /**
     * Retrieve the objects associated to 'key' in all Thread maps
     *
     * @param key
     *        the key to search for
     * @return the found objects, or an empty array
     */
    public static Object[] allThreadGet(Object key)
    {
        ArrayList<Object> list = new ArrayList<Object>();
        Set<Thread> threads = threadsMap.keySet();
        for (Thread thread : threads) {
            Object obj = get(thread, key);
            if (obj != null) {
                list.add(obj);
            }
        }

        return list.toArray();
    }

    /**
     * Insert the object 'value' associated to 'key' in the current Thread map
     *
     * @param key
     *        the key to use for mapping
     * @param value
     *        the value to insert
     */
    public static void put(Object key, Object value)
    {
        put(Thread.currentThread(), key, value);
    }

    /**
     * Insert the object 'value' associated to 'key' in all Thread maps
     *
     * @param key
     *        the key to use for mapping
     * @param value
     *        the value to insert
     */
    public static void allThreadPut(Object key, Object value)
    {
        Set<Thread> threads = threadsMap.keySet();
        for (Thread thread : threads) {
            put(thread, key, value);
        }
    }

    /**
     * Remove the object associated to 'key' in the current Thread map
     *
     * @param key
     *        the key to search for
     * @return the found object, or null
     */
    public static Object remove(Object key)
    {
        return remove(Thread.currentThread(), key);
    }

    /**
     * Remove the objects associated to 'key' in all Thread maps
     *
     * @param key
     *        the key to search for
     */
    public static void allThreadRemove(Object key)
    {
        Set<Thread> threads = threadsMap.keySet();
        for (Thread thread : threads) {
            remove(thread, key);
        }
    }

    /**
     * Clean the current Thread map
     *
     * @return the found object, or null
     */
    public static void clean()
    {
        Thread thread = Thread.currentThread();
        synchronized (thread) {
            HashMap<Object, Object> currThreadMap = threadsMap.get(thread);
            if (currThreadMap != null) {
                currThreadMap.clear();
                threadsMap.remove(thread);
            }
        }
    }

    /**
     * Clean all Thread maps
     */
    public static void cleanAll()
    {
        threadsMap.clear();
    }

    /**
     * Retrieve the object associated to 'key' in the given Thread map
     *
     * @param thread
     *        the tread on which operate
     * @param key
     *        the key to search for
     * @return the found object, or null
     */
    private static Object get(Thread thread, Object key)
    {
        Object result = null;

        HashMap<Object, Object> currThreadMap = threadsMap.get(thread);
        if (currThreadMap != null) {
            result = currThreadMap.get(key);
        }
        return result;
    }

    /**
     * Insert the object 'value' associated to 'key' in the given Thread map
     *
     * @param thread
     *        the tread on which operate
     * @param key
     *        the key to use for mapping
     * @param value
     *        the value to insert
     */
    private static void put(Thread thread, Object key, Object value)
    {
        synchronized (thread) {
            HashMap<Object, Object> currThreadMap = threadsMap.get(thread);
            if (currThreadMap == null) {
                currThreadMap = new HashMap<Object, Object>();
                threadsMap.put(thread, currThreadMap);
            }
            currThreadMap.put(key, value);
        }
    }

    /**
     * Remove the object associated to 'key' in the given Thread map
     *
     * @param thread
     *        the tread on which operate
     * @param key
     *        the key to search for
     * @return the found object, or null
     */
    private static Object remove(Thread thread, Object key)
    {
        Object result = null;

        synchronized (thread) {
            HashMap<Object, Object> currThreadMap = threadsMap.get(thread);
            if (currThreadMap != null) {
                result = currThreadMap.remove(key);
    
                if (currThreadMap.isEmpty()) {
                    threadsMap.remove(thread);
                }
            }
        }
        return result;
    }

}
