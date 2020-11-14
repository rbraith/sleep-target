package com.rbraithwaite.sleepapp.ui_tests;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.HiltFragmentScenarioWorkaround;
import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

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

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentTests
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Integer> sessionArchiveCount = new MutableLiveData<>(null);

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
    public void addSessionToArchiveWithSleepTrackerButton() throws InterruptedException
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sessionArchiveCount);

        // first note the current sleep sessions in the archive
        navigateFromSleepTrackerToSessionArchive();

        updateSessionArchiveCount(scenario, synchronizer);
        int initialCount = sessionArchiveCount.getValue();

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
        updateSessionArchiveCount(scenario, synchronizer);
        int updatedCount = sessionArchiveCount.getValue();
        assertThat(updatedCount, is(greaterThan(initialCount)));
    }

//*********************************************************
// private methods
//*********************************************************

    private void navigateFromSleepTrackerToSessionArchive()
    {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Session Archive")).perform(click());
    }

    private void updateSessionArchiveCount(
            ActivityScenario<MainActivity> scenario,
            TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer)
    {
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

                sessionArchiveCount.setValue(fragment.getRecyclerViewAdapter().getItemCount());
            }
        });
        if (synchronizer != null) { synchronizer.sync(); }
    }
}
