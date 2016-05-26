/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 * 
 * This file is part of GreenVulcano ESB.
 * 
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package tests.unit.util.txt;

import it.greenvulcano.util.txt.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * 
 * @version 3.0.0 17/giu/2010
 * @author GreenVulcano Developer Team
 */
public class DateUtilsTestCase extends TestCase
{

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#createCalendar()}.
     */
    @Test
    public void testCreateCalendar()
    {
        Calendar cal = DateUtils.createCalendar();
        assertNotNull("Unable to create Calendar instance", cal);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#stringToDate(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testStringToDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss.SSS");
        sdf.setLenient(false);
        sdf.setTimeZone(DateUtils.getDefaultTimeZone());
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("String to Date failed", now, DateUtils.stringToDate(str, "yyyyMMdd HHmmss.SSS"));
        assertEquals("String to Date failed", now,
                DateUtils.stringToDate("" + now.getTime(), DateUtils.FORMAT_SYSTEM_TIME));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#stringToDate(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testStringToDateTimeZone()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss.SSS");
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("String to Date TimeZone failed", now, DateUtils.stringToDate(str, "yyyyMMdd HHmmss.SSS", "UTC"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#stringToDate(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testStringToDateTimeZoneLang()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HHmmss.SSS", Locale.ITALIAN);
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("String to Date TimeZone Lang failed", now,
                DateUtils.stringToDate(str, "yyyy-MMM-dd HHmmss.SSS", "Europe/Rome", "it"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#dateToString(java.util.Date, java.lang.String)}
     * .
     */
    @Test
    public void testDateToString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss.SSS");
        sdf.setLenient(false);
        sdf.setTimeZone(DateUtils.getDefaultTimeZone());
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Date to String failed", str, DateUtils.dateToString(now, "yyyyMMdd HHmmss.SSS"));
        assertEquals("Date to String failed", "" + now.getTime(),
                DateUtils.dateToString(now, DateUtils.FORMAT_SYSTEM_TIME));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#dateToString(java.util.Date, java.lang.String)}
     * .
     */
    @Test
    public void testDateToStringTimeZone()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss.SSS");
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Date to String TimeZone failed", str, DateUtils.dateToString(now, "yyyyMMdd HHmmss.SSS", "UTC"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#dateToString(java.util.Date, java.lang.String)}
     * .
     */
    @Test
    public void testDateToStringTimeZoneLang()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HHmmss.SSS", Locale.ITALIAN);
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Date to String TimeZone Lang failed", str,
                DateUtils.dateToString(now, "yyyy-MMM-dd HHmmss.SSS", "Europe/Rome", "it"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertString(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertStringDateFormat() throws Exception
    {
        String str = "12/01/2010 12:35:27.340";
        assertEquals("Convert String Date Format failed", str,
                DateUtils.convertString("20100112 123527.340", "yyyyMMdd HHmmss.SSS", "dd/MM/yyyy HH:mm:ss.SSS"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        sdf.setLenient(false);
        sdf.setTimeZone(DateUtils.getDefaultTimeZone());
        Date date = sdf.parse(str);
        assertEquals("Convert String Date Format failed", str,
                DateUtils.convertString("" + date.getTime(), DateUtils.FORMAT_SYSTEM_TIME, "dd/MM/yyyy HH:mm:ss.SSS"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertString(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertStringDateFormatTimeZone()
    {
        String str1 = "2010-01-12 12:35";
        String str2 = "2010-01-12T11:35+0000";
        assertEquals("Convert String Date Format TimeZone failed", str2,
                DateUtils.convertString(str1, "yyyy-MM-dd HH:mm", "Europe/Rome", "yyyy-MM-dd'T'HH:mmZ", "GMT"));

        String str3 = "2010-04-12 12:35";
        String str4 = "2010-04-12T10:35+0000";
        assertEquals("Convert String Date Format TimeZone failed", str4,
                DateUtils.convertString(str3, "yyyy-MM-dd HH:mm", "Europe/Rome", "yyyy-MM-dd'T'HH:mmZ", "GMT"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertString(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertISO8601StringDateFormatTimeZone()
    {
    	assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 00:00:00",
    			DateUtils.convertString("2014-09-01T00:00:00+02:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 00:00:00",
                DateUtils.convertString("2014-08-31T22:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));

        assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 01:00:00",
    			DateUtils.convertString("2014-09-01T01:00:00+02:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 01:00:00",
                DateUtils.convertString("2014-08-31T23:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
    	
        assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 02:00:00",
    			DateUtils.convertString("2014-09-01T02:00:00+02:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone CEST failed", "2014-09-01 02:00:00",
                DateUtils.convertString("2014-09-01T00:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));

        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 00:00:00",
    			DateUtils.convertString("2014-11-01T00:00:00.000+01:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 00:00:00",
                DateUtils.convertString("2014-10-31T23:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));

        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 01:00:00",
    			DateUtils.convertString("2014-11-01T01:00:00.000+01:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 01:00:00",
                DateUtils.convertString("2014-11-01T00:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));

        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 02:00:00",
    			DateUtils.convertString("2014-11-01T02:00:00.000+01:00", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
        assertEquals("Convert ISO8601 String Date Format TimeZone EST failed", "2014-11-01 02:00:00",
                DateUtils.convertString("2014-11-01T01:00:00Z", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT", "yyyy-MM-dd HH:mm:ss", "Europe/Rome"));
    }
    
    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertString(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertISO8601StringDateFormatTimeZone2()
    {
        String dateTime = DateUtils.convertString("2014-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT");
    	assertTrue("Convert ISO8601 String Date Format TimeZone CEST failed(" + dateTime + ")", dateTime.equals("2014-09-01T00:00:00+02:00") || dateTime.equals("2014-08-31T22:00:00Z"));

    	dateTime = DateUtils.convertString("2014-09-01 01:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT");
    	assertTrue("Convert ISO8601 String Date Format TimeZone CEST failed(" + dateTime + ")", dateTime.equals("2014-09-01T01:00:00+02:00") || dateTime.equals("2014-08-31T23:00:00Z"));

    	dateTime = DateUtils.convertString("2014-09-01 02:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT");
    	assertTrue("Convert ISO8601 String Date Format TimeZone CEST failed(" + dateTime + ")", dateTime.equals("2014-09-01T02:00:00+02:00") || dateTime.equals("2014-09-01T00:00:00Z"));

    	dateTime = DateUtils.convertString("2014-11-01 00:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT");
    	assertTrue("Convert ISO8601 String Date Format TimeZone EST failed(" + dateTime + ")", dateTime.equals("2014-11-01T00:00:00+01:00") || dateTime.equals("2014-10-31T23:00:00Z"));

    	dateTime = DateUtils.convertString("2014-11-01 01:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT");
    	assertTrue("Convert ISO8601 String Date Format TimeZone EST failed(" + dateTime + ")", dateTime.equals("2014-11-01T01:00:00+01:00") || dateTime.equals("2014-11-01T00:00:00Z"));
    	
    	dateTime = DateUtils.convertString("2014-11-01 02:00:00", "yyyy-MM-dd HH:mm:ss", "Europe/Rome", "yyyy-MM-dd'T'HH:mm:ss.SSSX", "GMT"); 
    	assertTrue("Convert ISO8601 String Date Format TimeZone EST failed(" + dateTime + ")", dateTime.equals("2014-11-01T02:00:00+01:00") || dateTime.equals("2014-11-01T01:00:00Z"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertString(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertStringDateFormatTimeZoneLang()
    {
        String str1 = "2010-Gen-12 12:35";
        String str2 = "2010-Jan-12T11:35+0000";
        assertEquals("Convert String Date Format TimeZone Lang failed", str2, DateUtils.convertString(str1,
                "yyyy-MMM-dd HH:mm", "Europe/Rome", "it", "yyyy-MMM-dd'T'HH:mmZ", "GMT", "en"));

        String str3 = "2010-Mag-12 12:35";
        String str4 = "2010-May-12T10:35+0000";
        assertEquals("Convert String Date Format TimeZone Lang failed", str4, DateUtils.convertString(str3,
                "yyyy-MMM-dd HH:mm", "Europe/Rome", "it", "yyyy-MMM-dd'T'HH:mmZ", "GMT", "en"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#convertDayHourString(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testConvertDayHourString()
    {
        String str1 = "2010-01-12";
        String str2 = "2010-01-12T12:00+0100";
        assertEquals("Convert String Day Hour Format TimeZone failed", str2,
                DateUtils.convertDayHourString(str1, "yyyy-MM-dd", 13, "yyyy-MM-dd'T'HH:mmZ", "Europe/Rome"));

        String str3 = "2010-04-12";
        String str4 = "2010-04-12T23:00+0200";
        assertEquals("Convert String Day Hour Format TimeZone failed", str4,
                DateUtils.convertDayHourString(str3, "yyyy-MM-dd", 24, "yyyy-MM-dd'T'HH:mmZ", "Europe/Rome"));

    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#nowToString(java.lang.String)}.
     */
    @Test
    public void testNowToString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm");
        sdf.setLenient(false);
        sdf.setTimeZone(DateUtils.getDefaultTimeZone());
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Now to String failed", str, DateUtils.nowToString("yyyyMMdd HHmm"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#nowToString(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testNowToStringTimeZone()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm");
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Now to String TimeZone failed", str, DateUtils.nowToString("yyyyMMdd HHmm", "UTC"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#nowToString(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testNowToStringTimeZoneLang()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HHmm", Locale.ITALIAN);
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
        Date now = new Date();
        String str = sdf.format(now);

        assertEquals("Now to String TimeZone Locale failed", str,
                DateUtils.nowToString("yyyy-MMM-dd HHmm", "Europe/Rome", "it"));
    }

    /**
     * Test method for {@link it.greenvulcano.util.txt.DateUtils#getTomorrow()}.
     */
    @Test
    public void testGetTomorrow()
    {
        Calendar cal = Calendar.getInstance(DateUtils.getDefaultTimeZone());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        assertEquals("Tomorrow failed", cal.getTime(), DateUtils.getTomorrow());
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#tomorrowToString(java.lang.String)}
     * .
     */
    @Test
    public void testTomorrowToString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String str = sdf.format(cal.getTime());

        assertEquals("Tomorrow to String failed", str, DateUtils.tomorrowToString("yyyyMMdd"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#compare(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCompareString()
    {
        assertEquals("Compare String failed", 1, DateUtils.compare("20100110", "yyyyMMdd", "09/01/2010", "dd/MM/yyyy"));
        assertEquals("Compare String failed", 0, DateUtils.compare("20100110", "yyyyMMdd", "10/01/2010", "dd/MM/yyyy"));
        assertEquals("Compare String failed", -1, DateUtils.compare("20100110", "yyyyMMdd", "19/01/2010", "dd/MM/yyyy"));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#compare(java.lang.String, java.lang.String, java.util.Date)}
     * .
     */
    @Test
    public void testCompareStringDate()
    {
        Date date = DateUtils.stringToDate("20100110", "yyyyMMdd");
        assertEquals("Compare String failed", -1, DateUtils.compare("09/01/2010", "dd/MM/yyyy", date));
        assertEquals("Compare String failed", 0, DateUtils.compare("10/01/2010", "dd/MM/yyyy", date));
        assertEquals("Compare String failed", 1, DateUtils.compare("19/01/2010", "dd/MM/yyyy", date));
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekStart(java.util.Date)}.
     */
    @Test
    public void testGetWeekStartDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekEnd(java.util.Date)}.
     */
    @Test
    public void testGetWeekEndDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekStart(int, int)}.
     */
    @Test
    public void testGetWeekStartIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekEnd(int, int)}.
     */
    @Test
    public void testGetWeekEndIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekStart(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetWeekStartStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekEnd(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetWeekEndStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekStart(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetWeekStartStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getWeekEnd(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetWeekEndStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthStart(java.util.Date)}.
     */
    @Test
    public void testGetMonthStartDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthEnd(java.util.Date)}.
     */
    @Test
    public void testGetMonthEndDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthStart(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetMonthStartStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthEnd(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetMonthEndStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthStart(int, int)}.
     */
    @Test
    public void testGetMonthStartIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthEnd(int, int)}.
     */
    @Test
    public void testGetMonthEndIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthStart(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetMonthStartStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getMonthEnd(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetMonthEndStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getQuarterStart(int, int)}.
     */
    @Test
    public void testGetQuarterStartIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getQuarterEnd(int, int)}.
     */
    @Test
    public void testGetQuarterEndIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getQuarterStart(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetQuarterStartStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getQuarterEnd(java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetQuarterEndStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setTime(java.lang.String, java.lang.String, int, int)}
     * .
     */
    @Test
    public void testSetTimeStringStringIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setTime(java.util.Date, int, int)}
     * .
     */
    @Test
    public void testSetTimeDateIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#addTime(java.lang.String, java.lang.String, int, int)}
     * .
     */
    @Test
    public void testAddTimeStringStringIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#addTime(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testAddTimeStringStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#addTime(java.util.Date, int, int)}
     * .
     */
    @Test
    public void testAddTimeDateIntInt()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#addTime(int, int, boolean)}.
     */
    @Test
    public void testAddTimeIntIntBoolean()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#copyDate(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testCopyDateDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#copyDate(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testCopyDateCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#copyTime(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testCopyTimeDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#copyTime(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testCopyTimeCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#resetTime(java.util.Date)}.
     */
    @Test
    public void testResetTimeDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#resetTime(java.util.Calendar)}.
     */
    @Test
    public void testResetTimeCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setStartOfDay(java.util.Date)}.
     */
    @Test
    public void testSetStartOfDayDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setStartOfDay(java.util.Calendar)}
     * .
     */
    @Test
    public void testSetStartOfDayCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setEndOfDay(java.util.Date)}.
     */
    @Test
    public void testSetEndOfDayDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#setEndOfDay(java.util.Calendar)}
     * .
     */
    @Test
    public void testSetEndOfDayCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#generateRange(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGenerateRangeStringStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#generateRange(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testGenerateRangeDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#generateRange(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testGenerateRangeCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDiff(java.util.Date, java.util.Date, java.util.TimeZone)}
     * .
     */
    @Test
    public void testGetDiffDateDateTimeZone()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDiff(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.TimeZone)}
     * .
     */
    @Test
    public void testGetDiffStringStringStringStringTimeZone()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDaySolar(java.util.Date)}.
     */
    @Test
    public void testIsDaySolarDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDaySolar(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testIsDaySolarStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayLegal(java.util.Date)}.
     */
    @Test
    public void testIsDayLegalDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayLegal(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testIsDayLegalStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfHourChange(java.util.Date)}
     * .
     */
    @Test
    public void testIsDayOfHourChangeDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfHourChange(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testIsDayOfHourChangeStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDayOfSolarToLegalChange()}.
     */
    @Test
    public void testGetDayOfSolarToLegalChange()
    {
        System.out.println("Day Of Solar To Legal Change: " + DateUtils.getDayOfSolarToLegalChange());
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDayOfSolarToLegalChange(int)}
     * .
     */
    @Test
    public void testGetDayOfSolarToLegalChangeYear()
    {
        Date date = DateUtils.getDayOfSolarToLegalChange(2008);
        System.out.println("Day Of Solar To Legal Change: " + date);
        assertEquals(DateUtils.stringToDate("20080330", "yyyyMMdd"), date);

        date = DateUtils.getDayOfSolarToLegalChange(2009);
        System.out.println("Day Of Solar To Legal Change: " + date);
        assertEquals(DateUtils.stringToDate("20090329", "yyyyMMdd"), date);

        date = DateUtils.getDayOfSolarToLegalChange(2010);
        System.out.println("Day Of Solar To Legal Change: " + date);
        assertEquals(DateUtils.stringToDate("20100328", "yyyyMMdd"), date);

        date = DateUtils.getDayOfSolarToLegalChange(2011);
        System.out.println("Day Of Solar To Legal Change: " + date);
        assertEquals(DateUtils.stringToDate("20110327", "yyyyMMdd"), date);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfSolarToLegalChange(java.util.Date)}
     * .
     */
    @Test
    public void testIsDayOfSolarToLegalChangeDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfSolarToLegalChange(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testIsDayOfSolarToLegalChangeStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfSolarToLegalChange(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testEncloseDayOfSolarToLegalChangeStringStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfSolarToLegalChange(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testEncloseDayOfSolarToLegalChangeDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfSolarToLegalChange(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testEncloseDayOfSolarToLegalChangeCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDayOfLegalToSolarChange()}.
     */
    @Test
    public void testGetDayOfLegalToSolarChange()
    {
        System.out.println("Day Of Legal To Solar Change: " + DateUtils.getDayOfLegalToSolarChange());
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getDayOfLegalToSolarChange(int)}
     * .
     */
    @Test
    public void testGetDayOfLegalToSolarChangeYear()
    {
        Date date = DateUtils.getDayOfLegalToSolarChange(2008);
        System.out.println("Day Of Legal To Solar Change: " + date);
        assertEquals(DateUtils.stringToDate("20081026", "yyyyMMdd"), date);

        date = DateUtils.getDayOfLegalToSolarChange(2009);
        System.out.println("Day Of Legal To Solar Change: " + date);
        assertEquals(DateUtils.stringToDate("20091025", "yyyyMMdd"), date);

        date = DateUtils.getDayOfLegalToSolarChange(2010);
        System.out.println("Day Of Legal To Solar Change: " + date);
        assertEquals(DateUtils.stringToDate("20101031", "yyyyMMdd"), date);

        date = DateUtils.getDayOfLegalToSolarChange(2011);
        System.out.println("Day Of Legal To Solar Change: " + date);
        assertEquals(DateUtils.stringToDate("20111030", "yyyyMMdd"), date);
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfLegalToSolarChange(java.util.Date)}
     * .
     */
    @Test
    public void testIsDayOfLegalToSolarChangeDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#isDayOfLegalToSolarChange(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testIsDayOfLegalToSolarChangeStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfLegalToSolarChange(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testEncloseDayOfLegalToSolarChangeStringStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfLegalToSolarChange(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testEncloseDayOfLegalToSolarChangeDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfLegalToSolarChange(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testEncloseDayOfLegalToSolarChangeCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfHourChange(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testEncloseDayOfHourChangeStringStringStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfHourChange(java.util.Date, java.util.Date)}
     * .
     */
    @Test
    public void testEncloseDayOfHourChangeDateDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#encloseDayOfHourChange(java.util.Calendar, java.util.Calendar)}
     * .
     */
    @Test
    public void testEncloseDayOfHourChangeCalendarCalendar()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#numHoursInDay(java.util.Date)}.
     */
    @Test
    public void testNumHoursInDayDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#numHoursInDay(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testNumHoursInDayStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#checkDay(java.util.Date)}.
     */
    @Test
    public void testCheckDayDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#checkDay(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCheckDayStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#checkDayType(int, java.util.Date)}
     * .
     */
    @Test
    public void testCheckDayTypeIntDate()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#checkDayType(int, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCheckDayTypeIntStringString()
    {
        // TODO
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#haveEasterDay()}.
     */
    @Test
    public void testHaveEasterDay()
    {
        System.out.println("Have Easter Day: " + DateUtils.haveEasterDay());
    }

    /**
     * Test method for
     * {@link it.greenvulcano.util.txt.DateUtils#getEasterSunday(int)}.
     */
    @Test
    public void testGetEasterSunday()
    {
        Date date = DateUtils.getEasterSunday(1950).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("19500409", "yyyyMMdd"), date);

        date = DateUtils.getEasterSunday(2000).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("20000423", "yyyyMMdd"), date);

        date = DateUtils.getEasterSunday(2008).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("20080323", "yyyyMMdd"), date);

        date = DateUtils.getEasterSunday(2009).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("20090412", "yyyyMMdd"), date);

        date = DateUtils.getEasterSunday(2010).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("20100404", "yyyyMMdd"), date);

        date = DateUtils.getEasterSunday(2011).getTime();
        System.out.println("Easter Sunday: " + date);
        assertEquals(DateUtils.stringToDate("20110424", "yyyyMMdd"), date);
    }

}
