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
package it.greenvulcano.scheduler.util.quartz.impl.calendar;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.scheduler.util.quartz.CalendarBuilder;
import it.greenvulcano.util.txt.DateUtils;
import it.greenvulcano.util.txt.TextUtils;

import java.util.List;
import java.util.TimeZone;

import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 20/nov/2011
 * @author GreenVulcano Developer Team
 */
public abstract class BaseCalendarBuilder implements CalendarBuilder
{
	private static org.slf4j.Logger     logger  = org.slf4j.LoggerFactory.getLogger(BaseCalendarBuilder.class);

    protected String      name        = "UNDEFINED";
    protected String      baseCalName = null;
    protected String      description = "";
    protected TimeZone    timeZone    = DateUtils.getDefaultTimeZone();

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.CalendarBuilder#init(org.w3c.dom.Node)
     */
    @Override
    public void init(Node node) throws TaskException
    {
        try {
            name = XMLConfig.get(node, "@name");
            baseCalName = XMLConfig.get(node, "@baseCalendarName");
            description = XMLConfig.get(node, "Description", "");
            timeZone = TimeZone.getTimeZone(XMLConfig.get(node, "@timeZone", DateUtils.getDefaultTimeZone().getID()));

            initCB(node);
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing CalendarBuilder", exc);
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.CalendarBuilder#registerCalendar(org.quartz.Scheduler)
     */
    @Override
    public Calendar registerCalendar(Scheduler scheduler) throws TaskException
    {
        try {
            Calendar calendar = createCalendar();
            if (baseCalName != null) {
                calendar.setBaseCalendar(scheduler.getCalendar(baseCalName));
            }
            logger.info("Registering Calendar[" + name + "]= " + calendar);
            scheduler.addCalendar(name, calendar, true, true);
            return calendar;
        }
        catch (Exception exc) {
            throw new TaskException("Error registering Calendar[" + name + "]", exc);
        }
    }

    protected abstract void initCB(Node node) throws TaskException;

    protected abstract Calendar createCalendar() throws TaskException;

    protected int[] parseRange(String range, int min, int max, int maxElem) throws TaskException
    {
        List<String> re = TextUtils.splitByStringSeparator(range, ",");
        if (re.size() > maxElem) {
            throw new TaskException("Invalid range[" + range + "] size > " + maxElem);
        }
        int[] iRange = new int[re.size()];
        for (int i = 0; i < iRange.length; i++) {
            int val = Integer.valueOf(re.get(i).trim());
            if ((min > val) || (val > max)) {
                throw new TaskException("Invalid range[" + range + "] value[" + i + "]: " + val);
            }
            iRange[i] = val;
        }
        return iRange;
    }
}
