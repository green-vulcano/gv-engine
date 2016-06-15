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
package it.greenvulcano.scheduler;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.log.NMDC;
import it.greenvulcano.scheduler.util.quartz.StatefulJob;
import it.greenvulcano.scheduler.util.quartz.TriggerBuilder;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.heartbeat.HeartBeatManager;
import it.greenvulcano.util.txt.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public abstract class Task
{
    public static String        TASK_FIRST_RUN        = "TASK_FIRST_RUN";
    public static String        TASK_RECOVERY_RUN     = "TASK_RECOVERY_RUN";
    public static String        TASK_MISFIRE_RUN      = "TASK_MISFIRE_RUN";
    public static String        TASK_FIRE_TIME        = "TASK_FIRE_TIME";
    private Logger              logger                = null;
    private boolean             mustDestroy           = false;
    private boolean             suspended             = false;
    private boolean             autoStart             = false;
    private boolean             enabled               = false;
    private String              group                 = "UNDEFINED";
    private String              name                  = "UNDEFINED";
    private TaskManager         manager               = null;
    private boolean             running               = false;
    private Thread              currentThread         = null; 
    private Map<String, String> properties            = new HashMap<String, String>();
    //private List<TriggerBuilder> triggerBuilders       = new ArrayList<TriggerBuilder>();
    private List<Trigger>       triggers              = new ArrayList<Trigger>();
    private JobDetail           jobDetail             = null;

    public void init(Node node, String group, TaskManager manager) throws TaskException
    {
        try {
            logger = getLogger();
            this.group = group;
            this.manager = manager;
            this.name = XMLConfig.get(node, "@name");
            this.autoStart = XMLConfig.getBoolean(node, "@auto-start", false);
            this.enabled = XMLConfig.getBoolean(node, "@enabled", false);

            logger.debug("BEGIN - initializing TimerTask[" + getFullName() + "][" + getClass() + "]");
            NodeList pnl = XMLConfig.getNodeList(node, "JbProperties/PropertyDef");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    properties.put(XMLConfig.get(n, "@name"), XMLConfig.get(n, "@value"));
                }
                logger.debug("Added properties: " + properties);
            }
            NodeList tnl = XMLConfig.getNodeList(node, "Triggers/*[@type='cron-trigger']");
            if (tnl != null) {
                for (int i = 0; i < tnl.getLength(); i++) {
                    Node n = tnl.item(i);
                    TriggerBuilder tb = (TriggerBuilder) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                    tb.init(group, getName(), n);
                    //triggerBuilders.add(tb);
                    if (tb.isEnabled()) {
                        Trigger trigger = tb.newTrigger();
                        triggers.add(trigger);
                        logger.debug("Added Trigger: " + tb);
                    }
                    else {
                        logger.debug("Disabled Trigger: " + tb);
                    }
                }
            }
            if (triggers.isEmpty()) {
                throw new TaskException("Error initializing Task[" + getFullName() + "] - Empty Trigger list");
            }

            jobDetail = new JobDetail(getName(), getGroup(), StatefulJob.class);
            JobDataMap jdm = jobDetail.getJobDataMap();
            jdm.put(TASK_FIRST_RUN, true);
            for (Map.Entry<String, String> prop : properties.entrySet()) {
                jdm.put(prop.getKey(), prop.getValue());
            }

            initTask(node);

            logger.debug("END - initialized TimerTask[" + getFullName() + "][" + getClass() + "]");
        }
        catch (TaskException exc) {
            throw exc;
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing Task[" + getFullName() + "]", exc);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getGroup()
    {
        return group;
    }

    protected TaskManager getTaskManager()
    {
        return manager;
    }

    public String getFullName()
    {
        return group + "." + name;
    }

    public boolean isAutoStart()
    {
        return this.autoStart;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public List<Trigger> getTriggers()
    {
        return this.triggers;
    }

    public void handleTask(JobExecutionContext context)
    {
        if (mustDestroy || suspended) {
            return;
        }
        String evName = context.getJobDetail().getGroup() + "." + context.getJobDetail().getName();
        if (!getFullName().equals(evName)) {
            logger.warn("Task [" + getFullName() + "] bad activation event received [" + evName + "]!");
            return;
        }
        if (!running) {
            JobDataMap jdm = context.getMergedJobDataMap();
            Map<String, String> locProperties = MapUtils.convertToHMStringString(jdm.getWrappedMap());
            boolean firstRun = jdm.getBooleanValue(TASK_FIRST_RUN);
            boolean recoveryRun = context.isRecovering();
            Date schedFireTime = context.getScheduledFireTime();
            //Date fireTime = context.getFireTime();
            //boolean misfireRun = (fireTime.getTime() - schedFireTime.getTime()) >= (5 * 60 * 1000); // max 5' delay
            locProperties.put(TASK_FIRST_RUN, String.valueOf(firstRun).toUpperCase());
            locProperties.put(TASK_RECOVERY_RUN, String.valueOf(recoveryRun).toUpperCase());
            //locProperties.put(TASK_MISFIRE_RUN, String.valueOf(misfireRun).toUpperCase());
            locProperties.put(TASK_FIRE_TIME,
                    DateUtils.dateToString(schedFireTime, DateUtils.FORMAT_ISO_DATETIME_UTC));
            try {
                run(evName, schedFireTime, locProperties);
            }
            finally {
                JobDataMap jdm2 = context.getJobDetail().getJobDataMap();
                jdm2.put(TASK_FIRST_RUN, false);
            }
        }
        else {
            logger.warn("Task [" + getFullName() + "] already scheduled!");
        }
    }

    public void recoveryTask(String evName, Date fireTime)
    {
        if (mustDestroy || suspended) {
            return;
        }
        if (!running) {
            run(evName, fireTime, new HashMap<String, String>(properties));
        }
        else {
            logger.warn("Task [" + getFullName() + "] already scheduled!");
        }
    }

    /**
     * @return Returns the suspended.
     */
    public boolean isSuspended()
    {
        return suspended;
    }

    /**
     * Suspend the task.
     */
    public void suspend()
    {
        this.suspended = true;
    }

    /**
     * Resume the task.
     */
    public void resume()
    {
        this.suspended = false;
    }

    /**
     * Invoked before removing the task, perform cleanup operations.
     */
    public void destroy()
    {
        mustDestroy = true;
        if (!running) {
            try {
                logger.info("Destroing Task[" + getFullName() + "]");
                destroyTask();
            }
            catch (Exception exc) {
                // do nothing
            }
        }
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void run(String evName, Date fireTime, Map<String, String> locProperties)
    {
        if (running) {
            logger.warn("Task [" + getFullName() + "] already scheduled!");
            return;
        }

        NMDC.push();
        int id = -1;
      
        try {
            currentThread = Thread.currentThread();
            running = true;
            if (sendHeartBeat()) {
                id = prepareBeat("TRUE".equals(locProperties.get(TASK_RECOVERY_RUN)) || "TRUE".equals(locProperties.get(TASK_MISFIRE_RUN)));
            }

            boolean success = executeTask(evName, fireTime, locProperties, false);

            confirmBeat(id, success);
        }
        catch (Exception exc) {
            cancelBeat(id);
            logger.error("Error handling Task [" + getFullName() + "]", exc);
        }
        finally {
            currentThread = null;
          
            if (mustDestroy) {
                try {
                    logger.info("Destroing Task[" + getFullName() + "]");
                    destroyTask();
                }
                catch (Exception exc) {
                    // do nothing
                }
            }
            running = false;
            NMDC.pop();
        }
    }

    /**
     * @return the key for heartbeat framework.
     */
    public String getBeatSubSystem()
    {
        return getFullName();
    }

    
    /**
     * @return the temporary beat id.
     */
    protected int prepareBeat(boolean isRecovery)
    {
        try {
            return HeartBeatManager.prepareBeat(getBeatSubSystem() + (isRecovery ? " (Recovered)" : ""));
        }
        catch (Exception exc) {
            // do nothing
        }
        return -1;
    }

    /**
     * @param id
     *        the temporary beat id to confirm.
     */
    protected void confirmBeat(int id, boolean success)
    {
        try {
            if (id != -1) {
                HeartBeatManager.confirmBeat(id, success);
            }
        }
        catch (Exception exc) {
            // do nothing
        }
    }

    /**
     * @param id
     *        the temporary beat id to cancel.
     */
    protected void cancelBeat(int id)
    {
        try {
            if (id != -1) {
                HeartBeatManager.cancelBeat(id);
            }
        }
        catch (Exception exc1) {
            // do nothing
        }
    }

    // abstract methods

    /**
     * @return
     */
    protected abstract Logger getLogger();

    /**
     * @return true for enabling the heartbeat handling for the task.
     */
    protected abstract boolean sendHeartBeat();

    protected abstract void initTask(Node node) throws TaskException;

    protected abstract boolean executeTask(String name, Date fireTime, Map<String, String> locProperties, boolean isLast);

    protected abstract void destroyTask();

    public JobDetail getJobDetail()
    {
        return jobDetail;
    }

}
