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
package it.greenvulcano.util.file.monitor.status;

import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.monitor.FileSystemMonitor;
import it.greenvulcano.util.file.monitor.MonitorException;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * Implementors of this interface holds status information of
 * <code>{@link FileSystemMonitor#analyze()}</code> invocation. Informations
 * about created/deleted files is retrieved by comparing the list
 * of matching files currently within the target directory with a previous list
 * holded by a <code>FileSystemStatus</code> instance. The
 * <code>loadStatus()</code> method
 * must compare the information provided through the <code>monitorInfo</code>
 * parameter with the info
 * stored to ensure that the <code> FileSystemStatus</code> is configured
 * consistently to the <code> FileSystemMonitor</code>.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
public interface FileSystemStatus
{
    /**
     * Initialization method.
     * 
     * @param node
     * @param monitorInfo
     * @throws MonitorException
     */
    void init(Node node, String monitorInfo) throws MonitorException;

    /**
     * Acquire a Lock on the status info.
     * 
     * @throws MonitorException
     */
    void lock(Map<String, String> optProperties) throws MonitorException;

    /**
     * Release the Lock.
     * 
     * @throws MonitorException
     */
    void unlock(Map<String, String> optProperties) throws MonitorException;

    /**
     * Loads a backup status.
     * 
     * @return true if the loaded status isn't empty.
     * @throws MonitorException
     */
    StatusInfo loadStatus(Map<String, String> optProperties) throws MonitorException;

    /**
     * Save the status.
     * 
     * @param fileSet
     * @param analysisTimestamp
     * @throws MonitorException
     */
    void saveStatus(Set<FileProperties> fileSet, long analysisTimestamp, Map<String, String> optProperties)
            throws MonitorException;

    /**
     * Save the status.
     * 
     * @param statusInfo
     * @throws MonitorException
     */
    void saveStatus(StatusInfo statusInfo, Map<String, String> optProperties) throws MonitorException;
}
