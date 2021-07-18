package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefs;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.data.prefs.Prefs;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class CurrentSessionPrefsTests
{
//*********************************************************
// public properties
//*********************************************************

    // REFACTOR [20-12-22 1:59AM] -- I should have a base test class which includes this timeout
    //  (and what else?)
    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
//*********************************************************
// package properties
//*********************************************************

    CurrentSessionPrefs prefs;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        // NOTE: I tried this with an async executor but was failing tests
        // probably a race condition - shouldn't be a problem in source where the view doesn't care
        // when its updated
        Context context = ApplicationProvider.getApplicationContext();
        prefs = new CurrentSessionPrefs(new Prefs(context), new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        prefs = null;
        TestUtils.resetSharedPreferences();
    }
    
    @Test
    public void clearCurrentSession_clearsSession()
    {
        LiveData<CurrentSessionPrefsData> currentSession = prefs.getCurrentSession();
        TestUtils.InstrumentationLiveDataSynchronizer<CurrentSessionPrefsData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        prefs.setCurrentSession(TestUtils.ArbitraryData.getCurrentSessionPrefsData());
        
        prefs.clearCurrentSession();
        
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(CurrentSessionPrefsData.empty())));
    }
    
    @Test
    public void getCurrentSession_reflects_setCurrentSession()
    {
        LiveData<CurrentSessionPrefsData> currentSession = prefs.getCurrentSession();
        TestUtils.InstrumentationLiveDataSynchronizer<CurrentSessionPrefsData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        assertThat(currentSession.getValue(), is(equalTo(CurrentSessionPrefsData.empty())));
        
        CurrentSessionPrefsData expected = TestUtils.ArbitraryData.getCurrentSessionPrefsData();
        prefs.setCurrentSession(expected);
        
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(expected)));
    }
}
