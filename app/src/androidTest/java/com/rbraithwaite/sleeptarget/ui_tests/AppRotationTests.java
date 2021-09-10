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
package com.rbraithwaite.sleeptarget.ui_tests;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.ApplicationTestDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;



/**
 * These are instrumented tests which exercise multiple components of the app related to device
 * rotation.
 */
@RunWith(AndroidJUnit4.class)
public class AppRotationTests
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
    public void postSleepDiscardDialogTest()
    {
        app.getSleepTracker().recordArbitrarySession();
        app.getPostSleep().clickDiscardButton();
        
        app.getPostSleep().rotateScreenTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // just making sure this doesn't crash lol
        app.getPostSleep().confirmDiscard();
    }
    
    @Test
    public void sessionDetailsDeleteDialogDoesntCrash()
    {
        database.addSleepSession(valueOf(aSleepSession().withNoTags()));
        app.navigateTo(ApplicationTestDriver.Destination.ARCHIVE);
        app.getSessionArchive().openSessionDetailsFor(0);
        
        app.getSessionDetails().pressNegativeButton();
        
        app.getSessionDetails().rotateScreenTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // no crash pls
        app.getSessionDetails().confirmDelete();
    }
}
