/*
 * Copyright (c) 2009-2013 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.scheduler.util.quartz.impl;

import it.greenvulcano.scheduler.Task;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 *
 * @version 3.4.0 26/lug/2013
 * @author GreenVulcano Developer Team
 *
 */
public class GVTriggerListener implements TriggerListener
{
    @Override
    public String getName() {
        return "GVTriggerDefaultListener";
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {
        trigger.getJobDataMap().put(Task.TASK_MISFIRE_RUN, "FALSE");
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // do nothing
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        trigger.getJobDataMap().put(Task.TASK_MISFIRE_RUN, "TRUE");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }
}
