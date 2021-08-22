/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleepapp.ui.session_archive;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionArchiveFormatting
{
//*********************************************************
// api
//*********************************************************

    public static String formatFullDate(Date date)
    {
        // REFACTOR [21-03-31 2:26AM] -- make this CommonFormatting.formatFullDate.
        if (date == null) {
            return null;
        }
        
        SimpleDateFormat fullDateFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                     Constants.STANDARD_LOCALE);
        
        return fullDateFormat.format(date);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatInterruptions(Interruptions interruptions)
    {
        if (interruptions == null || interruptions.isEmpty()) {
            return null;
        }
        
        // REFACTOR [21-07-20 2:58PM] -- this should be CommonFormatting & shared between here
        //  and sleep tracker.
        return SleepTrackerFormatting.formatInterruptionsTotal(
                interruptions.getTotalDuration(),
                interruptions.getCount());
    }
}
