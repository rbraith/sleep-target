package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.ApplicationTestDriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * These are instrumented tests which exercise multiple components of the app in a single test.
 */
@RunWith(AndroidJUnit4.class)
public class SleepAppTests
{
//*********************************************************
// private properties
//*********************************************************

    private ApplicationTestDriver app;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        app = new ApplicationTestDriver();
    }
    
    @After
    public void teardown()
    {
        app = null;
    }
    
    @Test
    public void keptSessionDetailsAppearInArchive()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        
        app.getSleepTracker().recordSpecificSession(sleepSession);
        app.navigateTo(ApplicationTestDriver.Destination.ARCHIVE);
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().assertThat().displayedValuesMatch(sleepSession);
    }
}
