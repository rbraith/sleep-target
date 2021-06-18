package com.rbraithwaite.sleepapp.ui.sleep_goals;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

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
}
