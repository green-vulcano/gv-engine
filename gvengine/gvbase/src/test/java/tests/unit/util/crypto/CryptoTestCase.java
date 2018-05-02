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
package tests.unit.util.crypto;

import static org.junit.Assert.assertArrayEquals;
import it.greenvulcano.util.crypto.CryptoHelper;
import tests.unit.BaseTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @version 3.0.0 08/giu/2010
 * @author GreenVulcano Developer Team
 */
public class CryptoTestCase extends BaseTestCase
{
    private static final String TEST_STRING_CLEAR = "Test string!";
    private static final String PREFIX_3DES = "{3DES}";
    private static final String PREFIX_3DES_CFB8_NoPadding = "{3DES/CFB8/NoPadding}";
    private static final String PREFIX_3DES_OFB32_PKCS5Padding = "{3DES/OFB32/PKCS5Padding}";
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    	super.setUp();
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
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testDefaultSettings() throws Exception
    {
        String result = CryptoHelper.encrypt(null, TEST_STRING_CLEAR, true);
        System.out.println("Encrypt: " + result);
        assertFalse("Encrypt Failed", result.equals(TEST_STRING_CLEAR));
        assertTrue("Encrypt Failed", result.startsWith(PREFIX_3DES));
        
        assertEquals(TEST_STRING_CLEAR, CryptoHelper.decrypt(null, result, true));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void test3DES_CFB8_NoPadding() throws Exception
    {
        String result = CryptoHelper.encrypt("test2", TEST_STRING_CLEAR, true);
        System.out.println("Encrypt(test2): " + result);
        assertFalse("Encrypt Failed", result.equals(TEST_STRING_CLEAR));
        assertTrue("Encrypt Failed", result.startsWith(PREFIX_3DES_CFB8_NoPadding));
        
        assertEquals(TEST_STRING_CLEAR, CryptoHelper.decrypt("test2", result, true));
        
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void test3DES_OFB32_PKCS5Padding() throws Exception
    {
        String result = CryptoHelper.encrypt("test3", TEST_STRING_CLEAR, true);
        System.out.println("Encrypt(test3): " + result);
        assertFalse("Encrypt Failed", result.equals(TEST_STRING_CLEAR));
        assertTrue("Encrypt Failed", result.startsWith(PREFIX_3DES_OFB32_PKCS5Padding));
        
        assertEquals(TEST_STRING_CLEAR, CryptoHelper.decrypt("test3", result, true));
        
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, byte[], boolean)}
     * .
     */
    @Test
    public void testByteArray() throws Exception
    {
        byte[] result = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, TEST_STRING_CLEAR.getBytes("ISO-8859-1"), false);
       
        assertArrayEquals("Encrypt Failed", TEST_STRING_CLEAR.getBytes("ISO-8859-1"), CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, result, false));
    }
       
  
}
