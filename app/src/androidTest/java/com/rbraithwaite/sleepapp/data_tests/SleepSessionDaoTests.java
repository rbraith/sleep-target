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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    public void updateSleepSession_updatesSleepSessionOnValidInput()
    {
        // create a sleep session
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        int testSleepSessionId = (int) sleepSessionDao.addSleepSession(testSleepSession);
        
        // update the sleep session
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MONTH, 5);
        Date newStartDate = calendar.getTime();
        long newDuration = TestUtils.ArbitraryData.getDurationMillis() + 2500L;
        SleepSessionEntity updatedSleepSession =
                SleepSessionEntity.create(newStartDate, newDuration);
        updatedSleepSession.id = testSleepSessionId;
        
        sleepSessionDao.updateSleepSession(updatedSleepSession);
        
        // verify the sleep session updated successfully
        LiveData<SleepSessionData> sleepSessionData =
                database.getSleepSessionDataDao().getSleepSessionData(testSleepSessionId);
        TestUtils.activateInstrumentationLiveData(sleepSessionData);
        assertThat(sleepSessionData.getValue().id, is(testSleepSessionId));
        assertThat(sleepSessionData.getValue().startTime, is(equalTo(newStartDate)));
        assertThat(sleepSessionData.getValue().duration, is(newDuration));
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        int testSleepSessionId = (int) sleepSessionDao.addSleepSession(testSleepSession);
        
        LiveData<SleepSessionData> sleepSessionData =
                database.getSleepSessionDataDao().getSleepSessionData(testSleepSessionId);
        
        TestUtils.activateInstrumentationLiveData(sleepSessionData);
        assertThat(sleepSessionData.getValue(), is(notNullValue()));
        assertThat(sleepSessionData.getValue().duration, is(equalTo(testSleepSession.duration)));
        assertThat(sleepSessionData.getValue().startTime, is(equalTo(testSleepSession.startTime)));
    }
}
