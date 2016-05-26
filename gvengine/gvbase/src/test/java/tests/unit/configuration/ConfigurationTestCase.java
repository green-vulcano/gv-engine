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
package tests.unit.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.crypto.CryptoHelper;
import it.greenvulcano.util.crypto.CryptoUtils;
import it.greenvulcano.util.crypto.KeyID;
import it.greenvulcano.util.crypto.KeyStoreID;
import it.greenvulcano.util.crypto.KeyStoreUtils;
import it.greenvulcano.util.xml.DOMWriter;
import junit.framework.TestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class ConfigurationTestCase extends TestCase
{
	private static final String BASE_DIR = "target" + File.separator + "test-classes";
    private static final String CONFIGURATION_FILE       = "GVCore.xml";
    private static final String CONFIGURATION_FILE_PROPS = "test.xml";
    private static final String CONFIGURATION_FILE_FUNC  = "testFunc1.xml";
    private static final String KEYSTORE_NAME            = "GVEsb.jks";   
    private static final String KEY_STORE_PWD            = "__GreenVulcanoPassword__";
    private static final String ALIAS_KEY_NAME           = "XMLConfigKey";
    private static final String ALIAS_KEY_PWD            = "XMLConfigPassword";

    private static final String TEST_KS_XPATH            = "/GVCore/GVCryptoHelper/KeyStoreID[@id='testKS']";
    private static final String TEST_K_XPATH             = "/GVCore/GVCryptoHelper/KeyID[@id='test']";

    /**
     * @param name
     */
    public ConfigurationTestCase(String name)
    {
        super(name);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        XMLConfig.setBaseConfigPath(getClass().getClassLoader().getResource(".").getPath());
        System.setProperty("gv.app.home", BASE_DIR);
        
        try {
            FileUtils.moveFile(new File(BASE_DIR + File.separator+ KEYSTORE_NAME), 
            				   new File(BASE_DIR + File.separator+ KEYSTORE_NAME + ".orig"));
        }
        catch (Exception exc) {
        	exc.printStackTrace();
            fail();
        }
        try {
            KeyStoreID keySid = new KeyStoreID("TEMP", KeyStoreUtils.DEFAULT_KEYSTORE_TYPE, "", "",
                    KeyStoreUtils.DEFAULT_KEYSTORE_PROVIDER);
            KeyID keyid = new KeyID("TEMP", keySid, "");
            keySid.setKeyStoreName(KEYSTORE_NAME);
            keySid.setKeyStorePwd(KEY_STORE_PWD);
            keyid.setKeyAlias(ALIAS_KEY_NAME);
            keyid.setKeyPwd(ALIAS_KEY_PWD);
            SecretKey secretKey = CryptoUtils.generateSecretKey(CryptoUtils.TRIPLE_DES_TYPE, KEY_STORE_PWD.getBytes());
            System.out.println("***************************************");
            System.out.println("Registering SecretKey: " + secretKey.getAlgorithm() + " " + secretKey.getFormat() + " "
                    + secretKey.toString());
            System.out.println("In: " + keyid);
            
           
            
            KeyStoreUtils.writeKey(keyid, secretKey, null);

            CryptoHelper.resetCache();
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw exc;
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        new File(BASE_DIR + File.separator + KEYSTORE_NAME).delete();
        new File("target/keystores/" + KEYSTORE_NAME).delete();
        try {
            FileUtils.moveFile(new File(BASE_DIR  + File.separator+ KEYSTORE_NAME + ".orig"), 
            				   new File(BASE_DIR  + File.separator+ KEYSTORE_NAME));
        }
        catch (Exception exc) {
            // TODO: handle exception
        }
        super.tearDown();
    }

    /**
     *
     */
    public void testConfiguration()
    {
        try {
            URL configURL = XMLConfig.load(CONFIGURATION_FILE);
            assertNotNull("Cannot load " + CONFIGURATION_FILE + " configuration.", configURL);
            URL retrievedURL = XMLConfig.getURL(CONFIGURATION_FILE);
            assertSame(configURL, retrievedURL);
            String[] files = XMLConfig.getLoadedFiles();
            assertTrue("Loaded files empty", (files != null) && (files.length > 0));

            Document confDocument = XMLConfig.getDocument(CONFIGURATION_FILE);
            Node testNode_KS = XMLConfig.getNode(confDocument, TEST_KS_XPATH);
            Node testNode_K = XMLConfig.getNode(confDocument, TEST_K_XPATH);

            Node originalKeystoreAttr = XMLConfig.getNode(testNode_KS, "@key-store-pwd");
            Node originalAliasAttr = XMLConfig.getNode(testNode_K, "@key-pwd");
            String originalKeystorePwd = originalKeystoreAttr.getNodeValue();
            String originalAliasPwd = originalAliasAttr.getNodeValue();
            String encryptedData = CryptoHelper.encrypt(null, originalKeystorePwd, true);
            originalKeystoreAttr.setNodeValue(encryptedData);
            encryptedData = CryptoHelper.encrypt(null, originalAliasPwd, true);
            originalAliasAttr.setNodeValue(encryptedData);

            System.out.println("Overwriting " + CONFIGURATION_FILE);
            DOMWriter writer = new DOMWriter();
            OutputStream out = new FileOutputStream(URLDecoder.decode(configURL.getPath(), "UTF-8"));
            writer.write(confDocument, out);
            out.close();

            System.out.println("Reloading " + CONFIGURATION_FILE);
            XMLConfig.reload(CONFIGURATION_FILE);

            String decrypted = XMLConfig.getDecrypted(CONFIGURATION_FILE, TEST_KS_XPATH + "/@key-store-pwd",
                    XMLConfig.DEFAULT_KEY_ID, false);
            assertEquals(originalKeystorePwd, decrypted);

            String encrypted = XMLConfig.get(CONFIGURATION_FILE, TEST_KS_XPATH + "/@key-store-pwd");
            decrypted = XMLConfig.getDecrypted(encrypted);
            assertEquals(originalKeystorePwd, decrypted);
            assertEquals(encrypted, XMLConfig.getEncrypted(decrypted));

            confDocument = XMLConfig.getDocument(CONFIGURATION_FILE);
            testNode_KS = XMLConfig.getNode(confDocument, TEST_KS_XPATH);
            testNode_K = XMLConfig.getNode(confDocument, TEST_K_XPATH);
            Node encKeystoreAttr = XMLConfig.getNode(testNode_KS, "@key-store-pwd");
            Node encAliasAttr = XMLConfig.getNode(testNode_K, "@key-pwd");
            encKeystoreAttr.setNodeValue(originalKeystorePwd);
            encAliasAttr.setNodeValue(originalAliasPwd);
            out = new FileOutputStream(URLDecoder.decode(configURL.getPath(), "UTF-8"));
            writer.write(confDocument, out);

            XMLConfig.discard(CONFIGURATION_FILE);
            files = XMLConfig.getLoadedFiles();
            assertTrue("Discarded file " + CONFIGURATION_FILE + " is again returned",
                    Arrays.binarySearch(files, CONFIGURATION_FILE) < 0);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("Exception occurred while testing configuration: " + exc.getMessage());
        }
    }

    /**
     *
     */
    public void testProperties()
    {
        try {      	
        
            assertEquals("Bad properties from XMLConfig.properties", "val_prop00_XMConfig",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='A']"));
            assertEquals("Bad properties from testInc01.properties", "val_prop01_testInc01",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='B']"));
            assertEquals("Bad properties from testInc02.properties", "val_prop02_testInc02",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='C']"));
            assertEquals("Bad properties from test.properties", "val_prop03_test",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='D']"));
            assertEquals("Bad properties from testInc02.properties", "val_prop04_testInc02",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='E']"));
            assertEquals("Bad properties from testInc02b.properties", "val_prop05_testInc02b",
                    XMLConfig.get(CONFIGURATION_FILE_PROPS, "/root/elem[@id='F']"));
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("Exception occurred while testing properties configuration: " + exc.getMessage());
        }
    }

    /**
     *
     */
    public void testXPathFunctions()
    {
        try {
            Node n = XMLConfig.getNode(CONFIGURATION_FILE_FUNC, "/root/elem[@id='A']");
            assertEquals("Bad current() function", "AAAA", XMLConfig.get(n, "/root/elem[@id=current()/text()]"));
            assertEquals("Bad current()/document() function", "AAAA__2", XMLConfig.get(n,
            		"gvf:document('target/test-classes/testFunc2.xml')/root/elem[@id=current()/text()]"));
                   
            /*try {
                Thread.sleep(190000);
            }
            catch (Exception exc) {
                // do nothing
            }*/
        }
        catch (Exception exc) {
            exc.printStackTrace();
            fail("Exception occurred while testing xpath functions: " + exc.getMessage());
        }
    }

}
