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
package tests.unit.util.file;

import java.io.File;

import it.greenvulcano.util.file.cache.FileCache;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import tests.unit.BaseTestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FileCacheTestCase extends BaseTestCase
{
    private static final String TEST_FILE_DIR  = System.getProperty("user.dir") + File.separator + "target"
    												+ File.separator+ "test-classes" + File.separator + "TESTFS";
    private static final String TEST_FILE_1    = TEST_FILE_DIR + File.separator + "test.txt";
    private static final String TEST_FILE_2    = TEST_FILE_DIR + File.separator + "test2.txt";

    private static final String TEST_FILE_1_TXT    = "Test content to read";
    private static final String TEST_FILE_2_TXT    = "Second test content to read";

    private static final String TEST_FILE_1_B64    = "VGVzdCBjb250ZW50IHRvIHJlYWQ=";
    private static final String TEST_FILE_2_B64    = "U2Vjb25kIHRlc3QgY29udGVudCB0byByZWFk";

    private class Processor implements Runnable {
    	@Override
    	public void run() {
    		String th = Thread.currentThread().getName();
    		for (int i = 0; i < 1000; i++) {
    			long start = System.currentTimeMillis();
    			try {
					String data = PropertiesHandler.expand("file{{" + TEST_FILE_1 + "::y::text}}");
				} catch (PropertiesHandlerException e) {
					e.printStackTrace();
				}
    			System.out.println(th + "[" + i + "] - " + (System.currentTimeMillis() - start));
    			/*try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
			}
    	}
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testF1_text() throws Exception
    {
        String data = (String) FileCache.getContent(TEST_FILE_1, FileCache.Type.TEXT);
        assertEquals("Error readind content of " + TEST_FILE_1 + " as String", data, TEST_FILE_1_TXT);
    }

    /**
     * @throws Exception
     */
    public void testF2_text() throws Exception
    {
        String data = (String) FileCache.getContent(TEST_FILE_2, FileCache.Type.TEXT);
        assertEquals("Error readind content of " + TEST_FILE_2 + " as String", data, TEST_FILE_2_TXT);
    }
    
    /**
     * @throws Exception
     */
    public void testF1_base64() throws Exception
    {
        String data = (String) FileCache.getContent(TEST_FILE_1, FileCache.Type.BASE64);
        assertEquals("Error readind content of " + TEST_FILE_1 + " as Base64", data, TEST_FILE_1_B64);
    }

    /**
     * @throws Exception
     */
    public void testF2_base64() throws Exception
    {
        String data = (String) FileCache.getContent(TEST_FILE_2, FileCache.Type.BASE64);
        assertEquals("Error readind content of " + TEST_FILE_2 + " as Base64", data, TEST_FILE_2_B64);
    }
    
    /**
     * @throws Exception
     */
    public void testF1_PH_text() throws Exception
    {
        String data = PropertiesHandler.expand("file{{" + TEST_FILE_1 + "}}");
        assertEquals("Error readind content of " + TEST_FILE_1 + " as String from PH", data, TEST_FILE_1_TXT);
    }

    /**
     * @throws Exception
     */
    public void testF1_PH_base64() throws Exception
    {
        String data = PropertiesHandler.expand("file{{" + TEST_FILE_1 + "::Y::base64}}");
        assertEquals("Error readind content of " + TEST_FILE_1 + " as Base64 from PH", data, TEST_FILE_1_B64);
    }

    /**
     * @throws Exception
     */
    public void testDump() throws Exception
    {
    	FileCache.getContent(TEST_FILE_1, FileCache.Type.TEXT);
    	FileCache.getContent(TEST_FILE_1, FileCache.Type.BASE64);
    	FileCache.getContent(TEST_FILE_2, FileCache.Type.TEXT);
    	FileCache.getContent(TEST_FILE_2, FileCache.Type.BASE64);
    	String dump = FileCache.dump();
    	System.out.println("FileCache: " + dump);
        assertNotNull("Error generating FileCache dump", dump);
    }
    

    /**
     * @throws Exception
     */
    public void testProcessor() throws Exception
    {
    	for (int i = 0; i < 20; i++) {
			Thread t = new Thread(new Processor());
			t.setDaemon(true);
			t.start();
			Thread.sleep(10);
		}
    	
    	Thread.sleep(10000);
    }
}
