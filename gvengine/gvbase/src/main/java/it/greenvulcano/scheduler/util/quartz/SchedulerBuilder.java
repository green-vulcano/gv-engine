/*
 * Copyright (c) 2009-2012 GreenVulcano ESB Open Source Project.
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
 */
package it.greenvulcano.scheduler.util.quartz;

import it.greenvulcano.scheduler.TaskException;

import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public interface SchedulerBuilder
{
    public void init(Node node) throws TaskException;

    public Scheduler getScheduler(String schedName) throws TaskException;

    public Calendar getCalendar(String schedName, String calName) throws TaskException;
}
