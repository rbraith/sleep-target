package com.rbraithwaite.sleepapp.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithExtras;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
    SleepInterruptionDao mockSleepInterruptionDao;
    
    SleepSessionRepositoryImpl repository;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionDao = mock(SleepSessionDao.class);
        mockSleepInterruptionDao = mock(SleepInterruptionDao.class);
        Executor synchronousExecutor = new TestUtils.SynchronizedExecutor();
        repository =
                new SleepSessionRepositoryImpl(mockSleepSessionDao,
                                               mockSleepInterruptionDao,
                                               synchronousExecutor);
    }
    
    @After
    public void teardown()
    {
        mockSleepSessionDao = null;
        mockSleepInterruptionDao = null;
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
        when(mockSleepSessionDao.getSleepSessionWithExtras(positiveId))
                .thenReturn(new MutableLiveData<>(new SleepSessionWithExtras(
                        testEntity,
                        // REFACTOR [21-07-20 5:44PM] -- this should test tag & interruption
                        //  data as well.
                        new ArrayList<>(),
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

    // TODO [21-07-27 2:11AM] -- test tag data here as well.
    @Test
    public void updateSleepSession_updatesOnValidInput()
    {
        SleepSession testSleepSession = aSleepSession()
                .withId(5)
                .withInterruptions(
                        anInterruption().withId(2),
                        anInterruption().withId(3))
                .build();
        
        testSleepSession.deleteInterruption(2);
        testSleepSession.updateInterruption(valueOf(anInterruption().withId(3)));
        
        repository.updateSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> entityArg =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao).updateSleepSessionWithTags(
                entityArg.capture(),
                anyListOf(Integer.class));
        
        SleepSessionEntity sleepSessionEntity = entityArg.getValue();
        assertThat(sleepSessionEntity.id, is(testSleepSession.getId()));
        assertThat(sleepSessionEntity.startTime, is(testSleepSession.getStart()));
        assertThat(sleepSessionEntity.duration, is(testSleepSession.getDurationMillis()));
        
        ArgumentCaptor<List> deleteManyArg = ArgumentCaptor.forClass(List.class);
        verify(mockSleepInterruptionDao).deleteMany(deleteManyArg.capture());
        List<Integer> interruptionIds = deleteManyArg.getValue();
        assertThat(interruptionIds, contains(2));
        
        ArgumentCaptor<List> updateManyArg = ArgumentCaptor.forClass(List.class);
        verify(mockSleepInterruptionDao).updateMany(updateManyArg.capture());
        List<SleepInterruptionEntity> updatedEntities = updateManyArg.getValue();
        assertThat(updatedEntities.size(), is(1));
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
        // REFACTOR [21-07-27 2:15AM] -- lol I'm so dumb - times(1) can be omitted from verify
        //  calls - search the codebase for instances of this.
        verify(mockSleepSessionDao, times(1)).addSleepSessionWithExtras(
                sleepSessionArg.capture(),
                eq(newSleepSession.tagIds),
                anyListOf(SleepInterruptionEntity.class));
        
        SleepSessionEntity entity = sleepSessionArg.getValue();
        
        assertThat_NewSleepSession_equalTo_SleepSessionEntity(newSleepSession, entity);
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
//*********************************************************
// private methods
//*********************************************************

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
