package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SleepTrackerTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public void postSleepDialogDisplaysCorrectValuesWhenDetailsAreUnset()
    {
        // details are unset by default, so you just need to stop a session
        sleepTracker.stopSleepSession(12345); // arbitrary duration
        
        sleepTracker.assertThat().postSleepDialogCommentsAreUnset();
        sleepTracker.assertThat().postSleepDialogMoodIsUnset();
        sleepTracker.assertThat().postSleepDialogTagsAreUnset();
    }
    
    // regression for #14
    @Test
    public void trackerIsClearedAfterKeepingSession()
    {
        // SMELL [21-05-7 1:20AM] -- [big job] In general, my tests have abstraction level
        //  problems - ie they should be way more abstract, clear, and simple - I need a better
        //  "test harness".
        sleepTracker.addNewMood(2);
        List<Integer> tagIndices = sleepTracker.addTags(Arrays.asList("tag1", "tag2"));
        // BUG [21-06-27 2:43AM] -- this doesn't seem to be toggling the right tags currently?
        sleepTracker.toggleTagSelections(tagIndices);
        sleepTracker.setAdditionalComments("some comments");
        
        sleepTracker.keepSleepSession(12345); // arbitrary duration
        
        sleepTracker.assertThat().screenIsClear();
        
        sleepTracker.restartFragment();
        sleepTracker.assertThat().screenIsClear();
        
        sleepTracker.restartApp();
        sleepTracker.assertThat().screenIsClear();
    }
    
    @Test
    public void postSleepDialogOpensWithCorrectValues()
    {
        int expectedMoodIndex = 2;
        sleepTracker.addNewMood(2);
        
        List<Integer> expectedSelectedTagIds = sleepTracker.addTags(Arrays.asList("tag1", "tag2"));
        // hard-coded toggle of the 2 tags added above
        sleepTracker.toggleTagSelections(Arrays.asList(0, 1));
        
        String expectedComments = "some comments";
        sleepTracker.setAdditionalComments(expectedComments);
        
        int expectedDuration = 12345;
        sleepTracker.stopSleepSession(expectedDuration);
        
        sleepTracker.assertThat().postSleepDialogHasMood(expectedMoodIndex);
        sleepTracker.assertThat().postSleepDialogHasSelectedTags(expectedSelectedTagIds);
        sleepTracker.assertThat().postSleepDialogHasComments(expectedComments);
        sleepTracker.assertThat().postSleepDialogHasDuration(expectedDuration);
        sleepTracker.assertThat().postSleepDialogRatingIsUnset();
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
        
        // After leaving a session
        sleepTracker.stopAndDiscardSessionManually();
        
        sleepTracker.assertThat().sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.NOT_STARTED);
        sleepTracker.assertThat().sessionStartTimeIsNotDisplayed();
        sleepTracker.assertThat().sessionTimerIsNotDisplayed();
    }
    
    @Test
    public void keptSleepSessionIsAddedToTheDatabase()
    {
        database.assertThat.sleepSessionCountIs(0);
        sleepTracker.keepSleepSession(12345);
        database.assertThat.sleepSessionCountIs(1);
    }
}
