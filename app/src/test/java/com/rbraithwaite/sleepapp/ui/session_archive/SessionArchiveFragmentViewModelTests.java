package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    SleepSessionRepository mockSleepSessionRepository;
    SessionArchiveFragmentViewModel viewModel;
    DateTimeFormatter dateTimeFormatter;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        dateTimeFormatter = new DateTimeFormatter();
        viewModel = new SessionArchiveFragmentViewModel(
                mockSleepSessionRepository, dateTimeFormatter);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockSleepSessionRepository = null;
        dateTimeFormatter = null;
    }
    
    @Test
    public void getInitialAddSessionData_returnsZeroId()
    {
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getInitialAddSessionData();
        TestUtils.activateLocalLiveData(sleepSession);
        SleepSessionModel sleepSessionModel = sleepSession.getValue().getModel();
        assertThat(sleepSessionModel.getId(), is(0));
    }
    
    @Test
    public void getSleepSessionEntity_positiveInput()
    {
        int testId = 5;
        SleepSessionModel expected = TestUtils.ArbitraryData.getSleepSessionModel();
        expected.setId(testId);
        when(mockSleepSessionRepository.getSleepSession(testId)).thenReturn(
                new MutableLiveData<SleepSessionModel>(expected));
        
        // SUT
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getSleepSession(testId);
        
        TestUtils.activateLocalLiveData(sleepSession);
        assertThat(sleepSession.getValue().getModel(), is(expected));
    }
    
    @Test
    public void addSessionFromResult_addsSessionOnValidInput()
    {
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        viewModel.addSleepSession(new SleepSessionWrapper(sleepSession));
        verify(mockSleepSessionRepository).addSleepSession(sleepSession);
    }
    
    @Test
    public void updateSessionFromResult_updatesSessionOnValidInput()
    {
        // for this test, it doesn't matter that the id for this data is 0
        SleepSessionModel expected = TestUtils.ArbitraryData.getSleepSessionModel();
        
        viewModel.updateSleepSession(new SleepSessionWrapper(expected));
        
        verify(mockSleepSessionRepository, times(1)).updateSleepSession(expected);
    }
    
    @Test
    public void deleteSession_deletesSession()
    {
        SleepSessionModel toDelete = TestUtils.ArbitraryData.getSleepSessionModel();
        int sessionId = 5;
        toDelete.setId(sessionId);
        viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        verify(mockSleepSessionRepository).deleteSleepSession(sessionId);
    }
    
    @Test
    public void deleteSession_returnsDeletedSessionId()
    {
        SleepSessionModel toDelete = TestUtils.ArbitraryData.getSleepSessionModel();
        int sessionId = 5;
        toDelete.setId(sessionId);
        int deletedId = viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        assertThat(deletedId, is(sessionId));
    }
    
    
    @Test
    public void getListItemData_nullWhenBadID()
    {
        int badSessionID = 0;
        
        when(mockSleepSessionRepository.getSleepSession(badSessionID)).thenReturn(
                new MutableLiveData<SleepSessionModel>(null));
        
        LiveData<SessionArchiveListItem> retrievedUILiveData =
                viewModel.getListItemData(badSessionID);
        TestUtils.activateLocalLiveData(retrievedUILiveData);
        
        assertThat(retrievedUILiveData.getValue(), is(nullValue()));
    }
    
    @Test
    public void getListItemData_positiveInput()
    {
        int sessionID = 1;
        
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        Date start = calendar.getTime();
        int duration = 18000000; // 5 hours in millis
        calendar.add(Calendar.MILLISECOND, duration);
        Date end = calendar.getTime();
        
        String expectedFormattedDuration = new DurationFormatter().formatDurationMillis(duration);
        String expectedFormattedStartTime = dateTimeFormatter.formatFullDate(start);
        String expectedFormattedEndTime = dateTimeFormatter.formatFullDate(end);
        
        SleepSessionModel mockData = new SleepSessionModel(
                start,
                duration);
        
        LiveData<SleepSessionModel> mockLiveData = new MutableLiveData<>(mockData);
        when(mockSleepSessionRepository.getSleepSession(sessionID)).thenReturn(mockLiveData);
        
        // SUT
        LiveData<SessionArchiveListItem> retrievedListItemLiveData =
                viewModel.getListItemData(sessionID);
        // REFACTOR [20-11-14 7:50PM] -- consider grouping LiveData utils into
        //  TestUtils.LiveDataUtils (this would include the synchronizers)
        TestUtils.activateLocalLiveData(retrievedListItemLiveData);
        
        SessionArchiveListItem retrievedListItem = retrievedListItemLiveData.getValue();
        assertThat(retrievedListItem.startTime, is(equalTo(expectedFormattedStartTime)));
        assertThat(retrievedListItem.endTime, is(equalTo(expectedFormattedEndTime)));
        assertThat(retrievedListItem.sessionDuration, is(equalTo(expectedFormattedDuration)));
    }
    
    @Test
    public void getAllSleepSessionIds_returnsEmptyListWhenNoIds()
    {
        when(mockSleepSessionRepository.getAllSleepSessionIds()).thenReturn(new MutableLiveData<List<Integer>>(
                new ArrayList<Integer>()));
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionIds();
        
        assertThat(ids.getValue().size(), is(0));
    }
    
    
    @Test
    public void getAllSleepSessionIds_returnsIds()
    {
        LiveData<List<Integer>> testList =
                new MutableLiveData<>(TestUtils.ArbitraryData.getIdList());
        when(mockSleepSessionRepository.getAllSleepSessionIds()).thenReturn(testList);
        LiveData<List<Integer>> ids = viewModel.getAllSleepSessionIds();
        
        assertThat(ids.getValue(), is(equalTo(testList.getValue())));
    }
}
