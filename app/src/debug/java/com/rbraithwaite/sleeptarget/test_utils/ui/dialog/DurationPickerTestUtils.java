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

package com.rbraithwaite.sleeptarget.test_utils.ui.dialog;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleeptarget.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class DurationPickerTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DurationPickerTestUtils() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    
    /**
     * This only sets the value of the pickers, it does not confirm the dialog.
     */
    public static void setDuration(int hour, int minute)
    {
        setHourPicker(hour);
        setMinutePicker(minute);
    }
    
    public static void setHourPicker(int hour)
    {
        if (hour < 0) {
            throw new IllegalArgumentException(String.format("setHourPicker: invalid hour %d",
                                                             hour));
        }
        
        setNumberPicker(hour, R.id.hour_picker);
    }
    
    public static void setMinutePicker(int minute)
    {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("setMinutePicker: invalid minute %d",
                                                             minute));
        }
        
        setNumberPicker(minute, R.id.minute_picker);
    }
    
    public static void checkMatchesDuration(int hours, int minutes)
    {
        checkHourPickerMatches(hours);
        checkMinutePickerMatches(minutes);
    }
    
    public static void checkHourPickerMatches(int hours)
    {
        // REFACTOR [21-02-3 5:29PM] -- this could be checkNumberPickerInput(id, value).
        onPickerInput(R.id.hour_picker).check(matches(withText(Integer.toString(hours))));
    }
    
    public static void checkMinutePickerMatches(int minutes)
    {
        onPickerInput(R.id.minute_picker).check(matches(withText(Integer.toString(minutes))));
    }

//*********************************************************
// private methods
//*********************************************************

    private static ViewInteraction onPickerInput(final int pickerId)
    {
        return onView(allOf(
                withParent(withId(pickerId)),
                withClassName(is("android.widget.NumberPicker$CustomEditText"))));
    }
    
    private static void setNumberPicker(int number, final int pickerId)
    {
        ViewInteraction onThisPickerInput = onPickerInput(pickerId);
        
        onThisPickerInput.perform(click());
        onThisPickerInput.perform(replaceText(Integer.toString(number)));
        onThisPickerInput.perform(closeSoftKeyboard());
        onThisPickerInput.perform(pressImeActionButton());
    }
}
