package com.rbraithwaite.sleepapp.data.sleep_session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
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
public class SleepSessionRepositoryTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppDataPrefs mockPrefs;
    SleepSessionDao mockSleepSessionDao;
    
    SleepSessionRepository repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        mockSleepSessionDao = mock(SleepSessionDao.class);
        Executor synchronousExecutor = new TestUtils.SynchronizedExecutor();
        repository =
                new SleepSessionRepository(mockPrefs, mockSleepSessionDao, synchronousExecutor);
    }
    
    @After
    public void teardown()
    {
        mockPrefs = null;
        mockSleepSessionDao = null;
        repository = null;
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
        
        LiveData<SleepSessionModel> liveData = repository.getSleepSession(positiveId);
        TestUtils.activateLocalLiveData(liveData);
        SleepSessionModel sleepSessionModel = liveData.getValue();
        assertThat(sleepSessionModel.getId(), is(testEntity.id));
        assertThat(sleepSessionModel.getStart(), is(equalTo(testEntity.startTime)));
        assertThat(sleepSessionModel.getDuration(), is(equalTo(testEntity.duration)));
        assertThat(sleepSessionModel.getWakeTimeGoal(), is(equalTo(testEntity.wakeTimeGoal)));
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
        SleepSessionModel testSleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        testSleepSession.setId(5);
        
        repository.updateSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> databaseUpdateSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao,
               times(1)).updateSleepSession(databaseUpdateSessionCaptor.capture());
        SleepSessionEntity sleepSessionEntity = databaseUpdateSessionCaptor.getValue();
        assertThat(sleepSessionEntity.id, is(testSleepSession.getId()));
        assertThat(sleepSessionEntity.startTime, is(testSleepSession.getStart()));
        assertThat(sleepSessionEntity.duration, is(testSleepSession.getDuration()));
        assertThat(sleepSessionEntity.wakeTimeGoal, is(testSleepSession.getWakeTimeGoal()));
    }
    
    // TODO [20-12-16 12:37AM] -- define updateSleepSession() behaviour on null or invalid args.
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionModel testSleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        repository.addSleepSession(testSleepSession);
        
        ArgumentCaptor<SleepSessionEntity> addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockSleepSessionDao, times(1)).addSleepSession(addSleepSessionArg.capture());
        
        SleepSessionEntity daoArg = addSleepSessionArg.getValue();
        assertThat(testSleepSession.getId(), is(equalTo(daoArg.id)));
        assertThat(testSleepSession.getStart(), is(equalTo(daoArg.startTime)));
        assertThat(testSleepSession.getDuration(), is(equalTo(daoArg.duration)));
        assertThat(testSleepSession.getWakeTimeGoal(), is(equalTo(daoArg.wakeTimeGoal)));
    }
}
