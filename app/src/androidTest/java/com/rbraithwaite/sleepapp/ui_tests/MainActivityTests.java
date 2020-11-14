package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void setBottomNavVisibilityTest()
    {
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);

        // bottom nav starts visible
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));

        // set the bottom nav to not be visible
        mainActivityScenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>()
        {
            @Override
            public void perform(MainActivity activity)
            {
                activity.setBottomNavVisibility(false);
            }
        });

        // assert that the bottom nav is not visible
        onView(withId(R.id.main_bottomnav)).check(matches(not(isDisplayed())));

        // set bottom nav back to visible
        mainActivityScenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>()
        {
            @Override
            public void perform(MainActivity activity)
            {
                activity.setBottomNavVisibility(true);
            }
        });

        // assert that bottom nav has gone back to visible
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
    }
}
