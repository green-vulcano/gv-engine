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
package it.greenvulcano.scheduler.util.quartz.impl;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.j2ee.db.connections.impl.ConnectionBuilder;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.scheduler.util.quartz.CalendarBuilder;
import it.greenvulcano.scheduler.util.quartz.SchedulerBuilder;
import it.greenvulcano.util.txt.DateUtils;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.core.SchedulingContext;
import org.quartz.impl.SchedulerRepository;
import org.quartz.impl.StdJobRunShellFactory;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.plugins.management.ShutdownHookPlugin;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.utils.DBConnectionManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public class GVSchedulerBuilder implements SchedulerBuilder
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(GVSchedulerBuilder.class);

    private int                   maxThreads             = 5;
    private int                   misfireThreshold       = 60000;
    private int                   clusterCheckinInterval = 15000;
    private String                storeType              = "RamStore";
    private String                jdbcConnectionName     = "UNDEFINED";
    private ConnectionBuilder     cBuilder               = null;
    private String                driverDelegate         = "UNDEFINED";
    private String                tablePrefix            = "UNDEFINED";
    private List<CalendarBuilder> calendars              = new ArrayList<CalendarBuilder>();

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.SchedulerBuilder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws TaskException
    {
        try {
            maxThreads = XMLConfig.getInteger(node, "@maxThreads", 5);
            Node st = XMLConfig.getNode(node, "*[@type='quartz-store']");
            storeType = st.getLocalName();
            if ("RamStore".equals(storeType)) {
                misfireThreshold = XMLConfig.getInteger(st, "@misfireThreshold", 60000);
            }
            else if ("JdbcStore".equals(storeType)) {
                driverDelegate = XMLConfig.get(st, "@driverDelegate");
                tablePrefix = XMLConfig.get(st, "@tablePrefix");
                misfireThreshold = XMLConfig.getInteger(st, "@misfireThreshold", 60000);
                clusterCheckinInterval = XMLConfig.getInteger(st, "@clusterCheckinInterval", 15000);

                Node cbn = XMLConfig.getNode(st, "*[@type='jdbc-connection-builder']");
                String className = XMLConfig.get(cbn, "@class");
                jdbcConnectionName = XMLConfig.get(cbn, "@name");
                cBuilder = (ConnectionBuilder) Class.forName(className).newInstance();
                cBuilder.init(cbn);
            }
            else {
                throw new TaskException("Invalid Qartz store type: " + storeType);
            }

            NodeList cnl = XMLConfig.getNodeList(node, "Calendars/*[@type='cron-calendar']");
            if ((cnl != null) && (cnl.getLength() > 0)) {
                for (int i = 0; i < cnl.getLength(); i++) {
                    Node n = cnl.item(i);
                    CalendarBuilder cb = (CalendarBuilder) Class.forName(XMLConfig.get(n, "@class")).newInstance();
                    cb.init(n);
                    calendars.add(cb);
                    logger.debug("Added CalendarBuilder: " + cb);
                }
            }
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing GVSchedulerBuilder", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.SchedulerBuilder#getScheduler(java.lang.String)
     */
    @Override
    public synchronized Scheduler getScheduler(String schedulerName) throws TaskException
    {
        try {
            SchedulerRepository schedRep = SchedulerRepository.getInstance();
            Scheduler scheduler = schedRep.lookup(schedulerName);
            if (scheduler == null) {
                String instanceId = schedulerName + "#"
                        + DateUtils.nowToString(DateUtils.FORMAT_ISO_TIMESTAMP_L);

                logger.debug("BEGIN - creating Scheduler[" + instanceId + "]");

                // create the thread pool
                SimpleThreadPool thPool = new SimpleThreadPool(maxThreads, Thread.NORM_PRIORITY);
                thPool.setInstanceName(schedulerName);
                thPool.setInstanceId(instanceId);
                thPool.setMakeThreadsDaemons(true);
                thPool.setThreadNamePrefix(instanceId);

                // create the job store
                JobStore jobStore = null;
                if ("JdbcStore".equals(storeType)) {
                    DBConnectionManager.getInstance().addConnectionProvider(jdbcConnectionName,
                            new GVQuartzConnectionProvider(cBuilder));
                    jobStore = new JobStoreTX();
                    ((JobStoreTX) jobStore).setDataSource(jdbcConnectionName);
                    ((JobStoreTX) jobStore).setDriverDelegateClass(driverDelegate);
                    ((JobStoreTX) jobStore).setTablePrefix(tablePrefix);
                    ((JobStoreTX) jobStore).setInstanceId(instanceId);
                    ((JobStoreTX) jobStore).setMisfireThreshold(misfireThreshold);
                    ((JobStoreTX) jobStore).setIsClustered(true);
                    ((JobStoreTX) jobStore).setClusterCheckinInterval(clusterCheckinInterval);
                }
                else {
                    jobStore = new RAMJobStore();
                    ((RAMJobStore) jobStore).setMisfireThreshold(misfireThreshold);
                }

                JobRunShellFactory jrsf = new StdJobRunShellFactory();

                SchedulingContext schedCtxt = new SchedulingContext();
                schedCtxt.setInstanceId(instanceId);

                QuartzSchedulerResources qrs = new QuartzSchedulerResources();
                qrs.setName(schedulerName);
                qrs.setInstanceId(instanceId);
                qrs.setThreadName(schedulerName + "_QuartzSchedulerThread");
                qrs.setJobRunShellFactory(jrsf);
                qrs.setJMXExport(false);
                /*if (jmxObjectName != null)
                   qrs.setJMXObjectName(jmxObjectName);*/
                qrs.setInterruptJobsOnShutdownWithWait(true);
                qrs.setMakeSchedulerThreadDaemon(true);
                qrs.setRunUpdateCheck(false);

                qrs.setThreadPool(thPool);
                thPool.initialize();
                qrs.setJobStore(jobStore);

                SchedulerPlugin shPlugin = new ShutdownHookPlugin();
                ((ShutdownHookPlugin) shPlugin).setCleanShutdown(true);
                qrs.addSchedulerPlugin(shPlugin);

                QuartzScheduler qs = new QuartzScheduler(qrs, schedCtxt, 30000, 15000);

                ClassLoadHelper cch = new CascadingClassLoadHelper();
                cch.initialize();
                jobStore.initialize(cch, qs.getSchedulerSignaler());

                SchedulingContext schedCtxt2 = new SchedulingContext();
                schedCtxt.setInstanceId(qrs.getInstanceId());

                scheduler = new StdScheduler(qs, schedCtxt2);

                shPlugin.initialize(qrs.getInstanceId(), scheduler);

                jrsf.initialize(scheduler, schedCtxt);
                qs.initialize();

                /*
                Iterator pluginEntryIter;
                if (schedulerPluginMap != null) {
                    for (pluginEntryIter = schedulerPluginMap.entrySet().iterator(); pluginEntryIter.hasNext();) {
                        Map.Entry pluginEntry = (Map.Entry) pluginEntryIter.next();

                        ((SchedulerPlugin) pluginEntry.getValue()).initialize((String) pluginEntry.getKey(), scheduler);
                    }

                }*/
                logger.info("Quartz scheduler '" + scheduler.getSchedulerName());
                logger.info("Quartz scheduler version: " + qs.getVersion());

                qs.addNoGCObject(schedRep);

                // initialize Calendars
                for (CalendarBuilder cal : calendars) {
                    cal.registerCalendar(scheduler);
                }

                schedRep.bind(scheduler);
                
                // Add default trigger listener
                scheduler.addGlobalTriggerListener(new GVTriggerListener());
                
                scheduler.start();
                logger.debug("END - created Scheduler[" + instanceId + "]");
            }

            return scheduler;
        }
        catch (Exception exc) {
            throw new TaskException("Error creating Scheduler[" + schedulerName + "]", exc);
        }
    }

    @Override
    public Calendar getCalendar(String schedName, String calName) throws TaskException
    {
        try {
            return getScheduler(schedName).getCalendar(calName);
        }
        catch (SchedulerException exc) {
            throw new TaskException("Error obtaining Calendar[" + calName + "] of Scheduler[" + schedName + "]", exc);
        }
    }

    @Override
    public String toString()
    {
        String desc = "GVSchedulerBuilder:";
        if ("JdbcStore".equals(storeType)) {
            desc += " - JdbcStore[" + jdbcConnectionName + "/" + tablePrefix + "/" + driverDelegate + "]";
        }
        else {
            desc += " - RamStore";
        }
        desc += " - maxThreads[" + maxThreads + "]";
        return desc;
    }
}
