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
import it.greenvulcano.util.thread.BaseThread;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 09/11/2011
 * @author GreenVulcano Developer Team
 */
public class TaskManagerFactory implements ConfigurationListener
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(TaskManagerFactory.class);
    public static final String        CFG_FILE_NAME   = "GVTaskManagerFactory-Configuration.xml";
    private static TaskManagerFactory instance        = null;
    private Map<String, TaskManager>  managers        = new HashMap<String, TaskManager>();
    private boolean                   confChangedFlag = false;
    private String                    descriptorName;

    private TaskManagerFactory()
    {
        // do nothing
    }

    public static synchronized TaskManagerFactory instance() throws TaskException
    {
        if (instance == null) {
            try {
                logger.info("Initialing TaskManagerFactory");
                instance = new TaskManagerFactory();
                instance.init();
                XMLConfig.addConfigurationListener(instance, CFG_FILE_NAME);
            }
            catch (Exception exc) {
                logger.info("Error initialing TaskManagerFactory", exc);
                if (instance != null) {
                    instance.destroy();
                }
                instance = null;
            }
        }
        return instance;
    }

    public TaskManager getTaskManager(String name) throws TaskException
    {
        TaskManager manager = managers.get(name);
        if (manager == null) {
            throw new TaskException("TaskManager[" + name + "] not configured.");
        }

        return manager;
    }

    public void executeTask(String sName, String gName, String tName, JobExecutionContext context) throws TaskException
    {
        getTaskManager(sName).execTask(gName, tName, context);
    }

    /**
     * @return Returns the descriptorName.
     */
    public String getDescriptorName()
    {
        return descriptorName;
    }

    /*
     * (non-Javadoc)
     *
     * @see it.eai.utils.config.ConfigurationListener#configurationChanged(it.eai.utils.config.ConfigurationEvent)
     */
    @Override
    public synchronized void configurationChanged(ConfigurationEvent event)
    {
        if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE_NAME)) {
            confChangedFlag = true;
            // destroy now
            killTaskManagers();
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

            BaseThread bt = new BaseThread(rr, "Config reloader for TaskManagerFactory");
            bt.setDaemon(true);
            bt.start();
        }
    }

    private void init() throws TaskException
    {
        try {
            descriptorName = XMLConfig.get(CFG_FILE_NAME, "/GVTaskManagerFactory/@jmx-descriptor-name");
            NodeList nl = XMLConfig.getNodeList(CFG_FILE_NAME, "/GVTaskManagerFactory/*[@type='task-manager']");
            if ((nl != null) && (nl.getLength() > 0)) {
                for (int i = 0; i < nl.getLength(); i++) {
                    String name = null;
                    try {
                        Node n = nl.item(i);
                        name = XMLConfig.get(n, "@name");
                        String cname = XMLConfig.get(n, "@class");
                        String cfile = XMLConfig.get(n, "@config-file");
                        Constructor<?> ctr = Class.forName(cname).getConstructor(
                                new Class[]{String.class, String.class});
                        TaskManager taskM = (TaskManager) ctr.newInstance(new Object[]{name, cfile});
                        logger.info("Initialized TaskManager[" + taskM.getName() + "]");
                        managers.put(taskM.getName(), taskM);
                    }
                    catch (Exception exc) {
                        logger.error("Error initializing TaskManager[" + name + "]", exc);
                    }
                }
            }
            confChangedFlag = false;
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing TaskManagerFactory", exc);
        }
    }

    private void reinit()
    {
        if (!confChangedFlag) {
            return;
        }
        confChangedFlag = false;
        killTaskManagers();
        try {
            init();
        }
        catch (Exception exc) {
            logger.info("Error reinitialing TaskManagerFactory", exc);
            if (instance != null) {
                instance.destroy();
            }
            instance = null;
        }
    }

    private void destroy()
    {
        XMLConfig.removeConfigurationListener(instance);
        killTaskManagers();
    }

    private void killTaskManagers()
    {
        Iterator<String> i = managers.keySet().iterator();
        while (i.hasNext()) {
            TaskManager tmanager = null;
            try {
                tmanager = managers.get(i.next());
                tmanager.destroy();
            }
            catch (Exception exc) {
                logger.error("Error destroing TaskManager[" + ((tmanager != null) ? tmanager.getName() : "undefined")
                        + "]", exc);
            }
        }
        managers.clear();
    }
}
