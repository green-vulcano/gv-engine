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

public class ThreadsStatus {
	
	//Current number of live threads including both daemon and non-daemon threads
	private final int totalThreads;
	
	//The current number of live daemon threads
	private final int daemonThreads;
	
	//The peak live thread count since the Java virtual machine started or peak was reset
	private final int peakThreads;

	private final Instant time;

	public ThreadsStatus(Instant time, int totalThreads, int daemonThreads, int peakThreads) {
		super();
		this.totalThreads = totalThreads;
		this.daemonThreads = daemonThreads;
		this.peakThreads = peakThreads;
		this.time = time;
	}

	public int getTotalThreads() {
		return totalThreads;
	}

	public int getDaemonThreads() {
		return daemonThreads;
	}

	public int getPeakThreads() {
		return peakThreads;
	}

	public Instant getTime() {
		return time;
	}
	

}