package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
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
    public void addSessionFromResult_addsSessionOnValidInput()
    {
        // setup
        long startDateTime = 100;
        long endDateTime = 200;
        SessionEditData result = new SessionEditData(startDateTime, endDateTime);
        
        // SUT
        viewModel.addSessionFromResult(result);
        
        // verification
        ArgumentCaptor<SleepSessionEntity> repoAddSessionCaptor =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockRepository).addSleepSession(repoAddSessionCaptor.capture());
        
        SleepSessionEntity sleepSessionEntity = repoAddSessionCaptor.getValue();
        assertThat(sleepSessionEntity.id, is(0));
        assertThat(sleepSessionEntity.startTime,
                   is(equalTo(DateUtils.getDateFromMillis(startDateTime))));
        assertThat(sleepSessionEntity.duration, is(endDateTime - startDateTime));
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
