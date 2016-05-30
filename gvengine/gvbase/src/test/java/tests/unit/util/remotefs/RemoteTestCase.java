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
package tests.unit.util.remotefs;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.bin.BinaryUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tests.unit.BaseTestCase;

import org.apache.commons.io.FileUtils;
import org.mockftpserver.fake.FakeFtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class RemoteTestCase extends BaseTestCase
{

    private static final String TEST_FILE_DEST_RESOURCES = System.getProperty("java.io.tmpdir") + File.separator
                                                                 + "TestFTP";

    private FakeFtpServer       fakeFtpServer;
    private RemoteManager       ra                       = null;
    private Map<String, String> optProperties            = new HashMap<String, String>();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        System.setProperty("gv.app.home", BASE_DIR);
        
        File destDir = new File(TEST_FILE_DEST_RESOURCES);
        destDir.mkdirs();
        BinaryUtils.writeBytesToFile("123456789012345".getBytes(), new File(TEST_FILE_DEST_RESOURCES, "TestX.txt"));

        Node node = XMLConfig.getNode("GVCore.xml",
                "//remotemanager-call[@name='testConnectOnly']/*[@type='remote-manager']");
        ra = (RemoteManager) Class.forName(XMLConfig.get(node, "@class")).newInstance();
        ra.init(node);

        ApplicationContext context = new ClassPathXmlApplicationContext("fakeFTP.xml");
        fakeFtpServer = (FakeFtpServer) context.getBean("FakeFtpServer");
        fakeFtpServer.start();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        if (fakeFtpServer != null) {
            fakeFtpServer.stop();
        }
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testExistFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", ".*", null, RegExFileFilter.ALL, optProperties);
            System.out.println("testExistFile : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 5));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testExistXMLFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", ".*\\.xml", null, RegExFileFilter.ALL, optProperties);
            System.out.println("testExistXMLFile : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 2));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testExistDir() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", ".*", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            System.out.println("testExistDir : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testDownloadFile() throws Exception
    {
        try {
            ra.connect();
            ra.get(".", "Test0.txt", TEST_FILE_DEST_RESOURCES, "Test0.txt", optProperties);
            assertTrue("Resource Test0.txt not found in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                    + File.separator + "Test0.txt").exists());
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testDownloadDirectory() throws Exception
    {
        try {
            ra.connect();
            ra.getDir("dir0", TEST_FILE_DEST_RESOURCES, "dirX", optProperties);
            assertTrue("Resource dirX not found in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                    + File.separator + "dirX").exists());
            assertTrue("Resource dirX/Test1.txt not found in " + TEST_FILE_DEST_RESOURCES, new File(
                    TEST_FILE_DEST_RESOURCES + File.separator + "dirX" + File.separator + "Test1.txt").exists());
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testDownloadDirectoryAutoConn() throws Exception
    {
        ra.setAutoconnect(true);
        ra.getDir("dir0", TEST_FILE_DEST_RESOURCES, "dirX", optProperties);
        assertTrue("Resource dirX not found in " + TEST_FILE_DEST_RESOURCES, new File(TEST_FILE_DEST_RESOURCES
                + File.separator + "dirX").exists());
        assertTrue("Resource dirX/Test1.txt not found in " + TEST_FILE_DEST_RESOURCES, new File(
                TEST_FILE_DEST_RESOURCES + File.separator + "dirX" + File.separator + "Test1.txt").exists());
    }

    /**
     * @throws Exception
     */
    public void testUploadFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls("dir0", "Test2.txt", null, RegExFileFilter.FILES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.put(TEST_FILE_DEST_RESOURCES, "TestX.txt", ".", "Test2.txt", optProperties);
            files = ra.ls(".", "Test2.txt", null, RegExFileFilter.FILES_ONLY, optProperties);
            System.out.println("testUploadFile : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testUploadDirectory() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "dir2", null, RegExFileFilter.FILES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.putDir(TEST_FILE_DEST_RESOURCES, ".", "dir2", optProperties);
            files = ra.ls("dir2", ".*", null, RegExFileFilter.FILES_ONLY, optProperties);
            System.out.println("testUploadDirectory : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testUploadDirectoryAutoConn() throws Exception
    {
        ra.setAutoconnect(true);
        Set<FileProperties> files = ra.ls(".", "dir2", null, RegExFileFilter.FILES_ONLY, optProperties);
        assertTrue("Resource found", (files != null) && (files.size() == 0));

        ra.putDir(TEST_FILE_DEST_RESOURCES, ".", "dir2", optProperties);
        files = ra.ls("dir2", ".*", null, RegExFileFilter.FILES_ONLY, optProperties);
        System.out.println("testUploadDirectoryAutoConn : " + files);
        assertTrue("Resource not found", (files != null) && (files.size() == 1));
    }

    /**
     * @throws Exception
     */
    public void testDeleteFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", ".*\\.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            assertTrue("Resource not found", (files != null) && (files.size() == 2));

            ra.rm(".", ".*\\.xml", optProperties);
            files = ra.ls(".", ".*\\.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            System.out.println("testDeleteFile : " + files);
            assertTrue("Resource found", (files != null) && (files.size() == 0));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testDeleteDir() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "dir0", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));

            ra.rm(".", "dir0", optProperties);
            files = ra.ls(".", "dir0", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            System.out.println("testDeleteDir : " + files);
            assertTrue("Resource found", (files != null) && (files.size() == 0));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testDeleteDirAutoConn() throws Exception
    {
        ra.setAutoconnect(true);
        Set<FileProperties> files = ra.ls(".", "dir0", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
        assertTrue("Resource not found", (files != null) && (files.size() == 1));

        ra.rm(".", "dir0", optProperties);
        files = ra.ls(".", "dir0", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
        System.out.println("testDeleteDirAutoConn : " + files);
        assertTrue("Resource found", (files != null) && (files.size() == 0));
    }

    /**
     * @throws Exception
     */
    public void testDeleteAll() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", ".*", null, RegExFileFilter.ALL, optProperties);
            assertTrue("Resource not found", (files != null) && (files.size() == 5));

            ra.rm(".", ".*", optProperties);
            files = ra.ls(".", ".*", null, RegExFileFilter.ALL, optProperties);
            System.out.println("testDeleteAll : " + files);
            assertTrue("Resource found", (files != null) && (files.size() == 0));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testRenameFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "TestC.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.mv(".", "TestA.xml", "TestC.xml", optProperties);
            files = ra.ls(".", "TestC.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            System.out.println("testRenameFile : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testMoveFile() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "TestC.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.mv(".", "TestA.xml", "dir0/TestA.xml", optProperties);
            files = ra.ls("dir0", "TestA.xml", null, RegExFileFilter.FILES_ONLY, optProperties);
            System.out.println("testMoveFile : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testRenameDir() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.mv(".", "dir0", "dirX", optProperties);
            files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            System.out.println("testRenameDir : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testCreateDir() throws Exception
    {
        try {
            ra.connect();
            Set<FileProperties> files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            assertTrue("Resource found", (files != null) && (files.size() == 0));

            ra.mkdir(".", "dirX", optProperties);
            files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
            System.out.println("testCreateDir : " + files);
            assertTrue("Resource not found", (files != null) && (files.size() == 1));
        }
        finally {
            ra.disconnect();
        }
    }

    /**
     * @throws Exception
     */
    public void testCreateDirAutoConn() throws Exception
    {
        ra.setAutoconnect(true);
        Set<FileProperties> files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
        assertTrue("Resource found", (files != null) && (files.size() == 0));

        ra.mkdir(".", "dirX", optProperties);
        files = ra.ls(".", "dirX", null, RegExFileFilter.DIRECTORIES_ONLY, optProperties);
        System.out.println("testCreateDirAutoConn : " + files);
        assertTrue("Resource not found", (files != null) && (files.size() == 1));
    }
}
