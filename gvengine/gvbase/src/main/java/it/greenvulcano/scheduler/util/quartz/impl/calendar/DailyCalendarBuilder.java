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
import org.quartz.impl.calendar.DailyCalendar;
import org.w3c.dom.Node;

/**
 * @version 3.2.0 27/nov/2011
 * @author GreenVulcano Developer Team
 */
public class DailyCalendarBuilder extends BaseCalendarBuilder
{
    private String  rangeStartingTime = null;
    private String  rangeEndingTime   = null;
    private boolean invertTimeRange   = false;

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#initCB(org.w3c.dom.Node)
     */
    @Override
    protected void initCB(Node node) throws TaskException
    {
        try {
            rangeStartingTime = XMLConfig.get(node, "@rangeStartingTime");
            rangeEndingTime = XMLConfig.get(node, "@rangeEndingTime");
            invertTimeRange = XMLConfig.getBoolean(node, "@invertTimeRange", false);
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing DailyCalendarBuilder", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#createCalendar()
     */
    @Override
    protected Calendar createCalendar() throws TaskException
    {
        DailyCalendar dc = new DailyCalendar(rangeStartingTime, rangeEndingTime);
        dc.setTimeZone(timeZone);
        dc.setInvertTimeRange(invertTimeRange);
        return dc;
    }

}
