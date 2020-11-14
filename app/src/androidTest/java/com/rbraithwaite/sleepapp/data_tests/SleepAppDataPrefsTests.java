package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class SleepAppDataPrefsTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppDataPrefs prefs;
    Context context;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        // NOTE: I tried this with an async executor but was failing tests
        // probably a race condition - shouldn't be a problem in source where the view doesn't care
        // when its updated
        prefs = new SleepAppDataPrefs(new TestUtils.SynchronizedExecutor());
        context = ApplicationProvider.getApplicationContext();
    }
    
    @After
    public void teardown()
    {
        prefs = null;
        context = null;
        TestUtils.resetSharedPreferences();
    }
    
    @Test
    public void getCurrentSession_nullWhenNew()
    {
        LiveData<Date> currentSession = prefs.getCurrentSession(context);
        
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        assertThat(currentSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSession_reflects_setCurrentSession()
    {
        // initial is null
        LiveData<Date> currentSession = prefs.getCurrentSession(context);
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        assertThat(currentSession.getValue(), is(nullValue()));
        
        // setting to date
        Date testDate = TestUtils.ArbitraryData.getDate();
        prefs.setCurrentSession(context, testDate);
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(testDate)));
        
        //setting to null
        prefs.setCurrentSession(context, null);
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void DataPrefs_managesLiveDataUpdates()
    {
        LiveData<Date> currentSession = prefs.getCurrentSession(context);
        
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        assertThat(currentSession.getValue(), is(nullValue()));
        
        Date testDate = TestUtils.ArbitraryData.getDate();
        prefs.setCurrentSession(context, testDate);
        
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(testDate)));
    }
    
    @Test
    public void setCurrentSession_setNull()
    {
        prefs.setCurrentSession(context, TestUtils.ArbitraryData.getDate());
        prefs.setCurrentSession(context, null);
        
        LiveData<Date> currentSession = prefs.getCurrentSession(context);
        TestUtils.activateInstrumentationLiveData(currentSession);
        
        assertThat(currentSession.getValue(), is(nullValue()));
    }
}
