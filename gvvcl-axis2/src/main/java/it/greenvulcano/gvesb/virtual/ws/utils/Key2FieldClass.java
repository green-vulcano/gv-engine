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
package it.greenvulcano.gvesb.virtual.ws.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;


/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class Key2FieldClass
{
    private static final Logger        logger = org.slf4j.LoggerFactory.getLogger(Key2FieldClass.class);

    private static Map<String, String> keyMap = new HashMap<String, String>();

    /**
     *
     * @param key
     * @return the new key
     */
    public static String handleKey(String key)
    {
        if (keyMap.containsKey(key)) {
            return keyMap.get(key);
        }
        String newKey = "";
        try {
            Class<?> propClass = null;
            int idx = key.lastIndexOf('.');
            String klass = key.substring(0, idx);
            try {
                propClass = Class.forName(klass);
            }
            catch (ClassNotFoundException exc) {
                int klassIdx = klass.lastIndexOf('.');
                klass = klass.substring(0, klassIdx) + '$' + klass.substring(klassIdx + 1);
                propClass = Class.forName(klass);
            }
            Field propField = propClass.getField(key.substring(idx + 1));
            newKey = (String) propField.get(null);
        }
        catch (Exception exc) {
            logger.warn("cannot parse key " + key, exc);
        }
        keyMap.put(key, newKey);
        return newKey;
    }
}
