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
package it.greenvulcano.gvesb.monitoring.service.runtime;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;

import it.greenvulcano.gvesb.monitoring.model.CPUStatus;
import it.greenvulcano.gvesb.monitoring.model.ClassesStatus;
import it.greenvulcano.gvesb.monitoring.model.MemoryStatus;
import it.greenvulcano.gvesb.monitoring.model.ThreadsStatus;
import it.greenvulcano.gvesb.monitoring.service.SystemMonitor;

public class SystemMonitorService implements SystemMonitor {	
		
	private final static OperatingSystemMXBean osMXBean;
	private final static ThreadMXBean THREAD_MX_BEAN;
	private final static ClassLoadingMXBean CLASS_LOADING_MX_BEAN;
	
	static {		
		osMXBean = ManagementFactory.getOperatingSystemMXBean();
		THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
		CLASS_LOADING_MX_BEAN = ManagementFactory.getClassLoadingMXBean();
	}
	
	@Override
	public CPUStatus getCPUStatus() {
	
		return new CPUStatus(Instant.now(), osMXBean.getSystemLoadAverage(), osMXBean.getAvailableProcessors());
			
	}

	@Override
	public MemoryStatus getMemoryStatus() {
		
		return new MemoryStatus(Instant.now(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
		
	}

	@Override
	public ThreadsStatus getThreadsStatus() {
		
		return new ThreadsStatus(Instant.now(), THREAD_MX_BEAN.getThreadCount(), THREAD_MX_BEAN.getDaemonThreadCount(), THREAD_MX_BEAN.getPeakThreadCount());
	}

	@Override
	public ClassesStatus getClassesStatus() {
		
		return new ClassesStatus(Instant.now(), CLASS_LOADING_MX_BEAN.getTotalLoadedClassCount(), CLASS_LOADING_MX_BEAN.getLoadedClassCount(), CLASS_LOADING_MX_BEAN.getUnloadedClassCount());
	}

}
