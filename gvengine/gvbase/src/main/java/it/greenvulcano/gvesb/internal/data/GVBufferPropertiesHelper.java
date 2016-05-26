/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.gvesb.internal.data;

import it.greenvulcano.gvesb.buffer.GVBuffer;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version 3.2.0 23/nov/2011
 * @author GreenVulcano Developer Team
 */
public class GVBufferPropertiesHelper
{
    private GVBufferPropertiesHelper()
    {
        // do nothing
    }

    public static Map<String, Object> getPropertiesMapSO(GVBuffer gvBuffer, boolean nullOrEmptyAsNULL)
    {
        Map<String, Object> props = new TreeMap<String, Object>();
        addProperties(props, gvBuffer, nullOrEmptyAsNULL);

        return props;
    }

    public static Map<String, String> getPropertiesMapSS(GVBuffer gvBuffer, boolean nullOrEmptyAsNULL)
    {
        Map<String, String> props = new TreeMap<String, String>();
        addProperties(props, gvBuffer, nullOrEmptyAsNULL);

        return props;
    }

    @SuppressWarnings("unchecked")
    public static <V> void addProperties(Map<String, V> props, GVBuffer gvBuffer, boolean nullOrEmptyAsNULL)
    {
        Iterator<String> i = gvBuffer.getPropertyNamesIterator();
        while (i.hasNext()) {
            String name = i.next();
            props.put(name, (V) handleProperty(gvBuffer.getProperty(name), nullOrEmptyAsNULL));
        }
    }

    public static String handleProperty(Object value, boolean nullOrEmptyAsNULL)
    {
        if (value == null) {
            return (nullOrEmptyAsNULL ? "NULL" : "");
        }
        if ("".equals(value)) {
            return (nullOrEmptyAsNULL ? "NULL" : "");
        }
        return String.valueOf(value);
    }
}
