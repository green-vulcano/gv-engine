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

import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.FileManager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import tests.unit.BaseTestCase;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FileManagerTestCase extends BaseTestCase
{
    private static final String TEST_FILE_RESOURCES;
    private static final String TEST_FILE_DIR             = "TestFileManager";
    private static final String TEST_FILE_DIR_RENAMED     = "TestFileManager_Renamed";
    private static final String TEST_FILE_DEST_RESOURCES  = System.getProperty("java.io.tmpdir") + File.separator
                                                                  + TEST_FILE_DIR;
    private static final String TEST_FILE_MANAGER         = "fileManager_test.txt";
    private static final String TEST_FILE_MANAGER_XML     = "fileManager_test.xml";
    private static final String TEST_FILE_MANAGER_RENAMED = "fileManager_test_renamed.txt";

    static {
  		
		URI resourceFile = null;
		
		try {
			resourceFile = FileManagerTestCase.class.getClassLoader().getResource(TEST_FILE_MANAGER).toURI();
		} catch (URISyntaxException e) {			
			e.printStackTrace();
		}
		
		TEST_FILE_RESOURCES = Paths.get(resourceFile).getParent().toAbsolutePath().toString();		
			    	
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
             
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testExistFile() throws Exception
    {
        Set<FileProperties> files = FileManager.ls(TEST_FILE_RESOURCES, TEST_FILE_MANAGER);
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_RESOURCES, files != null
                && !files.isEmpty());
    }

    /**
     * @throws Exception
     */
    public void testCopyFile() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_MANAGER + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());

        FileManager.cp(TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER, null);

        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyMoveFile() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_MANAGER_RENAMED + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());

        FileManager.cp(TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER,
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER, null);

        FileManager.mv(TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER_RENAMED, null);

        assertFalse("Resource " + TEST_FILE_MANAGER + " yet in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER_RENAMED).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyMove2File() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_MANAGER_RENAMED + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());

        FileManager.cp(TEST_FILE_RESOURCES,
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER, TEST_FILE_MANAGER);

        FileManager.mv(TEST_FILE_DEST_RESOURCES,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED, TEST_FILE_MANAGER);

        assertFalse("Resource " + TEST_FILE_MANAGER + " yet in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED + File.separator + TEST_FILE_MANAGER).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyRemoveFile() throws Exception
    {
        FileManager.cp(TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER, null);

        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_RESOURCES, new File(
                TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());

        FileManager.rm(TEST_FILE_DEST_RESOURCES, TEST_FILE_MANAGER);

        assertFalse("Resource " + TEST_FILE_MANAGER + " yet in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyRemoveFilterFile() throws Exception
    {
        FileManager.cp(TEST_FILE_RESOURCES,
                 TEST_FILE_DEST_RESOURCES, "file.*\\.(xml|txt)");

        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_RESOURCES, new File(
                TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
        assertTrue("Resource " + TEST_FILE_MANAGER_XML + " not found in " + TEST_FILE_RESOURCES, new File(
                TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER_XML).exists());

        System.out.println("testCopyRemoveFilterFile: " + FileManager.ls(TEST_FILE_DEST_RESOURCES, null));

        FileManager.rm(TEST_FILE_DEST_RESOURCES, ".*\\.xml");

        System.out.println("testCopyRemoveFilterFile: " + FileManager.ls(TEST_FILE_DEST_RESOURCES, null));

        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_RESOURCES, new File(
                TEST_FILE_RESOURCES + File.separator + TEST_FILE_MANAGER).exists());
        assertFalse("Resource " + TEST_FILE_MANAGER_XML + " yet in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_MANAGER_XML).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyDirectory() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_DIR + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR).isDirectory());

        FileManager.cp(TEST_FILE_RESOURCES,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR, null);

        FileManager.cp(TEST_FILE_RESOURCES,
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR, null);

        assertTrue("Resource " + TEST_FILE_DIR + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR).isDirectory());
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR + File.separator + TEST_FILE_MANAGER).exists());
    }

    /**
     * @throws Exception
     */
    public void testCopyMoveDirectory() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_DIR_RENAMED + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED).isDirectory());

        FileManager.cp(TEST_FILE_RESOURCES,
                 TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR, null);

        FileManager.mv(TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR,
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED, null);

        assertFalse("Resource " + TEST_FILE_DIR + " ALREADY found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR).isDirectory());
        assertTrue("Resource " + TEST_FILE_DIR_RENAMED + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED).isDirectory());
        assertTrue("Resource " + TEST_FILE_MANAGER + " not found in " + TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR_RENAMED + File.separator + TEST_FILE_MANAGER).exists());
    }
}
