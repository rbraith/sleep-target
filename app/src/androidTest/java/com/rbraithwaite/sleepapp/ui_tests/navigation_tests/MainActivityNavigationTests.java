package com.rbraithwaite.sleepapp.ui_tests.navigation_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityNavigationTests
{
//*********************************************************
// api
//*********************************************************

    // Scenario: the user navigates from the home screen (sleep tracker) to the session archive
    // screen and back
    @Test
    public void navigateBetweenSleepTrackerAndSleepSessionArchive()
    {
        // GIVEN the user is on the home screen
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);
        // AND the bottom navigation is displayed
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
        
        // WHEN the user selects the session archive menu option
        // https://developer.android.com/training/testing/espresso/recipes#matching-view-inside
        // -action-bar
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Session Archive")).perform(click());
        
        // THEN the user is navigated to the session archive screen
        onView(withId(R.id.session_archive_list)).check(matches(isDisplayed()));
        // AND the bottom navigation is not displayed
        onView(withId(R.id.main_bottomnav)).check(matches(not(isDisplayed())));
        // AND the up button is displayed
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).check(matches(
                isDisplayed()));
        
        // --------------------------------------------------------
        
        // GIVEN the preceding app state
        
        // WHEN the user presses the up button from the session archive screen
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click());
        
        // THEN the user is returned to the home screen
        onView(withId(R.id.sleep_fragment_root)).check(matches(isDisplayed()));
        // AND the bottom navigation is displayed
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
    }
    
    // Scenario: proper nav elem (up btn, bottom nav) display when app is opened
    @Test
    public void sleepTrackerScreenNavElemsDisplayProperlyAtStart()
    {
        // GIVEN the app is running
        // WHEN the user is on the home screen
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);
        
        // THEN the up button should not be displayed
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).check(
                doesNotExist());
        // AND the bottom nav should be displayed
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
    }
}
