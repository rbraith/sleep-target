package com.rbraithwaite.sleepapp.data.sleep_session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.repositories.SleepSessionRepositoryImpl;
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
    public void getAllSleepSessionIds_returnAllIds()
    {
        ArrayList<Integer> allIds = new ArrayList<>(Arrays.asList(1, 2, 3));
        when(mockSleepSessionDao.getAllSleepSessionIds()).thenReturn(
                new MutableLiveData<List<Integer>>(allIds));
        
        LiveData<List<Integer>> allIdsLive = repository.getAllSleepSessionIds();
        TestUtils.activateLocalLiveData(allIdsLive);
        assertThat(allIdsLive.getValue(), is((List<Integer>) allIds));
    }
    
    @Test
    public void getSleepSession_positiveInput()
    {
        int positiveId = 1;
        
        SleepSessionEntity testEntity = TestUtils.ArbitraryData.getSleepSessionEntity();
        when(mockSleepSessionDao.getSleepSession(positiveId))
                .thenReturn(new MutableLiveData<>(testEntity));
        
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
        
        repository.updateSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> databaseUpdateSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao,
               times(1)).updateSleepSession(databaseUpdateSessionCaptor.capture());
        SleepSessionEntity sleepSessionEntity = databaseUpdateSessionCaptor.getValue();
        assertThat(sleepSessionEntity.id, is(testSleepSession.getId()));
        assertThat(sleepSessionEntity.startTime, is(testSleepSession.getStart()));
        assertThat(sleepSessionEntity.duration, is(testSleepSession.getDurationMillis()));
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSession testSleepSession = TestUtils.ArbitraryData.getSleepSession();
        repository.addSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao, times(1)).addSleepSession(addSleepSessionArg.capture());
        
        SleepSessionEntity daoArg = addSleepSessionArg.getValue();
        assertThat(testSleepSession.getId(), is(equalTo(daoArg.id)));
        assertThat(testSleepSession.getStart(), is(equalTo(daoArg.startTime)));
        assertThat(testSleepSession.getDurationMillis(), is(equalTo(daoArg.duration)));
    }
}