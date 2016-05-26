/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.util.file.change;

import it.greenvulcano.event.EventHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version 3.3.0 05/lug/2012
 * @author GreenVulcano Developer Team
 */
public class FileChangeMonitor
{
    /**
     * FileChange events source.
     */
    public static final String             EVENT_SOURCE = "FileChangeMonitor";

    private static final FileChangeMonitor instance     = new FileChangeMonitor();

    private Timer                          timer;
    private Map<String, FileMonitorTask>   timerEntries;

    class FileMonitorTask extends TimerTask
    {
        String fileName;
        File   monitoredFile;
        long   lastModified;

        public FileMonitorTask(String fileName) throws FileNotFoundException
        {
            this.fileName = fileName;
            this.lastModified = 0;

            monitoredFile = new File(fileName);
            if (!monitoredFile.exists()) { // search in classpath
                URL fileURL = FileMonitorTask.class.getClassLoader().getResource(fileName);
                if (fileURL != null) {
                    monitoredFile = new File(fileURL.getFile());
                }
                else {
                    throw new FileNotFoundException("File Not Found: " + fileName);
                }
            }
            this.lastModified = monitoredFile.lastModified();
        }

        @Override
        public void run()
        {
            try {
                long currModified = monitoredFile.lastModified();
                if (lastModified != currModified) {
                    lastModified = currModified;
                    fireFileChangeEvent(this.fileName);
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    public static FileChangeMonitor getInstance()
    {
        return instance;
    }

    protected FileChangeMonitor()
    {
        timer = new Timer(true);
        timerEntries = new HashMap<String, FileMonitorTask>();
    }

    protected void fireFileChangeEvent(String fileName)
    {
        FileChangeEvent event = new FileChangeEvent(fileName);
        try {
            EventHandler.fireEvent("fileChanged", event);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a FileChangeEventListener.
     * 
     * @param listener
     */
    public static void addFileChangeListener(FileChangeEventListener listener)
    {
        try {
            EventHandler.addEventListener(listener, FileChangeEventListener.class, new FileChangeEventSelector(),
                    EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a FileChangeEventListener listening for events related to a single
     * particular file.
     * 
     * @param listener
     *        a <tt>FileChangeEventListener</tt> object
     * @param filename
     *        a <tt>String</tt> containing the name of a file whose changes must
     *        be notified to the given listener
     * @throws FileNotFoundException
     */
    public static void addFileChangeListener(FileChangeEventListener listener, String fileName)
            throws FileNotFoundException
    {
        try {
            getInstance().addFile(fileName);
            EventHandler.addEventListener(listener, FileChangeEventListener.class,
                    new FileChangeEventSelector(fileName), EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Add a FileChangeEventListener listening for events related to a
     * particular
     * set of files.
     * 
     * @param listener
     *        a <tt>FileChangeEventListener</tt> object
     * @param fileList
     *        a <tt>List</tt> of <tt>String</tt> s containing the name of files
     *        whose changes must be notified to the given listener
     * @throws FileNotFoundException
     */
    public static void addFileChangeListener(FileChangeEventListener listener, List<String> fileList)
            throws FileNotFoundException
    {
        try {
            FileChangeMonitor fcm = getInstance();
            for (String fileName : fileList) {
                fcm.addFile(fileName);
            }
            EventHandler.addEventListener(listener, FileChangeEventListener.class,
                    new FileChangeEventSelector(fileList), EVENT_SOURCE);
        }
        catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Remove a FileChangeEventListener
     * 
     * @param listener
     */
    public static void removeFileChangeListener(FileChangeEventListener listener)
    {
        EventHandler.removeEventListener(listener, FileChangeEventListener.class, EVENT_SOURCE);
    }

    /**
     * Remove a FileChangeEventListener listening for changes on a single file
     * 
     * @param listener
     *        a <tt>FileChangeEventListener</tt> object
     * @param filename
     *        a <tt>String</tt> containing the name of a file whose changes must
     *        be notified to the given listener
     */
    public static void removeFileChangeListener(FileChangeEventListener listener, String filename)
    {
        EventHandler.removeEventListener(listener, FileChangeEventListener.class,
                new FileChangeEventSelector(filename), EVENT_SOURCE);
    }

    /**
     * Remove a FileChangeEventListener listening for changes on a subset of
     * files
     * 
     * @param listener
     *        a <tt>FileChangeEventListener</tt> object
     * @param fileList
     *        a <tt>List</tt> of <tt>String</tt> s containing the name of files
     *        whose changes must be notified to the given listener
     */
    public static void removeFileChangeListener(FileChangeEventListener listener, List<String> fileList)
    {
        EventHandler.removeEventListener(listener, FileChangeEventListener.class,
                new FileChangeEventSelector(fileList), EVENT_SOURCE);
    }

    public void resetMonitor() {
        synchronized (timerEntries) {
            Iterator<Entry<String, FileMonitorTask>> tEntryIt = timerEntries.entrySet().iterator();
            while (tEntryIt.hasNext()) {
                Entry<String, FileMonitorTask> tEntry = tEntryIt.next();
                //String fileName = tEntry.getKey();
                FileMonitorTask task = tEntry.getValue();
                if (task != null) {
                    task.cancel();
                }
                tEntryIt.remove();
                EventHandler.removeAllEventListener(FileChangeEventListener.class, EVENT_SOURCE);
            }
        }
    }

    /**
     * Add a monitored file.
     * 
     * @param fileName
     *        name of the file to monitor.
     */
    private void addFile(String fileName) throws FileNotFoundException
    {
        FileMonitorTask task = timerEntries.get(fileName);
        if (task == null) {
            synchronized (timerEntries) {
                task = timerEntries.get(fileName);
                if (task == null) {
                    task = new FileMonitorTask(fileName);
                    timerEntries.put(fileName, task);
                    timer.schedule(task, 5000, 5000);
                }
            }
        }
    }
}
