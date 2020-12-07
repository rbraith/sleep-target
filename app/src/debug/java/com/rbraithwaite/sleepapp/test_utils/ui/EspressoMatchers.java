package com.rbraithwaite.sleepapp.test_utils.ui;

import android.os.Build;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class EspressoMatchers
{
//*********************************************************
// constructors
//*********************************************************

    private EspressoMatchers() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    
    /**
     * eg onView({@literal <date-picker>}).check(matches(datePickerWithDate(1, 2, 3)));
     */
    // https://stackoverflow.com/a/44840330
    public static Matcher<View> datePickerWithDate(
            final int year,
            final int month,
            final int dayOfMonth)
    {
        return new BoundedMatcher<View, DatePicker>(DatePicker.class)
        {
            @Override
            protected boolean matchesSafely(DatePicker item)
            {
                return (year == item.getYear() && month == item.getMonth() &&
                        dayOfMonth == item.getDayOfMonth());
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("date picker with date:");
            }
        };
    }
    
    public static Matcher<View> timePickerWithTime(final int hourOfDay, final int minute)
    {
        return new BoundedMatcher<View, TimePicker>(TimePicker.class)
        {
            @Override
            protected boolean matchesSafely(TimePicker item)
            {
                if (Build.VERSION.SDK_INT >= 23) {
                    return (hourOfDay == item.getHour() && minute == item.getMinute());
                } else {
                    return (hourOfDay == item.getCurrentHour() &&
                            minute == item.getCurrentMinute());
                }
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("time picker with time");
            }
        };
    }
}
