package com.rbraithwaite.sleepapp.test_utils.ui;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.PickerActions;

public class EspressoActions
{
//*********************************************************
// constructors
//*********************************************************

    private EspressoActions() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    
    /**
     * Calendar & DatePickerDialog compliant version of espresso.contrib.PickerAction.setDate() The
     * former use 0-11 for the months, while the latter seems to use 1-12.
     */
    public static ViewAction setDatePickerDate(int year, int month, int dayOfMonth)
    {
        return PickerActions.setDate(year, month + 1, dayOfMonth);
    }
}
