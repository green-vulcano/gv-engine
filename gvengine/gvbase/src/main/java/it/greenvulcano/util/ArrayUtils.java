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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * ArrayUtils class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ArrayUtils
{

    /**
     * @param a
     * @param b
     * @param type
     * @return the arrays concatenated
     */
    public static final Object[] concat(Object[] a, Object[] b, Class<?> type)
    {
        if (a == null) {
            a = (Object[]) Array.newInstance(type, 0);
        }

        if (b == null) {
            b = (Object[]) Array.newInstance(type, 0);
        }

        Object[] result = (Object[]) Array.newInstance(type, a.length + b.length);

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }
    
    public static final <T> List<T> arrayToList(final T[] array) {
    	final List<T> list = new ArrayList<T>(array.length);

    	for (final T item : array) {
    		list.add(item);
    	}
    	return list;
    }
    
    public static final List<?> arrayToList(Object arr) {
    	List<?> list = null; 

    	Class<?> ac = arr.getClass();
    	if (!ac.isArray()) {
    		throw new IllegalArgumentException("The input parameter isn't an array");
    	}

    	Class<?> act = ac.getComponentType();
    	if (act.isPrimitive()) {
    		if (act.equals(boolean.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((boolean[]) arr));
    		}
    		else if (act.equals(byte.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((byte[]) arr));
    		}
    		else if (act.equals(char.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((char[]) arr));
    		}
    		else if (act.equals(short.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((short[]) arr));
    		}
    		else if (act.equals(int.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((int[]) arr));
    		}
    		else if (act.equals(long.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((long[]) arr));
    		}
    		else if (act.equals(float.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((float[]) arr));
    		}
    		else if (act.equals(double.class)) {
    			list = Arrays.asList(org.apache.commons.lang.ArrayUtils.toObject((double[]) arr));
    		}
    	}
    	else {
    		list = arrayToList((Object[]) arr); 
    	}

    	return list;
    }
}
