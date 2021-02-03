package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;

import java.util.Locale;

public class CommonFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private CommonFormatting() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoalModel goalModel)
    {
        if (!goalModel.isSet()) {
            return "";
        }
        
        // REFACTOR [21-02-2 8:13PM] -- hardcoded locale & format.
        return String.format(
                Locale.CANADA,
                "%dh %02dm",
                goalModel.getHours(),
                goalModel.getRemainingMinutes());
    }
}
