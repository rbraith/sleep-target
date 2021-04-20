package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
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
    public void getSleepSessionWithTags_returnsNullIfNoSleepSession()
    {
        LiveData<SleepSessionWithTags> result = sleepSessionDao.getSleepSessionWithTags(2);
        TestUtils.activateInstrumentationLiveData(result);
        
        assertThat(result.getValue(), is(nullValue()));
    }
    
    @Test
    public void getSleepSessionWithTags_returnsCorrectData()
    {
        TagEntity tag1 = new TagEntity();
        tag1.text = "tag1";
        TagEntity tag2 = new TagEntity();
        tag2.text = "tag2";
        
        TagDao tagDao = database.getTagDao();
        int tag1Id = (int) tagDao.addTag(tag1);
        int tag2Id = (int) tagDao.addTag(tag2);
        tag1.id = tag1Id;
        tag2.id = tag2Id;
        
        int sessionId = (int) sleepSessionDao.addSleepSessionWithTags(
                TestUtils.ArbitraryData.getSleepSessionEntity(),
                Arrays.asList(tag1Id, tag2Id));
        
        LiveData<SleepSessionWithTags> result = sleepSessionDao.getSleepSessionWithTags(sessionId);
        TestUtils.activateInstrumentationLiveData(result);
        
        assertThat(result.getValue().tags.size(), is(2));
        assertThat(result.getValue().tags.get(0), is(equalTo(tag1)));
        assertThat(result.getValue().tags.get(1), is(equalTo(tag2)));
    }
    
    @Test
    public void getSleepSessionWithTags_returnsEmptyTagListIfNoTags()
    {
        int newId =
                (int) sleepSessionDao.addSleepSession(TestUtils.ArbitraryData.getSleepSessionEntity());
        LiveData<SleepSessionWithTags> result = sleepSessionDao.getSleepSessionWithTags(newId);
        TestUtils.activateInstrumentationLiveData(result);
        
        assertThat(result.getValue().tags.isEmpty(), is(true));
    }
    
    @Test
    public void getFirstSleepSessionStartingBefore_returnsNullIfNoSleepSession()
    {
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        sleepSessionDao.addSleepSession(entity);
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(entity.startTime.getTime() - 500);
        
        assertThat(sleepSessionDao.getFirstSleepSessionStartingBefore(cal.getTimeInMillis()),
                   is(nullValue()));
    }
    
    @Test
    public void getFirstSleepSessionStartingBefore_returnsCorrectSleepSession()
    {
        // 2 entities starting before
        SleepSessionEntity entity1 = TestUtils.ArbitraryData.getSleepSessionEntity();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(entity1.startTime);
        
        cal.add(Calendar.HOUR, 12);
        SleepSessionEntity expected = TestUtils.ArbitraryData.getSleepSessionEntity();
        expected.startTime = cal.getTime();
        
        // 1 entity starting after
        cal.add(Calendar.HOUR, 34);
        SleepSessionEntity entity2 = TestUtils.ArbitraryData.getSleepSessionEntity();
        entity2.startTime = cal.getTime();
        
        sleepSessionDao.addSleepSession(entity1);
        sleepSessionDao.addSleepSession(expected);
        sleepSessionDao.addSleepSession(entity2);
        
        SleepSessionEntity result = sleepSessionDao.getFirstSleepSessionStartingBefore(
                expected.startTime.getTime() + 1234);
        
        assertThat(result.startTime, is(equalTo(expected.startTime)));
    }
    
    @Test
    public void getSleepSessionsInRange_returnsCorrectSleepSessions()
    {
        int year = 2021;
        int month = 1;
        int rangeStartDay = 21;
        int rangeEndDay = 24;
        GregorianCalendar rangeStart = new GregorianCalendar(year, month, rangeStartDay);
        GregorianCalendar rangeEnd = new GregorianCalendar(year, month, rangeEndDay);
        
        long hourInMillis = 60 * 60 * 1000;
        
        // outside of range (too early)
        SleepSession sleepSession1 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession1.setStart(new GregorianCalendar(year, month, rangeStartDay - 1).getTime());
        sleepSession1.setDurationMillis(hourInMillis);
        
        // starting before range, ending in range
        SleepSession sleepSession2 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession2.setStart(new GregorianCalendar(year,
                                                     month,
                                                     rangeStartDay - 1,
                                                     22,
                                                     0).getTime());
        sleepSession2.setDurationMillis(hourInMillis * 4);
        
        // fully in range
        SleepSession sleepSession3 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession3.setStart(new GregorianCalendar(year, month, rangeStartDay, 5, 0).getTime());
        sleepSession3.setDurationMillis(hourInMillis);
        
        // starting in range, ending after range
        SleepSession sleepSession4 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession4.setStart(new GregorianCalendar(year,
                                                     month,
                                                     rangeEndDay - 1,
                                                     22,
                                                     0).getTime());
        sleepSession4.setDurationMillis(hourInMillis * 4);
        
        // outside of range (too late)
        SleepSession sleepSession5 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession5.setStart(new GregorianCalendar(year, month, rangeEndDay, 5, 0).getTime());
        
        sleepSessionDao.addSleepSession(ConvertSleepSession.toEntity(
                sleepSession1));
        sleepSessionDao.addSleepSession(ConvertSleepSession.toEntity(
                sleepSession2));
        sleepSessionDao.addSleepSession(ConvertSleepSession.toEntity(
                sleepSession3));
        sleepSessionDao.addSleepSession(ConvertSleepSession.toEntity(
                sleepSession4));
        sleepSessionDao.addSleepSession(ConvertSleepSession.toEntity(
                sleepSession5));
        
        // SUT
        LiveData<List<SleepSessionEntity>> entities = sleepSessionDao.getSleepSessionsInRange(
                rangeStart.getTimeInMillis(),
                rangeEnd.getTimeInMillis());
        TestUtils.InstrumentationLiveDataSynchronizer<List<SleepSessionEntity>> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(entities);
        
        // verify
        SleepSession[] modelsInRange = {sleepSession2, sleepSession3, sleepSession4};
        assertThat(entities.getValue().size(), is(modelsInRange.length));
        for (int i = 0; i < modelsInRange.length; i++) {
            assertThat(entities.getValue().get(i).startTime,
                       is(equalTo(modelsInRange[i].getStart())));
            assertThat(entities.getValue().get(i).duration,
                       is(equalTo(modelsInRange[i].getDurationMillis())));
        }
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
        SleepSessionEntity updatedSleepSession = new SleepSessionEntity();
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MONTH, 5);
        updatedSleepSession.startTime = calendar.getTime();
        updatedSleepSession.duration = TestUtils.ArbitraryData.getDurationMillis() + 2500L;
        calendar.add(Calendar.MILLISECOND, (int) updatedSleepSession.duration);
        updatedSleepSession.endTime = calendar.getTime();
        updatedSleepSession.id = testSleepSessionId;
        updatedSleepSession.additionalComments = "updated!";
        
        sleepSessionDao.updateSleepSession(updatedSleepSession);
        
        // verify the sleep session updated successfully
        LiveData<SleepSessionEntity> sleepSession =
                database.getSleepSessionDao().getSleepSession(testSleepSessionId);
        TestUtils.activateInstrumentationLiveData(sleepSession);
        assertThat(sleepSession.getValue(), is(equalTo(updatedSleepSession)));
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        testSleepSession.id = (int) sleepSessionDao.addSleepSession(testSleepSession);
        
        LiveData<SleepSessionEntity> sleepSession =
                sleepSessionDao.getSleepSession(testSleepSession.id);
        
        TestUtils.activateInstrumentationLiveData(sleepSession);
        assertThat(sleepSession.getValue(), is(equalTo(testSleepSession)));
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
        SleepSessionEntity expected = TestUtils.ArbitraryData.getSleepSessionEntity();
        expected.id = (int) sleepSessionDao.addSleepSession(expected);
        
        LiveData<SleepSessionEntity> sleepSessionLiveData =
                sleepSessionDao.getSleepSession(expected.id);
        
        TestUtils.activateInstrumentationLiveData(sleepSessionLiveData);
        
        SleepSessionEntity sleepSession = sleepSessionLiveData.getValue();
        assertThat(sleepSession, is(equalTo(expected)));
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
