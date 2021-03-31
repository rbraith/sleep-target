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
        
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        // REFACTOR [20-12-16 7:23PM] -- consider making activateLocalLiveData variadic.
        TestUtils.activateLocalLiveData(startDateTime);
        TestUtils.activateLocalLiveData(endDateTime);
        assertThat(startDateTime.getValue(), is(equalTo(initialData.getStart().getTime())));
        assertThat(endDateTime.getValue(), is(equalTo(initialData.getEnd().getTime())));
    }
    
    @Test
    public void clearSessionData_clearsData()
    {
        viewModel.setSessionData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSession()));
        
        viewModel.clearSessionData();
        
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
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
        // REFACTOR [21-12-30 3:05PM] -- implement SleepSessionModel.equals().
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
        viewModel.setEndTime(
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
        LiveData<String> endDate = viewModel.getEndDateText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDate);
        
        String originalEndDate = endDate.getValue();
        
        // update end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // assert end date did not change
        // SMELL [20-12-6 8:31PM] -- see smell in setStartDate_leavesTimeOfDayUnchanged.
        synchronizer.sync();
        assertThat(endDate.getValue(), is(equalTo(originalEndDate)));
    }
    
    @Test
    public void setEndTime_updatesEndDateTime()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch end datetime
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        TestUtils.LocalLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        // set end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // end datetime changes
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar.getTimeInMillis())));
    }
    
    @Test
    public void setEndTime_updatesEndTime()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch end time
        LiveData<String> endTime = viewModel.getEndTimeText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endTime);
        
        // set end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // end time changes
        synchronizer.sync();
        assertThat(endTime.getValue(),
                   is(equalTo(dateTimeFormatter.formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void setEndDate_updatesEndDateTime()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        TestUtils.LocalLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        assertThat(endDateTime.getValue(), is(equalTo(calendar.getTimeInMillis())));
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar.getTimeInMillis())));
    }
    
    @Test
    public void setEndDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> endTime = viewModel.getEndTimeText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endTime);
        
        String originalEndTime = endTime.getValue();
        
        // update the end date
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        // SMELL [20-12-8 1:02AM] -- see smell in setStartDate_leavesTimeOfDayUnchanged.
        synchronizer.sync();
        assertThat(endTime.getValue(), is(equalTo(originalEndTime)));
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setEndDate_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set end before start
        calendar.add(Calendar.MONTH, -1);
        viewModel.setEndDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setEndDate_updatesEndDate()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> endDate = viewModel.getEndDateText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDate);
        
        assertThat(endDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(endDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
    }
    
    
    @Test
    public void getEndDateTime_isNullWhenNotSet()
    {
        assertThat(viewModel.getEndDateTime().getValue(), is(nullValue()));
    }
    
    @Test
    public void getStartDateTime_isNullWhenNotSet()
    {
        assertThat(viewModel.getStartDateTime().getValue(), is(nullValue()));
    }
    
    @Test
    public void setStartDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> startTimeText = viewModel.getStartTimeText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTimeText);
        
        String originalStartTimeText = startTimeText.getValue();
        
        // update the start date
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        // SMELL [20-11-30 11:07PM] -- should setStartDate not be notifying the time of day
        //  LiveData? Right now this happens because internally both track the same
        //  getStartDateTime long.
        //  If setStartDate doesn't notify the time of day LiveData, then this sync won't work,
        //  and the test will need to be fixed
        //  This would end up needing to be a pretty big refactor now that I think about it
        //      It would require keeping the Date & TimeOfDay values separate internally.
        synchronizer.sync();
        assertThat(startTimeText.getValue(), is(equalTo(originalStartTimeText)));
    }
    
    @Test
    public void setStartDate_updatesStartDate()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> startDate = viewModel.getStartDateText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        assertThat(startDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
        
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(startDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
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
        viewModel.setStartDay(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    // TODO [20-12-1 12:01AM] -- test needed for setStartDate being called without
    //  setStartDateTime first being called.
    // TODO [20-12-6 8:20PM] -- test needed for setStartTime being called without
    //  setStartDateTime first being called.
    
    @Test
    public void setStartTime_leavesDateUnchanged()
    {
        // set startDateTime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // begin watching getStartDate
        LiveData<String> startDate = viewModel.getStartDateText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        String originalStartDate = startDate.getValue();
        
        // update setStartTime
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // assert getStartDate is the same
        // SMELL [20-12-6 8:31PM] -- see smell in setStartDate_leavesTimeOfDayUnchanged.
        synchronizer.sync();
        assertThat(startDate.getValue(), is(equalTo(originalStartDate)));
    }
    
    @Test
    public void setStartTime_updatesStartTime()
    {
        // set startdatetime
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch start time
        LiveData<String> startTime = viewModel.getStartTimeText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // start time changes
        synchronizer.sync();
        assertThat(startTime.getValue(),
                   is(equalTo(dateTimeFormatter.formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void setStartTime_updatesStartDateTime()
    {
        // set startdatetime
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch startdatetime
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        TestUtils.LocalLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDateTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        // start time changes
        synchronizer.sync();
        assertThat(startDateTime.getValue(), is(equalTo(calendar.getTimeInMillis())));
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setStartTime_throwsIfStartIsAfterEnd()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setSessionData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set start after end
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        viewModel.setStartTime(
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
        viewModel.setEndTime(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
        synchronizer.sync();
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(formatter.formatDurationMillis(endOffsetSeconds * 1000))));
        
        // change start, check for duration update
        calendar.setTime(start);
        int startOffsetSeconds = 60; // 1 min
        calendar.add(GregorianCalendar.SECOND, startOffsetSeconds);
        viewModel.setStartTime(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
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
    
    @Test
    public void startTimeAndStartDate_nullIfNoSessionData()
    {
        LiveData<String> startTime = viewModel.getStartTimeText();
        LiveData<String> startDate = viewModel.getStartDateText();
        
        TestUtils.activateLocalLiveData(startTime);
        TestUtils.activateLocalLiveData(startDate);
        
        assertThat(startTime.getValue(), is(nullValue()));
        assertThat(startDate.getValue(), is(nullValue()));
    }
    
    @Test
    public void endTimeAndEndDate_nullIfNoSessionData()
    {
        LiveData<String> endTime = viewModel.getEndTimeText();
        LiveData<String> endDate = viewModel.getEndDateText();
        
        TestUtils.activateLocalLiveData(endTime);
        TestUtils.activateLocalLiveData(endDate);
        
        assertThat(endTime.getValue(), is(nullValue()));
        assertThat(endDate.getValue(), is(nullValue()));
    }
}
