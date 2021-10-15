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

package com.rbraithwaite.sleeptarget.ui_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.ApplicationTestDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.value_matchers.SleepInterruptionEntityMatchers.interruptionWithReason;



/**
 * These are instrumented tests which exercise multiple components of the app in a single test.
 */
@RunWith(AndroidJUnit4.class)
public class AppTests
{
//*********************************************************
// private properties
//*********************************************************

    private ApplicationTestDriver app;
    private DatabaseTestDriver database;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        app = new ApplicationTestDriver();
        database = new DatabaseTestDriver();
    }
    
    @After
    public void teardown()
    {
        app = null;
        database = null;
    }
    
    @Test
    public void keptSessionDetailsAppearInArchive()
    {
        SleepSessionBuilder sleepSession = aSleepSession();
        
        app.recordSpecificSession(sleepSession);
        app.navigateTo(ApplicationTestDriver.Destination.ARCHIVE);
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().assertThat().displayedValuesMatch(sleepSession);
    }
    
    @Test
    public void trackerIsClearedAfterKeepingSession()
    {
        app.getSleepTracker().setDetailsFrom(aSleepSession());
        app.getSleepTracker().recordArbitrarySession();
        
        app.getPostSleep().keep();
        app.getSleepTracker().assertThat().detailsAreCleared();
        
        app.getSleepTracker().restartFragment();
        app.getSleepTracker().assertThat().screenIsClear();
        
        // REFACTOR [21-08-20 4:29PM] -- This should be app.restartApp()
        app.getSleepTracker().restartApp();
        app.getSleepTracker().assertThat().screenIsClear();
    }
    
    @Test
    public void trackerIsClearedAfterDiscardingSession()
    {
        app.getSleepTracker().setDetailsFrom(aSleepSession());
        app.getSleepTracker().recordArbitrarySession();
        app.getPostSleep().discard();
        app.getSleepTracker().assertThat().detailsAreCleared();
    }
    
    @Test
    public void trackerRetainsSessionAfterCancellingPostSleep()
    {
        SleepSessionBuilder sleepSession = aSleepSession();
        
        app.getSleepTracker().setDetailsFrom(sleepSession);
        app.getSleepTracker().recordArbitrarySession();
        app.getPostSleep().up();
        
        app.getSleepTracker().assertThat().detailsMatch(sleepSession);
        app.getSleepTracker().assertThat().isRecordingSession();
    }
    
    @Test
    public void keptSleepSessionIsAddedToTheDatabase()
    {
        database.assertThat.sleepSessionCountIs(0);
        app.recordArbitrarySleepSession();
        database.assertThat.sleepSessionCountIs(1);
    }
    
    @Test
    public void interruptionReasonIsClearedAfterEndingSession()
    {
        app.getSleepTracker().startSessionManually();
        app.getSleepTracker().startInterruptionWithReason("any reason");
        app.stopAndKeepSessionManually();
        app.getSleepTracker().startSessionManually();
        app.getSleepTracker().assertThat().interruptionReasonTextIsEmpty();
    }
    
    // BUG [21-10-15 5:43PM] -- flaky test.
    @Test
    public void interruptionIsAddedToTheDatabase()
    {
        database.assertThat.interruptionCountIs(0);
        
        app.getSleepTracker().startSessionManually();
        
        String expectedReason = "reason";
        app.getSleepTracker().startInterruptionWithReason(expectedReason);
        
        app.stopAndKeepSessionManually();
        
        database.assertThat.interruptionWithId(1).matches(interruptionWithReason(expectedReason));
    }
}
