package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.session_archive.data.UISleepSessionData;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
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
    public void getDefaultAddSessionData_sessionIdIsZero()
    {
        SessionEditData sessionEditData = viewModel.getDefaultAddSessionData();
        assertThat(sessionEditData.sessionId, is(0));
    }
    
    @Test
    public void getInitialEditSessionData_returnsCorrectDataOnValidId()
    {
        int testSessionId = 1;
        
        SleepSessionData expectedData = new SleepSessionData();
        expectedData.startTime = TestUtils.ArbitraryData.getDate();
        expectedData.duration = 10000L;
        expectedData.id = testSessionId;
        when(mockRepository.getSleepSessionData(testSessionId)).thenReturn(new MutableLiveData<SleepSessionData>(
                expectedData));
        
        LiveData<SessionEditData> editData = viewModel.getInitialEditSessionData(testSessionId);
        TestUtils.activateInstrumentationLiveData(editData);
        
        assertThat(editData.getValue().startDateTime, is(expectedData.startTime.getTime()));
        assertThat(editData.getValue().endDateTime,
                   is(expectedData.startTime.getTime() + expectedData.duration));
        assertThat(editData.getValue().sessionId, is(equalTo(expectedData.id)));
    }
    
    // TODO [20-12-15 1:17AM] -- define null arg behaviour of getInitialEditSessionData()
    
    @Test
    public void addSessionFromResult_addsSessionOnValidInput()
    {
        // setup
        long startDateTime = 100;
        long endDateTime = 200;
        SessionEditData result = new SessionEditData(startDateTime, endDateTime);
        
        // SUT
        viewModel.addSessionFromResult(result);
        
        // verification
        ArgumentCaptor<SleepSessionData> repoAddSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionData.class);
        verify(mockRepository).addSleepSessionData(repoAddSessionCaptor.capture());
        
        SleepSessionData sleepSessionData = repoAddSessionCaptor.getValue();
        assertThat(sleepSessionData.id, is(0));
        assertThat(sleepSessionData.startTime,
                   is(equalTo(DateUtils.getDateFromMillis(startDateTime))));
        assertThat(sleepSessionData.duration, is(endDateTime - startDateTime));
    }
    
    @Test
    public void updateSessionFromResult_updatesSessionOnValidInput()
    {
        // setup
        int sessionId = 5;
        long startDateTime = 100;
        long endDateTime = 200;
        SessionEditData result = new SessionEditData(sessionId, startDateTime, endDateTime);
        
        // SUT
        viewModel.updateSessionFromResult(result);
        
        // verification
        ArgumentCaptor<SleepSessionData> repoUpdateSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionData.class);
        verify(mockRepository).updateSleepSessionData(repoUpdateSessionCaptor.capture());
        
        SleepSessionData sleepSessionData = repoUpdateSessionCaptor.getValue();
        assertThat(sleepSessionData.id, is(sessionId));
        assertThat(sleepSessionData.startTime,
                   is(equalTo(DateUtils.getDateFromMillis(startDateTime))));
        assertThat(sleepSessionData.duration, is(endDateTime - startDateTime));
    }
    
    @Test
    public void deleteSession_deletesSession()
    {
        int sessionId = 5;
        viewModel.deleteSession(sessionId);
        verify(mockRepository).deleteSleepSessionData(sessionId);
    }
    
    
    @Test
    public void getSleepSessionData_nullWhenBadID()
    {
        int badSessionID = 0;
        
        when(mockRepository.getSleepSessionData(badSessionID)).thenReturn(new MutableLiveData<SleepSessionData>(
                null));
        
        LiveData<UISleepSessionData> retrievedUILiveData =
                viewModel.getSleepSessionData(badSessionID);
        TestUtils.activateLocalLiveData(retrievedUILiveData);
        
        assertThat(retrievedUILiveData.getValue(), is(nullValue()));
    }
    
    @Test
    public void getSleepSessionData_positiveInput()
    {
        int sessionID = 0;
        
        SleepSessionData mockData = new SleepSessionData();
        mockData.duration = 18000000L; // 5 hours in millis
        mockData.startTime =
                new GregorianCalendar(2019, 8, 7, 6, 5).getTime();
        String expectedFormattedDuration = "5h 00m 00s";
        String expectedFormattedStartTime = "6:05 AM, Sep 7 2019";
        String expectedFormattedEndTime = "11:05 AM, Sep 7 2019";
        
        LiveData<SleepSessionData> mockLiveData = new MutableLiveData<>(mockData);
        when(mockRepository.getSleepSessionData(sessionID)).thenReturn(mockLiveData);
        
        LiveData<UISleepSessionData> retrievedUILiveData = viewModel.getSleepSessionData(sessionID);
        // REFACTOR [20-11-14 7:50PM] -- consider grouping LiveData utils into TestUtils
        //  .LiveDataUtils
        //  (this would include the synchronizers)
        TestUtils.activateLocalLiveData(retrievedUILiveData);
        
        UISleepSessionData retrievedUIData = retrievedUILiveData.getValue();
        assertThat(retrievedUIData.startTime, is(equalTo(expectedFormattedStartTime)));
        assertThat(retrievedUIData.endTime, is(equalTo(expectedFormattedEndTime)));
        assertThat(retrievedUIData.sessionDuration, is(equalTo(expectedFormattedDuration)));
    }
    
    @Test
    public void getAllSleepSessionIds_returnsEmptyListWhenNoIds()
    {
        when(mockRepository.getAllSleepSessionDataIds()).thenReturn(new MutableLiveData<List<Integer>>(
                new ArrayList<Integer>()));
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionDataIds();
        
        assertThat(ids.getValue().size(), is(0));
    }
    
    
    @Test
    public void getAllSleepSessionIds_returnsIds()
    {
        LiveData<List<Integer>> testList =
                new MutableLiveData<>(TestUtils.ArbitraryData.getIdList());
        when(mockRepository.getAllSleepSessionDataIds()).thenReturn(testList);
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionDataIds();
        
        assertThat(ids.getValue(), is(equalTo(testList.getValue())));
    }
}
