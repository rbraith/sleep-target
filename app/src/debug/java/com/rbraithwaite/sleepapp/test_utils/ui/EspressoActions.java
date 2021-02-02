package com.rbraithwaite.sleepapp.test_utils.ui;

import android.view.InputDevice;
import android.view.MotionEvent;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.ViewActions;
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

    public static ViewAction clickBottomCentre()
    {
        // copying ViewActions.click() implementation
        // idea from: https://blog.stylingandroid.com/numberpicker-espresso-testing/.
        return ViewActions.actionWithAssertions(
                new GeneralClickAction(
                        Tap.SINGLE,
                        GeneralLocation.BOTTOM_CENTER,
                        Press.FINGER,
                        InputDevice.SOURCE_UNKNOWN,
                        MotionEvent.BUTTON_PRIMARY));
    }
    
    
    /**
     * Calendar & DatePickerDialog compliant version of espresso.contrib.PickerAction.setDate() The
     * former use 0-11 for the months, while the latter seems to use 1-12.
     */
    public static ViewAction setDatePickerDate(int year, int month, int dayOfMonth)
    {
        return PickerActions.setDate(year, month + 1, dayOfMonth);
    }
}
