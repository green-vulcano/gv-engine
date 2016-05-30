/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.util.txt;

import it.greenvulcano.util.txt.TextUtils;
import tests.unit.BaseTestCase;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @version 3.4.0 10/mar/2013
 * @author GreenVulcano Developer Team
 */
public class TextUtilsTestCase extends BaseTestCase
{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // do nothing
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // do nothing
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#matches(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testMatches() {
        String base = "234";
        assertTrue("match \\d{3} failed", TextUtils.matches("\\d{3}", base));

        base = "Test ;aa234EE";
        assertTrue("match Test\\p{Blank}\\p{Punct}\\p{Lower}{2}\\d{3}\\p{Upper}{2} failed", TextUtils.matches("Test\\p{Blank}\\p{Punct}\\p{Lower}{2}\\d{3}\\p{Upper}{2}", base));
        assertTrue("match Test ;[a-z]{2}\\d{3}[A-Z]{2} failed", TextUtils.matches("Test ;[a-z]{2}\\d{3}[A-Z]{2}", base));
        assertFalse("not match Test failed", TextUtils.matches("Test", base));
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#replaceSubstring(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testReplaceSubstring() {
        assertEquals("failed \"a\" -> \"A\"", "A", TextUtils.replaceSubstring("a", "a", "A"));
        assertEquals("failed \"aaaBBcccD\" -> \"aaaNNcccD\"", "aaaNNcccD", TextUtils.replaceSubstring("aaaBBcccD", "BB", "NN"));
        assertEquals("failed \"aaaBBaaaD\" -> \"sssBBsssD\"", "sssBBsssD", TextUtils.replaceSubstring("aaaBBaaaD", "aaa", "sss"));
        assertEquals("failed \"aBB\" -> \"sBB\"", "sBB", TextUtils.replaceSubstring("aBB", "a", "s"));
        assertEquals("failed \"BaB\" -> \"BsB\"", "BsB", TextUtils.replaceSubstring("BaB", "a", "s"));
        assertEquals("failed \"BBa\" -> \"BBs\"", "BBs", TextUtils.replaceSubstring("BBa", "a", "s"));
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#replacePlaceholder(java.lang.String, java.lang.String, java.lang.String, java.util.Hashtable)}.
     */
    @Test
    public void testReplacePlaceholder() {
        Hashtable<String, String> phValues = new Hashtable<String, String>();
        phValues.put("AA", "11");
        phValues.put("BB", "22");
        phValues.put("CC", "33");
        
        String input = "aaph{{AA}}aa ph{{BB}} ph{{DD}} ccph{{CC}}";
        String output = "aa11aa 22 ph{{DD}} cc33";
        assertEquals(output, TextUtils.replacePlaceholder(input, "ph{{", "}}", phValues));
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#splitByStringSeparator(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSplitByStringSeparator() {
        String input1 = "s1;s2;s3;s4;";
        String input2 = "s1 s2 s3 s4 ";
        String input3 = "s1aas2aas3aas4aa";
        List<String> output = new ArrayList<String>();
        output.add("s1");
        output.add("s2");
        output.add("s3");
        output.add("s4");
        
        assertEquals("split failed: " + input1, output, TextUtils.splitByStringSeparator(input1, ";"));
        assertEquals("split failed: " + input2, output, TextUtils.splitByStringSeparator(input2, " "));
        assertEquals("split failed: " + input3, output, TextUtils.splitByStringSeparator(input3, "aa"));
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#replaceJSInvalidChars(java.lang.String)}.
     */
    @Test
    public void testReplaceJSInvalidChars() {
        // TODO
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#replaceSQLInvalidChars(java.lang.String)}.
     */
    @Test
    public void testReplaceSQLInvalidChars() {
        // TODO
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.TextUtils#getEOL(java.lang.String)}.
     */
    @Test
    public void testGetEOL() {
        // TODO
    }

}
