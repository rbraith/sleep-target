package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;

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
}
