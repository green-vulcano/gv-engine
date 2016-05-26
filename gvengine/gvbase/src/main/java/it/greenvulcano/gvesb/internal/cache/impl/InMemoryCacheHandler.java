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
package it.greenvulcano.gvesb.internal.cache.impl;

import it.greenvulcano.gvesb.internal.cache.CacheDescriptor;
import it.greenvulcano.gvesb.internal.cache.CacheException;
import it.greenvulcano.util.txt.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
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
public class InMemoryCacheHandler extends BaseCacheHandler
{
    private HashMap<Map<String, Object>, Object> cacheOptions2Data = new HashMap<Map<String, Object>, Object>();
    private HashMap<String, Map<String, Object>> cacheKey2Options  = new HashMap<String, Map<String, Object>>();
    private HashMap<Map<String, Object>, String> cacheOptions2Key  = new HashMap<Map<String, Object>, String>();

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#initInt(org.w3c
     *      .dom.Node)
     */
    @Override
    protected void initInt(Node node) throws CacheException
    {
        // do nothing
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#putInt(it.greenvulcano.gvesb.internal.cache.CacheDescriptor)
     */
    @Override
    protected String putInt(CacheDescriptor descr) throws CacheException
    {
        String key = cacheOptions2Key.get(descr.getOptions());
        if (key == null) {
            key = TextUtils.generateRandomString(128);
        }

        try {
            byte[] data = null;
            if (descr.getData() instanceof InputStream) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                byte[] buffer = new byte[2048];
                InputStream is = (InputStream) descr.getData();
                int count = is.read(buffer);
                while (count != -1) {
                    baos.write(buffer, 0, count);
                    count = is.read(buffer);
                }
                data = baos.toByteArray();
            }
            else if (descr.getData() instanceof byte[]) {
                data = (byte[]) descr.getData();
            }
            else if (descr.getData() instanceof String) {
                data = ((String) descr.getData()).getBytes();
            }
            else {
                throw new CacheException("Unhandled data type [" + descr + "]");
            }

            cacheKey2Options.put(key, descr.getOptions());
            cacheOptions2Data.put(descr.getOptions(), data);
            cacheOptions2Key.put(descr.getOptions(), key);
        }
        catch (CacheException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new CacheException("Error caching data [" + descr + "]", exc);
        }
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#putInt(CacheDescriptor,
     *      java.lang.String)
     */
    @Override
    protected String putInt(CacheDescriptor descr, String forceKey) throws CacheException
    {
        String key = forceKey;

        try {
            byte[] data = null;
            if (descr.getData() instanceof InputStream) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
                byte[] buffer = new byte[2048];
                InputStream is = (InputStream) descr.getData();
                int count = is.read(buffer);
                while (count != -1) {
                    baos.write(buffer, 0, count);
                    count = is.read(buffer);
                }
                data = baos.toByteArray();
            }
            else if (descr.getData() instanceof byte[]) {
                data = (byte[]) descr.getData();
            }
            else if (descr.getData() instanceof String) {
                data = ((String) descr.getData()).getBytes();
            }
            else {
                throw new CacheException("Unhandled data type [" + descr + "]");
            }

            cacheKey2Options.put(key, descr.getOptions());
            cacheOptions2Data.put(descr.getOptions(), data);
            cacheOptions2Key.put(descr.getOptions(), key);
        }
        catch (CacheException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new CacheException("Error caching data [" + descr + "]", exc);
        }
        return key;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#getInt(java.lang.String)
     */
    @Override
    protected CacheDescriptor getInt(String key) throws CacheException
    {
        CacheDescriptor descr = null;
        Map<String, Object> options = cacheKey2Options.get(key);
        if (options != null) {
            byte[] data = (byte[]) cacheOptions2Data.get(options);
            descr = new CacheDescriptor(new ByteArrayInputStream(data), options);
        }
        return descr;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#getOptionsInt(java.lang.String)
     */
    @Override
    protected Map<String, Object> getOptionsInt(String key) throws CacheException
    {
        Map<String, Object> options = cacheKey2Options.get(key);
        return options;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#removeInt(java.lang.String)
     */
    @Override
    protected Map<String, Object> removeInt(String key) throws CacheException
    {
        Map<String, Object> options = cacheKey2Options.remove(key);
        if (options != null) {
            cacheOptions2Data.remove(options);
            cacheOptions2Key.remove(options);
        }
        return options;
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#getIteratorInt()
     */
    @Override
    protected Iterator<String> getIteratorInt() throws CacheException
    {
        return Collections.unmodifiableSet(cacheKey2Options.keySet()).iterator();
    }

    /**
     * @see it.greenvulcano.gvesb.internal.cache.impl.BaseCacheHandler#checkCache()
     */
    @Override
    protected void checkCache()
    {
        // do nothing
    }
}
