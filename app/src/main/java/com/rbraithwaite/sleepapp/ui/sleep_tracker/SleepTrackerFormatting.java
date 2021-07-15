package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFormatting;

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
    public static String formatInterruptionsTotal(long interruptionsTotalDuration)
    {
        return "(" + formatDuration(interruptionsTotalDuration) + ")";
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
