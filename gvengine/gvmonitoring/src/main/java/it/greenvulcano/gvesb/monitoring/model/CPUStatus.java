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
package it.greenvulcano.gvesb.monitoring.model;

import java.time.Instant;

public class CPUStatus {
	
	private final double usage;
	private final int count;
	private final Instant time;
		
	public CPUStatus(Instant time, double usage, int count) {
		super();
		this.time = time;
		this.usage = usage;
		this.count = count;
	}

	public double getUsage() {
		return usage;
	}
	
	public int getCount() {
		return count;
	}
	
	public Instant getTime() {
		return time;
	}
	
}
