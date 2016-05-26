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
package tests.unit.util;

import java.util.List;

import it.greenvulcano.util.ArrayUtils;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 27, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class ArrayUtilsTestCase extends TestCase
{

    /**
     *
     */
    public void test_concat()
    {
        String[] a = {"aaa", "bbb"};
        String[] b = {"ccc", "ddd", "eee"};

        Object[] oresult = ArrayUtils.concat(a, b, String.class);
        assertTrue(oresult != null && oresult.length == 5);
        assertEquals(oresult[0], "aaa");
        assertEquals(oresult[1], "bbb");
        assertEquals(oresult[2], "ccc");
        assertEquals(oresult[3], "ddd");
        assertEquals(oresult[4], "eee");
    }
    
    /**
    *
    */
    public void test_arrayToList_G()
    {
    	Long[] arr = {new Long(1), new Long(2), new Long(3), new Long(4), new Long(5)};
    	List<Long> list = ArrayUtils.arrayToList(arr);

    	assertTrue(list.size() == 5);
       	assertEquals(list.get(0).longValue(), 1);
       	assertEquals(list.get(1).longValue(), 2);
       	assertEquals(list.get(2).longValue(), 3);
       	assertEquals(list.get(3).longValue(), 4);
       	assertEquals(list.get(4).longValue(), 5);
    }
   
    /**
     *
     */
    public void test_arrayToList_O()
    {
    	long[] arr = {1, 2, 3, 4, 5};
        List<?> list = ArrayUtils.arrayToList(arr);

        assertTrue(list.size() == 5);
        assertEquals(((Long) list.get(0)).longValue(), 1);
        assertEquals(((Long) list.get(1)).longValue(), 2);
        assertEquals(((Long) list.get(2)).longValue(), 3);
        assertEquals(((Long) list.get(3)).longValue(), 4);
        assertEquals(((Long) list.get(4)).longValue(), 5);
    }
    
    /**
    *
    */
    public void test_arrayToList_O2()
    {
    	Object arr = new Long[] {new Long(1), new Long(2), new Long(3), new Long(4), new Long(5)};
		List<?> list = ArrayUtils.arrayToList(arr);

    	assertTrue(list.size() == 5);
    	assertEquals(((Long) list.get(0)).longValue(), 1);
        assertEquals(((Long) list.get(1)).longValue(), 2);
        assertEquals(((Long) list.get(2)).longValue(), 3);
        assertEquals(((Long) list.get(3)).longValue(), 4);
        assertEquals(((Long) list.get(4)).longValue(), 5);
    }
}
