package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.dao.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

// https://developer.android.com/training/data-storage/room/testing-db
@RunWith(AndroidJUnit4.class)
public class SleepSessionDaoTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private SleepSessionDao sleepSessionDao;
    
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, SleepAppDatabase.class).build();
        sleepSessionDao = database.getSleepSessionDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        int testSleepSessionId = (int) sleepSessionDao.addSleepSession(testSleepSession);
        
        LiveData<SleepSessionData> sleepSessionData =
                database.getSleepSessionDataDao().getSleepSessionData(testSleepSessionId);
        
        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSessionData);
        
        assertThat(sleepSessionData.getValue(), is(notNullValue()));
        assertThat(sleepSessionData.getValue().duration, is(equalTo(testSleepSession.duration)));
        assertThat(sleepSessionData.getValue().startTime, is(equalTo(testSleepSession.startTime)));
    }
}
