/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
