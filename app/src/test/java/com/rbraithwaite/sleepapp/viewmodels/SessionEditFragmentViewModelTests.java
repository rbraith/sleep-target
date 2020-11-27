package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditFragmentViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class SessionEditFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SessionEditFragmentViewModel viewModel;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        viewModel = new SessionEditFragmentViewModel();
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
    }
    
    @Test
    public void sessionDuration_updatesWhenStartAndEndChange()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        Date start = calendar.getTime(); // saving this here, as the calendar is moved to the end
        
        DurationFormatter formatter = new DurationFormatter();
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        LiveData<String> sessionDuration = viewModel.getSessionDuration();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(sessionDuration);
        
        assertThat(sessionDuration.getValue(), is(equalTo(formatter.formatDurationMillis(0))));
        
        // change end, check for duration update
        int endOffsetMillis = 10000;
        calendar.add(GregorianCalendar.MILLISECOND, endOffsetMillis);
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        synchronizer.sync();
        assertThat(sessionDuration.getValue(),
                   is(equalTo(formatter.formatDurationMillis(endOffsetMillis))));
        
        // change start, check for duration update
        calendar.setTime(start);
        int startOffsetMillis = 5000;
        calendar.add(GregorianCalendar.MILLISECOND, startOffsetMillis);
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        
        synchronizer.sync();
        assertThat(sessionDuration.getValue(),
                   is(equalTo(formatter.formatDurationMillis(
                           endOffsetMillis - startOffsetMillis))));
    }
    
    // REFACTOR [20-11-26 10:06PM] -- convert the below getSessionDuration tests to
    //  a parameterized one? would I be able to have individually parameterized tests
    //  with robolectric? initial research is bleak, look at ParameterizedRobolectricTestRunner
    //  https://medium.com/@harmittaa/parametrized-tests-with-robolectric-3666c4794f6b
    //  https://blog.jayway.com/2015/03/19/parameterized-testing-with-robolectric/
    @Test
    public void sessionDurationIsZero_whenStartIsNull()
    {
        Date endDate = TestUtils.ArbitraryData.getDate();
        viewModel.setEndDateTime(endDate.getTime());
        
        LiveData<String> sessionDuration = viewModel.getSessionDuration();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = new DurationFormatter().formatDurationMillis(0);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void sessionDurationIsZero_whenEndIsNull()
    {
        Date startDate = TestUtils.ArbitraryData.getDate();
        viewModel.setStartDateTime(startDate.getTime());
        
        LiveData<String> sessionDuration = viewModel.getSessionDuration();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = new DurationFormatter().formatDurationMillis(0);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void sessionDurationIsZero_whenStartAndEndAreNull()
    {
        LiveData<String> sessionDuration = viewModel.getSessionDuration();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = new DurationFormatter().formatDurationMillis(0);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void getSessionDuration_positiveInput()
    {
        // ________________________________________ init the data
        Date startDateTime = TestUtils.ArbitraryData.getDate();
        
        int testDurationMillis = 300000; // 5 min
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startDateTime);
        calendar.add(GregorianCalendar.MILLISECOND, testDurationMillis);
        
        Date endDateTime = calendar.getTime();
        
        // ________________________________________ update the viewmodel and assert
        viewModel.setStartDateTime(startDateTime.getTime());
        viewModel.setEndDateTime(endDateTime.getTime());
        
        LiveData<String> sessionDuration = viewModel.getSessionDuration();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = new DurationFormatter().formatDurationMillis(testDurationMillis);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void startTimeAndStartDate_startNull()
    {
        LiveData<String> startTime = viewModel.getStartTime();
        LiveData<String> startDate = viewModel.getStartDate();
        
        TestUtils.activateLocalLiveData(startTime);
        TestUtils.activateLocalLiveData(startDate);
        
        assertThat(startTime.getValue(), is(nullValue()));
        assertThat(startDate.getValue(), is(nullValue()));
    }
    
    @Test
    public void setStartDateTime_updatesStartTimeAndStartDate()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        
        LiveData<String> startTime = viewModel.getStartTime();
        LiveData<String> startDate = viewModel.getStartDate();
        
        viewModel.setStartDateTime(testDate.getTime());
        
        TestUtils.activateLocalLiveData(startTime);
        TestUtils.activateLocalLiveData(startDate);
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        assertThat(startTime.getValue(), is(equalTo(formatter.formatTimeOfDay(testDate))));
        assertThat(startDate.getValue(), is(equalTo(formatter.formatDate(testDate))));
    }
    
    @Test
    public void endTimeAndEndDate_startNull()
    {
        LiveData<String> endTime = viewModel.getEndTime();
        LiveData<String> endDate = viewModel.getEndDate();
        
        TestUtils.activateLocalLiveData(endTime);
        TestUtils.activateLocalLiveData(endDate);
        
        assertThat(endTime.getValue(), is(nullValue()));
        assertThat(endDate.getValue(), is(nullValue()));
    }
    
    @Test
    public void setEndDateTime_updatesEndTimeAndEndDate()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        
        LiveData<String> endTime = viewModel.getEndTime();
        LiveData<String> endDate = viewModel.getEndDate();
        
        viewModel.setEndDateTime(testDate.getTime());
        
        TestUtils.activateLocalLiveData(endTime);
        TestUtils.activateLocalLiveData(endDate);
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        assertThat(endTime.getValue(), is(equalTo(formatter.formatTimeOfDay(testDate))));
        assertThat(endDate.getValue(), is(equalTo(formatter.formatDate(testDate))));
    }
}
