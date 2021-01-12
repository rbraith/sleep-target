package com.rbraithwaite.sleepapp.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepAppRepositoryTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppDataPrefs mockPrefs;
    SleepAppDatabase mockDatabase;
    
    SleepAppRepository repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        mockDatabase = mock(SleepAppDatabase.class);
        Executor synchronousExecutor = new TestUtils.SynchronizedExecutor();
        repository = new SleepAppRepository(mockPrefs, mockDatabase, synchronousExecutor);
    }
    
    @After
    public void teardown()
    {
        mockPrefs = null;
        mockDatabase = null;
        repository = null;
    }
    
    @Test
    public void setCurrentSession_setsCurrentSession()
    {
        Date testStartTime = TestUtils.ArbitraryData.getDate();
        repository.setCurrentSession(testStartTime);
        verify(mockPrefs, times(1)).setCurrentSession(testStartTime);
    }
    
    @Test
    public void clearCurrentSession_callsPrefs()
    {
        repository.clearCurrentSession();
        verify(mockPrefs, times(1)).setCurrentSession(null);
    }
    
    
    @Test
    public void getCurrentSession_nullWhenNoSession()
    {
        LiveData<Date> mockLiveData = new MutableLiveData<>(null);
        when(mockPrefs.getCurrentSession()).thenReturn(mockLiveData);
        
        assertThat(repository.getCurrentSession().getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSession_reflectsSetSession()
    {
        final GregorianCalendar calendar = new GregorianCalendar();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                calendar.setTime(invocation.getArgumentAt(0, Date.class));
                return null;
            }
        }).when(mockPrefs).setCurrentSession(any(Date.class));
        doAnswer(new Answer<LiveData<Date>>()
        {
            @Override
            public LiveData<Date> answer(InvocationOnMock invocation) throws Throwable
            {
                return new MutableLiveData<Date>(calendar.getTime());
            }
        }).when(mockPrefs).getCurrentSession();
        
        calendar.set(2020, 6, 6, 13, 15);
        Date testDate1 = calendar.getTime();
        
        calendar.set(2012, 3, 4, 15, 6);
        Date testDate2 = calendar.getTime();
        
        repository.setCurrentSession(testDate1);
        assertThat(repository.getCurrentSession().getValue(), is(equalTo(testDate1)));
        
        repository.setCurrentSession(testDate2);
        assertThat(repository.getCurrentSession().getValue(), is(equalTo(testDate2)));
    }
    
    @Test
    public void updateSleepSession_updatesOnValidInput()
    {
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        testSleepSession.id = 5;
        
        repository.updateSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> databaseUpdateSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao,
               times(1)).updateSleepSession(databaseUpdateSessionCaptor.capture());
        SleepSessionEntity sleepSessionEntity = databaseUpdateSessionCaptor.getValue();
        assertThat(sleepSessionEntity.id, is(testSleepSession.id));
        assertThat(sleepSessionEntity.startTime, is(testSleepSession.startTime));
        assertThat(sleepSessionEntity.duration, is(testSleepSession.duration));
        // TODO [20-12-16 10:43PM] -- eventually, checks for other sleep session data daos.
    }
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        repository.addSleepSession(testSleepSession);
        
        verify(mockSleepSessionDao, times(1)).addSleepSession(testSleepSession);
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void getAllSleepSessionIds_returnEmptyListWhenNoIds()
    {
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        when(mockSleepSessionDao.getAllSleepSessionIds()).thenReturn(
                new MutableLiveData<List<Integer>>(new ArrayList<Integer>()));
        
        List<Integer> ids = repository.getAllSleepSessionIds().getValue();
        assertThat(ids.size(), is(0));
    }
    
    // REFACTOR [20-11-14 8:08PM] -- this test just duplicates
    //  getAllSleepSessionIds_returnEmptyListWhenNoIds
    @Test
    public void getAllSleepSessionIds_returnsAllIds()
    {
        List<Integer> testIdData = TestUtils.ArbitraryData.getIdList();
        
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        when(mockSleepSessionDao.getAllSleepSessionIds())
                .thenReturn(new MutableLiveData<>(testIdData));
        
        List<Integer> ids = repository.getAllSleepSessionIds().getValue();
        assertThat(ids, is(equalTo(testIdData)));
    }
    
    @Test
    public void getSleepSessionEntity_postiveInput()
    {
        int positiveId = 1;
        
        SleepSessionEntity expectedData = TestUtils.ArbitraryData.getSleepSessionEntity();
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        when(mockSleepSessionDao.getSleepSession(positiveId))
                .thenReturn(new MutableLiveData<>(expectedData));
        
        LiveData<SleepSessionEntity> liveData = repository.getSleepSession(positiveId);
        assertThat(liveData.getValue(), is(equalTo(expectedData)));
    }
    
    @Test
    public void deleteSleepSessionData_deletesSleepSessionData()
    {
        SleepSessionDao mockSleepSessionDao = setupMockSleepSessionDao();
        
        int sessionDataId = 5;
        
        repository.deleteSleepSession(sessionDataId);
        
        verify(mockSleepSessionDao).deleteSleepSession(sessionDataId);
    }
    
    @Test
    public void setWakeTimeGoal_updatesPrefs()
    {
        long expectedWakeTime = 12345L;
        
        repository.setWakeTimeGoal(expectedWakeTime);
        
        verify(mockPrefs, times(1)).setWakeTimeGoal(expectedWakeTime);
    }
    
    @Test
    public void getWakeTimeGoal_updatesFromPrefs()
    {
        LiveData<Long> expected = new MutableLiveData<>(12345L);
        when(mockPrefs.getWakeTimeGoal()).thenReturn(expected);
        
        LiveData<Long> wakeTimeGoal = repository.getWakeTimeGoal();
        assertThat(wakeTimeGoal, is(expected));
    }

//*********************************************************
// private methods
//*********************************************************

    private SleepSessionDao setupMockSleepSessionDao()
    {
        SleepSessionDao mockSleepSessionDao = mock(SleepSessionDao.class);
        when(mockDatabase.getSleepSessionDao()).thenReturn(mockSleepSessionDao);
        return mockSleepSessionDao;
    }
    // TODO
    //  null input
    //  negative input
}
