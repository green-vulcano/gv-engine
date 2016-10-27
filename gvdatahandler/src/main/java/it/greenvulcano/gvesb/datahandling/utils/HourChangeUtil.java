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
package it.greenvulcano.gvesb.datahandling.utils;

import it.greenvulcano.util.txt.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>
 * Interacts directly with the stylesheet and opportunely handle the hour
 * changing.
 * </p>
 *
 * <p>
 * Works in Italian Locale only.
 * </p>
 *
 * @version 3.0.0 Mar 30, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class HourChangeUtil
{

    private static TimeZone tz = Calendar.getInstance(Locale.ITALIAN).getTimeZone();

    /**
     * Works in Italian Locale only.
     *
     * @param date
     * @return if parameter <i>date</i> represents the day of hour change.
     */
    public static boolean isDayOfHourChange(Date date)
    {
        return isDayOfSolarToLegalChange(date) || isDayOfLegalToSolarChange(date);
    }

    /**
     * Works in Italian Locale only.
     *
     * @param date
     * @param format
     * @return if parameter <i>date</i> represents the day of hour change.
     */
    public static boolean isDayOfHourChange(String date, String format)
    {
        return isDayOfHourChange(DateUtils.stringToDate(date, format));
    }

    /**
     * Works in Italian Locale only.
     *
     * @param date
     * @return if parameter <i>date</i> represents the day of hour change from
     *         Solar to Legal.
     */
    public static boolean isDayOfSolarToLegalChange(Date date)
    {
        Calendar localCal = Calendar.getInstance(Locale.ITALIAN);
        localCal.setTime(date);
        localCal.set(Calendar.HOUR_OF_DAY, 4);
        Date dayBefore = new Date(localCal.getTimeInMillis() - 86400000);
        return !tz.inDaylightTime(dayBefore) && tz.inDaylightTime(localCal.getTime());
    }

    /**
     * Works in Italian Locale only.
     *
     * @param data
     * @return if parameter <i>date</i> represents the day of hour change from
     *         Solar to Legal.
     */
    public static boolean isDayOfLegalToSolarChange(Date data)
    {
        Calendar localCal = Calendar.getInstance(Locale.ITALIAN);
        localCal.setTime(data);
        localCal.set(Calendar.HOUR_OF_DAY, 4);
        Date dayBefore = new Date(localCal.getTimeInMillis() - 86400000);
        return tz.inDaylightTime(dayBefore) && !tz.inDaylightTime(localCal.getTime());
    }

    /**
     * @param date
     * @return the number of hours in the day represented by parameter
     *         <i>date</i>.
     */
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

    /**
     * @param date
     * @param format
     * @return the number of hours in the day represented by parameter
     *         <i>date</i>.
     */
    public static int numHoursInDay(String date, String format)
    {
        return numHoursInDay(DateUtils.stringToDate(date, format));
    }

    /**
     * @param data
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     */
    public static String handleExtraction(Date data)
    {
        Calendar cal = Calendar.getInstance(Locale.ITALIAN);
        cal.setTime(data);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        String hourStr = "";
        if (!isDayOfHourChange(data)) {
            hourStr = String.valueOf(hours + 1);
        }
        else if (isDayOfSolarToLegalChange(data)) {
            if (hours < 2) {
                hourStr = String.valueOf(hours + 1);
            }
            else {
                hourStr = String.valueOf(hours);
            }
        }
        else if (isDayOfLegalToSolarChange(data)) {
            if (hours < 2) {
                hourStr = String.valueOf(hours + 1);
            }
            else {
                if (hours == 2) {
                    if (cal.get(Calendar.SECOND) == 0) {
                        hourStr = String.valueOf(hours + 1);
                    }
                    else if (cal.get(Calendar.SECOND) == 1) {
                        hourStr = String.valueOf(hours + 2);
                    }
                }
                else {
                    hourStr = String.valueOf(hours + 2);
                }
            }
        }
        if (hourStr.length() == 1) {
            hourStr = "0" + hourStr;
        }
        if ("00".equals(hourStr)) {
            hourStr = "24";
        }
        return hourStr;
    }

    /**
     * @param data
     * @param format
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     * @see #handleExtraction(Date)
     */
    public static String handleExtraction(String data, String format)
    {
        return handleExtraction(DateUtils.stringToDate(data, format));
    }

    /**
     * @param data
     * @param format
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     */
    public static String handleFormExtraction(String data, String format)
    {
        Calendar cal = Calendar.getInstance(Locale.ITALIAN);
        cal.setTime(DateUtils.stringToDate(data, format));
        return String.valueOf(cal.get(Calendar.HOUR_OF_DAY) + 1);
    }

    /**
     *
     * @param data
     *        reference data
     * @param hour
     *        hour to insert, between 1 to 23,24,25
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     */
    public static String handleInsertion(Date data, int hour)
    {
        Calendar cal = Calendar.getInstance(Locale.ITALIAN);
        cal.setTime(data);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        hour = hour - 1;
        if (isDayOfLegalToSolarChange(data)) {
            if ((hour > 3) && (hour <= 24)) {
                cal.add(Calendar.HOUR_OF_DAY, hour);
            }
            else if (hour == 3) {
                cal.add(Calendar.HOUR_OF_DAY, hour - 1);
                cal.add(Calendar.SECOND, 1);
            }
            else {
                cal.add(Calendar.HOUR_OF_DAY, hour);
            }
        }
        else if (isDayOfSolarToLegalChange(data)) {
            cal.add(Calendar.HOUR_OF_DAY, hour);
        }
        else {
            cal.add(Calendar.HOUR_OF_DAY, hour);
        }
        return DateUtils.dateToString(cal.getTime(), "yyyyMMddHHmmss");
    }

    /**
     * @param data
     * @param format
     * @param hour
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     * @see #handleInsertion(Date, int)
     */
    public static String handleInsertion(String data, String format, int hour)
    {
        return handleInsertion(DateUtils.stringToDate(data, format), hour);
    }

    /**
     * @param data
     * @param format
     * @param hour
     * @return the hour of day as two-digit <code>java.lang.String</code>
     *         considering legal/solar daylight change.
     * @see #handleInsertion(Date, int)
     */
    public static String handleInsertion(String data, String format, String hour)
    {
        return handleInsertion(data, format, Integer.parseInt(hour));
    }
}
