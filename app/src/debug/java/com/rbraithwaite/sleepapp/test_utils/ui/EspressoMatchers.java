package com.rbraithwaite.sleepapp.test_utils.ui;

import android.view.View;
import android.widget.DatePicker;

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
}
