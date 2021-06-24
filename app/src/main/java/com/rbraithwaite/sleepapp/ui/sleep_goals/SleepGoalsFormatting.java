package com.rbraithwaite.sleepapp.ui.sleep_goals;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

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
