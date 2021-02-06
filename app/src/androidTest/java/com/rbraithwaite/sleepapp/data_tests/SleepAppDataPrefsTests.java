package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class SleepAppDataPrefsTests
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

    SleepAppDataPrefs prefs;

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
        prefs = new SleepAppDataPrefs(context, new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        prefs = null;
        TestUtils.resetSharedPreferences();
    }
    
    @Test
    public void getCurrentSession_nullWhenNew()
    {
        LiveData<Date> currentSession = prefs.getCurrentSession();
        
        TestUtils.activateInstrumentationLiveData(currentSession);
        assertThat(currentSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSession_reflects_setCurrentSession()
    {
        // initial is null
        LiveData<Date> currentSession = prefs.getCurrentSession();
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        assertThat(currentSession.getValue(), is(nullValue()));
        
        // setting to date
        Date testDate = TestUtils.ArbitraryData.getDate();
        prefs.setCurrentSession(testDate);
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(testDate)));
        
        //setting to null
        prefs.setCurrentSession(null);
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSession_reflects_setCurrentSession_afterSecondCall()
    {
        LiveData<Date> currentSession = prefs.getCurrentSession();
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        // a second call to getCurrentSession
        // currentSession should still be getting updates after this call
        LiveData<Date> currentSession2 = prefs.getCurrentSession();
        
        Date expectedCurrentSession = TestUtils.ArbitraryData.getDate();
        prefs.setCurrentSession(expectedCurrentSession);
        
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(expectedCurrentSession));
    }
    
    @Test
    public void DataPrefs_managesLiveDataUpdates()
    {
        LiveData<Date> currentSession = prefs.getCurrentSession();
        
        TestUtils.InstrumentationLiveDataSynchronizer<Date> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentSession);
        
        assertThat(currentSession.getValue(), is(nullValue()));
        
        Date testDate = TestUtils.ArbitraryData.getDate();
        prefs.setCurrentSession(testDate);
        
        synchronizer.sync();
        assertThat(currentSession.getValue(), is(equalTo(testDate)));
    }
    
    @Test
    public void setCurrentSession_setNull()
    {
        prefs.setCurrentSession(TestUtils.ArbitraryData.getDate());
        prefs.setCurrentSession(null);
        
        LiveData<Date> currentSession = prefs.getCurrentSession();
        TestUtils.activateInstrumentationLiveData(currentSession);
        
        assertThat(currentSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void getSleepDurationGoal_reflects_setSleepDurationGoal_afterSecondCall()
    {
        LiveData<Integer> sleepDurationGoal1 = prefs.getSleepDurationGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepDurationGoal1);
        
        // second call to getSleepDurationGoal
        // sleepDurationGoal1 should still be receiving updates after this
        LiveData<Integer> sleepDurationGoal2 = prefs.getSleepDurationGoal();
        
        int testDuration1 = 123;
        prefs.setSleepDurationGoal(testDuration1);
        synchronizer.sync();
        assertThat(sleepDurationGoal1.getValue(), is(equalTo(testDuration1)));
    }
    
    @Test
    public void getSleepDurationGoal_reflects_setSleepDurationGoal()
    {
        LiveData<Integer> sleepDurationGoal = prefs.getSleepDurationGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepDurationGoal);
        
        int testDuration1 = 123;
        prefs.setSleepDurationGoal(testDuration1);
        synchronizer.sync();
        assertThat(sleepDurationGoal.getValue(), is(equalTo(testDuration1)));
        
        int testDuration2 = 456;
        prefs.setSleepDurationGoal(testDuration2);
        synchronizer.sync();
        assertThat(sleepDurationGoal.getValue(), is(equalTo(testDuration2)));
    }
    
    @Test
    public void setSleepDurationGoal_setNull()
    {
        LiveData<Integer> sleepDurationGoal = prefs.getSleepDurationGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepDurationGoal);
        
        prefs.setSleepDurationGoal(123);
        synchronizer.sync();
        
        prefs.setSleepDurationGoal(null);
        synchronizer.sync();
        assertThat(sleepDurationGoal.getValue(), is(nullValue()));
    }
    
    @Test
    public void getSleepDurationGoal_isNullIfNotSet()
    {
        LiveData<Integer> sleepDurationGoal = prefs.getSleepDurationGoal();
        TestUtils.activateInstrumentationLiveData(sleepDurationGoal);
        assertThat(sleepDurationGoal.getValue(), is(nullValue()));
    }
    
    @Test
    public void getWakeTimeGoal_reflects_setWakeTimeGoal()
    {
        LiveData<Long> wakeTimeGoal = prefs.getWakeTimeGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(wakeTimeGoal);
        
        // starts null
        assertThat(wakeTimeGoal.getValue(), is(nullValue()));
        
        // setting the wake time
        long expectedWakeTime = 12345L;
        prefs.setWakeTimeGoal(expectedWakeTime);
        
        // verify
        synchronizer.sync();
        assertThat(wakeTimeGoal.getValue(), is(expectedWakeTime));
    }
    
    @Test
    public void getWakeTimeGoal_reflects_setWakeTimeGoal_afterSecondCall()
    {
        LiveData<Long> wakeTimeGoal = prefs.getWakeTimeGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(wakeTimeGoal);
        
        // a second call to getWakeTimeGoal
        // wakeTimeGoal should still be getting updates after this call
        LiveData<Long> wakeTimeGoal2 = prefs.getWakeTimeGoal();
        
        long expectedWakeTime = 12345L;
        prefs.setWakeTimeGoal(expectedWakeTime);
        
        synchronizer.sync();
        assertThat(wakeTimeGoal.getValue(), is(expectedWakeTime));
    }
    
    @Test
    public void clearWakeTimeGoal_clearsWakeTimeGoal()
    {
        long wakeTimeVal = 12345;
        prefs.setWakeTimeGoal(wakeTimeVal);
        LiveData<Long> wakeTime = prefs.getWakeTimeGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(wakeTime);
        assertThat(wakeTime.getValue(), is(wakeTimeVal));
        
        prefs.clearWakeTimeGoal();
        
        synchronizer.sync();
        assertThat(wakeTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void clearSleepDurationGoal_clearsGoal()
    {
        int testGoal = 1234;
        prefs.setSleepDurationGoal(testGoal);
        LiveData<Integer> sleepDurationGoal = prefs.getSleepDurationGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<Integer> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepDurationGoal);
        assertThat(sleepDurationGoal.getValue(), is(testGoal));
        
        prefs.clearSleepDurationGoal();
        
        synchronizer.sync();
        assertThat(sleepDurationGoal.getValue(), is(nullValue()));
    }
}
