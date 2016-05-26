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

import org.quartz.Calendar;
import org.quartz.impl.calendar.MonthlyCalendar;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 27/nov/2011
 * @author GreenVulcano Developer Team
 */
public class MonthlyCalendarBuilder extends BaseCalendarBuilder
{
    private boolean[] days = new boolean[31];

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#initCB(org.w3c.dom.Node)
     */
    @Override
    protected void initCB(Node node) throws TaskException
    {
        try {
            String excludedDays = XMLConfig.get(node, "@excludedDays", "");

            int[] ir = parseRange(excludedDays, 1, 31, 31);
            for (int i = 0; i < ir.length; i++) {
                days[ir[i] - 1] = true;
            }
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing MonthlyCalendarBuilder", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#createCalendar()
     */
    @Override
    protected Calendar createCalendar() throws TaskException
    {
        MonthlyCalendar wc = new MonthlyCalendar();
        wc.setTimeZone(timeZone);
        wc.setDaysExcluded(days);
        return wc;
    }

}
