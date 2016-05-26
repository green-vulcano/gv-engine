/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project. All rights
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
package tests.unit.util.file;

import it.greenvulcano.event.EventHandler;
import it.greenvulcano.event.util.DefaultEventListener;
import it.greenvulcano.event.util.debug.DebuggerEventListener;
import it.greenvulcano.util.file.change.FileChangeEvent;
import it.greenvulcano.util.file.change.FileChangeEventListener;
import it.greenvulcano.util.file.change.FileChangeMonitor;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * @version 3.3.0 05/lug/2012
 * @author GreenVulcano Developer Team
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileChangeMonitorTestCase extends TestCase
{
    private static final String TEST_FILE_DIR = System.getProperty("user.dir") + File.separator
                                                      + "target" + File.separator + "test-classes" + File.separator + "TestFileChange";
    private static final String TEST_FILE_1   = TEST_FILE_DIR + File.separator + "file_test_1.txt";
    private static final String TEST_FILE_2   = TEST_FILE_DIR + File.separator + "file_test_2.txt";
    private static final String TEST_FILE_3   = TEST_FILE_DIR + File.separator + "file_test_3.txt";

    public class TestListener implements FileChangeEventListener
    {
        private String  file;
        private boolean changed = false;

        public TestListener(String file)
        {
            this.file = file;
        }

        public boolean isChanged()
        {
            return this.changed;
        }

        public void resetChanged()
        {
            this.changed = false;
        }

        @Override
        public void fileChanged(FileChangeEvent event)
        {
            changed = (file == null) ? true : event.getFile().equals(file);

            System.out.println("TestListener(" + changed + ")[" + file + "] -> " + event);
        }
    }

    static {
        try {
            EventHandler.addEventListener(new DebuggerEventListener(), DefaultEventListener.class, null);
        }
        catch (NoSuchMethodException exc) {
            // do nothing
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        FileUtils.deleteQuietly(new File(TEST_FILE_DIR));
        FileUtils.forceMkdir(new File(TEST_FILE_DIR));
        assertTrue("System property 'it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath' not set.",
                System.getProperty("it.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath") != null);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        FileUtils.deleteQuietly(new File(TEST_FILE_DIR));
        FileChangeMonitor.getInstance().resetMonitor();
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testNoFile() throws Exception
    {
        System.out.println("---------------- File exists: " + (new File(TEST_FILE_1)).exists());
        try {
            TestListener tl = new TestListener(TEST_FILE_1);
            FileChangeMonitor.addFileChangeListener(tl, TEST_FILE_1);
            fail("File " + TEST_FILE_1 + " must NOT exists!!!");
        }
        catch (FileNotFoundException exc) {
            assertTrue("Resource " + TEST_FILE_1 + " not found", exc.getMessage().contains(TEST_FILE_1));
        }
    }

    /**
     * @throws Exception
     */
    public void testFileChange() throws Exception
    {
        TestListener tl = new TestListener(TEST_FILE_1);
        File f1 = new File(TEST_FILE_1);
        f1.createNewFile();

        FileChangeMonitor.addFileChangeListener(tl, TEST_FILE_1);
        assertFalse("Resource " + TEST_FILE_1 + " already modified", tl.isChanged());

        Thread.sleep(1000);
        f1.setLastModified(System.currentTimeMillis());
        Thread.sleep(6000);

        assertTrue("Resource " + TEST_FILE_1 + " modified", tl.isChanged());
    }

    /**
     * @throws Exception
     */
    public void testMultiFileChange() throws Exception
    {
        System.out.println("---- testMultiFileChange");
        TestListener tl0 = new TestListener(null);

        TestListener tl1 = new TestListener(TEST_FILE_1);
        File f1 = new File(TEST_FILE_1);
        f1.createNewFile();

        TestListener tl2 = new TestListener(TEST_FILE_2);
        File f2 = new File(TEST_FILE_2);
        f2.createNewFile();

        TestListener tl3 = new TestListener(TEST_FILE_3);
        File f3 = new File(TEST_FILE_3);
        f3.createNewFile();

        FileChangeMonitor.addFileChangeListener(tl0);
        FileChangeMonitor.addFileChangeListener(tl1, TEST_FILE_1);
        assertFalse("Resource " + TEST_FILE_1 + " already modified", tl1.isChanged());
        FileChangeMonitor.addFileChangeListener(tl2, TEST_FILE_2);
        assertFalse("Resource " + TEST_FILE_2 + " already modified", tl2.isChanged());
        FileChangeMonitor.addFileChangeListener(tl3, TEST_FILE_3);
        assertFalse("Resource " + TEST_FILE_3 + " already modified", tl3.isChanged());

        Thread.sleep(1000);
        f1.setLastModified(System.currentTimeMillis());
        Thread.sleep(6000);
        assertTrue("Resource " + TEST_FILE_1 + " modified", tl0.isChanged());
        assertTrue("Resource " + TEST_FILE_1 + " modified", tl1.isChanged());
        assertFalse("Resource " + TEST_FILE_2 + " NOT modified", tl2.isChanged());
        assertFalse("Resource " + TEST_FILE_3 + " NOT modified", tl3.isChanged());

        tl0.resetChanged();
        tl1.resetChanged();
        tl2.resetChanged();
        tl3.resetChanged();

        Thread.sleep(1000);
        f2.setLastModified(System.currentTimeMillis());
        Thread.sleep(6000);
        assertTrue("Resource " + TEST_FILE_2 + " modified", tl0.isChanged());
        assertFalse("Resource " + TEST_FILE_1 + " NOT modified", tl1.isChanged());
        assertTrue("Resource " + TEST_FILE_2 + " modified", tl2.isChanged());
        assertFalse("Resource " + TEST_FILE_3 + " NOT modified", tl3.isChanged());

        tl0.resetChanged();
        tl1.resetChanged();
        tl2.resetChanged();
        tl3.resetChanged();

        Thread.sleep(1000);
        f3.setLastModified(System.currentTimeMillis());
        Thread.sleep(6000);
        assertTrue("Resource " + TEST_FILE_3 + " modified", tl0.isChanged());
        assertFalse("Resource " + TEST_FILE_1 + " NOT modified", tl1.isChanged());
        assertFalse("Resource " + TEST_FILE_2 + " NOT modified", tl2.isChanged());
        assertTrue("Resource " + TEST_FILE_3 + " modified", tl3.isChanged());

        tl0.resetChanged();
        tl1.resetChanged();
        tl2.resetChanged();
        tl3.resetChanged();
        System.out.println("---- testMultiFileChange");
    }

}
