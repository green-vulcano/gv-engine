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

import java.util.Date;

import org.w3c.dom.Document;

/**
 * Interface implemented by the object returned by a call to the
 * <code>{@link FileSystemMonitor#analyze()}</code> method.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 */
public interface AnalysisReport
{
    static final String ORDER_BY_NAME = "by-name";
    static final String ORDER_BY_TIME = "by-time";

    /**
     * This method returns <code>true</code> if the most recently performed
     * analysis produced results.
     * 
     * @return <code>true</code> if the analysis produced results.
     */
    boolean resultsAvailable();

    /**
     * This method returns an XML report (whose structure is defined in
     * <code>FileSystemMonitor.xsd</code> schema file) containing info about the
     * results of the most recently performed analysis.<br>
     * 
     * @return an XML report for the most recently performed analysis
     * @throws MonitorException
     *         if the most recently performed analysis produced no results
     */
    Document toXML() throws MonitorException;

    /**
     * Returns the current analyzed directory.
     * 
     * @return
     */
    String getAnalysisDirectory();

    /**
     * Returns the analisys timestamp.
     * 
     * @return
     */
    Date getAnalysisTime();

    /**
     * Returns the current filter.
     * 
     * @return
     */
    String getFilter();

    /**
     * If the <code>{@link FileSystemMonitor}</code> instance was configured to
     * detect <i>existing</i> matching files, and if the most recently performed
     * analysis actually detected some matching file within the target
     * directory, this method returns the <i>number</i> of detected files.<br>
     * Otherwise, it returns <code>-1</code>.
     * 
     * @return the number of detected <i>existing</i> files or <code>-1</code>.
     */
    int getExistingFilesCount();

    /**
     * If the <code>{@link FileSystemMonitor}</code> instance was configured to
     * detect <i>created</i> matching files, and if the most recently performed
     * analysis actually detected some matching file within the target
     * directory, this method returns the <i>number</i> of detected files.<br>
     * Otherwise, it returns <code>-1</code>.
     * 
     * @return the number of detected <i>created</i> files or <code>-1</code>.
     */
    int getCreatedFilesCount();

    /**
     * If the <code>{@link FileSystemMonitor}</code> instance was configured to
     * detect <i>modified</i> matching files, and if the most recently performed
     * analysis actually detected some matching file within the target
     * directory, this method returns the <i>number</i> of detected files.<br>
     * Otherwise, it returns <code>-1</code>.
     * 
     * @return the number of detected <i>modified</i> files or <code>-1</code>.
     */
    int getModifiedFilesCount();

    /**
     * If the <code>{@link FileSystemMonitor}</code> instance was configured to
     * detect <i>deleted</i> matching files, and if the most recently performed
     * analysis detected some matching file within the target
     * directory, this method returns the <i>number</i> of detected files.<br>
     * Otherwise, it returns <code>-1</code>.
     * 
     * @return the number of detected <i>deleted</i> files, or <code>-1</code>.
     */
    int getDeletedFilesCount();
}
