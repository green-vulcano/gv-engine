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

public class MemoryStatus {

	/*The maximum amount of memory available to
    the Java Virtual Machine*/
	private final long maxMemory;
	
	/* Total memory allocated from the system
       (which can at most reach the maximum memory value
       returned by the previous function*/
	private final long totalMemory;
	
	/*The free memory *within* the total memory
       returned by the previous function */
	private final long freeMemory;
	
	
	private final Instant time;

	public MemoryStatus(Instant time, long maxMemory, long totalMemory, long freeMemory) {
		super();
		this.maxMemory = maxMemory;
		this.totalMemory = totalMemory;
		this.freeMemory = freeMemory;
		
		this.time = time;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public long getHeapMemory() {
		return totalMemory-freeMemory;
	}
	
	public Instant getTime() {
		return time;
	}	
	
}