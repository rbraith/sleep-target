package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
    public void deleteSleepSession_deletesSleepSession()
    {
        LiveData<List<Integer>> sessionIds =
                database.getSleepSessionDao().getAllSleepSessionIds();
        TestUtils.InstrumentationLiveDataSynchronizer<List<Integer>> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sessionIds);
        
        int sleepSessionId =
                (int) sleepSessionDao.addSleepSession(TestUtils.ArbitraryData.getSleepSessionEntity());
        
        synchronizer.sync();
        assertThat(sessionIds.getValue().size(), is(1));
        
        // SUT
        sleepSessionDao.deleteSleepSession(sleepSessionId);
        
        synchronizer.sync();
        assertThat(sessionIds.getValue().size(), is(0));
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
        calendar.add(Calendar.MINUTE, 10);
        Date newWakeTimeGoal = calendar.getTime();
        
        SleepSessionEntity updatedSleepSession = new SleepSessionEntity();
        updatedSleepSession.startTime = newStartDate;
        updatedSleepSession.duration = newDuration;
        updatedSleepSession.wakeTimeGoal = newWakeTimeGoal;
        updatedSleepSession.id = testSleepSessionId;
        
        sleepSessionDao.updateSleepSession(updatedSleepSession);
        
        // verify the sleep session updated successfully
        LiveData<SleepSessionEntity> sleepSession =
                database.getSleepSessionDao().getSleepSession(testSleepSessionId);
        TestUtils.activateInstrumentationLiveData(sleepSession);
        assertThat(sleepSession.getValue().id, is(testSleepSessionId));
        assertThat(sleepSession.getValue().startTime, is(equalTo(newStartDate)));
        assertThat(sleepSession.getValue().duration, is(newDuration));
        assertThat(sleepSession.getValue().wakeTimeGoal, is(equalTo(newWakeTimeGoal)));
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        int testSleepSessionId = (int) sleepSessionDao.addSleepSession(testSleepSession);
        
        LiveData<SleepSessionEntity> sleepSession =
                database.getSleepSessionDao().getSleepSession(testSleepSessionId);
        
        TestUtils.activateInstrumentationLiveData(sleepSession);
        assertThat(sleepSession.getValue(), is(notNullValue()));
        assertThat(sleepSession.getValue().duration, is(equalTo(testSleepSession.duration)));
        assertThat(sleepSession.getValue().startTime, is(equalTo(testSleepSession.startTime)));
    }
    
    @Test
    public void getAllSleepSessionIds_returnsEmptyListWhenViewIsEmpty()
    {
        LiveData<List<Integer>> ids = sleepSessionDao.getAllSleepSessionIds();
        
        TestUtils.activateInstrumentationLiveData(ids);
        
        assertThat(ids.getValue().size(), is(0));
    }
    
    @Test
    public void getAllSleepSessionIds_LiveDataUpdatesWhenSleepSessionIsAdded() throws
            InterruptedException
    {
        LiveData<List<Integer>> ids = sleepSessionDao.getAllSleepSessionIds();
        
        TestUtils.InstrumentationLiveDataSynchronizer<List<Integer>> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(ids);
        
        // assert initial value
        assertThat(ids.getValue().size(), is(0));
        
        // assert livedata was updated
        int id = (int) database.getSleepSessionDao()
                .addSleepSession(TestUtils.ArbitraryData.getSleepSessionEntity());
        assertThat(id, is(not(0)));
        
        synchronizer.sync();
        assertThat(ids.getValue().size(), is(1));
        assertThat(id, is(ids.getValue().get(0)));
    }
    
    @Test
    public void getSleepSession_returnsNullWhenBadId()
    {
        int badId = 0; // bad since db is empty
        LiveData<SleepSessionEntity> sleepSession = sleepSessionDao.getSleepSession(badId);
        
        TestUtils.activateInstrumentationLiveData(sleepSession);
        
        assertThat(sleepSession.getValue(), is(nullValue()));
    }
    
    @Test
    public void getSleepSession_positiveInput()
    {
        SleepSessionEntity expectedData = TestUtils.ArbitraryData.getSleepSessionEntity();
        int id = (int) sleepSessionDao.addSleepSession(expectedData);
        
        LiveData<SleepSessionEntity> sleepSessionLiveData =
                sleepSessionDao.getSleepSession(id);
        
        TestUtils.activateInstrumentationLiveData(sleepSessionLiveData);
        
        SleepSessionEntity sleepSession = sleepSessionLiveData.getValue();
        assertThat(sleepSession.id, is(equalTo(id)));
        assertThat(sleepSession.startTime, is(equalTo(expectedData.startTime)));
        assertThat(sleepSession.duration, is(equalTo(expectedData.duration)));
    }
    
    @Test
    public void getSleepSession_updatesNullLiveData()
    {
        int id = 1;
        
        LiveData<SleepSessionEntity> sleepSessionLiveData =
                sleepSessionDao.getSleepSession(id);
        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionEntity> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSessionLiveData);
        
        // data is initially invalid
        assertThat(sleepSessionLiveData.getValue(), is(nullValue()));
        
        // add a sleep session and verify that the invalid data is now valid
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        int databaseId = (int) sleepSessionDao.addSleepSession(entity);
        assertThat(id, is(databaseId));
        synchronizer.sync();
        assertThat(sleepSessionLiveData.getValue(), is(notNullValue()));
    }
}
