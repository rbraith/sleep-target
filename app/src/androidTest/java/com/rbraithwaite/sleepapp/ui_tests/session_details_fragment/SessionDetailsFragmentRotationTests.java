package com.rbraithwaite.sleepapp.ui_tests.session_details_fragment;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SessionDetailsTestDriver;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SessionDetailsFragmentRotationTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void displayedValuesPersistAcrossOrientationChange()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        sessionDetails.rotateScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sessionDetails.assertThat().displayedValuesMatch(sleepSession);
    }
}
