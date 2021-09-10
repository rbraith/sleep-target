/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.ui_tests.sleep_tracker;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.SleepTrackerTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFragment;

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
        
        sleepTracker.rotateScreenTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sleepTracker.assertThat().sessionTimerMatches(expectedDurationMillis);
        sleepTracker.assertThat().detailsMatch(sleepSession);
    }
    
    @Test
    public void withTagSelectorDialogOpen()
    {
        SleepTrackerTestDriver sleepTracker =
                new SleepTrackerTestDriver(HiltFragmentTestHelper.launchFragment(
                        SleepTrackerFragment.class));
        
        sleepTracker.openTagSelectorDialog();
        
        // just making sure no crash happens here
        sleepTracker.rotateScreenTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    
    @Test
    public void moodSelectorDialogWorksAcrossOrientationChange()
    {
        SleepTrackerTestDriver sleepTracker =
                new SleepTrackerTestDriver(HiltFragmentTestHelper.launchFragment(
                        SleepTrackerFragment.class));
        sleepTracker.scrollToDetails();
        
        int expectedMoodIndex = 3;
        
        sleepTracker.openMoodSelectorDialog();
        sleepTracker.selectMoodInOpenDialog(expectedMoodIndex);
        
        sleepTracker.rotateScreenTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sleepTracker.confirmMoodDialog();
        
        sleepTracker.assertThat().selectedMoodMatches(expectedMoodIndex);
    }
}
