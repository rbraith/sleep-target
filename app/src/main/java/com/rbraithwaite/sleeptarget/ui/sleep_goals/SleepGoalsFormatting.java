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

package com.rbraithwaite.sleeptarget.ui.sleep_goals;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.ui.Constants;
import com.rbraithwaite.sleeptarget.ui.common.CommonFormatting;

import java.text.SimpleDateFormat;

public class SleepGoalsFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SleepGoalsFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoal goalModel)
    {
        return CommonFormatting.formatSleepDurationGoal(goalModel);
    }
    
    // TEST NEEDED [21-06-18 3:47AM] -- .
    public static String formatWakeTimeGoal(WakeTimeGoal wakeTimeGoal)
    {
        if (wakeTimeGoal == null || !wakeTimeGoal.isSet()) {
            return null;
        }
        
        SimpleDateFormat wakeTimeFormat = new SimpleDateFormat(
                Constants.STANDARD_FORMAT_TIME_OF_DAY,
                Constants.STANDARD_LOCALE);
        return wakeTimeFormat.format(wakeTimeGoal.asDate());
    }
}
