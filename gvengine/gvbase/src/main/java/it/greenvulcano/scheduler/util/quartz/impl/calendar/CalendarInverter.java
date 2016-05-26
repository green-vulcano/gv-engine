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

import org.quartz.Calendar;

/**
 * @version 3.2.0 28/nov/2011
 * @author GreenVulcano Developer Team
 */
public class CalendarInverter implements Calendar
{
    private static final long serialVersionUID = 1L;
	
    private Calendar toAdapt         = null;
    private boolean  invertTimeRange = false;

    public CalendarInverter(Calendar toAdapt, boolean invertTimeRange)
    {
        this.toAdapt = toAdapt;
        this.invertTimeRange = invertTimeRange;
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#setBaseCalendar(org.quartz.Calendar)
     */
    @Override
    public void setBaseCalendar(Calendar cal)
    {
        toAdapt.setBaseCalendar(cal);
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#getBaseCalendar()
     */
    @Override
    public Calendar getBaseCalendar()
    {
        return toAdapt.getBaseCalendar();
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#isTimeIncluded(long)
     */
    @Override
    public boolean isTimeIncluded(long time)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#getNextIncludedTime(long)
     */
    @Override
    public long getNextIncludedTime(long time)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#getDescription()
     */
    @Override
    public String getDescription()
    {
        return toAdapt.getDescription();
    }

    /* (non-Javadoc)
     * @see org.quartz.Calendar#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String descr)
    {
        toAdapt.setDescription(descr);
    }

    @Override
    public Object clone()
    {
        Calendar cloneA = (Calendar) toAdapt.clone();
        return new CalendarInverter(cloneA, invertTimeRange);
    }
}
