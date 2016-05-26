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
package it.greenvulcano.scheduler.util.quartz.impl.trigger;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.Task;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.scheduler.util.quartz.TriggerBuilder;
import it.greenvulcano.util.txt.DateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public abstract class BaseTriggerBuilder implements TriggerBuilder
{
    protected Map<String, String> properties   = new HashMap<String, String>();
    protected String              group        = "UNDEFINED";
    protected String              taskName     = "UNDEFINED";
    protected String              name         = "UNDEFINED";
    protected boolean             enabled      = true;
    protected String              calendarName = null;
    protected TimeZone            timeZone     = DateUtils.getDefaultTimeZone();

    protected String              misfire      = "smart-policy";
    protected Map<String, Integer> misfires    = new HashMap<String, Integer>();
    
    {
        misfires.put("smart-policy", Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.TriggerBuilder#init(java.lang.String, java.lang.String, org.w3c.dom.Node)
     */
    @Override
    public void init(String group, String task, Node node) throws TaskException
    {
        try {
            this.group = group;
            this.taskName = task;
            name = XMLConfig.get(node, "@name");
            enabled = XMLConfig.getBoolean(node, "@enabled", true);
            calendarName = XMLConfig.get(node, "@calendarName", null);
            timeZone = TimeZone.getTimeZone(XMLConfig.get(node, "@timeZone", DateUtils.getDefaultTimeZone().getID()));
            misfire = XMLConfig.get(node, "@misfireMode", "smart-policy");
            NodeList pnl = XMLConfig.getNodeList(node, "TgProperties/PropertyDef");
            if ((pnl != null) && (pnl.getLength() > 0)) {
                for (int i = 0; i < pnl.getLength(); i++) {
                    Node n = pnl.item(i);
                    properties.put(XMLConfig.get(n, "@name"), XMLConfig.get(n, "@value"));
                }
            }
            initTB(node);
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing TriggerBuilder", exc);
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.TriggerBuilder#newTrigger()
     */
    @Override
    public Trigger newTrigger() throws TaskException
    {
        try {
            Trigger trigger = createTrigger();
            if (calendarName != null) {
                trigger.setCalendarName(calendarName);
            }
            updateJobDataMap(trigger);
            return trigger;
        }
        catch (Exception exc) {
            throw new TaskException("Error creating Trigger[" + group + "." + name + "]", exc);
        }
    }

    protected int getMisfireMode() {
        return misfires.get(misfire);
    }

    protected abstract void initTB(Node node) throws TaskException;

    protected abstract Trigger createTrigger() throws TaskException;

    protected void updateJobDataMap(Trigger trigger) throws TaskException
    {
        try {
            JobDataMap jdm = trigger.getJobDataMap();
            for (Map.Entry<String, String> prop : properties.entrySet()) {
                jdm.put(prop.getKey(), prop.getValue());
            }
            jdm.put(Task.TASK_MISFIRE_RUN, "FALSE");
        }
        catch (Exception exc) {
            throw new TaskException("Error creating updating JobDataMap[" + group + "." + name + " - "
                    + trigger.getName() + "]", exc);
        }
    }
}
