package com.rbraithwaite.sleepapp.ui.session_data;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;

public class SessionDataFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SessionDataFormatting() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoalModel sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
    }
}
