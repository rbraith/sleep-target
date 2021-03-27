package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;

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

    public static String formatSleepDurationGoal(SleepDurationGoal goalModel)
    {
        if (goalModel == null || !goalModel.isSet()) {
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
