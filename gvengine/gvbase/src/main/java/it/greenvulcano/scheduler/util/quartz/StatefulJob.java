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
package it.greenvulcano.scheduler.util.quartz;

import it.greenvulcano.log.NMDC;
import it.greenvulcano.scheduler.TaskManagerFactory;
import it.greenvulcano.util.thread.ThreadMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class StatefulJob implements org.quartz.StatefulJob
{
    private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(StatefulJob.class);
    private Thread currentThread = null; 

    /**
     *
     */
    public StatefulJob()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        String sName = "UNDEFINED";
        String gName = "UNDEFINED";
        String tName = "UNDEFINED";
        currentThread = Thread.currentThread();
        String cthName = currentThread.getName();

        NMDC.push();
        try {
            sName = context.getScheduler().getSchedulerName();
            gName = context.getJobDetail().getGroup();
            tName = context.getJobDetail().getName();
            currentThread.setName(cthName + "#" + gName + "." + tName);
            
         
            TaskManagerFactory.instance().executeTask(sName, gName, tName, context);
        }
        catch (Exception exc) {
            logger.error("Error executing TimerTask[" + sName + "." + gName + "." + tName + "]", exc);
        }
        finally {
            NMDC.pop();
            ThreadMap.clean();
            currentThread.setName(cthName);
            currentThread = null;
        }
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

}
