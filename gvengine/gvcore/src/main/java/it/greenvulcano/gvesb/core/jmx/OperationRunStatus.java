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
package it.greenvulcano.gvesb.core.jmx;

import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;

/**
 * OperationInfo class.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 * 
 */
public class OperationRunStatus
{
	public Date startTime;
	public Date currNodeTime;
	public String currNodeId;

	public OperationRunStatus() {
		startTime = new Date();
	}

	@Override
	public String toString() {
		return DateUtils.dateToString(startTime, DateUtils.DEFAULT_FORMAT_TIMESTAMP) + " - " + currNodeId + " - " + DateUtils.dateToString(currNodeTime, DateUtils.DEFAULT_FORMAT_TIMESTAMP);
	}

}
