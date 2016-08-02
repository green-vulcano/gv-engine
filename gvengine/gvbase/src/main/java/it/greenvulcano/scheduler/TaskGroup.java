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

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.thread.BaseThread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class TaskGroup
{
    private static Logger     logger  = org.slf4j.LoggerFactory.getLogger(TaskGroup.class);

    private String            name    = "UNDEFINED";
    private boolean           enabled = false;
    private Map<String, Task> tasks   = new HashMap<String, Task>();
    private TaskManager       manager = null;

    public void init(Node node, TaskManager manager) throws TaskException
    {
        try {
            this.manager = manager;
            this.name = XMLConfig.get(node, "@name");
            this.enabled = XMLConfig.getBoolean(node, "@enabled", true);

            NodeList tnl = XMLConfig.getNodeList(node, "*[@type='task']");
            if ((tnl != null) && (tnl.getLength() > 0)) {
                for (int i = 0; i < tnl.getLength(); i++) {
                    try {
                        Node n = tnl.item(i);
                        String cn = XMLConfig.get(n, "@class");
                        Task task = (Task) Class.forName(cn).newInstance();
                        task.init(n, name, manager);
                        logger.info("TaskGroup[" + name + "] - Initialized Task[" + task.getName() + "]");
                        tasks.put(task.getName(), task);
                    }
                    catch (Exception exc) {
                        logger.error("TaskGroup[" + name + "] - Error initializing Task", exc);
                    }
                }
            }
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing TaskGroup[" + getName() + "]", exc);
        }
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Task getTask(String task)
    {
        return this.tasks.get(task);
    }

    public Map<String, Task> getTasks()
    {
        return this.tasks;
    }

    public void startTasks(boolean onlyAutoStart)
    {
        logger.debug((onlyAutoStart ? "Auto" : "") + "Starting Tasks of Group[" + name + "]");
        Iterator<String> i = tasks.keySet().iterator();
        while (i.hasNext()) {
            String taskN = i.next();
            try {
                Task task = tasks.get(taskN);
                if (!onlyAutoStart || (task.isAutoStart() && onlyAutoStart)) {
                    manager.registerTask(task);
                }
            }
            catch (Exception exc) {
                logger.error(
                        "TaskManager[" + manager.getName() + "] - Error starting Task[" + name + "." + taskN + "]", exc);
            }
        }
    }

    class TaskKiller implements Runnable {
    	private String taskName;
    	private TaskManager tm;
    	
    	public TaskKiller(String taskN, TaskManager tm) {
			this.taskName = taskN;
			this.tm = tm;
		}
    	
        @Override
        public void run()
        {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException exc) {
                // do nothing
            }
            try {
                Task task = tasks.remove(taskName);
                logger.debug("Start searching for Thread running Task[" + task.getFullName() + "]");
                Thread thd = task.getCurrentThread();
                if (thd != null) {
                    logger.warn("Interrupting Thread[" + thd.getName() + "] running Task[" + task.getFullName() + "]");
                    thd.interrupt();
                }
                else {
                    logger.debug("Not found a Thread running Task[" + task.getFullName() + "]");
                }
                task.destroy();
                tm.unregisterTask(task);
            }
            catch (Exception exc) {
                logger.error("TaskManager[" + tm.getName() + "] - Error halting Task[" + name + "." + taskName
                        + "]", exc);
            }
            finally {
                tm = null;
            }
        }
    }

    public void killTasks()
    {
        killTasks(manager);
    }
    
    private void killTasks(TaskManager tm)
    {
        Iterator<String> i = tasks.keySet().iterator();
        while (i.hasNext()) {
            String taskN = i.next();

            Runnable rd = new TaskKiller(taskN, tm);
            BaseThread btd = new BaseThread(rd, "Task destroyer for: " + taskN);
            btd.setDaemon(true);
            btd.start();
        }
    }

    public void destroy()
    {
        killTasks(manager);
        manager = null;
    }

}
