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

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.ui.Constants;
import com.rbraithwaite.sleeptarget.ui.common.CommonFormatting;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepTrackerFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SleepTrackerFormatting() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    // TEST NEEDED [21-07-14 11:50PM] -- .
    public static String formatInterruptionsTotal(long durationMillis, int interruptionCount)
    {
        return formatDuration(durationMillis) + " (" + interruptionCount + ")";
    }
    
    public static String formatSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
    }
    
    public static String formatWakeTimeGoal(WakeTimeGoal wakeTimeGoal)
    {
        // REFACTOR [21-06-18 3:50AM] -- This should be CommonFormatting.
        return SleepGoalsFormatting.formatWakeTimeGoal(wakeTimeGoal);
    }
    
    // TEST NEEDED [21-06-18 3:53AM] -- .
    public static String formatSessionStartTime(Date sessionStart)
    {
        SimpleDateFormat format = new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                                       Constants.STANDARD_LOCALE);
        return format.format(sessionStart);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
}
