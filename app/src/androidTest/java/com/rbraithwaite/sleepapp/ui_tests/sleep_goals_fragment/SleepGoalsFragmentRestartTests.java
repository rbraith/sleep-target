package com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SleepGoalsTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

public class SleepGoalsFragmentRestartTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
//*********************************************************
// package properties
//*********************************************************

    SleepGoalsTestDriver sleepGoals;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        sleepGoals = new SleepGoalsTestDriver(HiltFragmentTestHelper.launchFragment(
                SleepGoalsFragment.class));
    }
    
    @After
    public void teardown()
    {
        sleepGoals = null;
    }
    
    @Test
    public void goalsStateIsRetainedOnFragmentRestart()
    {
        SleepDurationGoal expectedSleepDurationGoal =
                TestUtils.ArbitraryData.getSleepDurationGoal();
        WakeTimeGoal expectedWakeTimeGoal = TestUtils.ArbitraryData.getWakeTimeGoal();
        
        sleepGoals.addSleepDurationGoal(expectedSleepDurationGoal);
        sleepGoals.addWakeTimeGoal(expectedWakeTimeGoal);
        
        sleepGoals.restartFragment();
        
        sleepGoals.assertThat().sleepDurationGoalMatches(expectedSleepDurationGoal);
        sleepGoals.assertThat().wakeTimeGoalMatches(expectedWakeTimeGoal);
        
        sleepGoals.deleteSleepDurationGoal();
        sleepGoals.deleteWakeTimeGoal();
        
        sleepGoals.restartFragment();
        
        sleepGoals.assertThat().sleepDurationGoalIsUnset();
        sleepGoals.assertThat().wakeTimeGoalIsUnset();
    }
    
    @Test
    public void goalStateIsRetainedOnAppRestart()
    {
        SleepDurationGoal expectedSleepDurationGoal =
                TestUtils.ArbitraryData.getSleepDurationGoal();
        WakeTimeGoal expectedWakeTimeGoal = TestUtils.ArbitraryData.getWakeTimeGoal();
        
        sleepGoals.addSleepDurationGoal(expectedSleepDurationGoal);
        sleepGoals.addWakeTimeGoal(expectedWakeTimeGoal);
        
        sleepGoals.restartApp();
        
        sleepGoals.assertThat().sleepDurationGoalMatches(expectedSleepDurationGoal);
        sleepGoals.assertThat().wakeTimeGoalMatches(expectedWakeTimeGoal);
        
        sleepGoals.deleteSleepDurationGoal();
        sleepGoals.deleteWakeTimeGoal();
        
        sleepGoals.restartApp();
        
        sleepGoals.assertThat().sleepDurationGoalIsUnset();
        sleepGoals.assertThat().wakeTimeGoalIsUnset();
    }
}
