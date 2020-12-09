package com.rbraithwaite.sleepapp.ui_tests.rotation_tests;

import android.content.pm.ActivityInfo;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentActivity;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentRotationTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(15, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    // regression test for #42
    @Test
    public void currentSessionDisplay_persistsAcrossOrientationChange() throws InterruptedException
    {
        // GIVEN the user has an ongoing sleep session
        HiltFragmentTestHelper<SleepTrackerFragment>
                testHelper = HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        Thread.sleep(1100); // give the session time display time to update (1s)
        final TestUtils.DoubleRef<String> expectedStartTimeText = new TestUtils.DoubleRef<>(null);
        // get the start time text
        // REFACTOR [20-11-22 9:44PM] -- consider making this HiltFragmentTestHelper
        //  .performSyncedActivityAction?
        //  (tell don't ask)
        TestUtils.performSyncedActivityAction(
                testHelper.getScenario(),
                new TestUtils.SyncedActivityAction<HiltFragmentActivity>()
                {
                    @Override
                    public void perform(HiltFragmentActivity activity)
                    {
                        expectedStartTimeText.ref =
                                ((TextView) activity.findViewById(R.id.sleep_tracker_start_time))
                                        .getText().toString();
                    }
                });
        
        // WHEN the device is rotated
        // REFACTOR [20-11-22 9:46PM] -- consider making this HiltFragmentTestHelper
        //  .rotateActivitySynced?
        //  (tell don't ask)
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the sleep session and its displayed information persist
        onView(withId(R.id.sleep_tracker_start_time))
                .check(matches(allOf(
                        isDisplayed(),
                        withText(expectedStartTimeText.ref))));
        onView(withId(R.id.sleep_tracker_session_time))
                .check(matches(not(withText(new DurationFormatter().formatDurationMillis(0)))));
    }
}
