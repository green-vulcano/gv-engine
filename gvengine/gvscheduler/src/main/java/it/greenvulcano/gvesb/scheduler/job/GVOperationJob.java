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
package it.greenvulcano.gvesb.scheduler.job;

import java.util.Objects;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;

public class GVOperationJob implements Job {

	public static final String GVBUFFER = "gvbuffer";
	public static final String OPERATION_NAME = "gvoperation";
		
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			
			GVBuffer input = (GVBuffer) Objects.requireNonNull(context.getJobDetail().getJobDataMap().get(GVBUFFER));
			String operation = Objects.requireNonNull(context.getJobDetail().getJobDataMap().getString(OPERATION_NAME));
			
			GreenVulcanoPool gv = GreenVulcanoPoolManager.instance().getGreenVulcanoPool("gvscheduler");
			gv.forward(input, operation);			
			
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}

	}

}
