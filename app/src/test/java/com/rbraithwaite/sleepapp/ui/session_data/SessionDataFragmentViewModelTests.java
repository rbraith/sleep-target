package com.rbraithwaite.sleepapp.ui.session_data;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

// REFACTOR [20-12-8 8:52PM] -- consider splitting this into separate test classes?
@RunWith(AndroidJUnit4.class)
public class SessionDataFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SessionDataFragmentViewModel viewModel;
    DateTimeFormatter dateTimeFormatter;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        dateTimeFormatter = new DateTimeFormatter();
        viewModel = new SessionDataFragmentViewModel(dateTimeFormatter);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        dateTimeFormatter = null;
    }
    
    @Test
    public void initSessionData_initializesDataOnValidInput()
    {
        SleepSession initialData = TestUtils.ArbitraryData.getSleepSession();
        viewModel.setSessionData(new SleepSessionWrapper(initialData));
        
        LiveData<GregorianCalendar> start = viewModel.getStartCalendar();
        LiveData<GregorianCalendar> end = viewModel.getEndCalendar();
        // REFACTOR [20-12-16 7:23PM] -- consider making activateLocalLiveData variadic.
        TestUtils.activateLocalLiveData(start);
        TestUtils.activateLocalLiveData(end);
        assertThat(start.getValue().getTime(), is(equalTo(initialData.getStart())));
        assertThat(end.getValue().getTime(), is(equalTo(initialData.getEnd())));
    }
    
    @Test
    public void clearSessionData_clearsData()
    {
        viewModel.setSessionData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSession()));
        
        viewModel.clearSessionData();
        
        LiveData<GregorianCalendar> startDateTime = viewModel.getStartCalendar();
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.activateLocalLiveData(startDateTime);
        TestUtils.activateLocalLiveData(endDateTime);
        assertThat(startDateTime.getValue(), is(nullValue()));
        assertThat(endDateTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void getResult_matchesViewModelState()
    {
        // set viewmodel state
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date startDateTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10);
        calendar.add(Calendar.MONTH, 1);
        long duration = calendar.getTimeInMillis() - startDateTime.getTime();
        
        SleepSession expected = new SleepSession(
                5,
                startDateTime,
                duration);
        viewModel.setSessionData(new SleepSessionWrapper(expected));
        String expectedComments = "test";
        viewModel.setAdditionalComments(expectedComments);
        
        // check result values
        SleepSession result = viewModel.getResult().getModel();
        // REFACTOR [21-12-30 3:05PM] -- implement SleepSession.equals().
        assertThat(result.getId(), is(equalTo(expected.getId())));
        assertThat(result.getStart(), is(equalTo(expected.getStart())));
        assertThat(result.getDurationMillis(), is(equalTo(expected.getDurationMillis())));
        assertThat(result.getAdditionalComments(), is(equalTo(expectedComments)));
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setEndTime_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set end before start
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        viewModel.setEndTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }
    
    @Test
    public void setEndTime_leavesDateUnchanged()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // begin watching end date
        LiveData<GregorianCalendar> endDate = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDate);
        
        GregorianCalendar originalEnd = endDate.getValue();
        
        // update end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                  calendar.get(Calendar.MINUTE));
        
        // assert end date did not change
        synchronizer.sync();
        assertDatesAreTheSame(endDate.getValue(), originalEnd);
    }
    
    @Test
    public void setEndTime_updatesEnd()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch end datetime
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        // set end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                  calendar.get(Calendar.MINUTE));
        
        // end datetime changes
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar)));
    }
    
    @Test
    public void setEndDate_updatesEnd()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar)));
    }
    
    @Test
    public void setEndDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> endTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endTime);
        
        GregorianCalendar originalEnd = endTime.getValue();
        
        // update the end date
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertTimesOfDayAreTheSame(endTime.getValue(), originalEnd);
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setEndDate_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set end before start
        calendar.add(Calendar.MONTH, -1);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setStartDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> start = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(start);
        
        GregorianCalendar originalStart = start.getValue();
        
        // update the start date
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertTimesOfDayAreTheSame(start.getValue(), originalStart);
    }
    
    @Test
    public void setStartDate_updatesStart()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> startDate = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(startDate.getValue(), is(equalTo(calendar)));
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setStartDate_throwsIfStartIsAfterEnd()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set start after end
        calendar.add(Calendar.MONTH, 1);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setStartTime_leavesDateUnchanged()
    {
        // set startDateTime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // begin watching getStartDate
        LiveData<GregorianCalendar> startDate = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        GregorianCalendar originalStart = startDate.getValue();
        
        // update setStartTime
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE));
        
        // assert getStartDate is the same
        synchronizer.sync();
        assertDatesAreTheSame(startDate.getValue(), originalStart);
    }
    
    @Test
    public void setStartTime_updatesStart()
    {
        // set startdatetime
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch start time
        LiveData<GregorianCalendar> startTime = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE));
        
        // start time changes
        synchronizer.sync();
        assertThat(startTime.getValue(), is(equalTo(calendar)));
    }
    
    // TODO [20-12-1 12:01AM] -- test needed for setStartDate being called without
    //  setStartDateTime first being called.
    // TODO [20-12-6 8:20PM] -- test needed for setStartTime being called without
    //  setStartDateTime first being called.
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setStartTime_throwsIfStartIsAfterEnd()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set start after end
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        viewModel.setStartTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }
    
    @Test
    public void sessionDuration_updatesWhenStartAndEndChange()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date start = calendar.getTime(); // saving this here, as the calendar is moved to the end
        
        DurationFormatter formatter = new DurationFormatter();
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> sessionDurationText = viewModel.getSessionDurationText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(sessionDurationText);
        
        assertThat(sessionDurationText.getValue(), is(equalTo(formatter.formatDurationMillis(0))));
        
        // change end, check for duration update
        int endOffsetSeconds = 120; // 2 min
        calendar.add(GregorianCalendar.SECOND, endOffsetSeconds);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
        synchronizer.sync();
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(formatter.formatDurationMillis(endOffsetSeconds * 1000))));
        
        // change start, check for duration update
        calendar.setTime(start);
        int startOffsetSeconds = 60; // 1 min
        calendar.add(GregorianCalendar.SECOND, startOffsetSeconds);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
        synchronizer.sync();
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(formatter.formatDurationMillis(
                           (endOffsetSeconds - startOffsetSeconds) * 1000))));
    }
    
    @Test
    public void getSessionDuration_positiveInput()
    {
        // ________________________________________ init the data
        Date startDateTime = TestUtils.ArbitraryData.getDate();
        
        int testDurationMillis = 300000; // 5 min
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(startDateTime, testDurationMillis)));
        
        LiveData<String> sessionDuration = viewModel.getSessionDurationText();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = new DurationFormatter().formatDurationMillis(testDurationMillis);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void assertDatesAreTheSame(GregorianCalendar a, GregorianCalendar b)
    {
        assertThat(a.get(Calendar.YEAR), is(equalTo(b.get(Calendar.YEAR))));
        assertThat(a.get(Calendar.MONTH), is(equalTo(b.get(Calendar.MONTH))));
        assertThat(a.get(Calendar.DAY_OF_MONTH), is(equalTo(b.get(Calendar.DAY_OF_MONTH))));
    }
    
    private void assertTimesOfDayAreTheSame(GregorianCalendar a, GregorianCalendar b)
    {
        assertThat(a.get(Calendar.HOUR_OF_DAY), is(equalTo(b.get(Calendar.HOUR_OF_DAY))));
        assertThat(a.get(Calendar.MINUTE), is(equalTo(b.get(Calendar.MINUTE))));
    }
}
