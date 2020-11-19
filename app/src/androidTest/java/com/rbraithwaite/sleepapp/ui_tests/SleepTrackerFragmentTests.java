package com.rbraithwaite.sleepapp.ui_tests;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.HiltFragmentScenarioWorkaround;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void sleepTrackerButtonTextChangesOnSessionStatus()
    {
        // GIVEN the user is on the sleep tracker screen
        ActivityScenario<HiltFragmentScenarioWorkaround> scenario =
                HiltFragmentScenarioWorkaround.launchFragmentInHiltContainer(SleepTrackerFragment.class);
        // AND there is no current session
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start)));
        
        // WHEN the user presses the sleep tracking button
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the text changes to indicate there is a session in progress
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_stop)));
        
        //-------------------------------------------------
        
        // GIVEN there is a session in progress
        // WHEN the user eventually presses the button again to stop the session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the text returns to its original state
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start)));
    }
    
    @Test
    public void addSessionToArchiveWithSleepTrackerButton() throws
            InterruptedException,
            ExecutionException
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // first note the current sleep sessions in the archive
        navigateFromSleepTrackerToSessionArchive();
        
        int initialCount = getSessionArchiveCount(scenario);
        
        // GIVEN the user records a sleep session with the sleep tracker button
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click()); // return to sleep tracker screen
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start))); // confirm that a session is not in progress
        // record the session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // WHEN the user navigates to the sleep archive screen
        navigateFromSleepTrackerToSessionArchive();
        
        // THEN the user should see the archive updated with that new session
        int updatedCount = getSessionArchiveCount(scenario);
        assertThat(updatedCount, is(greaterThan(initialCount)));
    }
    
    @Test
    public void currentSessionTimeDisplay_isZeroWhenNoSession()
    {
        // GIVEN the user is on the sleep tracker screen
        // WHEN there is no current session
        ActivityScenario<HiltFragmentScenarioWorkaround> scenario =
                HiltFragmentScenarioWorkaround.launchFragmentInHiltContainer(SleepTrackerFragment.class);
        
        // THEN the time display is zeroed out
        DurationFormatter durationFormatter = new DurationFormatter();
        onView(withId(R.id.sleep_tracker_session_time))
                .check(matches(withText(durationFormatter.formatDurationMillis(0))));
    }
    
    @Test
    public void currentSessionTimeDisplay_updatesWhenInSession() throws InterruptedException
    {
        // GIVEN the user is on the sleep tracker screen
        ActivityScenario<HiltFragmentScenarioWorkaround> scenario =
                HiltFragmentScenarioWorkaround.launchFragmentInHiltContainer(SleepTrackerFragment.class);
        
        // WHEN the user is in a session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        Thread.sleep(1100); // give enough time (>1s) for display to update
        
        // THEN the time display reflects the current session duration
        // (just testing that it is not zero)
        DurationFormatter durationFormatter = new DurationFormatter();
        onView(withId(R.id.sleep_tracker_session_time))
                .check(matches(not(withText(durationFormatter.formatDurationMillis(0)))));
    }

//*********************************************************
// private methods
//*********************************************************

    private void navigateFromSleepTrackerToSessionArchive()
    {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Session Archive")).perform(click());
    }
    
    private int getSessionArchiveCount(ActivityScenario<MainActivity> scenario) throws
            InterruptedException,
            ExecutionException
    {
        final CompletableFuture<Integer> sessionArchiveCount = new CompletableFuture<>();
        // assumes session archive fragment is open
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>()
        {
            @Override
            public void perform(MainActivity activity)
            {
                // https://stackoverflow.com/a/59279744
                Fragment navHostFragment =
                        activity.getSupportFragmentManager().findFragmentById(R.id.main_navhost);
                SessionArchiveFragment fragment =
                        (SessionArchiveFragment) navHostFragment.getChildFragmentManager()
                                .getFragments()
                                .get(0);
                
                sessionArchiveCount.complete(fragment.getRecyclerViewAdapter().getItemCount());
            }
        });
        return sessionArchiveCount.get();
    }
}
