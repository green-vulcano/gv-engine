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
package it.greenvulcano.gvesb.scheduler;


import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.quartz.SchedulerException;
import org.quartz.Trigger;

public interface ScheduleManager {
	
public final static class Authority {
		
		public final static String ADMINISTRATOR = "gvadmin";
		public final static String MANAGER = "gvmanager_schedule";			
		public final static String GUEST = "gvguest_schedule";
		
		public final static Set<String> entries;
		
		static {
			Set<String> e = new LinkedHashSet<>();
			e.add(ADMINISTRATOR);
			e.add(MANAGER);			
			e.add(GUEST);
			
			entries = Collections.unmodifiableSet(e);
		}
		
	}	
	
	public List<Trigger> getTriggersList() throws SchedulerException;		
	
	public Optional<Trigger> getTrigger(final String triggerName) throws SchedulerException;	
	
	public String scheduleOperation(String cronExpression, String serviceName, String operationName, Map<String, String> properties, Object object) throws ParseException, SchedulerException;

	public void deleteTrigger(final String triggerName) throws SchedulerException, NoSuchElementException;
	
	public void suspendTrigger(final String triggerName) throws SchedulerException, NoSuchElementException;
	
	public void resumeTrigger(final String triggerName) throws SchedulerException, NoSuchElementException;
	
	public String getTriggerStatus(final String triggerName) throws SchedulerException, NoSuchElementException;

}
