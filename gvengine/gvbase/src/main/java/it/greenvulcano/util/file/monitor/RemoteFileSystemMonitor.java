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
package it.greenvulcano.util.file.monitor;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.file.monitor.status.FileSystemStatus;
import it.greenvulcano.util.file.monitor.status.StatusInfo;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.remotefs.RemoteManager;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * A call to the <code>{@link RemoteFileSystemMonitor#analyze()}</code> method
 * detects any created/modified/deleted file/directory matching a given filename
 * mask within a given target directory on a remote filesystem accessible via
 * FTP and returns a <code>{@link AnalysisReport}</code> object containing
 * analysis information.<br>
 * Informations about created/deleted files is retrieved by comparing the list
 * of matching files currently within the target directory with a previous list
 * (created during the last call to the
 * <code>{@link FileSystemMonitor#analyze()}</code> method) which is holded by a
 * <code>{@link FileSystemStatus}</code> instance.
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public class RemoteFileSystemMonitor extends LocalFileSystemMonitor
{
    private String           localAnalisysDir;
    private int              fileType;

    private String           filePattern   = null;

    /**
     * Hold the last analysis result.
     */
    private FileSystemStatus monitorStatus = null;

    /**
     * Interface APIs for FTP access to the remote directory.
     */
    private RemoteManager    manager;

    /**
     * Initialize the instance.
     * 
     * @param node
     * @throws MonitorException
     */
    @Override
    public void init(Node node) throws MonitorException
    {
        try {
            localAnalisysDir = XMLConfig.get(node, "@path");
            filePattern = XMLConfig.get(node, "FileFilter/@file-mask", "");
            fileType = RegExFileFilter.getFileType(XMLConfig.get(node, "FileFilter/@file-type", "files-only"));
            Node rf = XMLConfig.getNode(node, "ResultFilter");
            returnExisting = XMLConfig.getBoolean(rf, "@existing", false);
            returnCreated = XMLConfig.getBoolean(rf, "@created", false);
            returnModified = XMLConfig.getBoolean(rf, "@modified", false);
            returnDeleted = XMLConfig.getBoolean(rf, "@deleted", false);
            sortMode = XMLConfig.get(rf, "@sort-mode", "by-name");
            sortAscending = XMLConfig.getBoolean(rf, "@sort-ascending", true);

            if ((localAnalisysDir == null) || ("".equals(localAnalisysDir))) {
                throw new MonitorException("Analysis directory is null or empty");
            }

            Node nm = XMLConfig.getNode(node, "*[@type='remote-manager']");
            manager = (RemoteManager) Class.forName(XMLConfig.get(nm, "@class")).newInstance();
            manager.init(nm);

            Node fsStatus = XMLConfig.getNode(node, "*[@type='fs-monitor-status']");
            monitorStatus = (FileSystemStatus) Class.forName(XMLConfig.get(fsStatus, "@class")).newInstance();
            monitorStatus.init(fsStatus, "RemoteFileSystemMonitor#" + manager.getManagerKey() + "#" + localAnalisysDir
                    + "#" + filePattern + "||" + fileType);

            currentAnalysisFileSet = null;
            modifiedFileSet = null;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (XMLConfigException exc) {
            throw new MonitorException("XMLConfig error during initialization.", exc);
        }
        catch (Exception exc) {
            throw new MonitorException("Generic error during initialization.", exc);
        }
    }

    /**
     * This method performs the scan of the given target directory, checking if
     * any file/directory, matching the given file mask, was
     * created/modified/deleted since the previous call to this method. The
     * method returns a <code>{@link AnalysisReport}</code> object containing
     * info about all detected file events.<br>
     * If any file event is detected, a call to the
     * <code>{@link AnalysisReport#resultsAvailable()}</code> method of the
     * returned object will return <code>true</code>.<br>
     * If no file events are detected during the directory scan, a call to the
     * <code>{@link AnalysisReport#resultsAvailable()}</code> method of the
     * returned object will return <code>false</code>.
     * 
     * @return a <code>{@link AnalysisReport}</code> object containing the
     *         directory scan report<br>
     *         .
     * @throws MonitorException
     * @see it.greenvulcano.util.file.monitor.FileSystemMonitor#analyze(Map)
     */
    @Override
    public AnalysisReport analyze(Map<String, String> optProperties) throws MonitorException
    {
        String currAnalysisDir = localAnalisysDir;
        String currFilePattern = filePattern;
        monitorStatus.lock(optProperties);
        try {
            try {
                Map<String, Object> localProps = MapUtils.convertToHMStringObject(optProperties);
                currFilePattern = PropertiesHandler.expand(filePattern, localProps);
                currAnalysisDir = PropertiesHandler.expand(localAnalisysDir, localProps);

                initMonitorStatus(currAnalysisDir, currFilePattern, optProperties);

                manager.connect(optProperties);
                currentAnalysisFileSet = manager.ls(currAnalysisDir, currFilePattern, null, fileType, optProperties);
                modifiedFileSet = manager.ls(currAnalysisDir, currFilePattern, new Date(lastAnalysisTimestamp),
                        fileType, optProperties);
                AnalysisReport result = generateReport(new File(currAnalysisDir), currFilePattern + "||" + fileType);
                lastAnalysisTimestamp = manager.getRemoteTime();
                lastAnalysisFileSet = currentAnalysisFileSet;
                currentAnalysisFileSet = null;
                modifiedFileSet = null;
                monitorStatus.saveStatus(lastAnalysisFileSet, lastAnalysisTimestamp, optProperties);
                return result;
            }
            catch (MonitorException exc) {
                throw exc;
            }
            catch (Exception exc) {
                throw new MonitorException("Error while analysing remote directory [" + currAnalysisDir + "]/["
                        + currFilePattern + "]", exc);
            }
            finally {
                manager.disconnect(optProperties);
            }
        }
        finally {
            monitorStatus.unlock(optProperties);
        }
    }

    /**
     * Field <code>ftpAccess</code>.
     * 
     * @return the field <code>ftpAccess</code>.
     */
    public RemoteManager getFTPAccess()
    {
        return manager;
    }

    /**
     * Initializes the Monitor Status.
     * 
     * @param currAnalysisDir
     * @param currFilePattern
     * @param optProperties
     * @throws MonitorException
     */
    protected void initMonitorStatus(String currAnalysisDir, String currFilePattern, Map<String, String> optProperties)
            throws MonitorException
    {
        StatusInfo statusInfo = monitorStatus.loadStatus(optProperties);
        if (statusInfo != null) {
            lastAnalysisTimestamp = statusInfo.getAnalysisTimestamp();
            lastAnalysisFileSet = statusInfo.getStatusFileSet();
        }
        else {
            try {
                manager.connect(optProperties);
                lastAnalysisTimestamp = manager.getRemoteTime();
                lastAnalysisFileSet = manager.ls(currAnalysisDir, currFilePattern, null, fileType, optProperties);
            }
            catch (Exception exc) {
                throw new MonitorException("Error while initializing internal file lists", exc);
            }
            finally {
                manager.disconnect(optProperties);
            }
        }
    }
}
