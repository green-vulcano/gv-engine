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
package it.greenvulcano.scheduler;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.event.util.shutdown.ShutdownEvent;
import it.greenvulcano.event.util.shutdown.ShutdownEventLauncher;
import it.greenvulcano.event.util.shutdown.ShutdownEventListener;
import it.greenvulcano.scheduler.util.quartz.SchedulerBuilder;
import it.greenvulcano.util.thread.BaseThread;
import it.greenvulcano.util.txt.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class TaskManager implements ConfigurationListener, ShutdownEventListener
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(TaskManager.class);

    private String                 managerName       = null;

    private Map<String, TaskGroup> groups            = new HashMap<String, TaskGroup>();
    private SchedulerBuilder       schedulerBuilder  = null;

    private String                 descriptorName    = null;

    private String                 cfgFileName       = null;
    private boolean                confChangedFlag   = false;
    public TaskManager(String name, String cfgFileName)
    {
        this.managerName = name;
        this.cfgFileName = cfgFileName;

        try {
            init();

            XMLConfig.addConfigurationListener(this, cfgFileName);
            try {
                ShutdownEventLauncher.addEventListener(this);
            }
            catch (Exception exc) {
                logger.warn("TaskManager - Unable to register as ShutdownEvent listener");
            }
            // MidnightReloadConfigTimer.getInstance().watchFile(cfgFileName);
        }
        finally {
        }
    }

    /**
     * @return Returns the descriptorName.
     */
    public String getDescriptorName()
    {
        return descriptorName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return managerName;
    }

    /**
     *
     */
    private void init()
    {
        try {
            descriptorName = XMLConfig.get(cfgFileName, "/GVTaskManagerConfiguration/@jmx-descriptor-name");
                     
            XMLConfig.getBoolean(cfgFileName, "/GVTaskManagerConfiguration/@force-task-recovery",
                    false);

            Node sbNode = XMLConfig.getNode(cfgFileName, "/GVTaskManagerConfiguration/*[@type='scheduler-builder']");
            String sbClass = XMLConfig.get(sbNode, "@class");
            logger.debug("TaskManager[" + managerName + "] - Initializing SchedulerBuilder[" + sbClass + "]");
            try {
                schedulerBuilder = (SchedulerBuilder) Class.forName(sbClass).newInstance();
                schedulerBuilder.init(sbNode);
                Scheduler sch = schedulerBuilder.getScheduler(managerName);

                // Remove already scheduled tasks... needed if using clustered configuration
                String jgns[] = sch.getJobGroupNames();
                for (int i = 0; i < jgns.length; i++) {
                    String gn = jgns[i];
                    String jns[] = sch.getJobNames(gn);
                    for (int j = 0; j < jns.length; j++) {
                        String jn = jns[j];
                        logger.debug("TaskManager[" + managerName + "] - Already Registered Job [" + gn + "." + jn
                                + "]... Cleaning up...");
                        try {
                            sch.deleteJob(jn, gn);
                        }
                        catch (Exception exc) {
                            logger.warn("TaskManager[" + managerName + "] - Error deleting orphaned JobTrace[" + gn
                                    + "." + jn + "]", exc);
                        }
                    }
                }

                logger.debug("TaskManager[" + managerName + "] - Starting Scheduler...");
                sch.start();
            }
            catch (Exception exc) {
                logger.error("TaskManager[" + managerName + "] - Error initializing Scheduler", exc);
            }

            NodeList gnl = XMLConfig.getNodeList(cfgFileName, "/GVTaskManagerConfiguration/TaskGroups/TaskGroup");
            if ((gnl != null) && (gnl.getLength() > 0)) {
                for (int i = 0; i < gnl.getLength(); i++) {
                    try {
                        Node n = gnl.item(i);
                        TaskGroup group = new TaskGroup();
                        group.init(n, this);
                        logger.info("TaskManager[" + managerName + "] - Initialized Group[" + group.getName() + "]");
                        groups.put(group.getName(), group);
                    }
                    catch (Exception exc) {
                        logger.error("TaskManager[" + managerName + "] - Error initializing Group", exc);
                    }
                }

                startTasks(true);
            }
            confChangedFlag = false;
        }
        catch (Exception exc) {
            logger.error("Error initializing TaskManager[" + managerName + "]", exc);
        }
    }

    public synchronized void destroy()
    {
        killTasks();
        try {
            getScheduler().shutdown();
        }
        catch (Exception exc) {
            // do nothing
        }
        logger.info("TaskManager[" + managerName + "] - Destroyed");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.eai.utils.config.ConfigurationListener#configurationChanged(it.eai
     * .utils.config.ConfigurationEvent)
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if (!confChangedFlag && (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(cfgFileName)) {
            confChangedFlag = true;
            // destroy now
            killTasks();

            // initialize after a delay
            Runnable rr = new Runnable() {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(30000);
                    }
                    catch (InterruptedException exc) {
                        // do nothing
                    }
                    reinit();
                }
            };

            BaseThread btr = new BaseThread(rr, "Config reloader for: " + managerName);
            btr.setDaemon(true);
            btr.start();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.eai.utils.event.util.shutdown.ShutdownEventListener#shutdownStarted
     * (it.eai.utils.event.util.shutdown.ShutdownEvent)
     */
    @Override
    public void shutdownStarted(ShutdownEvent event)
    {
        logger.info("TaskManager[" + managerName + "] - ShutdownEvent received: " + event);
        XMLConfig.removeConfigurationListener(this);
        ShutdownEventLauncher.removeEventListener(this);

       
    }

    public void execTask(String groupN, String taskN, JobExecutionContext context)
    {
        Task task = groups.get(groupN).getTask(taskN);
        if (task != null) {
            synchronized (task) {
                task.handleTask(context);
            }
        }
    }

    public Scheduler getScheduler() throws TaskException
    {
        try {
            return schedulerBuilder.getScheduler(managerName);
        }
        catch (Exception exc) {
            throw new TaskException(exc);
        }
    }

    private void killTasks()
    {
        Iterator<String> i = groups.keySet().iterator();
        while (i.hasNext()) {
            String groupN = i.next();
            try {
                TaskGroup group = groups.get(groupN);
                group.destroy();
            }
            catch (Exception exc) {
                logger.error("TaskManager[" + managerName + "] - Error unregistering TaskGroup[" + groupN + "]", exc);
            }
        }
        groups.clear();
    }

    private void startTasks(boolean onlyAutoStart)
    {
        Iterator<String> i = groups.keySet().iterator();
        while (i.hasNext()) {
            String groupN = i.next();
            try {
                TaskGroup group = groups.get(groupN);
                if (!group.isEnabled()) {
                    logger.warn("TaskManager[" + managerName + "] - Unable to register Tasks of TaskGroup[" + group.getName()
                        + "] - TaskGroup disabled!");
                    continue;
                }
                group.startTasks(onlyAutoStart);
            }
            catch (Exception exc) {
                logger.error("TaskManager[" + managerName + "] - Error starting TaskGroup[" + groupN + "]", exc);
            }
        }
    }

    void registerTask(Task task) throws TaskException
    {
        if (task == null) {
            return;
        }
        try {
            if (!task.isEnabled()) {
                logger.warn("TaskManager[" + managerName + "] - Unable to register Task[" + task.getFullName()
                        + "] - Task disabled!");
                return;
            }
            /*if (task.isRegistered()) {
                logger.warn("TaskManager[" + managerName + "] - Unable to register Task[" + task.getFullName()
                        + "] - Task already registered!");
                return;
            }*/

            /*
            tdata.regenerateStartDateTime();
            // if (tdata.getStartDateTime().getTime() <
            // System.currentTimeMillis()) {
            Date sdate = tdata.getInitialScheduleDateTime();
            if (sdate.getTime() < System.currentTimeMillis()) {
                logger.warn("TaskManager[" + managerName + "] - Unable to register Task[" + tdata.getName()
                        + "] - Task scheduled datetime (" + DateUtils.dateToString(sdate, Constants.DATE_TIME_FORMAT)
                        + ") occur in the past!");
                if (firstTime && forceTaskRecovery && tdata.isRecoverable()) {
                    Date today = DateUtils.stringToDate(DateUtils.nowToString("yyyyMMdd"), "yyyyMMdd");
                    if (HeartBeatManager.lastBeat(task.getBeatSubSystem(), today.getTime()) == -1) {
                        logger.info("TaskManager[" + managerName + "] - Task[" + tdata.getName()
                                + "] is recoverable... Forced execution...");
                        task.recoveryTask("TaskManager Recovery", new Date());
                    }
                }
                return;
            }*/
            logger.info("TaskManager[" + managerName + "] - Registering: " + task.getFullName());
            //Integer id = null;

            /*
            String calName = "CAL_" + tdata.getGroup() + "#" + tdata.getName();
            org.quartz.Calendar cal = new TaskCalendar(tdata);
            getScheduler().addCalendar(calName, cal, true, true);

            Trigger trg = null;
            JobDetail jobD = null;

            if ((tdata.getPeriod() > 0) && (tdata.getNbOccurrences() > 0)) {
                trg = TriggerUtils.makeSecondlyTrigger((int) (tdata.getPeriod() / 1000), tdata.getNbOccurrences());
                trg.setStartTime(tdata.getInitialScheduleDateTime());
                trg.setEndTime(tdata.getEndDateTime());
            }
            else if (tdata.getPeriod() > 0) {
                trg = TriggerUtils.makeSecondlyTrigger((int) (tdata.getPeriod() / 1000));
                trg.setStartTime(tdata.getInitialScheduleDateTime());
                trg.setEndTime(tdata.getEndDateTime());
            }
            else {
                Calendar st = DateUtils.createCalendar();
                st.setTime(tdata.getStartTime());
                trg = TriggerUtils.makeDailyTrigger(st.get(Calendar.HOUR_OF_DAY), st.get(Calendar.MINUTE));
                if (tdata.isUseStartDate()) {
                    trg.setStartTime(tdata.getStartDateTime());
                }
                if (tdata.isUseEndDate()) {
                    trg.setEndTime(tdata.getEndDateTime());
                }
            }

            TriggerUtils.setTriggerIdentity(trg, tdata.getName(), tdata.getGroup());
            trg.setCalendarName(calName);

            jobD = new JobDetail(tdata.getName(), tdata.getGroup(), Job.class);
            */

            List<Trigger> triggers = task.getTriggers();
            JobDetail jobD = task.getJobDetail();

            Scheduler sch = getScheduler();
            sch.addJob(jobD, true);

            Map<String, String> td = new HashMap<String, String>();
            for (Trigger trg : triggers) {
                //Date d = sch.scheduleJob(jobD, trg);
                try {
                    Date d = sch.scheduleJob(trg);
                    td.put(trg.getFullName(), DateUtils.dateToString(d, DateUtils.DEFAULT_FORMAT_TIMESTAMP));
                }
                catch (ObjectAlreadyExistsException exc) {
                    Date d = sch.rescheduleJob(trg.getName(), trg.getGroup(), trg);
                    td.put(trg.getFullName(), DateUtils.dateToString(d, DateUtils.DEFAULT_FORMAT_TIMESTAMP));
                }
            }
            //task.setTimerID("" + jobD.getKey());

            logger.info("TaskManager[" + managerName + "] - Registered: " + task.getFullName() + " - scheduled: " + td);
        }
        // catch (TaskException exc) {
        // throw exc;
        // }
        catch (Exception exc) {
            throw new TaskException("TaskManager[" + managerName + "] - Error registering Task: " + task.getFullName(),
                    exc);
        }
    }

    void unregisterTask(Task task) throws TaskException
    {
        if (task == null) {
            return;
        }
        try {
            /*if (!task.isRegistered()) {
                return;
            }*/

            Scheduler sch = getScheduler();
            sch.deleteJob(task.getName(), task.getGroup());

            //task.setTimerID(null);
        }
        // catch (TaskException exc) {
        // throw exc;
        // }
        catch (Exception exc) {
            throw new TaskException("TaskManager[" + managerName + "] - Error unregistering Task: "
                    + task.getFullName(), exc);
        }
    }

    private void reinit()
    {
        if (!confChangedFlag) {
            return;
        }
        confChangedFlag = false;
        //stopAllTask_Internal();
        killTasks();
        try {
            getScheduler().shutdown();
        }
        catch (Exception exc) {
            // do nothing
        }
        init();
    }

    /*
     * Metodi utilizzati internamente tramite JMX, da non invocare direttamente
     */

    /*
      public TaskData[] getTaskList_Internal() throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          Set<String> keySet = tasks.keySet();
          TaskData[] taskList = new TaskData[keySet.size()];
          Iterator<String> i = keySet.iterator();
          int c = 0;
          while (i.hasNext()) {
              TaskData tdata = (tasks.get(i.next())).getTaskData();
              System.out.println("getTaskList: " + tdata);
              taskList[c] = tdata;
              c++;
          }
          return taskList;
      }

      public void startAllTask_Internal() throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          Iterator<String> i = tasks.keySet().iterator();
          while (i.hasNext()) {
              registerTask(tasks.get(i.next()));
          }
      }

      public void stopAllTask_Internal()
      {
          if (confChangedFlag) {
              reinit();
          }
          Iterator<String> i = tasks.keySet().iterator();
          while (i.hasNext()) {
              String name = i.next();
              try {
                  unregisterTask(tasks.get(name));
              }
              catch (Exception exc) {
                  logger.error("Error unregistering Task[" + name + "]", exc);
              }
          }
      }

      public void resumeAllTask_Internal()
      {
          if (confChangedFlag) {
              reinit();
          }
          Iterator<String> i = tasks.keySet().iterator();
          while (i.hasNext()) {
              (tasks.get(i.next())).resume();
          }
      }

      public void suspendAllTask_Internal()
      {
          if (confChangedFlag) {
              reinit();
          }
          Iterator<String> i = tasks.keySet().iterator();
          while (i.hasNext()) {
              (tasks.get(i.next())).suspend();
          }
      }

      public void startTask_Internal(String name) throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          registerTask(tasks.get(name));
      }

      public void stopTask_Internal(String name) throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          unregisterTask(tasks.get(name));
      }

      public void resumeTask_Internal(String name)
      {
          if (confChangedFlag) {
              reinit();
          }
          (tasks.get(name)).resume();
      }

      public void suspendTask_Internal(String name)
      {
          if (confChangedFlag) {
              reinit();
          }
          (tasks.get(name)).suspend();
      }

      public void startTimer_Internal() throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          try {
              //JMXUtils.invoke(timerOName, "start", new Object[0], new String[0], true, null);
          }
          catch (Exception exc) {
              throw new TaskException("TaskManager[" + managerName + "] - Error starting timer", exc);
          }
      }

      public void stopTimer_Internal() throws TaskException
      {
          if (confChangedFlag) {
              reinit();
          }
          try {
              //   JMXUtils.invoke(timerOName, "stop", new Object[0], new String[0], true, null);
          }
          catch (Exception exc) {
              throw new TaskException("TaskManager[" + managerName + "] - Error stopping timer", exc);
          }
      }
    */
}
