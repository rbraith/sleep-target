package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_goals.WakeTimeGoalModel;
import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;
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

    public static String formatSleepDurationGoal(SleepDurationGoalModel sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
    }
    
    public static String formatWakeTimeGoal(WakeTimeGoalModel wakeTimeGoal)
    {
        return new DateTimeFormatter().formatTimeOfDay(wakeTimeGoal.asDate());
    }
}
