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
package tests.unit.util.clazz;

import static org.junit.Assert.assertEquals;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.clazz.ClassUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version 3.2.0 31/gen/2012
 * @author GreenVulcano Developer Team
 */
public class ClassUtilsTestCase
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // do nothing
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // do nothing
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.clazz.ClassUtils#getRealClass(java.lang.String)}
     * .
     */
    @Test
    public void testGetRealClass() throws Exception
    {
        Class<?> c = ClassUtils.getRealClass("java.lang.String");
        System.out.println("java.lang.String -> " + c);
        assertEquals("java.lang.String -> " + c, String.class, c);

        c = ClassUtils.getRealClass("int");
        System.out.println("int -> " + c);
        assertEquals("int -> " + c, Integer.TYPE, c);

        String[][] o = new String[0][0];
        c = ClassUtils.getRealClass("java.lang.String[][]");
        System.out.println("java.lang.String[][] -> " + c);
        assertEquals("java.lang.String[][] -> " + c, o.getClass(), c);

        int[][] i = new int[0][0];
        c = ClassUtils.getRealClass("int[][]");
        System.out.println("int[][] -> " + c);
        assertEquals("int[][] -> " + c, i.getClass(), c);

        c = ClassUtils.getRealClass("it.greenvulcano.gvesb.buffer.GVBuffer");
        System.out.println("it.greenvulcano.gvesb.buffer.GVBuffer -> " + c);
        assertEquals("it.greenvulcano.gvesb.buffer.GVBuffer -> " + c, GVBuffer.class, c);
    }

}
