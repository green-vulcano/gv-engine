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
package it.greenvulcano.util.txt;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.util.thread.BaseThread;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for handling Date/Time conversion.
 * 
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 */
@SuppressWarnings("synthetic-access")
public final class DateUtils
{
    private static Logger                                         logger                   = org.slf4j.LoggerFactory.getLogger(DateUtils.class);

    public static final String                                    CFG_FILE                 = "GVDateUtils.xml";

    public static final String                                    FORMAT_SYSTEM_TIME       = "SYSTEM_TIME";

    public static final String                                    FORMAT_ISO_DATE_S        = "yyyyMMdd";
    public static final String                                    FORMAT_ISO_TIME_S        = "HHmmss";
    public static final String                                    FORMAT_ISO_DATETIME_S    = "yyyyMMdd HHmmss";
    public static final String                                    FORMAT_ISO_TIMESTAMP_S   = "yyyyMMdd HHmmssSSS";

    public static final String                                    FORMAT_ISO_DATE_L        = "yyyy-MM-dd";
    public static final String                                    FORMAT_ISO_TIME_L        = "HH:mm:ss";
    public static final String                                    FORMAT_ISO_DATETIME_L    = "yyyy-MM-dd HH:mm:ss";
    public static final String                                    FORMAT_ISO_TIMESTAMP_L   = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String                                    FORMAT_ISO_DATETIME_UTC  = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    public static final String                                    FORMAT_ISO8601_DATETIME  = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    public static final String                                    FORMAT_IETF_DATETIME     = "EEE, d MMM yyyy HH:mm:ss z";

    public static final String                                    DEFAULT_FORMAT_DATE      = FORMAT_ISO_DATE_L;
    public static final String                                    DEFAULT_FORMAT_TIME      = FORMAT_ISO_TIME_L;
    public static final String                                    DEFAULT_FORMAT_DATETIME  = FORMAT_ISO_DATETIME_L;
    public static final String                                    DEFAULT_FORMAT_TIMESTAMP = FORMAT_ISO_TIMESTAMP_L;

    private static Locale                                         locale                   = Locale.getDefault();
    private static TimeZone                                       timeZone                 = TimeZone.getDefault();

    private static int                                            dslStartMonth            = -1;
    private static int                                            dslStartDay              = -1;
    private static int                                            dslStartDayOfWeek        = -1;
    private static String                                         dslStartTime             = null;

    private static int                                            dslEndMonth              = -1;
    private static int                                            dslEndDay                = -1;
    private static int                                            dslEndDayOfWeek          = -1;
    private static String                                         dslEndTime               = null;


    // DAY_TYPE_WORKING = { 0, 0, 1 } = 1
    public static final int                                       DAY_TYPE_WORKING         = 1;
    // DAY_TYPE_BEFORE_HOLIDAY = { 0, 1, 0 } = 2
    public static final int                                       DAY_TYPE_BEFORE_HOLIDAY  = 2;
    // DAY_TYPE_WORK_BEF_HOL = { 0, 1, 1 } = 3
    public static final int                                       DAY_TYPE_WORK_BEF_HOL    = 3;
    // DAY_TYPE_HOLIDAY = { 1, 0, 0 } = 4
    public static final int                                       DAY_TYPE_HOLIDAY         = 4;
    // DAY_TYPE_WORK_HOL = { 1, 0, 1 } = 5
    public static final int                                       DAY_TYPE_WORK_HOL        = 5;
    // DAY_TYPE_BEF_HOL_HOL = { 1, 1, 0 } = 6
    public static final int                                       DAY_TYPE_BEF_HOL_HOL     = 6;
    // DAY_TYPE_ALL = { 1, 1, 1 } = 7
    public static final int                                       DAY_TYPE_ALL             = 7;

    private static int                                            lastYear                 = -1;

    private static Calendar                                       dEasterMonday            = null;

    private static boolean                                        haveEasterDay            = false;

    private static final HashMap<String, Map<String, DateFormat>> tZoneDateFormatter       = new HashMap<String, Map<String, DateFormat>>();
    private static final Set<Calendar>                            holidays                 = new HashSet<Calendar>();

    public static final Calendar                                  END_OF_DAYS              = Calendar.getInstance();

    private static boolean                                        initialized              = false;


    /**
     * 
     * @version 3.0.0 Feb 17, 2010
     * @author GreenVulcano Developer Team
     */
    private static class ConfigEventHandler implements ConfigurationListener
    {
        /**
         * @see it.greenvulcano.configuration.ConfigurationListener#configurationChanged(ConfigurationEvent)
         */
        @Override
        public void configurationChanged(ConfigurationEvent event)
        {
            if ((event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED) && event.getFile().equals(CFG_FILE)) {
                initialized = false;
                // initialize after a delay
                Runnable rr = new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException exc) {
                            // do nothing
                        }
                        init();
                    }
                };

                BaseThread bt = new BaseThread(rr, "Config reloader for DateUtils");
                bt.setDaemon(true);
                bt.start();
            }
        }

    }


    static {
        try {
            END_OF_DAYS.set(2099, 11, 31, 23, 59, 59);
            END_OF_DAYS.getTimeInMillis();
            XMLConfig.addConfigurationListener(new ConfigEventHandler(), CFG_FILE);
            init();
        }
        catch (Exception exc) {
            logger.error("DateUtils initialization error", exc);
        }
    }

    public static Locale getDefaultLocale()
    {
        return locale;
    }

    public static TimeZone getDefaultTimeZone()
    {
        return timeZone;
    }

    /**
     * Create a Calendar instance with default timezone.
     * 
     * @return
     */
    public static Calendar createCalendar()
    {
        return Calendar.getInstance(timeZone, locale);
    }

    /**
     * Convert a {@link java.lang.String} representation to a
     * {@link java.util.Date} instance.
     * 
     * @param date
     *        the string representation of Date/Time
     * @param format
     *        the conversion pattern
     * @return a Date instance
     */
    public static Date stringToDate(String date, String format)
    {
        try {
            DateFormat sdf = getFormatter(format);
            synchronized (sdf) {
                return sdf.parse(date);
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.stringToDate(" + date + ", " + format + ") Error converting string", exc);
            }
        }
        return null;
    }

    /**
     * Convert a {@link java.lang.String} representation to a
     * {@link java.util.Date} instance.
     * 
     * @param date
     *        the string representation of Date/Time
     * @param format
     *        the conversion pattern
     * @param tZone
     *        the TimeZone
     * @return a Date instance
     */
    public static Date stringToDate(String date, String format, String tZone)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                return sdf.parse(date);
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.stringToDate(" + date + ", " + format + "," + tZone
                        + ") Error converting string", exc);
            }
        }
        return null;
    }

    /**
     * Convert a {@link java.lang.String} representation to a
     * {@link java.util.Date} instance.
     * 
     * @param date
     *        the string representation of Date/Time
     * @param format
     *        the conversion pattern
     * @param tZone
     *        the TimeZone
     * @param lang
     *        the locale to be used in conversion
     * @return a Date instance
     */
    public static Date stringToDate(String date, String format, String tZone, String lang)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                DateFormatSymbols dfs = ((SimpleDateFormat) sdf).getDateFormatSymbols();
                try {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(new DateFormatSymbols(new Locale(lang)));
                    return sdf.parse(date);
                }
                finally {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(dfs);
                }
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.stringToDate(" + date + ", " + format + "," + tZone + "," + lang
                        + ") Error converting string", exc);
            }
        }
        return null;
    }

    /**
     * Convert a {@link java.util.Date} instance to a {@link java.lang.String}
     * representation.
     * 
     * @param date
     *        the Date instance to convert
     * @param format
     *        the conversion pattern
     * @return a string representation of date
     */
    public static String dateToString(Date date, String format)
    {
        try {
            DateFormat sdf = getFormatter(format);
            synchronized (sdf) {
                return sdf.format(date);
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.dateToString(" + date + ", " + format + ") Error converting date", exc);
        }
        return null;
    }

    /**
     * Convert a {@link java.util.Date} instance to a {@link java.lang.String}
     * representation.
     * 
     * @param date
     *        the Date instance to convert
     * @param format
     *        the conversion pattern
     * @param tZone
     *        the TimeZone
     * @return a string representation of date
     */
    public static String dateToString(Date date, String format, String tZone)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                return sdf.format(date);
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.dateToString(" + date + ", " + format + "," + tZone + ") Error converting date",
                    exc);
        }
        return null;
    }

    /**
     * Convert a {@link java.util.Date} instance to a {@link java.lang.String}
     * representation.
     * 
     * @param date
     *        the Date instance to convert
     * @param format
     *        the conversion pattern
     * @param tZone
     *        the TimeZone
     * @param lang
     *        the locale to be used in conversion
     * @return a string representation of date
     */
    public static String dateToString(Date date, String format, String tZone, String lang)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                DateFormatSymbols dfs = ((SimpleDateFormat) sdf).getDateFormatSymbols();
                try {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(new DateFormatSymbols(new Locale(lang)));
                    return sdf.format(date);
                }
                finally {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(dfs);
                }
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.dateToString(" + date + ", " + format + "," + tZone + "," + lang
                    + ") Error converting date", exc);
        }
        return null;
    }


    /**
     * Convert a {@link java.lang.String} representation of date/Time to another
     * format. The output string representation is formatted according to
     * <b>local</b> timezone.
     * 
     * @param date
     *        the date/time representation to convert
     * @param formatIn
     *        the input date/time conversion pattern
     * @param formatOut
     *        the output date/time conversion pattern
     * @return the new string representation
     */
    public static String convertString(String date, String formatIn, String formatOut)
    {
        try {
            Date dateIn = null;
            DateFormat sdf = getFormatter(formatIn);
            synchronized (sdf) {
                dateIn = sdf.parse(date);
            }
            sdf = getFormatter(formatOut);
            synchronized (sdf) {
                return sdf.format(dateIn);
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.convertString(" + date + ", " + formatIn + ", " + formatOut
                        + ") Error converting string", exc);
            }
        }
        return null;
    }

    /**
     * Convert a {@link java.lang.String} representation of date/Time to another
     * format. The output string representation is formatted according to
     * <b>local</b> timezone.
     * 
     * @param date
     *        the date/time representation to convert
     * @param formatIn
     *        the input date/time conversion pattern
     * @param tZoneIn
     *        the input time zone
     * @param formatOut
     *        the output date/time conversion pattern
     * @param tZoneOut
     *        the output time zone
     * @return the new string representation
     */
    public static String convertString(String date, String formatIn, String tZoneIn, String formatOut, String tZoneOut)
    {
        try {
            Date dateIn = null;
            DateFormat sdf = getFormatter(tZoneIn, formatIn);
            synchronized (sdf) {
                dateIn = sdf.parse(date);
            }
            sdf = getFormatter(tZoneOut, formatOut);
            synchronized (sdf) {
                return sdf.format(dateIn);
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.convertString(" + date + ", [" + tZoneIn + "] " + formatIn + ", [" + tZoneOut
                        + "] " + formatOut + ") Error converting string", exc);
            }
        }
        return null;
    }

    /**
     * Convert a {@link java.lang.String} representation of date/Time to another
     * format. The output string representation is formatted according to
     * <b>local</b> timezone.
     * 
     * @param date
     *        the date/time representation to convert
     * @param formatIn
     *        the input date/time conversion pattern
     * @param tZoneIn
     *        the input time zone
     * @param langIn
     *        the input locale to be used in conversion
     * @param formatOut
     *        the output date/time conversion pattern
     * @param tZoneOut
     *        the output time zone
     * @param langOut
     *        the output locale to be used in conversion
     * @return the new string representation
     */
    public static String convertString(String date, String formatIn, String tZoneIn, String langIn, String formatOut,
            String tZoneOut, String langOut)
    {
        try {
            Date dateIn = null;
            DateFormat sdf = getFormatter(tZoneIn, formatIn);
            synchronized (sdf) {
                DateFormatSymbols dfs = ((SimpleDateFormat) sdf).getDateFormatSymbols();
                try {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(new DateFormatSymbols(new Locale(langIn)));
                    dateIn = sdf.parse(date);
                }
                finally {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(dfs);
                }
            }
            sdf = getFormatter(tZoneOut, formatOut);
            synchronized (sdf) {
                DateFormatSymbols dfs = ((SimpleDateFormat) sdf).getDateFormatSymbols();
                try {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(new DateFormatSymbols(new Locale(langOut)));
                    return sdf.format(dateIn);
                }
                finally {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(dfs);
                }
            }
        }
        catch (Exception exc) {
            if ((date != null) && !"".equals(date)) {
                logger.error("DateUtils.convertString(" + date + ", [" + tZoneIn + "/" + langIn + "] " + formatIn
                        + ", [" + tZoneOut + "/" + langOut + "] " + formatOut + ") Error converting string", exc);
            }
        }
        return null;
    }

    public static String convertDayHourString(String date, String formatIn, int hour, String formatOut, String tZoneOut)
    {
        try {
            Date dateIn = null;
            DateFormat sdf = getFormatter(formatIn);
            synchronized (sdf) {
                dateIn = sdf.parse(date);
            }

            Calendar cal = createCalendar();
            cal.setTime(dateIn);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            hour = hour - 1;
            if (isDayOfLegalToSolarChange(dateIn)) {
                if ((hour > 3) && (hour <= 24)) {
                    cal.add(Calendar.HOUR_OF_DAY, hour);
                }
                else if (hour == 2) {
                    cal.add(Calendar.HOUR_OF_DAY, hour - 1);
                    cal.add(Calendar.MINUTE, 59);
                    cal.add(Calendar.SECOND, 59);
                }
                else {
                    cal.add(Calendar.HOUR_OF_DAY, hour);
                }
            }
            else if (isDayOfSolarToLegalChange(dateIn)) {
                cal.add(Calendar.HOUR_OF_DAY, hour);
            }
            else {
                cal.add(Calendar.HOUR_OF_DAY, hour);
            }

            String out = null;
            Date date1 = cal.getTime();

            if (isDayOfLegalToSolarChange(dateIn)) {
                if (hour == 2) {
                    sdf = getFormatter(tZoneOut, "yyyyMMddHHmmss");
                    synchronized (sdf) {
                        out = sdf.format(date1);
                        date1 = sdf.parse(out);
                        date1 = addTime(date1, Calendar.SECOND, 1);
                    }
                }
            }

            sdf = getFormatter(tZoneOut, formatOut);
            synchronized (sdf) {
                out = sdf.format(date1);
            }

            return out;
        }
        catch (Exception exc) {
            System.out.println("DateUtils.convertDayHourString(" + date + ", " + formatIn + ", " + hour + ", ["
                    + tZoneOut + "] " + formatOut + ") Error converting string: " + exc);
        }
        return null;
    }

    /**
     * Return a {@link java.lang.String} representation of the <i>current</i>
     * date/time, according to <b>local</b> timezone.
     * 
     * @param format
     *        the date/time conversion pattern
     * @return the string representation of current date/time
     */
    public static String nowToString(String format)
    {
        try {
            DateFormat sdf = getFormatter(format);
            synchronized (sdf) {
                return sdf.format(new Date());
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.nowToString(" + format + ") Error converting date", exc);
        }
        return null;
    }

    /**
     * Return a {@link java.lang.String} representation of the <i>current</i>
     * date/time, according to <b>tZone</b> timezone.
     * 
     * @param format
     *        the date/time conversion pattern
     * @param tZone
     *        te destination timezone
     * @return the string representation of current date/time
     */
    public static String nowToString(String format, String tZone)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                return sdf.format(new Date());
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.nowToString(" + format + "," + tZone + ") Error converting date", exc);
        }
        return null;
    }

    /**
     * Return a {@link java.lang.String} representation of the <i>current</i>
     * date/time, according to <b>tZone</b> timezone.
     * 
     * @param format
     *        the date/time conversion pattern
     * @param tZone
     *        te destination timezone
     * @param lang
     *        the locale to be used in conversion
     * @return the string representation of current date/time
     */
    public static String nowToString(String format, String tZone, String lang)
    {
        try {
            DateFormat sdf = getFormatter(tZone, format);
            synchronized (sdf) {
                DateFormatSymbols dfs = ((SimpleDateFormat) sdf).getDateFormatSymbols();
                try {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(new DateFormatSymbols(new Locale(lang)));
                    return sdf.format(new Date());
                }
                finally {
                    ((SimpleDateFormat) sdf).setDateFormatSymbols(dfs);
                }
            }
        }
        catch (Exception exc) {
            logger.error("DateUtils.nowToString(" + format + "," + tZone + "," + lang + ") Error converting date", exc);
        }
        return null;
    }

    /**
     * Return a {@link java.lang.String} representation, in teh given format, of
     * tomorrow date.
     * 
     * @param format
     *        the required format.
     * @return a string representation of tomorrow.
     */
    public static String tomorrowToString(String format)
    {
        return dateToString(getTomorrow(), format);
    }

    /**
     * @return the tomorrow {@link java.util.Date} object
     */
    public static Date getTomorrow()
    {
        Calendar cal = createCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Compare two date/time represented as {@link java.lang.String} in the
     * given format.
     * 
     * @return -1 if date1 < date2, 0 if date1 = date2, 1 if date1 > date2
     */
    public static int compare(String date1, String format1, String date2, String format2)
    {
        Date d1 = stringToDate(date1, format1);
        Date d2 = stringToDate(date2, format2);
        return d1.compareTo(d2);
    }

    /**
     * Compare two date/time represented as {@link java.lang.String} in the
     * given format and {@link java.util.Date}.
     * 
     * @return -1 if date1 < date2, 0 if date1 = date2, 1 if date1 > date2
     */
    public static int compare(String date1, String format1, Date date2)
    {
        Date d1 = stringToDate(date1, format1);
        return d1.compareTo(date2);
    }


    /**
     * Return {@link java.util.Date} representing the first day of the week
     * containing <code>date</code>.
     * 
     * @param date
     *        the contained date
     * @return the first week day
     */
    public static Date getWeekStart(Date date)
    {
        Calendar dt = createCalendar();
        dt.setTime(date);
        dt.set(Calendar.DAY_OF_WEEK, dt.getFirstDayOfWeek());

        return dt.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the last day of the week
     * containing <code>date</code>.
     * 
     * @param date
     *        the contained date
     * @return the last week day
     */
    public static Date getWeekEnd(Date date)
    {
        Calendar dt = createCalendar();
        dt.setTime(date);
        dt.set(Calendar.DAY_OF_WEEK, dt.getFirstDayOfWeek() + 6);

        return dt.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the first day of the n't
     * <code>week</code> of <code>year</code>.
     * 
     * @param week
     *        the requested week
     * @param year
     *        the requested year
     * @return the first week day
     */
    public static Date getWeekStart(int week, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.WEEK_OF_YEAR, week);
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());

        return date.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the last day of the n't
     * <code>week</code> of <code>year</code>.
     * 
     * @param week
     *        the requested week
     * @param year
     *        the requested year
     * @return the last week day
     */
    public static Date getWeekEnd(int week, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.WEEK_OF_YEAR, week);
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek() + 6);

        return date.getTime();
    }

    /**
     * Return {@link java.lang.String} representing the first day of the week
     * containing <code>date</code> in the given format.
     * 
     * @param date
     *        the contained date
     * @param format
     *        the input/output date format
     * @return the first week day
     */
    public static String getWeekStart(String date, String format)
    {
        Date dt = getWeekStart(stringToDate(date, format));
        return dateToString(dt, format);
    }

    /**
     * Return {@link java.lang.String} representing the last day of the week
     * containing <code>date</code> in the given format.
     * 
     * @param date
     *        the contained date
     * @param format
     *        the input/output date format
     * @return the last week day
     */
    public static String getWeekEnd(String date, String format)
    {
        Date dt = getWeekEnd(stringToDate(date, format));
        return dateToString(dt, format);
    }

    /**
     * Return {@link java.lang.String} representing the first day of the n't
     * <code>week</code> of <code>year</code>, in the given format.
     * 
     * @param week
     *        the requested week
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the first week day
     */
    public static String getWeekStart(String week, String year, String format)
    {
        Date date = getWeekStart(Integer.parseInt(week), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Return {@link java.lang.String} representing the last day of the n't
     * <code>week</code> of <code>year</code>, in the given format.
     * 
     * @param week
     *        the requested week
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the last week day
     */
    public static String getWeekEnd(String week, String year, String format)
    {
        Date date = getWeekEnd(Integer.parseInt(week), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Return {@link java.util.Date} representing the first day of the month
     * containing <code>date</code>.
     * 
     * @param date
     *        the contained date
     * @return the first month day
     */
    public static Date getMonthStart(Date date)
    {
        Calendar dt = createCalendar();
        dt.setTime(date);
        dt.set(Calendar.DAY_OF_MONTH, 1);

        return dt.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the last day of the month
     * containing <code>date</code>.
     * 
     * @param date
     *        the contained date
     * @return the last month day
     */
    public static Date getMonthEnd(Date date)
    {
        Calendar dt = createCalendar();
        dt.setTime(date);
        dt.set(Calendar.DAY_OF_MONTH, dt.getActualMaximum(Calendar.DAY_OF_MONTH));

        return dt.getTime();
    }

    /**
     * Return {@link java.lang.String} representing the first day of the month
     * containing <code>date</code> in the given format.
     * 
     * @param date
     *        the contained date
     * @param format
     *        the input/output date format
     * @return the first month day
     */
    public static String getMonthStart(String date, String format)
    {
        Date dt = getMonthStart(stringToDate(date, format));
        return dateToString(dt, format);
    }

    /**
     * Return {@link java.lang.String} representing the last day of the month
     * containing <code>date</code> in the given format.
     * 
     * @param date
     *        the contained date
     * @param format
     *        the input/output date format
     * @return the last month day
     */
    public static String getMonthEnd(String date, String format)
    {
        Date dt = getMonthEnd(stringToDate(date, format));
        return dateToString(dt, format);
    }

    /**
     * Return {@link java.util.Date} representing the first day of the n't
     * <code>month</code> of <code>year</code>.
     * 
     * @param month
     *        the requested month
     * @param year
     *        the requested year
     * @return the first month day
     */
    public static Date getMonthStart(int month, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(year, month - 1, 1);

        return date.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the last day of the n't
     * <code>month</code> of <code>year</code>.
     * 
     * @param month
     *        the requested month
     * @param year
     *        the requested year
     * @return the last month day
     */
    public static Date getMonthEnd(int month, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(year, month - 1, 1);
        date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));

        return date.getTime();
    }

    /**
     * Return {@link java.lang.String} representing the first day of the n't
     * <code>month</code> of <code>year</code>, in the given format.
     * 
     * @param month
     *        the requested month
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the first month day
     */
    public static String getMonthStart(String month, String year, String format)
    {
        Date date = getMonthStart(Integer.parseInt(month), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Return {@link java.lang.String} representing the last day of the n't
     * <code>month</code> of <code>year</code>, in the given format.
     * 
     * @param month
     *        the requested month
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the last month day
     */
    public static String getMonthEnd(String month, String year, String format)
    {
        Date date = getMonthEnd(Integer.parseInt(month), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Return {@link java.util.Date} representing the first day of the n't
     * <code>quarter</code> of <code>year</code>.
     * 
     * @param quarter
     *        the requested quarter
     * @param year
     *        the requested year
     * @return the first quarter day
     */
    public static Date getQuarterStart(int quarter, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(year, (quarter - 1) * 3, 1);

        return date.getTime();
    }

    /**
     * Return {@link java.util.Date} representing the last day of the n't
     * <code>quarter</code> of <code>year</code>.
     * 
     * @param quarter
     *        the requested quarter
     * @param year
     *        the requested year
     * @return the last quarter day
     */
    public static Date getQuarterEnd(int quarter, int year)
    {
        Calendar date = createCalendar();
        date = resetTime(date);
        date.set(year, (quarter * 3) - 1, (((quarter == 2) || (quarter == 3)) ? 30 : 31));

        return date.getTime();
    }

    /**
     * Return {@link java.lang.String} representing the first day of the n't
     * <code>quarter</code> of <code>year</code>, in the given format.
     * 
     * @param quarter
     *        the requested quarter
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the first quarter day
     */
    public static String getQuarterStart(String quarter, String year, String format)
    {
        Date date = getQuarterStart(Integer.parseInt(quarter), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Return {@link java.lang.String} representing the last day of the n't
     * <code>quarter</code> of <code>year</code>, in the given format.
     * 
     * @param quarter
     *        the requested quarter
     * @param year
     *        the requested year
     * @param format
     *        the output date format
     * @return the last quarter day
     */
    public static String getQuarterEnd(String quarter, String year, String format)
    {
        Date date = getQuarterEnd(Integer.parseInt(quarter), Integer.parseInt(year));
        return dateToString(date, format);
    }

    /**
     * Change the date/time represented as {@link java.lang.String} in the given
     * format. The parameters <code>type</code> and <code>value</code> must be
     * the same of {@link java.util.Calendar}.
     * 
     * @param date
     *        the date/time to change
     * @param format
     *        the date/time conversion pattern
     * @param type
     *        the field to change
     * @param value
     *        the field value
     * @return the modified date
     */
    public static Date setTime(String date, String format, int type, int value)
    {
        Calendar cal = createCalendar();
        cal.setTime(stringToDate(date, format));
        cal.set(type, value);
        return cal.getTime();
    }

    /**
     * Change the date/time represented as {@link java.util.Date}. The
     * parameters <code>type</code> and <code>value</code> must be the same of
     * {@link java.util.Calendar}.
     * 
     * @param date
     *        the date/time to change
     * @param type
     *        the field to change
     * @param value
     *        the field value
     * @return the modified date
     */
    public static Date setTime(Date date, int type, int value)
    {
        Calendar cal = createCalendar();
        cal.setTime(date);
        cal.set(type, value);
        return cal.getTime();
    }

    /**
     * Change the date/time represented as {@link java.lang.String} in the given
     * format. The parameters <code>type</code> and <code>value</code> must be
     * the same of {@link java.util.Calendar}.
     * 
     * @param date
     *        the date/time to change
     * @param format
     *        the date/time conversion pattern
     * @param type
     *        the field to change
     * @param value
     *        the offset value
     * @return the modified date
     */
    public static Date addTime(String date, String format, int type, int value)
    {
        Calendar cal = createCalendar();
        cal.setTime(stringToDate(date, format));
        cal.add(type, value);
        return cal.getTime();
    }

    /**
     * Change the date/time represented as {@link java.lang.String} in the given
     * format. The parameters <code>type</code> and <code>value</code> must be
     * the same of {@link java.util.Calendar}.
     * 
     * @param date
     *        the date/time to change
     * @param format
     *        the date/time conversion pattern
     * @param type
     *        the field to change
     * @param value
     *        the offset value
     * @return the modified date
     */
    public static String addTime(String date, String format, String type, String value)
    {
        Date d = addTime(date, format, Integer.parseInt(type), Integer.parseInt(value));
        return dateToString(d, format);
    }

    /**
     * Change the date/time represented as {@link java.util.Date}. The
     * parameters <code>type</code> and <code>value</code> must be the same of
     * {@link java.util.Calendar}.
     * 
     * @param date
     *        the date/time to change
     * @param type
     *        the field to change
     * @param value
     *        the offset value
     * @return the modified date
     */
    public static Date addTime(Date date, int type, int value)
    {
        Calendar cal = createCalendar();
        cal.setTime(date);
        cal.add(type, value);
        return cal.getTime();
    }

    /**
     * Change the current date/time. The parameters <code>type</code> and
     * <code>value</code> must be the same of {@link java.util.Calendar}.
     * 
     * @param type
     *        the field to change
     * @param value
     *        the offset value
     * @param onlyDate
     *        if true the following fields are resetted: Calendar.HOUR_OF_DAY
     *        Calendar.MINUTE Calendar.SECOND Calendar.MILLISECOND
     * @return the modified date
     */
    public static Date addTime(int type, int value, boolean onlyDate)
    {
        Calendar cal = createCalendar();
        cal.add(type, value);
        if (onlyDate) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.AM_PM, Calendar.AM);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }

    /**
     * Copy the following fields of the date/time <code>date2</code> to
     * <code>date1</code>, represented as {@link java.util.Date}:
     * Calendar.DAY_OF_MONTH Calendar.MONTH Calendar.YEAR
     * 
     * @param date1
     *        the date/time to change
     * @param date2
     *        the source date/time
     * @return the modified date1
     */
    public static Date copyDate(Date date1, Date date2)
    {
        Calendar cal1 = createCalendar();
        cal1.setTime(date1);
        Calendar cal2 = createCalendar();
        cal2.setTime(date2);
        return copyDate(cal1, cal2).getTime();
    }

    /**
     * Copy the following fields of the date/time <code>date2</code> to
     * <code>date1</code>, represented as {@link java.util.Calendar}:
     * Calendar.DAY_OF_MONTH Calendar.MONTH Calendar.YEAR
     * 
     * @param date1
     *        the date/time to change
     * @param date2
     *        the source date/time
     * @return the modified date1
     */
    public static Calendar copyDate(Calendar date1, Calendar date2)
    {
        date1.set(Calendar.DAY_OF_MONTH, date2.get(Calendar.DAY_OF_MONTH));
        date1.set(Calendar.MONTH, date2.get(Calendar.MONTH));
        date1.set(Calendar.YEAR, date2.get(Calendar.YEAR));
        date1.getTimeInMillis();
        return date1;
    }

    /**
     * Copy the following fields of the date/time <code>date2</code> to
     * <code>date1</code>, represented as {@link java.util.Date}:
     * Calendar.HOUR_OF_DAY Calendar.MINUTE Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date1
     *        the date/time to change
     * @param date2
     *        the source date/time
     * @return the modified date1
     */
    public static Date copyTime(Date date1, Date date2)
    {
        Calendar cal1 = createCalendar();
        cal1.setTime(date1);
        Calendar cal2 = createCalendar();
        cal2.setTime(date2);
        return copyTime(cal1, cal2).getTime();
    }

    /**
     * Copy the following fields of the date/time <code>date2</code> to
     * <code>date1</code>, represented as {@link java.util.Calendar}:
     * Calendar.HOUR_OF_DAY Calendar.MINUTE Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date1
     *        the date/time to change
     * @param date2
     *        the source date/time
     * @return the modified date1
     */
    public static Calendar copyTime(Calendar date1, Calendar date2)
    {
        date1.set(Calendar.HOUR_OF_DAY, date2.get(Calendar.HOUR_OF_DAY));
        date1.set(Calendar.MINUTE, date2.get(Calendar.MINUTE));
        date1.set(Calendar.SECOND, date2.get(Calendar.SECOND));
        date1.set(Calendar.MILLISECOND, date2.get(Calendar.MILLISECOND));
        date1.getTimeInMillis();
        return date1;
    }

    /**
     * Reset the following fields of the date/time represented as
     * {@link java.util.Date}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Date resetTime(Date date)
    {
        Calendar cal = createCalendar();
        cal.setTime(date);
        cal = resetTime(cal);
        return cal.getTime();
    }

    /**
     * Reset the following fields of the date/time represented as
     * {@link java.util.Calendar}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Calendar resetTime(Calendar date)
    {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.getTimeInMillis();
        return date;
    }

    /**
     * Reset the following fields of the date/time represented as
     * {@link java.util.Date}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Date setStartOfDay(Date date)
    {
        Calendar cal = createCalendar();
        cal.setTime(date);
        cal = setStartOfDay(cal);
        return cal.getTime();
    }

    /**
     * Reset the following fields of the date/time represented as
     * {@link java.util.Calendar}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Calendar setStartOfDay(Calendar date)
    {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.getTimeInMillis();
        return date;
    }

    /**
     * Set to their maximum, the following fields of the date/time represented
     * as {@link java.util.Date}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Date setEndOfDay(Date date)
    {
        Calendar cal = createCalendar();
        cal.setTime(date);
        cal = setEndOfDay(cal);
        return cal.getTime();
    }

    /**
     * Set to their maximum, the following fields of the date/time represented
     * as {@link java.util.Calendar}: Calendar.HOUR_OF_DAY Calendar.MINUTE
     * Calendar.SECOND Calendar.MILLISECOND
     * 
     * @param date
     *        the date/time to change
     * @return the modified date
     */
    public static Calendar setEndOfDay(Calendar date)
    {
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        date.set(Calendar.MILLISECOND, 999);
        date.getTimeInMillis();
        return date;
    }

    /**
     * Generate a range of days starting from <code>date1</code> till
     * <code>date2</code>, represented as {@link java.lang.String} in the given
     * format.
     * 
     * @param date1
     *        start date (inclusive)
     * @param format1
     *        format of start date
     * @param date2
     *        end date (inclusive)
     * @param format2
     *        format of end date
     * @return an array of {@link java.util.Date}
     */
    public static Date[] generateRange(String date1, String format1, String date2, String format2)
    {
        return generateRange(stringToDate(date1, format1), stringToDate(date2, format2));
    }

    /**
     * Generate a range of days starting from <code>date1</code> till
     * <code>date2</code>, represented as {@link java.util.Date}.
     * 
     * @param date1
     *        start date (inclusive)
     * @param date2
     *        end date (inclusive)
     * @return an array of {@link java.util.Date}
     */
    public static Date[] generateRange(Date date1, Date date2)
    {
        Calendar cal1 = createCalendar();
        Calendar cal2 = createCalendar();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return generateRange(cal1, cal2);
    }

    /**
     * Generate a range of days starting from <code>date1</code> till
     * <code>date2</code>, represented as {@link java.util.Calendar}.
     * 
     * @param date1
     *        start date (inclusive)
     * @param date2
     *        end date (inclusive)
     * @return an array of {@link java.util.Date}
     */
    public static Date[] generateRange(Calendar date1, Calendar date2)
    {
        DateDiff dDiff = getDiff(date1.getTime(), date2.getTime(), date1.getTimeZone());

        Date[] dates = new Date[dDiff.dateDiff + 1];

        Calendar date = (Calendar) date1.clone();
        date = resetTime(date);
        for (int i = 0; i < dates.length; i++) {
            dates[i] = date.getTime();
            date.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    /**
     * Return the days difference as <code>date2</code> - <code>date1</code>,
     * represented as {@link java.util.Date}.
     * 
     * @param date1
     * @param date2
     * @param tZone
     * @return the days difference
     */
    public static DateDiff getDiff(Date date1, Date date2, TimeZone tZone)
    {
        Calendar cal1 = null;
        Calendar cal2 = null;

        if (tZone == null) {
            cal1 = createCalendar();
            cal2 = createCalendar();
        }
        else {
            cal1 = Calendar.getInstance(tZone);
            cal2 = Calendar.getInstance(tZone);
        }

        cal1.setTime(date1);
        long ldate1 = date1.getTime() + cal1.get(Calendar.ZONE_OFFSET) + cal1.get(Calendar.DST_OFFSET);

        cal2.setTime(date2);
        long ldate2 = date2.getTime() + cal2.get(Calendar.ZONE_OFFSET) + cal2.get(Calendar.DST_OFFSET);

        int hr1 = (int) (ldate1 / 3600000); // 60*60*1000
        int hr2 = (int) (ldate2 / 3600000);

        int days1 = hr1 / 24;
        int days2 = hr2 / 24;

        DateDiff dd = new DateDiff();
        dd.dateDiff = days2 - days1;
        dd.weekOffset = (cal2.get(Calendar.DAY_OF_WEEK) - cal1.get(Calendar.DAY_OF_WEEK)) < 0 ? 1 : 0;
        dd.weekDiff = (dd.dateDiff / 7) + dd.weekOffset;
        dd.yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        dd.monthDiff = ((dd.yearDiff * 12) + cal2.get(Calendar.MONTH)) - cal1.get(Calendar.MONTH);

        return dd;
    }

    /**
     * Return the days difference as <code>date2</code> - <code>date1</code>,
     * represented as {@link java.lang.String} in the given format.
     * 
     * @param date1
     * @param format1
     * @param date2
     * @param format2
     * @param tZone
     * @return the days difference
     */
    public static DateDiff getDiff(String date1, String format1, String date2, String format2, TimeZone tZone)
    {
        return getDiff(stringToDate(date1, format1), stringToDate(date2, format2), tZone);
    }

    /**
     * Return true if the given date is outside the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDaySolar(Date date)
    {
        if (isDayOfHourChange(date)) {
            return false;
        }
        return !timeZone.inDaylightTime(date);
    }

    /**
     * Return true if the given date is outside the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDaySolar(Date date, String tZone)
    {
        if (isDayOfHourChange(date)) {
            return false;
        }
        return !TimeZone.getTimeZone(tZone).inDaylightTime(date);
    }

    /**
     * Return true if the given date, represented as {@link java.lang.String} in
     * the given format,
     * is outside the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDaySolar(String data, String format)
    {
        return isDaySolar(stringToDate(data, format));
    }

    /**
     * Return true if the given date, represented as {@link java.lang.String} in
     * the given format,
     * is outside the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDaySolar(String data, String format, String tZone)
    {
        return isDaySolar(stringToDate(data, format, tZone), tZone);
    }

    /**
     * Return true if the given date is in the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayLegal(Date data)
    {
        if (isDayOfHourChange(data)) {
            return false;
        }
        return timeZone.inDaylightTime(data);
    }

    /**
     * Return true if the given date is in the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayLegal(Date data, String tZone)
    {
        if (isDayOfHourChange(data)) {
            return false;
        }
        return TimeZone.getTimeZone(tZone).inDaylightTime(data);
    }

    /**
     * Return true if the given date, represented as {@link java.lang.String} in
     * the given format,
     * is in the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayLegal(String data, String format)
    {
        return isDayLegal(stringToDate(data, format));
    }

    /**
     * Return true if the given date, represented as {@link java.lang.String} in
     * the given format,
     * is in the daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayLegal(String data, String format, String tZone)
    {
        return isDayLegal(stringToDate(data, format, tZone), tZone);
    }

    /**
     * Return true if the given date is the transition day to/from daylight
     * saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayOfHourChange(Date data)
    {
        return isDayOfSolarToLegalChange(data) || isDayOfLegalToSolarChange(data);
    }

    /**
     * Return true if the given date, represented as {@link java.lang.String} in
     * the given format,
     * is the transition day to/from daylight saving time.
     * 
     * @param date
     * @return
     */
    public static boolean isDayOfHourChange(String data, String format)
    {
        return isDayOfHourChange(stringToDate(data, format));
    }

    /**
     * Return the transition day to daylight saving time for the current year.
     * 
     * @param date
     * @return
     */
    public static Date getDayOfSolarToLegalChange()
    {
        Calendar localCal = createCalendar();
        return getDayOfSolarToLegalChange(localCal.get(Calendar.YEAR));
    }

    /**
     * Return the transition day to daylight saving time for the given
     * <code>year</code>.
     * 
     * @param date
     * @return
     */
    public static Date getDayOfSolarToLegalChange(int year)
    {
        if (!initialized) {
            throw new IllegalStateException("The DateUtils is not initialized");
        }
        Calendar localCal = createCalendar();
        localCal.set(Calendar.YEAR, year);
        localCal.set(Calendar.MONTH, dslStartMonth - 1);
        localCal.set(Calendar.DAY_OF_WEEK, dslStartDayOfWeek);
        localCal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dslStartDay);
        //localCal.setTime(copyTime(localCal.getTime(), stringToDate(dslStartTime, "HH:mm")));
        localCal = resetTime(localCal);
        return localCal.getTime();
    }

    public static boolean isDayOfSolarToLegalChange(Date date)
    {
        Calendar localCal = createCalendar();
        localCal.setTime(date);
        localCal = resetTime(localCal);
        Date dlsStart = getDayOfSolarToLegalChange(localCal.get(Calendar.YEAR));
        return dlsStart.equals(localCal.getTime());
    }

    public static boolean isDayOfSolarToLegalChange(String date, String format)
    {
        return isDayOfSolarToLegalChange(DateUtils.stringToDate(date, format));
    }

    public static boolean encloseDayOfSolarToLegalChange(String date1, String format1, String date2, String format2)
    {
        return encloseDayOfSolarToLegalChange(stringToDate(date1, format1), stringToDate(date2, format2));
    }

    public static boolean encloseDayOfSolarToLegalChange(Date date1, Date date2)
    {
        Calendar d1 = createCalendar();
        d1.setTime(date1);
        Calendar d2 = createCalendar();
        d2.setTime(date2);
        return encloseDayOfSolarToLegalChange(d1, d2);
    }

    public static boolean encloseDayOfSolarToLegalChange(Calendar date1, Calendar date2)
    {
        Calendar lts1 = createCalendar();
        lts1.setTime(getDayOfSolarToLegalChange(date1.get(Calendar.YEAR)));
        Calendar lts2 = createCalendar();
        lts2.setTime(getDayOfSolarToLegalChange(date2.get(Calendar.YEAR)));
        return ((date1.before(lts1) || date1.equals(lts1)) && (lts1.equals(date2) || lts1.before(date2)))
                || ((date1.before(lts2) || date1.equals(lts2)) && (lts2.equals(date2) || lts2.before(date2)));
    }

    public static Date getDayOfLegalToSolarChange()
    {
        Calendar localCal = createCalendar();
        return getDayOfLegalToSolarChange(localCal.get(Calendar.YEAR));
    }

    public static Date getDayOfLegalToSolarChange(int year)
    {
        if (!initialized) {
            throw new IllegalStateException("The DateUtils is not initialized");
        }
        Calendar localCal = createCalendar();
        localCal.set(Calendar.YEAR, year);
        localCal.set(Calendar.MONTH, dslEndMonth - 1);
        localCal.set(Calendar.DAY_OF_WEEK, dslEndDayOfWeek);
        localCal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dslEndDay);
        //localCal.setTime(copyTime(localCal.getTime(), stringToDate(dslEndTime, "HH:mm")));
        localCal = resetTime(localCal);
        return localCal.getTime();
    }

    public static boolean isDayOfLegalToSolarChange(Date date)
    {
        Calendar localCal = createCalendar();
        localCal.setTime(date);
        localCal = resetTime(localCal);
        Date dlsEnd = getDayOfLegalToSolarChange(localCal.get(Calendar.YEAR));
        return dlsEnd.equals(localCal.getTime());
    }

    public static boolean isDayOfLegalToSolarChange(String date, String format)
    {
        return isDayOfLegalToSolarChange(DateUtils.stringToDate(date, format));
    }

    public static boolean encloseDayOfLegalToSolarChange(String date1, String format1, String date2, String format2)
    {
        return encloseDayOfLegalToSolarChange(stringToDate(date1, format1), stringToDate(date2, format2));
    }

    public static boolean encloseDayOfLegalToSolarChange(Date date1, Date date2)
    {
        Calendar d1 = createCalendar();
        d1.setTime(date1);
        Calendar d2 = createCalendar();
        d2.setTime(date2);
        return encloseDayOfLegalToSolarChange(d1, d2);
    }

    public static boolean encloseDayOfLegalToSolarChange(Calendar date1, Calendar date2)
    {
        Calendar lts1 = createCalendar();
        lts1.setTime(getDayOfLegalToSolarChange(date1.get(Calendar.YEAR)));
        Calendar lts2 = createCalendar();
        lts2.setTime(getDayOfLegalToSolarChange(date2.get(Calendar.YEAR)));
        return ((date1.before(lts1) || date1.equals(lts1)) && (lts1.equals(date2) || lts1.before(date2)))
                || ((date1.before(lts2) || date1.equals(lts2)) && (lts2.equals(date2) || lts2.before(date2)));
    }

    public static boolean encloseDayOfHourChange(String date1, String format1, String date2, String format2)
    {
        return encloseDayOfHourChange(stringToDate(date1, format1), stringToDate(date2, format2));
    }

    public static boolean encloseDayOfHourChange(Date date1, Date date2)
    {
        Calendar d1 = createCalendar();
        d1.setTime(date1);
        Calendar d2 = createCalendar();
        d2.setTime(date2);
        return encloseDayOfHourChange(d1, d2);
    }

    public static boolean encloseDayOfHourChange(Calendar date1, Calendar date2)
    {
        return encloseDayOfSolarToLegalChange(date1, date2) || encloseDayOfLegalToSolarChange(date1, date2);
    }

    public static int numHoursInDay(Date date)
    {
        if (isDayOfLegalToSolarChange(date)) {
            return 25;
        }
        if (isDayOfSolarToLegalChange(date)) {
            return 23;
        }
        return 24;
    }

    public static int numHoursInDay(String date, String format)
    {
        return numHoursInDay(DateUtils.stringToDate(date, format));
    }

    /**
     * Convert a long representing a ms duration into a string in the format HH:mm:ss.SSS.
     *
     * @param millis
     * @return
     */
    public static String durationToString(long millis)
    {
        long h = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(h);
        long m = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(m);
        long s = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(s);
        return String.format("%d:%02d:%02d.%03d", h, m, s, millis);
    }

    /**
     * Returns type of day: 1 = Feriale; 2 = Prefestivo; 4 = Festivo;
     * 
     * @param today
     * @return
     */
    public static int checkDay(Date day) throws IllegalStateException
    {
        Calendar iday = createCalendar();
        iday.setTime(day);
        int dayOfWeek = iday.get(Calendar.DAY_OF_WEEK);
        int dayType = DAY_TYPE_WORKING;
        if (dayOfWeek == Calendar.SUNDAY) { // Domenica: festivo
            dayType = DAY_TYPE_HOLIDAY;
        }
        else if (dayOfWeek == Calendar.SATURDAY) { // Sabato: di solito
            // prefestivo...
            dayType = DAY_TYPE_BEFORE_HOLIDAY;
            if (isConfiguredAsHoliday(iday)) { // ma pu� anche essere festivo
                dayType = DAY_TYPE_HOLIDAY;
            }
        }
        else { // Luned�, Marted�, Mercoled�, Gioved� e Venerd�: di solito
               // feriale...
            if (isConfiguredAsHoliday(iday)) { // ...ma pu� anche essere
                // festivo...
                dayType = DAY_TYPE_HOLIDAY;
            }
            else if (haveEasterDay && isEasterMonday(iday)) { // ...pasquetta...
                dayType = DAY_TYPE_HOLIDAY;
            }
            else { // ...oppure prefestivo se domani � festivo!
                iday.add(Calendar.DAY_OF_MONTH, 1);
                if (isConfiguredAsHoliday(iday)) {
                    dayType = DAY_TYPE_BEFORE_HOLIDAY;
                }
            }
        }

        return dayType;
    }

    public static int checkDay(String data, String format)
    {
        return checkDay(DateUtils.stringToDate(data, format));
    }

    public static boolean checkDayType(int dayType, Date day) throws IllegalStateException
    {
        if (dayType == DAY_TYPE_ALL) {
            return true;
        }
        boolean match = false;
        String bin = Integer.toBinaryString(dayType);
        BitSet bs = new BitSet(3);
        for (int i = (bin.length() - 1); i > -1; i--) {
            if (bin.charAt(i) == '1') {
                bs.set(bin.length() - 1 - i);
            }
        }

        if (bs.get(0)) {
            match = (checkDay(day) == DAY_TYPE_WORKING);
        }
        if (!match && bs.get(1)) {
            match = (checkDay(day) == DAY_TYPE_BEFORE_HOLIDAY);
        }
        if (!match && bs.get(2)) {
            match = (checkDay(day) == DAY_TYPE_HOLIDAY);
        }
        return match;
    }

    public static boolean checkDayType(int dayType, String data, String format)
    {
        return checkDayType(dayType, DateUtils.stringToDate(data, format));
    }

    /**
     * @return the haveEasterDay
     */
    public static boolean haveEasterDay()
    {
        return haveEasterDay;
    }

    /**
     * Calculate Easter Sunday Written by Gregory N. Mirsky
     * Source: 2nd Edition by Peter Duffett-Smith.
     * It was originally from Butcher's Ecclesiastical Calendar, published in
     * 1876.
     * This algorithm has also been published in the 1922 book General Astronomy
     * by Spencer Jones
     * in The Journal of the British Astronomical Association (Vol.88, page 91,
     * December 1977) and in
     * Astronomical Algorithms (1991) by Jean Meeus.
     * This algorithm holds for any year in the Gregorian Calendar, which (of
     * course) means years
     * including and after 1583.
     * 
     * <pre>
     * a=year%19
     * b=year/100
     * c=year%100
     * d=b/4 e=b%4
     * f=(b+8)/25
     * g=(b-f+1)/3
     * h=(19a+b-d-g+15)%30
     * i=c/4 k=c%4
     * l=(32+2e+2i-h-k)%7
     * m=(a+11h+22l)/451
     * Easter Month =(h+l-7m+114)/31 [3=March, 4=April]
     * p=(h+l-7m+114)%31
     * Easter Date=p+1 (date in Easter Month)
     * </pre>
     * 
     * Note: Integer truncation is already factored into the calculations. Using
     * higher
     * percision variables will cause inaccurate calculations.
     * 
     * @param nYear
     *        4 digit year
     */
    public static Calendar getEasterSunday(int nYear)
    {
        int nA = 0;
        int nB = 0;
        int nC = 0;
        int nD = 0;
        int nE = 0;
        int nF = 0;
        int nG = 0;
        int nH = 0;
        int nI = 0;
        int nK = 0;
        int nL = 0;
        int nM = 0;
        int nP = 0;
        int nEasterMonth = 0;
        int nEasterDay = 0;

        // Calculate Easter
        nA = nYear % 19;
        nB = nYear / 100;
        nC = nYear % 100;
        nD = nB / 4;
        nE = nB % 4;
        nF = (nB + 8) / 25;
        nG = ((nB - nF) + 1) / 3;
        nH = ((((19 * nA) + nB) - nD - nG) + 15) % 30;
        nI = nC / 4;
        nK = nC % 4;
        nL = ((32 + (2 * nE) + (2 * nI)) - nH - nK) % 7;
        nM = (nA + (11 * nH) + (22 * nL)) / 451;

        // [3=March, 4=April]
        nEasterMonth = (((nH + nL) - (7 * nM)) + 114) / 31;
        --nEasterMonth;
        nP = (((nH + nL) - (7 * nM)) + 114) % 31;

        // Date in Easter Month.
        nEasterDay = nP + 1;

        // Populate the date object...
        Calendar cal = createCalendar();
        cal.set(Calendar.YEAR, nYear);
        cal.set(Calendar.MONTH, nEasterMonth);
        cal.set(Calendar.DAY_OF_MONTH, nEasterDay);
        cal = resetTime(cal);
        cal.getTime();
        return cal;
    }

    private static synchronized boolean isEasterMonday(Calendar day)
    {
        int nYear = day.get(Calendar.YEAR);
        if ((lastYear == -1) || (lastYear != nYear) || (dEasterMonday == null)) {
            dEasterMonday = getEasterSunday(nYear);
            dEasterMonday.add(Calendar.DAY_OF_MONTH, 1);
            lastYear = nYear;
        }
        if (dEasterMonday.equals(day)) {
            return true;
        }
        return false;
    }

    private static boolean isConfiguredAsHoliday(Calendar day) throws IllegalStateException
    {
        if (!initialized) {
            throw new IllegalStateException("The DateUtils is not initialized");
        }
        Calendar lday = (Calendar) day.clone();
        lday = resetTime(lday);
        if (holidays.contains(lday)) {
            return true;
        }
        lday.set(Calendar.YEAR, 1970);
        if (holidays.contains(lday)) {
            return true;
        }
        return false;
    }

    /**
     * Return a DateFormat capable of handling the given conversion
     * pattern. The formatters are cached after creation. By default, the
     * formatters format dates according to the <b>local</b> timezone.
     * 
     * @param format
     *        to conversion pattern to handle
     * @return the requested DateFormat instance
     */
    private static synchronized DateFormat getFormatter(String format)
    {
        return getFormatter(timeZone.getID(), format);
    }

    /**
     * @version 3.0.0 Feb 17, 2010
     * @author GreenVulcano Developer Team
     * 
     *         DateFormat to handle FORMAT_SYSTEM_TIME
     */
    private static class SystemTimeDateFormat extends DateFormat
    {
        private static final long serialVersionUID = -5064343456603593323L;

        /**
         *
         */
        public SystemTimeDateFormat()
        {
            setCalendar(DateUtils.createCalendar());
        }

        /* (non-Javadoc)
         * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
         */
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
        {
            return toAppendTo.append(date.getTime());
        }

        /* (non-Javadoc)
         * @see java.text.DateFormat#parse(java.lang.String, java.text.ParsePosition)
         */
        @Override
        public Date parse(String source, ParsePosition pos)
        {
            pos.setIndex(source.length());
            return new Date(Long.parseLong(source));
        }
    }

    /**
     * @version 3.4.0.4 Sep 2, 2014
     * @author GreenVulcano Developer Team
     * 
     * DateFormat to handle FORMAT_ISO8601_DATETIME,
     * TO BE REMOVED for Java version >= 7
     */
    private static class ISO8601DateFormat extends DateFormat
    {
        private static final long serialVersionUID = -5064344567714693323L;

        /**
         *
         */
        public ISO8601DateFormat()
        {
            setCalendar(DateUtils.createCalendar());
        }

        /* (non-Javadoc)
         * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
         */
        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
        {
        	Calendar cal = getCalendar();
        	cal.setTime(date);
            return toAppendTo.append(DatatypeConverter.printDateTime(cal));
        }

        /* (non-Javadoc)
         * @see java.text.DateFormat#parse(java.lang.String, java.text.ParsePosition)
         */
        @Override
        public Date parse(String source, ParsePosition pos)
        {
            pos.setIndex(source.length());
            Calendar cal = DatatypeConverter.parseDateTime(source);
            return cal.getTime();
        }
    }

    /**
     * Return a DateFormat capable of handling the given conversion
     * pattern for the <b>tZone</b> timezone.
     * 
     * @param format
     *        the conversion pattern to handle
     * @param tZone
     *        the timezone to handle
     * @return the requested DateFormat instance
     */
    private static synchronized DateFormat getFormatter(String tZone, String format)
    {
        Map<String, DateFormat> dateFormatter = tZoneDateFormatter.get(tZone);
        if (dateFormatter == null) {
            dateFormatter = new HashMap<String, DateFormat>();
            tZoneDateFormatter.put(tZone, dateFormatter);
        }
        DateFormat sdf = dateFormatter.get(format);
        if (sdf == null) {
            if (format.equals(FORMAT_SYSTEM_TIME)) {
                sdf = new SystemTimeDateFormat();
            }
            else if (format.equals(FORMAT_ISO8601_DATETIME)) {
                sdf = new ISO8601DateFormat();
            }
            else {
                sdf = new SimpleDateFormat(format, locale);
            }
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone(tZone));
            dateFormatter.put(format, sdf);
        }
        return sdf;
    }

    /**
     * Read configuration file
     */
    private static synchronized void init()
    {
        if (initialized) {
            return;
        }
        try {
            Node locNode = XMLConfig.getNode(CFG_FILE, "/GVDateUtils/Locale");
            String lang = XMLConfig.get(locNode, "@lang");
            String country = XMLConfig.get(locNode, "@country");
            String timezone = XMLConfig.get(locNode, "@timezone");
            locale = new Locale(lang, country);
            timeZone = TimeZone.getTimeZone(timezone);
            logger.debug("DateUtils - configured Locale [" + locale + "] and TimeZone [" + timeZone + "]");

            Node dlsNode = XMLConfig.getNode(locNode, "DayLightSaving");
            if (dlsNode != null) {
                dslStartMonth = XMLConfig.getInteger(dlsNode, "@startMonth");
                dslStartDay = XMLConfig.getInteger(dlsNode, "@startDay");
                dslStartDayOfWeek = XMLConfig.getInteger(dlsNode, "@startDayOfWeek");
                dslStartTime = XMLConfig.get(dlsNode, "@startTime");

                dslEndMonth = XMLConfig.getInteger(dlsNode, "@endMonth");
                dslEndDay = XMLConfig.getInteger(dlsNode, "@endDay");
                dslEndDayOfWeek = XMLConfig.getInteger(dlsNode, "@endDayOfWeek");
                dslEndTime = XMLConfig.get(dlsNode, "@endTime");

                logger.debug("DateUtils - configured DLS Start [" + dslStartMonth + " " + dslStartDay + " "
                        + dslStartDayOfWeek + " " + dslStartTime + "]");
                logger.debug("DateUtils - configured DLS End   [" + dslEndMonth + " " + dslEndDay + " "
                        + dslEndDayOfWeek + " " + dslEndTime + "]");
            }

            haveEasterDay = XMLConfig.getBoolean(CFG_FILE, "/GVDateUtils/Holidays/@have-easter-day", false);

            NodeList nl = XMLConfig.getNodeList(CFG_FILE, "/GVDateUtils/Holidays/Holiday");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    Calendar day = createCalendar();
                    day.set(Calendar.YEAR, XMLConfig.getInteger(n, "@year", day.get(Calendar.YEAR)));
                    day.set(Calendar.MONTH, XMLConfig.getInteger(n, "@month") - 1);
                    day.set(Calendar.DAY_OF_MONTH, XMLConfig.getInteger(n, "@day"));
                    day = resetTime(day);
                    holidays.add(day);
                }
            }
            initialized = true;
        }
        catch (Exception exc) {
            logger.error("DateUtils initialization error", exc);
        }
    }

}
