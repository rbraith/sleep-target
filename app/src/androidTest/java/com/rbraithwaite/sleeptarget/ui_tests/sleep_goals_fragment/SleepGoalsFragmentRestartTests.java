/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui_tests.sleep_goals_fragment;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.SleepGoalsTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFragment;

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
