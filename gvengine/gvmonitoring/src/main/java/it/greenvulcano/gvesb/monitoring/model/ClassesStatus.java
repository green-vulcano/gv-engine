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

public class ClassesStatus {
	
	/*Total number of classes that have been loaded since the
      Java virtual machine has started execution. */
	private final long totalLoadedClasses;
	
	/*The number of classes that are currently loaded in the Java
      virtual machine. */
	private final long loadedClasses;
	
	/*The total number of classes unloaded since the Java virtual
      machine has started execution. */
	private final long unLoadedClasses;
	
	private final Instant time;


	public ClassesStatus(Instant time, long totalLoadedClasses, long loadedClasses, long unLoadedClasses) {
		super();
		this.totalLoadedClasses = totalLoadedClasses;
		this.loadedClasses = loadedClasses;
		this.unLoadedClasses = unLoadedClasses;
		this.time = time;
	}
	
	public Instant getTime(){
		return time;
	}

	public long getTotalLoadedClasses() {
		return totalLoadedClasses;
	}

	public long getLoadedClasses() {
		return loadedClasses;
	}
	
	public long getUnLoadedClasses(){
		return unLoadedClasses;
	}
	
	

}
