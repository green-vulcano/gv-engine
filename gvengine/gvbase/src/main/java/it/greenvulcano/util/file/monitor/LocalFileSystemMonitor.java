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
package it.greenvulcano.util.file.monitor;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.file.FileProperties;
import it.greenvulcano.util.file.RegExFileFilter;
import it.greenvulcano.util.file.monitor.status.FileSystemStatus;
import it.greenvulcano.util.file.monitor.status.StatusInfo;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.w3c.dom.Node;

/**
 * A call to the <code>{@link LocalFileSystemMonitor#analyze()}</code> method
 * detects any created/modified/deleted file/directory matching a given
 * filename mask within a given target directory and returns a
 * <code>{@link AnalysisReport}</code> object containing analysis information.<br>
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
 **/
public class LocalFileSystemMonitor implements FileSystemMonitor
{
    private static final Logger   logger        = org.slf4j.LoggerFactory.getLogger(LocalFileSystemMonitor.class);

    protected String              analysisDir;
    protected long                lastAnalysisTimestamp;

    /**
     * A set of <code>FileProperties</code> for each matching filename
     * found within target directory on last analysis.
     */
    protected Set<FileProperties> lastAnalysisFileSet;

    /**
     * A set of <code>FileProperties</code> for each matching filename
     * found within target directory on current analysis.
     */
    protected Set<FileProperties> currentAnalysisFileSet;

    /**
     * A set of <code>FileProperties</code> for each matching filename,
     * found within target directory on current analysis, which has been
     * modified since the previous analysis.
     */
    protected Set<FileProperties> modifiedFileSet;

    /**
     * Flag indicating that the generated report must contains the current
     * listing
     * of all matching files.
     */
    protected boolean             returnExisting;

    /**
     * Flag indicating that the generated report must contains an entry for each
     * matching file created since last analysis.
     */
    protected boolean             returnCreated;

    /**
     * Flag indicating that the generated report must contains an entry for each
     * matching file modified since last analysis.
     */
    protected boolean             returnModified;

    /**
     * Flag indicating that the generated report must contains an entry for each
     * matching file deleted since last analysis.
     */
    protected boolean             returnDeleted;

    /**
     * The generated report must sort files: by-name or by-time.
     */
    protected String              sortMode;

    /**
     * Flag indicating that the sort order is ascending.
     */
    protected boolean             sortAscending;

    /**
     * A <code>java.io.FileFilter</code> object to filter files.
     */
    private RegExFileFilter       fileFilter;

    /**
     * Hold the last analysis result.
     */
    private FileSystemStatus      monitorStatus = null;

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
            analysisDir = XMLConfig.get(node, "@path");
            fileFilter = RegExFileFilter.buildFileFilter(XMLConfig.getNode(node, "FileFilter"));
            Node rf = XMLConfig.getNode(node, "ResultFilter");
            returnExisting = XMLConfig.getBoolean(rf, "@existing", false);
            returnCreated = XMLConfig.getBoolean(rf, "@created", false);
            returnModified = XMLConfig.getBoolean(rf, "@modified", false);
            returnDeleted = XMLConfig.getBoolean(rf, "@deleted", false);
            sortMode = XMLConfig.get(rf, "@sort-mode", "by-name");
            sortAscending = XMLConfig.getBoolean(rf, "@sort-ascending", true);

            if ((analysisDir == null) || ("".equals(analysisDir))) {
                throw new MonitorException("Analysis directory is null or empty");
            }

            Node fsStatus = XMLConfig.getNode(node, "*[@type='fs-monitor-status']");
            monitorStatus = (FileSystemStatus) Class.forName(XMLConfig.get(fsStatus, "@class")).newInstance();
            monitorStatus.init(fsStatus, "LocalFileSystemMonitor#" + analysisDir + "#" + fileFilter.toString());

            currentAnalysisFileSet = null;
            modifiedFileSet = null;
        }
        catch (MonitorException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new MonitorException("Generic error.", exc);
        }
    }

    /**
     * This method performs the scan of the given target directory, checking if
     * any file/directory, matching the given file mask, was
     * created/modified/deleted since the previous call to this method. The
     * method returns a <code>{@link AnalysisReport}</code> object
     * containing info about all detected file events.<br>
     * If any file event is detected, a call to the
     * <code>{@link AnalysisReport#resultsAvailable()}</code> method of
     * the returned object will return <code>true</code>.<br>
     * If no file events are detected during the directory scan, a call to the
     * <code>{@link AnalysisReport#resultsAvailable()}</code> method of
     * the returned object will return <code>false</code>.
     * 
     * @return a <code>{@link AnalysisReport}</code> object containing
     *         the directory scan report<br>
     *         .
     * @throws MonitorException
     *         if any error occurs.
     * @see it.greenvulcano.util.file.monitor.FileSystemMonitor#analyze(Map)
     */
    @Override
    public AnalysisReport analyze(Map<String, String> optProperties) throws MonitorException
    {
        File currAnalysisDir = new File(analysisDir);
        monitorStatus.lock(optProperties);
        try {
            fileFilter.compileNamePattern(optProperties);

            currAnalysisDir = new File(PropertiesHandler.expand(analysisDir,
                    MapUtils.convertToHMStringObject(optProperties)));
            checkAnalysisDir(currAnalysisDir);
            initMonitorStatus(currAnalysisDir, optProperties);

            fileFilter.setCheckLastModified(false, -1, true);
            currentAnalysisFileSet = buildFileSet(currAnalysisDir, fileFilter);

            fileFilter.setCheckLastModified(true, lastAnalysisTimestamp, true);
            modifiedFileSet = buildFileSet(currAnalysisDir, fileFilter);

            AnalysisReport result = generateReport(currAnalysisDir, fileFilter.toString());

            lastAnalysisTimestamp = new Date().getTime();
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
            throw new MonitorException("Error while generating analysis report of directory [" + currAnalysisDir
                    + "]/[" + fileFilter.toString() + "]", exc);
        }
        finally {
            monitorStatus.unlock(optProperties);
        }

    }

    /**
     * Generates the AnalysisReport object containing the report for the
     * most recently performed directory scan.
     * 
     * @return the AnalysisReport object containing the report
     * @throws MonitorException
     */
    protected AnalysisReport generateReport(File currAnalysisDir, String analysisFilter) throws MonitorException
    {
        Set<FileProperties> deletedFiles = new HashSet<FileProperties>(lastAnalysisFileSet);
        deletedFiles.removeAll(currentAnalysisFileSet);
        Set<FileProperties> createdFiles = new HashSet<FileProperties>(currentAnalysisFileSet);
        createdFiles.removeAll(lastAnalysisFileSet);

        boolean generateEntries = (returnExisting && (currentAnalysisFileSet.size() != 0));
        boolean generateCreated = (returnCreated && (createdFiles.size() != 0));
        boolean generateModified = (returnModified && (modifiedFileSet.size() != 0));
        boolean generateDeleted = (returnDeleted && (deletedFiles.size() != 0));
        boolean generateVariations = (generateCreated || generateModified || generateDeleted);
        boolean generateReport = (generateEntries || generateVariations);

        BasicAnalysisReport report = new BasicAnalysisReport(currAnalysisDir.getAbsolutePath(), analysisFilter,
                sortMode, sortAscending);

        if (generateReport) {
            try {
                if (generateEntries) {
                    report.addExistingFileSet(currentAnalysisFileSet);
                }
                if (generateCreated) {
                    report.addCreatedFileSet(createdFiles);
                    modifiedFileSet.removeAll(createdFiles);
                }
                if (generateModified) {
                    report.addModifiedFileSet(modifiedFileSet);
                }
                if (generateDeleted) {
                    report.addDeletedFileSet(deletedFiles);
                }
            }
            catch (Exception exc) {
                throw new MonitorException("Error while generating analysis report of directory [" + currAnalysisDir
                        + "]", exc);
            }
        }

        logger.debug("Analysis Report:\n" + report);
        return report;
    }


    /**
     * Verify the correctness of current analysis directory.
     * 
     * @param currAnalysisDir
     * @throws MonitorException
     */
    private void checkAnalysisDir(File currAnalysisDir) throws MonitorException
    {
        if (!currAnalysisDir.exists()) {
            throw new MonitorException("Analysis directory " + currAnalysisDir.getAbsolutePath() + " NOT found");
        }
        if (!currAnalysisDir.isDirectory()) {
            throw new MonitorException("Analysis directory is NOT a directory: " + currAnalysisDir.getAbsolutePath());
        }
        if (!currAnalysisDir.canRead()) {
            throw new MonitorException("No read access rights for analysis directory "
                    + currAnalysisDir.getAbsolutePath());
        }
    }

    /**
     * Initializes the Monito Status.
     * 
     * @param optProperties
     * @throws MonitorException
     */
    private void initMonitorStatus(File currAnalysisDir, Map<String, String> optProperties) throws MonitorException
    {
        StatusInfo statusInfo = monitorStatus.loadStatus(optProperties);
        if (statusInfo != null) {
            lastAnalysisTimestamp = statusInfo.getAnalysisTimestamp();
            lastAnalysisFileSet = statusInfo.getStatusFileSet();
        }
        else {
            lastAnalysisTimestamp = new Date().getTime();
            fileFilter.setCheckLastModified(false, 0, true);
            lastAnalysisFileSet = buildFileSet(currAnalysisDir, fileFilter);
        }
    }

    /**
     * Builds a <code>Set</code> containing a <code>FileProperties</code> object
     * for
     * each file, within the target directory, that matches the
     * <code>FileFilter</code> object passed as argument.<br>
     */
    private Set<FileProperties> buildFileSet(File currAnalysisDir, FileFilter filter)
    {
        File[] results = currAnalysisDir.listFiles(filter);
        Set<FileProperties> matchingFileset = new HashSet<FileProperties>(results.length);
        for (File file : results) {
            FileProperties currFile = new FileProperties(file.getName(), file.lastModified(), file.length(),
                    file.isDirectory(), file.canRead(), file.canWrite(), file.canExecute());
            matchingFileset.add(currFile);
        }
        return matchingFileset;
    }
}
