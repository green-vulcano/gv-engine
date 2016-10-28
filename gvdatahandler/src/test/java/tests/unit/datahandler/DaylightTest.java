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
package tests.unit.datahandler;

import it.greenvulcano.gvesb.datahandling.utils.HourChangeUtil;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Locale;

/**
 * @version 3.0.0 Mar 31, 2010
 * @author GreenVulcano Developer Team
 *
 */
public class DaylightTest extends TestCase
{
    /**
     * @throws Exception
     */
    public final void testDaylight() throws Exception
    {
        Calendar cal = Calendar.getInstance(Locale.ITALIAN);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.YEAR, 2005);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        assertTrue(HourChangeUtil.isDayOfLegalToSolarChange(cal.getTime()));
        assertFalse(HourChangeUtil.isDayOfSolarToLegalChange(cal.getTime()));
    }
}
