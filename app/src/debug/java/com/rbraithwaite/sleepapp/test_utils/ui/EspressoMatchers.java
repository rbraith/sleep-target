package com.rbraithwaite.sleepapp.test_utils.ui;

import android.os.Build;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TimePicker;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP.MoodView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Locale;

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
     * Match a RatingBar to a given rating
     */
    public static Matcher<View> withRating(float rating)
    {
        return new BoundedMatcher<View, RatingBar>(RatingBar.class)
        {
            @Override
            protected boolean matchesSafely(RatingBar item)
            {
                return item.getRating() == rating;
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("rating matches: " + rating);
            }
        };
    }
    
    /**
     * Match a MoodView against a given index
     */
    public static Matcher<View> withMoodIndex(int index)
    {
        return new BoundedMatcher<View, MoodView>(MoodView.class)
        {
            @Override
            protected boolean matchesSafely(MoodView item)
            {
                return item.getMoodIndex() == index;
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("MoodView with index: " + index);
            }
        };
    }
    
    
    /**
     * Matches a view with a particular tag value.
     *
     * @param value The value of the view's tag to be matched against.
     *
     * @return a Matcher (usually to be used with withTag())
     */
    public static Matcher<Object> tagValue(final Object value)
    {
        return new BaseMatcher<Object>()
        {
            @Override
            public boolean matches(Object item)
            {
                if (item == null) {
                    return false;
                }
                return item.equals(value);
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("View tag of value: " + value.toString());
            }
        };
    }
    
    public static Matcher<View> recyclerViewWithCount(int count)
    {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
        {
            @Override
            protected boolean matchesSafely(RecyclerView item)
            {
                RecyclerView.Adapter<?> adapter = item.getAdapter();
                return adapter != null && adapter.getItemCount() == count;
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("recycler view with count " + count);
            }
        };
    }
    
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
                // BUG [20-12-8 9:45PM] -- this should print the target date
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
                description.appendText(String.format(Locale.CANADA,
                                                     "time picker with time: %d:%d",
                                                     hourOfDay,
                                                     minute));
            }
        };
    }
}
