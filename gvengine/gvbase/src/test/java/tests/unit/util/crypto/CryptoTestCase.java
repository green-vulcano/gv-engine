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
package tests.unit.util.crypto;

import static org.junit.Assert.assertArrayEquals;
import java.util.Base64;

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
    private static final String TEST_STRING_CLEAR                          = "Test string!";
    private static final String TEST_STRING_CYPHER_3DES                    = "{3DES}Gxr7ntesEtC7R76H5bfRAQ==";
    private static final String TEST_STRING_CYPHER_3DES_CFB8_NoPadding     = "{3DES/CFB8/NoPadding}1o/eK55bUUD7TTMW";
    private static final String TEST_STRING_CYPHER_3DES_OFB32_PKCS5Padding = "{3DES/OFB32/PKCS5Padding}1lVdfSfjA5naQNUgoEm+5g==";
    private static final String TEST_STRING_CYPHER_3DES_L                  = "Gxr7ntesEtC7R76H5bfRAQ==";

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
    public void testEncrypt1() throws Exception
    {
        String result = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, TEST_STRING_CLEAR, true);
        System.out.println("Encrypt: " + result);
        assertEquals("Encrypt Failed", TEST_STRING_CYPHER_3DES, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testEncrypt2() throws Exception
    {
        String result = CryptoHelper.encrypt("test2", TEST_STRING_CLEAR, true);
        System.out.println("Encrypt(test2): " + result);
        assertEquals("Encrypt Failed", TEST_STRING_CYPHER_3DES_CFB8_NoPadding, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testEncrypt3() throws Exception
    {
        String result = CryptoHelper.encrypt("test3", TEST_STRING_CLEAR, true);
        System.out.println("Encrypt(test3): " + result);
        assertEquals("Encrypt Failed", TEST_STRING_CYPHER_3DES_OFB32_PKCS5Padding, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#encrypt(java.lang.String, byte[], boolean)}
     * .
     */
    @Test
    public void testEncrypt4() throws Exception
    {
        byte[] result = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, TEST_STRING_CLEAR.getBytes("ISO-8859-1"),
                false);
        assertArrayEquals("Encrypt Failed", Base64.getDecoder().decode(TEST_STRING_CYPHER_3DES_L), result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#decrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testDecrypt1() throws Exception
    {
        String result = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, TEST_STRING_CYPHER_3DES, true);
        System.out.println("Decrypt: " + result);
        assertEquals("Decrypt Failed", TEST_STRING_CLEAR, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#decrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testDecrypt2() throws Exception
    {
        String result = CryptoHelper.decrypt("test2", TEST_STRING_CYPHER_3DES_CFB8_NoPadding, true);
        System.out.println("Decrypt(test2): " + result);
        assertEquals("Decrypt Failed", TEST_STRING_CLEAR, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#decrypt(java.lang.String, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testDecrypt3() throws Exception
    {
        String result = CryptoHelper.decrypt("test3", TEST_STRING_CYPHER_3DES_OFB32_PKCS5Padding, true);
        System.out.println("Decrypt(test3): " + result);
        assertEquals("Decrypt Failed", TEST_STRING_CLEAR, result);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#decrypt(java.lang.String, byte[], boolean)}
     * .
     */
    @Test
    public void testDecrypt4() throws Exception
    {
        byte[] result = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID,
                Base64.getDecoder().decode(TEST_STRING_CYPHER_3DES_L), true);
        assertArrayEquals("Decrypt Failed", TEST_STRING_CLEAR.getBytes("ISO-8859-1"), result);
    }
    
    /**
     * Test method for
     * {@link it.greenvulcano.util.crypto.CryptoHelper#decrypt(java.lang.String, byte[], boolean)}
     * .
     */
    @Test
    public void testDecrypt5() throws Exception
    {
        String result = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, "Y", true);
        assertEquals("Decrypt Failed", "Y", result);
    }

    /**
     * Test method for encrypt/decrypt.
     */
    @Test
    public void testDecrypt6() throws Exception
    {
    	String resultE = CryptoHelper.encrypt(CryptoHelper.DEFAULT_KEY_ID, "", true);
        String resultD = CryptoHelper.decrypt(CryptoHelper.DEFAULT_KEY_ID, resultE, true);
        assertEquals("Decrypt Failed", "", resultD);
    }
}
