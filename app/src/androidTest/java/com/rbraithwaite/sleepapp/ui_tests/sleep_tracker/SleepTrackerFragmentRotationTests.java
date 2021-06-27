package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SleepTrackerTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

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
    public void currentSessionPersistsAcrossOrientationChange()
    {
        SleepTrackerTestDriver sleepTracker =
                new SleepTrackerTestDriver(HiltFragmentTestHelper.launchFragment(
                        SleepTrackerFragment.class));
        
        int expectedDurationMillis = 5 * 60 * 1000; // 5 min
        sleepTracker.startPausedSession(expectedDurationMillis);
        
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepTracker.setDetailsFrom(sleepSession);
        
        sleepTracker.rotateScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sleepTracker.assertThat().sessionTimerMatches(expectedDurationMillis);
        sleepTracker.assertThat().detailsMatch(sleepSession);
    }
}
