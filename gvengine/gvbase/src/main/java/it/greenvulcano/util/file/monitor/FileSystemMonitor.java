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

import it.greenvulcano.util.file.monitor.status.FileSystemStatus;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * A call to the <code>{@link FileSystemMonitor#analyze()}</code> method
 * detects any created/modified/deleted file/directory matching a given filename
 * mask within a given target directory and returns a
 * <code>{@link AnalysisReport}</code> object.<br>
 * Informations about created/deleted files is retrieved by comparing the list
 * of matching files currently within the target directory with a previous list
 * holded by a <code>{@link FileSystemStatus}</code> instance.
 * The method returns a <code>{@link AnalysisReport}</code> object
 * containing info about all detected file events.<br>
 * 
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 * 
 **/
public interface FileSystemMonitor
{
    /**
     * Initialization method.
     * 
     * @param node
     * @throws MonitorException
     */
    void init(Node node) throws MonitorException;

    /**
     * This method performs the analysis of the given target directory, checking
     * if
     * any file/directory, matching the given file mask, was
     * created/modified/deleted since the previous call to this method.
     * 
     * @return a <code>{@link AnalysisReport}</code> object containing
     *         the analysis report
     * @throws MonitorException
     */
    AnalysisReport analyze(Map<String, String> optProperties) throws MonitorException;

}
