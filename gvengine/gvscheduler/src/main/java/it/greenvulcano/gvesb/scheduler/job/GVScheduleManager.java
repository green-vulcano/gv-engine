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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.scheduler.ScheduleManager;

public class GVScheduleManager implements ScheduleManager {
	
	private Scheduler gvScheduler;
		
	public void setGvScheduler(Scheduler gvScheduler) {
		this.gvScheduler = gvScheduler;
	}
	
	@Override
	public List<Trigger> getTriggersList() throws SchedulerException {
		return gvScheduler.getTriggerKeys(GroupMatcher.groupEquals(Scheduler.DEFAULT_GROUP))
				   .stream().map( t->{  try { 
					   					return Optional.of(gvScheduler.getTrigger(t));
					   				  } catch (SchedulerException schedulerException) {
										return Optional.ofNullable((Trigger)null);
					   				  }
									})
				   .filter(Optional::isPresent)
				   .map(Optional::get)			
				   .collect(Collectors.toList());
		
	}

	@Override
	public Optional<Trigger> getTrigger(String triggerName) throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, Scheduler.DEFAULT_GROUP);
		
		return Optional.ofNullable(gvScheduler.getTrigger(triggerKey));
	}

	@Override
	public String scheduleOperation(String cronExpression, String serviceName, String operationName, Map<String, String> properties, Object object) throws SchedulerException {
		
		try {
			
			UUID scheduleUUID = UUID.randomUUID();
			
			GVBuffer gvBuffer = new GVBuffer();
			gvBuffer.setService(Objects.requireNonNull(serviceName));
			
			if (Objects.nonNull(properties)) {
				for (Entry<String, String> e : properties.entrySet()) {
					gvBuffer.setProperty(e.getKey(), e.getValue());
				}
			}			
			
			gvBuffer.setObject(object);
			
			JobDetail job = JobBuilder.newJob(GVOperationJob.class)
									  .withIdentity(scheduleUUID.toString(), Scheduler.DEFAULT_GROUP)
									  .withDescription(serviceName+"/"+operationName)
									  .build();
	
			job.getJobDataMap().put(GVOperationJob.OPERATION_NAME, Objects.requireNonNull(operationName));
			job.getJobDataMap().put(GVOperationJob.GVBUFFER, gvBuffer);
			
		    CronTrigger trigger = TriggerBuilder.newTrigger()
		    									.withIdentity(scheduleUUID.toString(), Scheduler.DEFAULT_GROUP)
		    									.withDescription(serviceName+"/"+operationName)
		    									.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
		    									.build();
	
		    gvScheduler.scheduleJob(job, trigger);
		    
		    return trigger.getKey().getName();
		    
		} catch (GVException gvException) {
			throw new SchedulerException(gvException);
		}

	}

	@Override
	public void deleteTrigger(String triggerName) throws SchedulerException, NoSuchElementException {
		Trigger trigger = getTrigger(triggerName).orElseThrow(NoSuchElementException::new);		
		gvScheduler.deleteJob(trigger.getJobKey());

	}

	@Override
	public void suspendTrigger(String triggerName) throws SchedulerException, NoSuchElementException  {
		TriggerKey triggerKey = getTrigger(triggerName).orElseThrow(NoSuchElementException::new).getKey();
		gvScheduler.pauseTrigger(triggerKey);

	}

	@Override
	public void resume(String triggerName) throws SchedulerException, NoSuchElementException  {
		TriggerKey triggerKey = getTrigger(triggerName).orElseThrow(NoSuchElementException::new).getKey();
		gvScheduler.resumeTrigger(triggerKey);

	}

	@Override
	public String getTriggerStatus(String triggerName) throws SchedulerException , NoSuchElementException  {
		TriggerKey triggerKey = getTrigger(triggerName).orElseThrow(NoSuchElementException::new).getKey();
		return gvScheduler.getTriggerState(triggerKey).name();
	
	}

}
