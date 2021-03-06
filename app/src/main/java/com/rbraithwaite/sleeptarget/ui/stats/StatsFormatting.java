/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui.stats;

import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatsFormatting
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "StatsFormatting";

//*********************************************************
// constructors
//*********************************************************

    private StatsFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    // TEST NEEDED [21-06-8 6:00PM]
    public static String formatDurationsYLabel(int hour)
    {
        return String.format(Locale.CANADA, "%dh", hour);
    }
    
    public static String formatIntervalsXLabelDate(Date date)
    {
        // IDEA [21-06-7 5:04PM] I should add the days of the week here eg:
        //   Mon
        //  06/07.
        // REFACTOR [21-02-27 8:50PM] -- hardcoded locale.
        SimpleDateFormat xLabelFormat = new SimpleDateFormat("MM/dd", Locale.CANADA);
        return xLabelFormat.format(date);
    }
    
    public static String formatIntervalsXLabelMonth(Date dayInMonth)
    {
        // REFACTOR [21-02-27 9:05PM] -- hardcoded locale.
        SimpleDateFormat xLabelFormat = new SimpleDateFormat("MM", Locale.CANADA);
        return xLabelFormat.format(dayInMonth);
    }
    
    public static String formatIntervalsRange(DateRange range)
    {
        // REFACTOR [21-02-25 12:39AM] -- hardcoded locale.
        SimpleDateFormat
                dateFormat = new SimpleDateFormat("yy/MM/dd", Locale.CANADA);
        return String.format(
                "%s - %s",
                dateFormat.format(range.getStart()),
                dateFormat.format(range.getEnd()));
    }
    
    public static String formatIntervalsMonthOf(Date date)
    {
        // REFACTOR [21-02-25 12:39AM] -- hardcoded locale.
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM, yyyy", Locale.CANADA);
        return monthFormat.format(date);
    }
    
    public static String formatIntervalsYearOf(Date date)
    {
        // REFACTOR [21-02-25 12:39AM] -- hardcoded locale.
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.CANADA);
        return yearFormat.format(date);
    }
    
    public static String formatIntervalsYLabel(int hour)
    {
        hour = hour % 24;
        String period = "am";
        if (hour >= 12) {
            period = "pm";
            if (hour > 12) { hour -= 12; }
        }
        if (hour == 0) { hour = 12; }
        return hour + period;
    }
}
