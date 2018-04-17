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
import it.greenvulcano.util.file.FileNameSorter;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.monitor.MonitorException;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * Hold the <code>FileSystemStatus</code> in memory. The contained information
 * NOT SURVIVE to application restart.
 * 
 * @version 3.0.0 07/giu/2010
 * @author GreenVulcano Developer Team
 */
public class InMemoryMonitorStatus implements FileSystemStatus
{
    private static final Logger            logger          = org.slf4j.LoggerFactory.getLogger(InMemoryMonitorStatus.class);
    private boolean                        dumpStatus;
    private String                         monitorInfo;
    private static Map<String, StatusInfo> statusInfoCache = new HashMap<String, StatusInfo>();
    private static Lock                    semaphore       = new ReentrantLock(true);

    /**
     *
     */
    public InMemoryMonitorStatus()
    {
        // do nothing
    }

    @Override
    public void lock(Map<String, String> optProperties) throws MonitorException
    {
        semaphore.lock();
    }

    @Override
    public void unlock(Map<String, String> optProperties) throws MonitorException
    {
        semaphore.unlock();
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#init(org.w3c.dom.Node,
     *      java.lang.String)
     */
    @Override
    public void init(Node node, String monitorInfo) throws MonitorException
    {
        this.monitorInfo = monitorInfo;
        try {
            dumpStatus = XMLConfig.getBoolean(node, "@dumpStatus", true);
        }
        catch (Exception exc) {
            throw new MonitorException("Generic error.", exc);
        }
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#loadStatus(Map)
     */
    @Override
    public StatusInfo loadStatus(Map<String, String> optProperties) throws MonitorException
    {
        String currMonitorInfo = monitorInfo;
        try {
            currMonitorInfo = getMonitorInfo(optProperties);
            // the first 'loaded status' is always invalid
            return statusInfoCache.get(currMonitorInfo);
        }
        catch (Exception exc) {
            logger.error("Error reading status for InMemoryMonitorStatus [" + currMonitorInfo + "]", exc);
            throw new MonitorException("Error reading status for InMemoryMonitorStatus [" + currMonitorInfo + "]", exc);
        }
    }

    /**
     * @see it.greenvulcano.util.file.monitor.status.FileSystemStatus#saveStatus(Map)
     */
    @Override
    public void saveStatus(Set<FileProperties> fileSet, long analysisTimestamp, Map<String, String> optProperties)
            throws MonitorException
    {
        String currMonitorInfo = monitorInfo;
        try {
            currMonitorInfo = getMonitorInfo(optProperties);
            statusInfoCache.put(currMonitorInfo, new StatusInfo(fileSet, analysisTimestamp));

            if (dumpStatus && logger.isDebugEnabled()) {
                StringBuilder fileDump = new StringBuilder("File list:\n");
                List<FileProperties> fileList = new ArrayList<FileProperties>(fileSet);
                Collections.sort(fileList, new FileNameSorter(true));

                for (FileProperties currFile : fileList) {
                    fileDump.append(currFile).append("\n");
                }
                logger.debug("BEGIN InMemoryMonitorStatus [");
                logger.debug("monitorInfo: " + currMonitorInfo);
                logger.debug(fileDump.toString());
                logger.debug("END   InMemoryMonitorStatus [");
            }
        }
        catch (Exception exc) {
            logger.error("Error writing status for InMemoryMonitorStatus [" + currMonitorInfo + "]", exc);
            throw new MonitorException("Error writing status for InMemoryMonitorStatus [" + currMonitorInfo + "]", exc);
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

    private String getMonitorInfo(Map<String, String> optProperties) throws Exception
    {
        return PropertiesHandler.expand(monitorInfo, MapUtils.convertToHMStringObject(optProperties));
    }
}
