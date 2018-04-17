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
package it.greenvulcano.gvesb.scheduler.quartz;

import java.util.Objects;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPool;
import it.greenvulcano.gvesb.core.pool.GreenVulcanoPoolManager;
import it.greenvulcano.gvesb.j2ee.XAHelper;
import it.greenvulcano.gvesb.j2ee.XAHelperException;

@DisallowConcurrentExecution
public class GVOperationJob implements Job {

	public static final String GVBUFFER = "gvbuffer";
	public static final String OPERATION_NAME = "gvoperation";
		
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		XAHelper xaHelper = null;
		
		try {
			
			GVBuffer input = (GVBuffer) Objects.requireNonNull(context.getJobDetail().getJobDataMap().get(GVBUFFER));
			String operation = Objects.requireNonNull(context.getJobDetail().getJobDataMap().getString(OPERATION_NAME));
			
			if (context.getJobDetail().getJobDataMap().getBoolean("transactional")) {
				xaHelper = new XAHelper(XAHelper.DEFAULT_JDNI_NAME);
			} 
			
			GreenVulcanoPool gv = GreenVulcanoPoolManager.instance()
														 .getGreenVulcanoPool("gvscheduler")
														 .orElseGet(GreenVulcanoPoolManager::getDefaultGreenVulcanoPool);
			
			if (xaHelper!=null) {
				xaHelper.begin();
			}
			
			gv.forward(input, operation);
			
			if (xaHelper!=null) {
				xaHelper.commit();
			}
			
		} catch (Exception e) {
			
			if (xaHelper!=null) {
				try {
					xaHelper.rollback();
				} catch (XAHelperException xaHelperException) {
					throw new JobExecutionException(xaHelperException);
				}
			}
			
			throw new JobExecutionException(e);
		}

	}

}
