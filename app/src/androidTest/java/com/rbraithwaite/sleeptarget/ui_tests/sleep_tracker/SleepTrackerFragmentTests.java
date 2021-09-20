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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.SleepTrackerTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

// REFACTOR [21-05-1 5:00PM] -- Putting this here, but this is a general refactoring:
//  - I should hide Espresso details behind more descriptive interfaces
//  - as a general rule I should not allow any Espresso dependencies in any test classes
//  ---
//  an idea:
//  - instead of SleepTrackerFragmentTestUtils have something like SleepTrackerTestHelper, which
//      will handle launching the sleep tracker screen, performing inputs, and checking values.
@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentTests
{
    // TODO [21-04-3 2:35AM] -- UI test for mood selection highlighting?
    
    // TODO [21-04-19 5:39PM] tag selector tests missing from more context tests below:
    //  - tag editing functionality.

//*********************************************************
// package properties
//*********************************************************

    SleepTrackerTestDriver sleepTracker;
    DatabaseTestDriver database;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        sleepTracker = new SleepTrackerTestDriver(
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class));
        database = new DatabaseTestDriver();
    }
    
    @After
    public void teardown()
    {
        sleepTracker = null;
        database = null;
    }
    
    @Test
    public void interruptionsDisplayProperly()
    {
        sleepTracker.assertThat().interruptionsCardIsNotDisplayed();
        
        sleepTracker.startSessionManually();
        
        sleepTracker.assertThat().interruptionsCardIsDisplayed();
    }
    
    @Test
    public void detailsAreRetainedOnFragmentRestart()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepTracker.setDetailsFrom(sleepSession);
        
        sleepTracker.restartFragment();
        
        sleepTracker.assertThat().detailsMatch(sleepSession);
    }
    
    @Test
    public void detailsAreRetainedOnAppRestart()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepTracker.setDetailsFrom(sleepSession);
        
        sleepTracker.restartApp();
        
        sleepTracker.assertThat().detailsMatch(sleepSession);
    }
    
    @Test
    public void tagsHaveCorrectPresetValues()
    {
        sleepTracker.assertThat().tagSelector().hasTagsMatching(
                "Relaxed",
                "Stressed",
                "Late Workout",
                "Late Night Snack",
                "Travel",
                "Alcohol",
                "Caffeine",
                "Feeling Sick");
    }
    
    @Test
    public void goalsDisplaysProperly()
    {
        // To start, no goals are displayed
        sleepTracker.assertThat().noGoalsAreDisplayed();
        
        // add a wake-time goal, verify it is displayed
        WakeTimeGoal expectedWakeTimeGoal = TestUtils.ArbitraryData.getWakeTimeGoal();
        database.setWakeTimeGoal(expectedWakeTimeGoal);
        sleepTracker.restartApp();
        
        sleepTracker.assertThat().onlyWakeTimeGoalIsDisplayed(expectedWakeTimeGoal);
        
        // add a sleep duration goal, verify both goals are displayed
        SleepDurationGoal expectedSleepDurationGoal =
                TestUtils.ArbitraryData.getSleepDurationGoal();
        database.setSleepDurationGoal(expectedSleepDurationGoal);
        sleepTracker.restartApp();
        
        sleepTracker.assertThat().bothGoalsAreDisplayed(
                expectedWakeTimeGoal,
                expectedSleepDurationGoal);
    }
    
    @Test
    public void screenStateChangesBasedOnSessionStatus()
    {
        // When there is no session
        sleepTracker.assertThat().sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.NOT_STARTED);
        sleepTracker.assertThat().sessionStartTimeIsNotDisplayed();
        sleepTracker.assertThat().sessionTimerIsNotDisplayed();
        
        // When in a session
        int expectedDuration = 123456;
        Date startTime = sleepTracker.startPausedSession(expectedDuration);
        
        sleepTracker.assertThat().sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.STARTED);
        sleepTracker.assertThat().sessionStartTimeMatches(startTime);
        sleepTracker.assertThat().sessionTimerMatches(expectedDuration);
    }
    
    @Test
    public void screenStateChangesBasedOnInterruption()
    {
        int duration = 5 * 60 * 1000; // 5 min
        sleepTracker.startPausedSession(duration);
        
        // verify 'before' state
        sleepTracker.assertThat()
                .interruptionButtonIsInState(SleepTrackerTestDriver.Assertions.InterruptButtonState.RESUMED);
        sleepTracker.assertThat().interruptionTimerIsNotDisplayed();
        sleepTracker.assertThat().interruptionsTotalIsNotDisplayed();
        
        sleepTracker.pressInterruptButton();
        
        // verify 'after' state
        sleepTracker.assertThat()
                .interruptionButtonIsInState(SleepTrackerTestDriver.Assertions.InterruptButtonState.INTERRUPTED);
        sleepTracker.assertThat().interruptionTimerMatches(0);
        sleepTracker.assertThat().interruptionsTotalMatches(0, 1);
        
        // press the interrupt button, *then* unpause, so that we can be sure the timer should
        // have the paused duration value.
        sleepTracker.unpause();
        
        TestUtils.sleep(1.2f); // give the session timer time to update in theory
        sleepTracker.assertThat().sessionTimerMatches(duration);
    }
    
    @Test
    public void lastInterruptionReasonIsRetained()
    {
        sleepTracker.startSessionManually();
        
        String expectedReason = "expected";
        sleepTracker.startInterruptionWithReason(expectedReason);
        
        // after the interruption ends
        sleepTracker.resumeSession();
        sleepTracker.assertThat().interruptionReasonTextMatches(expectedReason);
        
        // when the fragment is restarted
        sleepTracker.restartFragment();
        sleepTracker.assertThat().interruptionReasonTextMatches(expectedReason);
        
        // when the app is restarted
        sleepTracker.restartApp();
        sleepTracker.assertThat().interruptionReasonTextMatches(expectedReason);
    }
}
