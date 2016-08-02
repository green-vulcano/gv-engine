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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.file.monitor.AnalysisReport;
import it.greenvulcano.util.file.monitor.FileSystemMonitor;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tests.unit.BaseTestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.w3c.dom.Node;

/**
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public class FileSystemMonitorTestCase extends BaseTestCase
{
    private static final String TEST_FILE_DIR_MAIN       = "TESTFS";
    private static final String TEST_FILE_DIR            = "TestSearch";

    private static final String TEST_FILE_RESOURCES      = System.getProperty("user.dir") + File.separator
                                                                 + "target"+ File.separator+ "test-classes" + File.separator
                                                                 + TEST_FILE_DIR_MAIN;
    private static final String TEST_FILE_DEST_RESOURCES = System.getProperty("java.io.tmpdir") + File.separator
                                                                 + TEST_FILE_DIR_MAIN;
    private static final String TEST_FILE_STATUS         = TEST_FILE_DEST_RESOURCES + File.separator + "dirScan.state";
    private static final String TEST_FILE_STATUS_AA      = TEST_FILE_DEST_RESOURCES + File.separator
                                                                 + "dirScan_TestSearch_AA.state";
    private static final String TEST_FILE_STATUS_BB      = TEST_FILE_DEST_RESOURCES + File.separator
                                                                 + "dirScan_TestSearch_BB.state";
    private static final String TEST_FP_FILE_STATUS      = TEST_FILE_DEST_RESOURCES + File.separator
                                                                 + "dirScan_002.state";
    private static final String TEST_FP_FILE_STATUS_AA   = TEST_FILE_DEST_RESOURCES + File.separator
                                                                 + "dirScan_TestSearch_AA_002.state";
    private static final String TEST_FP_FILE_STATUS_BB   = TEST_FILE_DEST_RESOURCES + File.separator
                                                                 + "dirScan_TestSearch_BB_002.state";


    private static final String TEST_FILE_CHANGED        = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "001.txt";
    private static final String TEST_FILE_DELETED        = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "002.txt";
    private static final String TEST_FILE_CREATED        = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "999.txt";
    private static final String TEST_FILE_DELETED_2      = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "999.txt";
    private static final String TEST_FILE_CREATED_2      = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "999_2.txt";

    private static final String TEST_FP_FILE_CHANGED     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "002.txt";
    private static final String TEST_FP_FILE_DELETED     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "001.txt";
    private static final String TEST_FP_FILE_CREATED     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "002_1.txt";
    private static final String TEST_FP_FILE_DELETED_2   = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "002_1.txt";
    private static final String TEST_FP_FILE_CREATED_2   = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + File.separator + "002_2.txt";

    private static final String TEST_FILE_CHANGED_AA     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_AA" + File.separator + "001.txt";
    private static final String TEST_FILE_DELETED_AA     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_AA" + File.separator + "002.txt";
    private static final String TEST_FP_FILE_CHANGED_AA  = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_AA" + File.separator + "002.txt";
    private static final String TEST_FP_FILE_CREATED_AA  = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_AA" + File.separator + "002_1.txt";

    private static final String TEST_FILE_CHANGED_BB     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_BB" + File.separator + "002.txt";
    private static final String TEST_FILE_CREATED_BB     = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_BB" + File.separator + "999.txt";
    private static final String TEST_FP_FILE_CHANGED_BB  = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_BB" + File.separator + "002.txt";
    private static final String TEST_FP_FILE_CREATED_BB  = TEST_FILE_DEST_RESOURCES + File.separator + TEST_FILE_DIR
                                                                 + "_BB" + File.separator + "002_1.txt";

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
 
        FileUtils.deleteQuietly(new File(TEST_FILE_DEST_RESOURCES));
        FileUtils.copyDirectory(new File(TEST_FILE_RESOURCES), new File(TEST_FILE_DEST_RESOURCES));
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
    @Test
    public void testAnalyze() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_STATUS + " ALREADY found", new File(TEST_FILE_STATUS).exists());
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScan']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS + " not found", new File(TEST_FILE_STATUS).exists());
        assertTrue("Existing Files invalid", rep.getExistingFilesCount() == 5);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeMultiOper() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_STATUS + " ALREADY found", new File(TEST_FILE_STATUS).exists());
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScan']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS + " not found", new File(TEST_FILE_STATUS).exists());
        assertTrue("1) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FILE_CREATED);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED));

        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS + " not found", new File(TEST_FILE_STATUS).exists());
        assertTrue("2) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("2) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);

        Thread.sleep(1000);

        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS + " not found", new File(TEST_FILE_STATUS).exists());
        assertTrue("3) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("3) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("3) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("3) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FILE_CREATED_2);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED_2));

        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS + " not found", new File(TEST_FILE_STATUS).exists());
        assertTrue("4) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("4) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("4) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("4) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeMultiDir() throws Exception
    {
        assertFalse("Resource " + TEST_FILE_STATUS_AA + " ALREADY found", new File(TEST_FILE_STATUS_AA).exists());
        assertFalse("Resource " + TEST_FILE_STATUS_BB + " ALREADY found", new File(TEST_FILE_STATUS_BB).exists());
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScanMulti']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS_AA + " not found", new File(TEST_FILE_STATUS_AA).exists());
        assertTrue("1a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS_BB + " not found", new File(TEST_FILE_STATUS_BB).exists());
        assertTrue("1b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED_AA);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED_AA));

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS_AA + " not found", new File(TEST_FILE_STATUS_AA).exists());
        assertTrue("2a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 4);
        assertTrue("2a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("2a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED_BB);
        TextUtils.writeFile("new file", TEST_FILE_CREATED_BB);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FILE_STATUS_BB + " not found", new File(TEST_FILE_STATUS_BB).exists());
        assertTrue("2b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 6);
        assertTrue("2b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeFilterProps() throws Exception
    {
        Map<String, String> optProperties = new HashMap<String, String>();
        optProperties.put("FILTER", "002");

        assertFalse("Resource " + TEST_FP_FILE_STATUS + " ALREADY found", new File(TEST_FP_FILE_STATUS).exists());
        Node n = XMLConfig.getNode("GVSystems.xml",
                "//fsmonitor-call[@name='dirScanFilterProps']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS + " not found", new File(TEST_FP_FILE_STATUS).exists());
        assertTrue("Existing Files invalid", rep.getExistingFilesCount() == 2);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeFilterPropsMultiOper() throws Exception
    {
        Map<String, String> optProperties = new HashMap<String, String>();
        optProperties.put("FILTER", "002");

        assertFalse("Resource " + TEST_FP_FILE_STATUS + " ALREADY found", new File(TEST_FP_FILE_STATUS).exists());

        Node n = XMLConfig.getNode("GVSystems.xml",
                "//fsmonitor-call[@name='dirScanFilterProps']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS + " not found", new File(TEST_FP_FILE_STATUS).exists());
        assertTrue("1) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 2);
        assertTrue("1) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FP_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FP_FILE_CREATED);
        FileUtils.deleteQuietly(new File(TEST_FP_FILE_DELETED));

        rep = fsm.analyze(optProperties);

        assertTrue("2) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 3);
        assertTrue("2) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);

        rep = fsm.analyze(optProperties);

        assertTrue("3) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 3);
        assertTrue("3) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("3) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("3) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FP_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FP_FILE_CREATED_2);
        FileUtils.deleteQuietly(new File(TEST_FP_FILE_DELETED_2));

        rep = fsm.analyze(optProperties);

        assertTrue("4) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 3);
        assertTrue("4) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("4) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("4) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeFilterPropsMultiDir() throws Exception
    {
        assertFalse("Resource " + TEST_FP_FILE_STATUS_AA + " ALREADY found", new File(TEST_FP_FILE_STATUS_AA).exists());
        assertFalse("Resource " + TEST_FP_FILE_STATUS_BB + " ALREADY found", new File(TEST_FP_FILE_STATUS_BB).exists());
        Map<String, String> optProperties = new HashMap<String, String>();
        optProperties.put("FILTER", "002");

        Node n = XMLConfig.getNode("GVSystems.xml",
                "//fsmonitor-call[@name='dirScanFilterPropsMulti']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS_AA + " not found", new File(TEST_FP_FILE_STATUS_AA).exists());
        assertTrue("1a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 2);
        assertTrue("1a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS_BB + " not found", new File(TEST_FP_FILE_STATUS_BB).exists());
        assertTrue("1b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 2);
        assertTrue("1b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FP_FILE_CHANGED_AA);
        TextUtils.writeFile("new file", TEST_FP_FILE_CREATED_AA);

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS_AA + " not found", new File(TEST_FP_FILE_STATUS_AA).exists());
        assertTrue("2a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 3);
        assertTrue("2a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FP_FILE_CHANGED_BB);
        TextUtils.writeFile("new file", TEST_FP_FILE_CREATED_BB);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("Resource " + TEST_FP_FILE_STATUS_BB + " not found", new File(TEST_FP_FILE_STATUS_BB).exists());
        assertTrue("2b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 3);
        assertTrue("2b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeMem() throws Exception
    {
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScanMem']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("Existing Files invalid", rep.getExistingFilesCount() == 5);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeMemMultiOper() throws Exception
    {
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScanMem']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("1) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FILE_CREATED);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED));

        rep = fsm.analyze(optProperties);

        assertTrue("2) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("2) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);

        Thread.sleep(1000);

        rep = fsm.analyze(optProperties);

        assertTrue("3) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("3) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("3) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("3) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED);
        TextUtils.writeFile("new file", TEST_FILE_CREATED_2);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED_2));

        rep = fsm.analyze(optProperties);

        assertTrue("4) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("4) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("4) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("4) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testAnalyzeMemMultiDir() throws Exception
    {
        Map<String, String> optProperties = new HashMap<String, String>();

        Node n = XMLConfig.getNode("GVSystems.xml", "//fsmonitor-call[@name='dirScanMemMulti']/LocalFileSystemMonitor");
        FileSystemMonitor fsm = (FileSystemMonitor) Class.forName(XMLConfig.get(n, "@class")).newInstance();
        fsm.init(n);

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        AnalysisReport rep = fsm.analyze(optProperties);

        assertTrue("1a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("1b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 5);
        assertTrue("1b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 0);
        assertTrue("1b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("1b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED_AA);
        FileUtils.deleteQuietly(new File(TEST_FILE_DELETED_AA));

        optProperties.put("PATH", TEST_FILE_DIR + "_AA");
        rep = fsm.analyze(optProperties);

        assertTrue("2a) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 4);
        assertTrue("2a) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2a) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 0);
        assertTrue("2a) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 1);

        Thread.sleep(1000);
        TextUtils.writeFile("new content", TEST_FILE_CHANGED_BB);
        TextUtils.writeFile("new file", TEST_FILE_CREATED_BB);

        optProperties.put("PATH", TEST_FILE_DIR + "_BB");
        rep = fsm.analyze(optProperties);

        assertTrue("2b) Existing Files invalid [" + rep.getExistingFilesCount() + "]", rep.getExistingFilesCount() == 6);
        assertTrue("2b) Modified Files invalid [" + rep.getModifiedFilesCount() + "]", rep.getModifiedFilesCount() == 1);
        assertTrue("2b) Created  Files invalid [" + rep.getCreatedFilesCount() + "]", rep.getCreatedFilesCount() == 1);
        assertTrue("2b) Deleted  Files invalid [" + rep.getDeletedFilesCount() + "]", rep.getDeletedFilesCount() == 0);
    }

}
