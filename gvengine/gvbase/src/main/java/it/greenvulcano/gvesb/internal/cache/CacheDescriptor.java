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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 *
 */
public final class CacheDescriptor
{
    private Map<String, Object> options = null;
    private Object              data    = null;

    /**
     * @param data
     * @param options
     * @throws CacheException
     */
    public CacheDescriptor(Object data, Map<String, Object> options) throws CacheException
    {
        if (data == null) {
            throw new CacheException("data can't be null");
        }
        if ((options == null) || options.isEmpty()) {
            throw new CacheException("options can't be null or empty");
        }
        this.data = data;
        this.options = Collections.unmodifiableMap(options);
    }

    /**
     * @return the options
     */
    public Map<String, Object> getOptions()
    {
        return options;
    }

    /**
     * @return the data
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @return the data
     */
    public InputStream getInputStream()
    {
        return (InputStream) data;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "CacheDescriptor:\nOptions: " + options + "\nData:\n" + data;
    }

}
