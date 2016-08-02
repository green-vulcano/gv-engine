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
package it.greenvulcano.scheduler.util.quartz.impl.calendar;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.scheduler.TaskException;
import it.greenvulcano.util.txt.DateUtils;

import java.util.ArrayList;

import org.quartz.Calendar;
import org.quartz.impl.calendar.AnnualCalendar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 3.2.0 27/nov/2011
 * @author GreenVulcano Developer Team
 */
public class AnnualCalendarBuilder extends BaseCalendarBuilder
{
    private ArrayList<java.util.Calendar> days = new ArrayList<java.util.Calendar>();

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#initCB(org.w3c.dom.Node)
     */
    @Override
    protected void initCB(Node node) throws TaskException
    {
        try {
            NodeList nl = XMLConfig.getNodeList(node, "ExcludedDay");

            if ((nl != null) && (nl.getLength() > 0)) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    String val[] = XMLConfig.get(n, "@day").split("/");
                    java.util.Calendar cal = DateUtils.createCalendar();
                    cal.set(java.util.Calendar.MONTH, Integer.valueOf(val[1]) - 1);
                    cal.set(java.util.Calendar.DAY_OF_MONTH, Integer.valueOf(val[0]));
                    days.add(cal);
                }
            }
        }
        catch (Exception exc) {
            throw new TaskException("Error initializing AnnualCalendarBuilder", exc);
        }
    }

    /* (non-Javadoc)
     * @see it.greenvulcano.scheduler.util.quartz.impl.BaseCalendarBuilder#createCalendar()
     */
    @Override
    protected Calendar createCalendar() throws TaskException
    {
        AnnualCalendar ac = new AnnualCalendar();
        ac.setTimeZone(timeZone);
        ac.setDaysExcluded(days);
        return ac;
    }

}
