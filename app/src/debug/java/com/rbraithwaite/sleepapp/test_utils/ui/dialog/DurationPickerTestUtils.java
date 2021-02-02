package com.rbraithwaite.sleepapp.test_utils.ui.dialog;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
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
    
//*********************************************************
// private methods
//*********************************************************

    private static void setNumberPicker(int number, final int pickerId)
    {
        ViewInteraction onPickerInput = onView(allOf(
                withParent(withId(pickerId)),
                withClassName(is("android.widget.NumberPicker$CustomEditText"))));
        
        onPickerInput.perform(click());
        onPickerInput.perform(replaceText(Integer.toString(number)));
        onPickerInput.perform(closeSoftKeyboard());
        onPickerInput.perform(pressImeActionButton());
    }
}
