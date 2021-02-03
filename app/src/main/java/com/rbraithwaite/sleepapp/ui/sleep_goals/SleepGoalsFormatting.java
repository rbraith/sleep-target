package com.rbraithwaite.sleepapp.ui.sleep_goals;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;

public class SleepGoalsFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SleepGoalsFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoalModel goalModel)
    {
        return CommonFormatting.formatSleepDurationGoal(goalModel);
    }
}
