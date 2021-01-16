package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragmentViewModel;
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
    public void getWakeTimeGoal_reflectsInitialData()
    {
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        viewModel.initSessionData(new SleepSessionWrapper(initialData));
        
        LiveData<String> wakeTimeGoal = viewModel.getWakeTimeGoal();
        TestUtils.activateLocalLiveData(wakeTimeGoal);
        
        assertThat(wakeTimeGoal.getValue(), is(equalTo(
                dateTimeFormatter.formatTimeOfDay(initialData.getWakeTimeGoal()))));
    }
    
    @Test
    public void initSessionData_initializesDataOnValidInput()
    {
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        viewModel.initSessionData(new SleepSessionWrapper(initialData));
        
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
        viewModel.initSessionData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSessionModel()));
        
        viewModel.clearSessionData();
        
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        TestUtils.activateLocalLiveData(startDateTime);
        TestUtils.activateLocalLiveData(endDateTime);
        assertThat(startDateTime.getValue(), is(nullValue()));
        assertThat(endDateTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void sessionDataIsInitialized_exercise()
    {
        assertThat(viewModel.sessionDataIsInitialized(), is(false));
        
        viewModel.initSessionData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSessionModel()));
        
        assertThat(viewModel.sessionDataIsInitialized(), is(true));
        
        viewModel.clearSessionData();
        
        assertThat(viewModel.sessionDataIsInitialized(), is(false));
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
        
        SleepSessionModel expected = new SleepSessionModel(
                5,
                startDateTime,
                duration,
                TestUtils.ArbitraryData.getWakeTimeGoal());
        viewModel.initSessionData(new SleepSessionWrapper(expected));
        
        // check result values
        SleepSessionModel result = viewModel.getResult().getValue();
        // REFACTOR [21-12-30 3:05PM] -- implement SleepSessionModel.equals().
        assertThat(result.getId(), is(equalTo(expected.getId())));
        assertThat(result.getStart(), is(equalTo(expected.getStart())));
        assertThat(result.getDuration(), is(equalTo(expected.getDuration())));
        assertThat(result.getWakeTimeGoal(), is(equalTo(expected.getWakeTimeGoal())));
    }
    
    @Test(expected = SessionDataFragmentViewModel.InvalidDateTimeException.class)
    public void setEndTime_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
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
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        // begin watching end date
        LiveData<String> endDate = viewModel.getEndDate();
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
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
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
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        // watch end time
        LiveData<String> endTime = viewModel.getEndTime();
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
        
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        TestUtils.LocalLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        assertThat(endDateTime.getValue(), is(equalTo(calendar.getTimeInMillis())));
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
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
        
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        LiveData<String> endTime = viewModel.getEndTime();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endTime);
        
        String originalEndTime = endTime.getValue();
        
        // update the end date
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
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
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        // set end before start
        calendar.add(Calendar.MONTH, -1);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setEndDate_updatesEndDate()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        LiveData<String> endDate = viewModel.getEndDate();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDate);
        
        assertThat(endDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
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
    public void setStartDateTime_updates_getStartDateTime()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        
        viewModel.setStartDateTime(testDate.getTime());
        
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        TestUtils.activateLocalLiveData(startDateTime);
        
        assertThat(startDateTime.getValue(), is(equalTo(testDate.getTime())));
    }
    
    @Test
    public void setEndDateTime_updates_getEndDateTime()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        
        viewModel.setEndDateTime(testDate.getTime());
        
        LiveData<Long> endDateTime = viewModel.getEndDateTime();
        TestUtils.activateLocalLiveData(endDateTime);
        
        assertThat(endDateTime.getValue(), is(equalTo(testDate.getTime())));
    }
    
    @Test
    public void setStartDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        
        LiveData<String> startTime = viewModel.getStartTime();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTime);
        
        String originalStartTime = startTime.getValue();
        
        // update the start date
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setStartDate(
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
        assertThat(startTime.getValue(), is(equalTo(originalStartTime)));
    }
    
    @Test
    public void setStartDate_updatesStartDate()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        LiveData<String> startDate = viewModel.getStartDate();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        assertThat(startDate.getValue(),
                   is(equalTo(dateTimeFormatter.formatDate(calendar.getTime()))));
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setStartDate(
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
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        // set start after end
        calendar.add(Calendar.MONTH, 1);
        viewModel.setStartDate(
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
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        
        // begin watching getStartDate
        LiveData<String> startDate = viewModel.getStartDate();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        String originalStartDate = startDate.getValue();
        
        // update setStartTime
        calendar.add(Calendar.MINUTE, 15);
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
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        
        // watch start time
        LiveData<String> startTime = viewModel.getStartTime();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, 15);
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
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        
        // watch startdatetime
        LiveData<Long> startDateTime = viewModel.getStartDateTime();
        TestUtils.LocalLiveDataSynchronizer<Long> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDateTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, 15);
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
        
        viewModel.setStartDateTime(calendar.getTimeInMillis());
        viewModel.setEndDateTime(calendar.getTimeInMillis());
        
        // set start after end
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        viewModel.setStartTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
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
        
        assertThat(startTime.getValue(), is(equalTo(dateTimeFormatter.formatTimeOfDay(testDate))));
        assertThat(startDate.getValue(), is(equalTo(dateTimeFormatter.formatDate(testDate))));
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
        
        assertThat(endTime.getValue(), is(equalTo(dateTimeFormatter.formatTimeOfDay(testDate))));
        assertThat(endDate.getValue(), is(equalTo(dateTimeFormatter.formatDate(testDate))));
    }
}
