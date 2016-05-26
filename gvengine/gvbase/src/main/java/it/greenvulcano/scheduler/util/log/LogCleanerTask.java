/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
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
package it.greenvulcano.scheduler.util.log;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;


/**
 * @version 3.4.0 03/06/2013
 * @author GreenVulcano Developer Team
 */
public class LogCleanerTask extends Task
{
	private static Logger     logger  = org.slf4j.LoggerFactory.getLogger(LogCleanerTask.class);
    private static final String MODE_GZIP         = "gz";
    private static final String MODE_ZIP          = "zip";
    private static final String FILTER_ZIP        = "(.*\\.log$)|(.*\\.log\\.\\d*$)|(.*\\.log\\.\\d{4}-\\d{2}-\\d{2}$)";
    private static final String FILTER_DELETE     = "(.*\\.log$)|(.*\\.log\\.\\d*$)|(.*\\.log.*\\.zip$)|(.*\\.log.*\\.gz$)";
    private static final int    OLDER_THAN_ZIP    = 7;
    private static final int    OLDER_THAN_DELETE = 14;

    private int                 zipOlderThan      = OLDER_THAN_ZIP;
    private int                 deleteOlderThan   = OLDER_THAN_DELETE;
    private String              logBaseDir        = "sp{{gv.app.local}}/log";
    private String              zipMode           = MODE_ZIP;
    private String              zipFilter         = FILTER_ZIP;
    private String              deleteFilter      = FILTER_DELETE;

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.scheduler.Task#getLogger()
     */
    /**
     * @return
     */
    @Override
    protected Logger getLogger() {
        return logger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.scheduler.Task#initTask(org.w3c.dom.Node)
     */
    @Override
    protected void initTask(Node node) throws TaskException {
        zipOlderThan = XMLConfig.getInteger(node, "@zip-older-than", OLDER_THAN_ZIP);
        deleteOlderThan = XMLConfig.getInteger(node, "@delete-older-than", OLDER_THAN_DELETE);
        logBaseDir = XMLConfig.get(node, "@log-directory", "sp{{gv.app.home}}/log");
        zipMode = XMLConfig.get(node, "@zip-mode", MODE_ZIP);
        zipFilter = XMLConfig.get(node, "@zip-filter", FILTER_ZIP);
        deleteFilter = XMLConfig.get(node, "@delete-filter", FILTER_DELETE);
        logger.debug("Log directory: " + logBaseDir);
        if (zipOlderThan > 0) {
            zipMode = MODE_GZIP.equals(zipMode) ? MODE_GZIP : MODE_ZIP;
            logger.debug("Zip log files older than " + zipOlderThan + " days - mode: " + zipMode + " - filter: "
                    + zipFilter);
        }
        if (deleteOlderThan > 0) {
            logger.debug("Delete log files older than " + deleteOlderThan + " days - filter: " + deleteFilter);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.scheduler.Task#executeTask(java.lang.String, Date,
     * java.util.Map<java.lang.String, java.lang.String>, booolean)
     */
    @Override
    protected boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast) {
        logger.debug("BEGIN (" + getFullName() + ") - properties: " + locProperties);
        try {
            String logDir = PropertiesHandler.expand(logBaseDir, MapUtils.convertToHMStringObject(locProperties));

            if (zipOlderThan > 0) {
                Set<String> logs = listFiles(logDir, zipFilter,
                        DateUtils.addTime(Calendar.DAY_OF_MONTH, -(zipOlderThan - 1), true).getTime());
                if (!logs.isEmpty()) {
                    for (String log : logs) {
                        logger.debug("Compressing the file " + log);
                        zip(log);
                    }
                }
            }

            if (deleteOlderThan > 0) {
                Set<String> logs = listFiles(logDir, deleteFilter,
                        DateUtils.addTime(Calendar.DAY_OF_MONTH, -(deleteOlderThan - 1), true).getTime());
                if (!logs.isEmpty()) {
                    for (String log : logs) {
                        logger.debug("Deleting the file " + log);
                        delete(log);
                    }
                }
            }
            
            return true;
        }
        catch (Exception exc) {
            logger.error("Error cleaning logs", exc);
            return false;
        }
        finally {
            logger.debug("END (" + getFullName() + ")");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.scheduler.Task#destroyTask()
     */
    @Override
    protected void destroyTask() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.greenvulcano.scheduler.Task#sendHeartBeat()
     */
    @Override
    protected boolean sendHeartBeat() {
        return true;
    }

    private Set<String> listFiles(String logDir, String filePattern, long timestamp) throws Exception {
        File targetDir = new File(logDir);
        if (!targetDir.isAbsolute()) {
            throw new IllegalArgumentException("The pathname of the log directory is NOT absolute: " + logDir);
        }
        if (!targetDir.exists()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") NOT found on local file system.");
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Log directory (" + targetDir.getAbsolutePath()
                    + ") is NOT a directory.");
        }
        File[] files = targetDir.listFiles(new RegExFileFilter(filePattern, RegExFileFilter.FILES_ONLY, timestamp,
                false));
        Set<String> matchingFiles = new HashSet<String>(files.length);
        for (File file : files) {
            matchingFiles.add(file.getAbsolutePath());
        }
        return matchingFiles;
    }

    private void zip(String logFile) {
        InputStream in = null;
        OutputStream out = null;
        File inFile = new File(logFile);
        File outFile = new File(logFile);

        try {
            try {
                in = new FileInputStream(logFile);
                if (zipMode.equals(MODE_GZIP)) {
                    outFile = new File(logFile + "." + MODE_GZIP);
                    out = new GZIPOutputStream(new FileOutputStream(outFile));
                }
                else {
                    outFile = new File(logFile + "." + MODE_ZIP);
                    out = new ZipOutputStream(new FileOutputStream(outFile));
                    ZipEntry ze = new ZipEntry(inFile.getName());
                    ze.setTime(inFile.lastModified());
                    ((ZipOutputStream) out).putNextEntry(ze);
                }
                IOUtils.copy(in, out);
                out.flush();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception exc) {
                        // do nothing
                    }
                }
            }

            outFile.setLastModified(inFile.lastModified());

            FileUtils.deleteQuietly(inFile);
        }
        catch (Exception exc) {
            logger.error("Error trying to compress the file " + logFile, exc);
        }
    }

    private void delete(String logFile) {
        try {
            FileUtils.deleteQuietly(new File(logFile));
        }
        catch (Exception exc) {
            logger.error("Error trying to delete the file " + logFile, exc);
        }
    }
}
