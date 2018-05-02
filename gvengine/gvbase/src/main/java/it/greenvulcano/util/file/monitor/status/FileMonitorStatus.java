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
package it.greenvulcano.util.file.monitor.status;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.FileTimeSorter;
import it.greenvulcano.util.file.monitor.MonitorException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.txt.TextUtils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Hold the <code>FileSystemStatus</code> on a text file in the local file
 * system.
 * The file format is:
 * 
 * <pre>
 * row   description
 * 1     monitor info key
 * 2     last analisys timestamp
 * 3     file info 'filename::size::lastmodified::isDirectory::canRead::canWrite::canExecute'
 * ...   ...
 * N     file info
 * </pre>
 * 
 * If the provided <code>monitorInfo</code> don't match those read from the
 * file, the <code>loadStatus()</code> method must throw an exception.
 * 
 * @version 3.0.0 12/giu/2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class FileMonitorStatus implements FileSystemStatus
{
    private static final Logger logger             = org.slf4j.LoggerFactory.getLogger(FileMonitorStatus.class);

    private static String       PID                = ManagementFactory.getRuntimeMXBean().getName();

    private String              monitorInfo;
    private String              statusFilePath;
    private boolean             errorOnInvalidFile = true;
    private boolean             initialized        = false;

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#init(org.w3c.dom.Node,
     *      java.lang.String)
     */
    @Override
    public void init(Node node, String monitorInfo) throws MonitorException
    {
        try {
            this.monitorInfo = monitorInfo;
            statusFilePath = XMLConfig.get(node, "@statusFilePath", "");
            if (statusFilePath.length() == 0) {
                throw new MonitorException("Empty statusFilePath");
            }
            errorOnInvalidFile = XMLConfig.getBoolean(node, "@errorOnInvalidFile", true);
            logger.debug("FileMonitorStatus monitorInfo        : " + monitorInfo);
            logger.debug("FileMonitorStatus statusFilePath     : " + statusFilePath);
            logger.debug("FileMonitorStatus errorOnInvalidFile : " + errorOnInvalidFile);
            initialized = true;
        }
        catch (Exception exc) {
            logger.error("Error initializing FileMonitorStatus", exc);
            throw new MonitorException("Error initializing FileMonitorStatus", exc);
        }

    }

    /**
     * Waith max 5 s. to obtain the lock.
     */
    @Override
    public void lock(Map<String, String> optProperties) throws MonitorException
    {
        String lockFile = "";
        try {
            lockFile = getFilePath(optProperties) + ".lock";
            File lkf = new File(lockFile);
            for (int i = 0; i < 10; i++) {
                if (lkf.exists()) {
                    Thread.sleep(500);
                }
                else {
                    TextUtils.writeFile(PID, lkf);
                    return;
                }
            }
            throw new MonitorException("Error obtaining file lock on [" + lockFile + "]");
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error obtaining file lock on [" + lockFile + "]", exc);
        }
    }

    @Override
    public void unlock(Map<String, String> optProperties) throws MonitorException
    {
        String lockFile = "";
        try {
            lockFile = getFilePath(optProperties) + ".lock";
            File lkf = new File(lockFile);
            if (lkf.exists()) {
                String pid = TextUtils.readFile(lkf);
                if (PID.equals(pid)) {
                    FileUtils.deleteQuietly(lkf);
                }
                else {
                    throw new MonitorException("Error removing file lock on [" + lockFile
                            + "], created by a different process [" + pid + "]");
                }
            }
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Error removing file lock on [" + lockFile + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#loadStatus(Map)
     */
    @Override
    public StatusInfo loadStatus(Map<String, String> optProperties) throws MonitorException
    {
        if (!initialized) {
            throw new MonitorException("FileMonitorStatus not initialized");
        }
        String currStatusFile = "";
        try {
            currStatusFile = getFilePath(optProperties);
            String currMonitorInfo = getMonitorInfo(optProperties);

            File sfPath = new File(currStatusFile);
            File path = sfPath.getParentFile();
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!sfPath.exists()) {
                return null;
            }
            List<String> lines = TextUtils.readFileAsLines(currStatusFile);
            if (lines.size() < 2) {
                if (errorOnInvalidFile) {
                    throw new MonitorException("Invalid monitor status file [" + currStatusFile
                            + "]: missing monitorInfo");
                }
                logger.warn("Invalid monitor status file [" + currStatusFile + "]: missing monitorInfo");
                return null;
            }
            String monitorInfoLocal = lines.get(0);
            if (!currMonitorInfo.equals(monitorInfoLocal)) {
                logger.warn("MonitorInfo: " + currMonitorInfo + " - MonitorInfo in file [" + currStatusFile + "]: "
                        + monitorInfoLocal);
                if (errorOnInvalidFile) {
                    throw new MonitorException("Invalid monitor status file [" + currStatusFile
                            + "]: monitorInfo invalid");
                }
                return null;
            }

            long analysisTimestamp = Long.valueOf(lines.get(1)).longValue();

            Set<FileProperties> fileSet = new HashSet<FileProperties>();
            int size = lines.size();
            for (int i = 2; i < size; i++) {
                fileSet.add(FileProperties.parse(lines.get(i).trim()));
            }

            return new StatusInfo(fileSet, analysisTimestamp);
        }
        catch (Exception exc) {
            logger.error("Error reading status for FileMonitorStatus [" + currStatusFile + "]", exc);
            throw new MonitorException("Error reading status for FileMonitorStatus [" + currStatusFile + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#saveStatus(Set,
     *      long, Map)
     */
    @Override
    public void saveStatus(Set<FileProperties> fileSet, long analysisTimestamp, Map<String, String> optProperties)
            throws MonitorException
    {
        if (!initialized) {
            throw new MonitorException("FileMonitorStatus not initialized");
        }
        String currStatusFile = "";
        try {
            currStatusFile = getFilePath(optProperties);

            StringBuffer fileDump = new StringBuffer(getMonitorInfo(optProperties)).append("\n");
            fileDump.append(analysisTimestamp).append("\n");

            List<FileProperties> fileList = new ArrayList<FileProperties>(fileSet);
            Collections.sort(fileList, new FileTimeSorter(false));

            for (FileProperties currFile : fileList) {
                fileDump.append(currFile.serialize()).append("\n");
            }

            TextUtils.writeFile(fileDump, currStatusFile);
        }
        catch (Exception exc) {
            logger.error("Error writing status for FileMonitorStatus [" + currStatusFile + "]", exc);
            throw new MonitorException("Error writing status for FileMonitorStatus [" + currStatusFile + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#saveStatus(StatusInfo,
     *      Map)
     */
    @Override
    public void saveStatus(StatusInfo statusInfo, Map<String, String> optProperties) throws MonitorException
    {
        saveStatus(statusInfo.getStatusFileSet(), statusInfo.getAnalysisTimestamp(), optProperties);
    }

    private String getFilePath(Map<String, String> optProperties) throws Exception
    {
        return PropertiesHandler.expand(statusFilePath, MapUtils.convertToHMStringObject(optProperties));
    }

    private String getMonitorInfo(Map<String, String> optProperties) throws Exception
    {
        return PropertiesHandler.expand(monitorInfo, MapUtils.convertToHMStringObject(optProperties));
    }
}
