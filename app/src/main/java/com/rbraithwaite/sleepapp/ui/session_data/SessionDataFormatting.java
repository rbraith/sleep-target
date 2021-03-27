package com.rbraithwaite.sleepapp.ui.session_data;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
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

    public static String formatSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
    }
}
