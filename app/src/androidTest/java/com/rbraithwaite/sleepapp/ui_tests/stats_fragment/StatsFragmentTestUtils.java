package com.rbraithwaite.sleepapp.ui_tests.stats_fragment;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragmentViewModel;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class StatsFragmentTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private StatsFragmentTestUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static void checkIntervalsTextMatches(String text)
    {
        onView(allOf(
                withId(R.id.stats_time_period_value),
                withParent(withId(R.id.stats_intervals_time_period_selector)))).check(matches(
                withText(text)));
    }
    
    public static void changeIntervalsResolution(StatsFragmentViewModel.Resolution resolution)
    {
        onView(allOf(
                withId(R.id.stats_time_period_more),
                withParent(withId(R.id.stats_intervals_time_period_selector)))).perform(click());
        
        // REFACTOR [21-03-5 1:55AM] -- hardcoded strings.
        switch (resolution) {
        case WEEK:
            onView(withText("Week")).inRoot(isPlatformPopup()).perform(click());
            break;
        case MONTH:
            onView(withText("Month")).inRoot(isPlatformPopup()).perform(click());
            break;
        case YEAR:
            onView(withText("Year")).inRoot(isPlatformPopup()).perform(click());
            break;
        }
    }
}
