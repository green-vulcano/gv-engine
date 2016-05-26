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
package it.greenvulcano.util.file.monitor.status;

import it.greenvulcano.util.file.FileProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @version 3.2.0 01/11/2011
 * @author GreenVulcano Developer Team
 */
public class StatusInfo
{
    private Set<FileProperties> fileSet           = new HashSet<FileProperties>();
    private long                analysisTimestamp = -1;

    StatusInfo(Set<FileProperties> fileSet, long analysisTimestamp)
    {
        this.fileSet = fileSet;
        this.analysisTimestamp = analysisTimestamp;
    }

    public Set<FileProperties> getStatusFileSet()
    {
        return fileSet;
    }

    public long getAnalysisTimestamp()
    {
        return analysisTimestamp;
    }
}
