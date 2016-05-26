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
package tests.unit.util.zip;

import it.greenvulcano.util.zip.ZipHelper;
import it.greenvulcano.util.zip.ZipHelperException;

import java.io.File;
import java.math.BigInteger;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

/**
 * @version 3.0.0 Mar 15, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class ZipHelperTestCase extends TestCase
{
    private static final String EMPTY_STRING             = "";
    private static final String ONE_CHAR_STRING          = BigInteger.valueOf(0x248).toString();
    private static final String TEST_STRING              = "test string to zip/unzip";
    private static final String OTHER_TEST_STRING        = "gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca gianluca ";

    private static final String TEST_FILE_RESOURCES      = System.getProperty("user.dir") + File.separator
                                                                 + "target" + File.separator + "test-classes";
    private static final String TEST_FILE_DEST_RESOURCES = System.getProperty("java.io.tmpdir") + File.separator
                                                                 + "TestZip";

    private static final String TEST_FILE_ZIP            = "testPack.zip";

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.forceMkdir(new File(TEST_FILE_DEST_RESOURCES));
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
     * @throws ZipHelperException
     *
     */
    public void testZipHelper() throws ZipHelperException
    {
        ZipHelper zipHelper = new ZipHelper();
        zipTester(zipHelper, EMPTY_STRING);
        zipTester(zipHelper, ONE_CHAR_STRING);
        zipTester(zipHelper, TEST_STRING);
        zipTester(zipHelper, OTHER_TEST_STRING);
    }

    /**
     * @throws Exception
     */
    public void testZipFile() throws Exception
    {
        ZipHelper zipHelper = new ZipHelper();
        zipHelper.zipFile(TEST_FILE_RESOURCES, ".*", TEST_FILE_DEST_RESOURCES, TEST_FILE_ZIP);
        assertTrue("Resource " + TEST_FILE_ZIP + " not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_ZIP).exists());
        zipHelper.unzipFile(TEST_FILE_DEST_RESOURCES, TEST_FILE_ZIP, TEST_FILE_DEST_RESOURCES);
        assertTrue("Resource GVCore.xml not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + "GVCore.xml").exists());
        assertTrue("Resource GVAdapters.xml not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + "GVAdapters.xml").exists());
        assertTrue("Resource identity.xsl not found in " + TEST_FILE_DEST_RESOURCES + File.separator + "gvdte" + 
                File.separator + "datasource" + File.separator + "xsl", 
                new File(TEST_FILE_DEST_RESOURCES + File.separator + "gvdte" + File.separator + "datasource" 
                + File.separator + "xsl" + File.separator + "identity.xsl").exists());
    }

    /**
     * @param zipHelper
     * @throws ZipHelperException
     */
    private void zipTester(ZipHelper zipHelper, String testString) throws ZipHelperException
    {
        byte[] input = testString.getBytes();
        byte[] zipped = zipHelper.zip(input);
        byte[] unzipped = zipHelper.unzip(zipped);
        assertEquals(testString, new String(unzipped));
    }
}
