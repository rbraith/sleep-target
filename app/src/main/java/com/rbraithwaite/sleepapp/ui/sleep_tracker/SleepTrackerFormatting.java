package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;

public class SleepTrackerFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SleepTrackerFormatting() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
    }
    
    public static String formatWakeTimeGoal(WakeTimeGoal wakeTimeGoal)
    {
        return new DateTimeFormatter().formatTimeOfDay(wakeTimeGoal.asDate());
    }
}
