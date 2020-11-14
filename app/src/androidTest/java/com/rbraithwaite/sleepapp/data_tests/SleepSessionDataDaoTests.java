package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.data.database.views.dao.SleepSessionDataDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SleepSessionDataDaoTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private SleepSessionDataDao sleepSessionDataDao;

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
        sleepSessionDataDao = database.getSleepSessionDataDao();
    }

    @After
    public void teardown()
    {
        database.close();
        database = null;
    }

    @Test
    public void getAllSleepSessionDataIds_returnsEmptyListWhenViewIsEmpty()
    {
        LiveData<List<Integer>> ids = sleepSessionDataDao.getAllSleepSessionDataIds();

        TestUtils.InstrumentationLiveDataSynchronizer<List<Integer>> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(ids);

        assertThat(ids.getValue().size(), is(0));
    }

    @Test
    public void getAllSleepSessionDataIds_LiveDataUpdatesWhenSleepSessionIsAdded() throws
            InterruptedException
    {
        LiveData<List<Integer>> ids = sleepSessionDataDao.getAllSleepSessionDataIds();

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
    public void getSleepSessionData_returnsNullWhenBadId()
    {
        int badId = 0; // bad since db is empty
        LiveData<SleepSessionData> sleepSession = sleepSessionDataDao.getSleepSessionData(badId);

        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSession);

        assertThat(sleepSession.getValue(), is(nullValue()));
    }

    @Test
    public void getSleepSessionData_positiveInput()
    {
        SleepSessionEntity expectedData = TestUtils.ArbitraryData.getSleepSessionEntity();
        int id = (int) database.getSleepSessionDao().addSleepSession(expectedData);

        LiveData<SleepSessionData> sleepSessionLiveData
                = database.getSleepSessionDataDao().getSleepSessionData(id);

        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSessionLiveData);

        SleepSessionData sleepSessionData = sleepSessionLiveData.getValue();
        assertThat(sleepSessionData.id, is(equalTo(id)));
        assertThat(sleepSessionData.startTime, is(equalTo(expectedData.startTime)));
        assertThat(sleepSessionData.duration, is(equalTo(expectedData.duration)));
    }

    @Test
    public void getSleepSessionData_updatesNullLiveData()
    {
        int id = 1;

        LiveData<SleepSessionData> sleepSessionLiveData
                = database.getSleepSessionDataDao().getSleepSessionData(id);
        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionData> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSessionLiveData);

        // data is initially invalid
        assertThat(sleepSessionLiveData.getValue(), is(nullValue()));

        // add a sleep session and verify that the invalid data is now valid
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        int databaseId = (int) database.getSleepSessionDao().addSleepSession(entity);
        assertThat(id, is(databaseId));
        synchronizer.sync();
        assertThat(sleepSessionLiveData.getValue(), is(notNullValue()));
    }
}
