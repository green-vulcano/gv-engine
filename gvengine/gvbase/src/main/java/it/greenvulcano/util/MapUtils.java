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
package it.greenvulcano.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * MapUtils class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public final class MapUtils
{
    private MapUtils()
    {
        // do nothing
    }

    /**
     * @param input
     * @return a map
     */
    public static Map<String, String> convertToHMStringString(Map<?, ?> input)
    {
        Map<String, String> output = new HashMap<String, String>();

        for (Entry<?, ?> entry : input.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            output.put((key != null) ? key.toString() : null, (value != null) ? value.toString() : null);
        }
        return output;
    }

    /**
     * @param input
     * @return a map
     */
    public static Map<String, Object> convertToHMStringObject(Map<?, ?> input)
    {
        Map<String, Object> output = new HashMap<String, Object>();

        for (Entry<?, ?> entry : input.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            output.put((key != null) ? key.toString() : null, value);
        }
        return output;
    }
}
