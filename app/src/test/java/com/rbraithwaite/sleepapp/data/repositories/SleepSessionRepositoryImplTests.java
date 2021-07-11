package com.rbraithwaite.sleepapp.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepSessionRepositoryImplTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepSessionDao mockSleepSessionDao;
    
    SleepSessionRepositoryImpl repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionDao = mock(SleepSessionDao.class);
        Executor synchronousExecutor = new TestUtils.SynchronizedExecutor();
        repository =
                new SleepSessionRepositoryImpl(mockSleepSessionDao, synchronousExecutor);
    }
    
    @After
    public void teardown()
    {
        mockSleepSessionDao = null;
        repository = null;
    }
    
    // REFACTOR [21-03-15 3:08PM] make this a stub test instead? - test the value of the
    //  model returned.
    @Test
    public void getFirstSleepSessionStartingBefore_callsDatabase()
    {
        long millis = 12345L;
        repository.getFirstSleepSessionStartingBefore(millis);
        verify(mockSleepSessionDao, times(1)).getFirstSleepSessionStartingBefore(millis);
    }
    
    @Test
    public void getFirstSleepSessionStartingAfter_callsDatabase()
    {
        long millis = 12345L;
        repository.getFirstSleepSessionStartingAfter(millis);
        verify(mockSleepSessionDao, times(1)).getFirstSleepSessionStartingAfter(millis);
    }
    
    @Test
    public void getSleepSessionsInRange_callsDao()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date end = cal.getTime();
        
        repository.getSleepSessionsInRange(start, end);
        verify(mockSleepSessionDao, times(1)).getSleepSessionsInRange(
                start.getTime(),
                end.getTime());
    }
    
    @Test
    public void getSleepSession_positiveInput()
    {
        int positiveId = 1;
        
        SleepSessionEntity testEntity = TestUtils.ArbitraryData.getSleepSessionEntity();
        when(mockSleepSessionDao.getSleepSessionWithTags(positiveId))
                .thenReturn(new MutableLiveData<>(new SleepSessionWithTags(
                        testEntity,
                        new ArrayList<>())));
        
        LiveData<SleepSession> liveData = repository.getSleepSession(positiveId);
        TestUtils.activateLocalLiveData(liveData);
        SleepSession sleepSession = liveData.getValue();
        assertThat(sleepSession.getId(), is(testEntity.id));
        assertThat(sleepSession.getStart(), is(equalTo(testEntity.startTime)));
        assertThat(sleepSession.getDurationMillis(), is(equalTo(testEntity.duration)));
    }
    
    @Test
    public void deleteSleepSession_deletesSleepSession()
    {
        int sessionDataId = 5;
        
        repository.deleteSleepSession(sessionDataId);
        
        verify(mockSleepSessionDao).deleteSleepSession(sessionDataId);
    }
    
    @Test
    public void updateSleepSession_updatesOnValidInput()
    {
        SleepSession testSleepSession = TestUtils.ArbitraryData.getSleepSession();
        testSleepSession.setId(5);
        testSleepSession.setTags(Arrays.asList(
                new Tag(1, "tag 1"),
                new Tag(2, "tag 2")));
        
        repository.updateSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> databaseUpdateSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao,
               times(1)).updateSleepSessionWithTags(
                databaseUpdateSessionCaptor.capture(),
                // TODO [21-04-22 9:04PM] -- capture the tag ids & verify them.
                anyListOf(Integer.class));
        SleepSessionEntity sleepSessionEntity = databaseUpdateSessionCaptor.getValue();
        assertThat(sleepSessionEntity.id, is(testSleepSession.getId()));
        assertThat(sleepSessionEntity.startTime, is(testSleepSession.getStart()));
        assertThat(sleepSessionEntity.duration, is(testSleepSession.getDurationMillis()));
    }
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionRepository.NewSleepSessionData newSleepSession =
                TestUtils.ArbitraryData.getNewSleepSessionData();
        
        // SUT
        repository.addSleepSession(newSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> sleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao, times(1)).addSleepSessionWithExtras(
                sleepSessionArg.capture(),
                eq(newSleepSession.tagIds),
                anyListOf(SleepInterruptionEntity.class));
        
        SleepSessionEntity entity = sleepSessionArg.getValue();
        
        assertThat_NewSleepSession_equalTo_SleepSessionEntity(newSleepSession, entity);
    }

//*********************************************************
// private methods
//*********************************************************

    private MutableLiveData<List<SleepSessionEntity>> initDaoWithSleepSessions()
    {
        List<SleepSessionEntity> sleepSessions = createEntityList();
        
        MutableLiveData<List<SleepSessionEntity>> sleepSessionsLive =
                new MutableLiveData<>(sleepSessions);
        when(mockSleepSessionDao.getAllSleepSessions()).thenReturn(sleepSessionsLive);
        
        return sleepSessionsLive;
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    private List<SleepSessionEntity> createEntityList()
    {
        SleepSessionEntity sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        sleepSession.id = 1;
        SleepSessionEntity sleepSession2 = TestUtils.ArbitraryData.getSleepSessionEntity();
        sleepSession2.id = 2;
        ArrayList<SleepSessionEntity> sleepSessions = new ArrayList<>();
        sleepSessions.add(sleepSession);
        sleepSessions.add(sleepSession2);
        return sleepSessions;
    }
    
    private void assertThat_NewSleepSession_equalTo_SleepSessionEntity(
            SleepSessionRepository.NewSleepSessionData newSleepSession,
            SleepSessionEntity entity)
    {
        assertThat(entity.startTime, is(equalTo(newSleepSession.start)));
        assertThat(entity.endTime, is(equalTo(newSleepSession.end)));
        assertThat(entity.duration, is(equalTo(newSleepSession.durationMillis)));
        assertThat(entity.additionalComments, is(equalTo(newSleepSession.additionalComments)));
        assertThat(entity.moodIndex, is(equalTo(newSleepSession.mood.asIndex())));
        assertThat(entity.rating, is(equalTo(newSleepSession.rating)));
    }
}
