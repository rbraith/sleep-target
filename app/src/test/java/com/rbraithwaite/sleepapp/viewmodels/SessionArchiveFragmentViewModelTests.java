package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppRepository mockRepository;
    SessionArchiveFragmentViewModel viewModel;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockRepository = mock(SleepAppRepository.class);
        viewModel = new SessionArchiveFragmentViewModel(mockRepository);
    }
    
    @After
    public void teardown()
    {
        mockRepository = null;
        viewModel = null;
    }
    
    @Test
    public void getInitialAddSessionData_returnsZeroId()
    {
        when(mockRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<Long>(null));
        
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getInitialAddSessionData();
        TestUtils.activateLocalLiveData(sleepSession);
        assertThat(sleepSession.getValue().entity.id, is(0));
    }
    
    @Test
    public void getInitialAddSessionData_usesCurrentWakeTimeGoal()
    {
        LiveData<Long> wakeTimeGoal = new MutableLiveData<>(100L);
        when(mockRepository.getWakeTimeGoal()).thenReturn(wakeTimeGoal);
        
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getInitialAddSessionData();
        TestUtils.activateLocalLiveData(sleepSession);
        assertThat(sleepSession.getValue().entity.wakeTimeGoal.getTime(),
                   is(equalTo(wakeTimeGoal.getValue())));
    }
    
    @Test
    public void getSleepSessionEntity_positiveInput()
    {
        int testId = 5;
        SleepSessionEntity expected = TestUtils.ArbitraryData.getSleepSessionEntity();
        expected.id = testId;
        when(mockRepository.getSleepSession(testId)).thenReturn(
                new MutableLiveData<SleepSessionEntity>(expected));
        
        // SUT
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getSleepSession(testId);
        
        TestUtils.activateLocalLiveData(sleepSession);
        assertThat(sleepSession.getValue().entity, is(expected));
    }
    
    @Test
    public void addSessionFromResult_addsSessionOnValidInput()
    {
        SleepSessionEntity sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        viewModel.addSleepSession(new SleepSessionWrapper(sleepSession));
        verify(mockRepository).addSleepSession(sleepSession);
    }
    
    @Test
    public void updateSessionFromResult_updatesSessionOnValidInput()
    {
        // for this test, it doesn't matter that the id for this data is 0
        SleepSessionEntity expected = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        viewModel.updateSleepSession(new SleepSessionWrapper(expected));
        
        verify(mockRepository, times(1)).updateSleepSession(expected);
    }
    
    @Test
    public void deleteSession_deletesSession()
    {
        SleepSessionEntity toDelete = TestUtils.ArbitraryData.getSleepSessionEntity();
        int sessionId = 5;
        toDelete.id = sessionId;
        viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        verify(mockRepository).deleteSleepSession(sessionId);
    }
    
    @Test
    public void deleteSession_returnsDeletedSessionId()
    {
        SleepSessionEntity toDelete = TestUtils.ArbitraryData.getSleepSessionEntity();
        int sessionId = 5;
        toDelete.id = sessionId;
        int deletedId = viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        assertThat(deletedId, is(sessionId));
    }
    
    
    @Test
    public void getListItemData_nullWhenBadID()
    {
        int badSessionID = 0;
        
        when(mockRepository.getSleepSession(badSessionID)).thenReturn(new MutableLiveData<SleepSessionEntity>(
                null));
        
        LiveData<SessionArchiveListItem> retrievedUILiveData =
                viewModel.getListItemData(badSessionID);
        TestUtils.activateLocalLiveData(retrievedUILiveData);
        
        assertThat(retrievedUILiveData.getValue(), is(nullValue()));
    }
    
    @Test
    public void getListItemData_positiveInput()
    {
        int sessionID = 0;
        
        SleepSessionEntity mockData = new SleepSessionEntity();
        mockData.duration = 18000000L; // 5 hours in millis
        mockData.startTime =
                new GregorianCalendar(2019, 8, 7, 6, 5).getTime();
        String expectedFormattedDuration = "5h 00m 00s";
        String expectedFormattedStartTime = "6:05 AM, Sep 7 2019";
        String expectedFormattedEndTime = "11:05 AM, Sep 7 2019";
        
        LiveData<SleepSessionEntity> mockLiveData = new MutableLiveData<>(mockData);
        when(mockRepository.getSleepSession(sessionID)).thenReturn(mockLiveData);
        
        LiveData<SessionArchiveListItem> retrievedListItemLiveData =
                viewModel.getListItemData(sessionID);
        // REFACTOR [20-11-14 7:50PM] -- consider grouping LiveData utils into TestUtils
        //  .LiveDataUtils
        //  (this would include the synchronizers)
        TestUtils.activateLocalLiveData(retrievedListItemLiveData);
        
        SessionArchiveListItem retrievedListItem = retrievedListItemLiveData.getValue();
        assertThat(retrievedListItem.startTime, is(equalTo(expectedFormattedStartTime)));
        assertThat(retrievedListItem.endTime, is(equalTo(expectedFormattedEndTime)));
        assertThat(retrievedListItem.sessionDuration, is(equalTo(expectedFormattedDuration)));
    }
    
    @Test
    public void getAllSleepSessionIds_returnsEmptyListWhenNoIds()
    {
        when(mockRepository.getAllSleepSessionIds()).thenReturn(new MutableLiveData<List<Integer>>(
                new ArrayList<Integer>()));
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionIds();
        
        assertThat(ids.getValue().size(), is(0));
    }
    
    
    @Test
    public void getAllSleepSessionIds_returnsIds()
    {
        LiveData<List<Integer>> testList =
                new MutableLiveData<>(TestUtils.ArbitraryData.getIdList());
        when(mockRepository.getAllSleepSessionIds()).thenReturn(testList);
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionIds();
        
        assertThat(ids.getValue(), is(equalTo(testList.getValue())));
    }
}
