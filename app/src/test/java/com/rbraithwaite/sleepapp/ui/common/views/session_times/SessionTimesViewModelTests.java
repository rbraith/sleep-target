package com.rbraithwaite.sleepapp.ui.common.views.session_times;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SessionBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.Day;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.LiveDataTestUtils.activateLocally;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aCalendar;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SessionTimesViewModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getDurationText_updatesWhenStartOrEndChange()
    {
        DateBuilder start = aDate();
        DateBuilder end = aDate();
        
        SessionTimesViewModel viewModel =
                createViewModelWith(aSession().withStart(start).withNoDuration());
        
        LiveData<String> durationText = activateLocally(viewModel::getDurationText);
        assertThat(durationText.getValue(), is(equalTo("0h 00m 00s")));
        
        viewModel.setStartTimeOfDay(TimeOfDay.of(valueOf(start.addHours(-1))));
        assertThat(durationText.getValue(), is(equalTo("1h 00m 00s")));
        
        viewModel.setEndTimeOfDay(TimeOfDay.of(valueOf(end.addHours(1))));
        assertThat(durationText.getValue(), is(equalTo("2h 00m 00s")));
    }
    
    @Test
    public void getStartCalendar_returnsCorrectCalendar()
    {
        DateBuilder expectedStart = aDate();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withStart(expectedStart));
        
        assertThat(viewModel.getStartCalendar(),
                   is(equalTo(aCalendar().with(expectedStart).build())));
    }
    
    @Test
    public void getEndCalendar_returnsCorrectCalendar()
    {
        DateBuilder expectedEnd = aDate();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withEnd(expectedEnd));
        
        assertThat(viewModel.getEndCalendar(), is(equalTo(aCalendar().with(expectedEnd).build())));
    }
    
    @Test
    public void setStartDate_updatesStartProperly()
    {
        DateBuilder start = aDate();
        TimeUtils timeUtils = new TimeUtils();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withStart(start));
        
        long originalTimeOfDay = timeUtils.getTimeOfDayOf(valueOf(start));
        
        viewModel.setStartDate(Day.of(valueOf(start.addTime(-1, -1, -1))));
        
        LiveData<Date> newStart = viewModel.getStart();
        TestUtils.activateLocalLiveData(newStart);
        
        // time of day is left unchanged
        assertThat(timeUtils.getTimeOfDayOf(newStart.getValue()), is(equalTo(originalTimeOfDay)));
        // day is updated
        assertThat(Day.of(newStart.getValue()), is(equalTo(Day.of(valueOf(start)))));
    }
    
    @Test(expected = SessionTimesViewModel.InvalidDateTimeException.class)
    public void setStartDate_throwsIfStartIsAfterEnd()
    {
        DateBuilder start = aDate();
        SessionTimesViewModel viewModel =
                createViewModelWith(aSession().withStart(start).withNoDuration());
        
        viewModel.setStartDate(Day.of(valueOf(start.addDays(1))));
    }
    
    @Test
    public void setStartTimeOfDay_updatesStartProperly()
    {
        DateBuilder start = aDate();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withStart(start));
        
        Day originalDay = Day.of(valueOf(start));
        
        // 25: change both the day & time of day
        viewModel.setStartTimeOfDay(TimeOfDay.of(valueOf(start.addHours(-25))));
        
        LiveData<Date> newStart = activateLocally(viewModel::getStart);
        
        // day is left unchanged
        assertThat(Day.of(newStart.getValue()), is(equalTo(originalDay)));
        // time of day is updated
        TimeUtils timeUtils = new TimeUtils();
        assertThat(timeUtils.getTimeOfDayOf(newStart.getValue()),
                   is(equalTo(timeUtils.getTimeOfDayOf(valueOf(start)))));
    }
    
    @Test(expected = SessionTimesViewModel.InvalidDateTimeException.class)
    public void setStartTimeOfDay_throwsIfStartIsAfterEnd()
    {
        DateBuilder start = aDate();
        SessionTimesViewModel viewModel =
                createViewModelWith(aSession().withStart(start).withNoDuration());
        
        viewModel.setStartTimeOfDay(TimeOfDay.of(valueOf(start.addMinutes(1))));
    }
    
    @Test
    public void setEndDate_updatesEndProperly()
    {
        DateBuilder end = aDate().withDay(2020, 1, 1);
        TimeUtils timeUtils = new TimeUtils();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withEnd(end));
        
        long originalTimeOfDay = timeUtils.getTimeOfDayOf(valueOf(end));
        
        viewModel.setEndDate(Day.of(valueOf(end.addTime(1, 1, 1))));
        
        LiveData<Date> newEnd = activateLocally(viewModel::getEnd);
        
        // time of day is left unchanged
        assertThat(timeUtils.getTimeOfDayOf(newEnd.getValue()), is(equalTo(originalTimeOfDay)));
        // day is updated
        assertThat(Day.of(newEnd.getValue()), is(equalTo(Day.of(valueOf(end)))));
    }
    
    @Test(expected = SessionTimesViewModel.InvalidDateTimeException.class)
    public void setEndDate_throwsIfEndIsBeforeStart()
    {
        DateBuilder end = aDate();
        SessionTimesViewModel viewModel =
                createViewModelWith(aSession().withEnd(end).withNoDuration());
        
        viewModel.setEndDate(Day.of(valueOf(end.addDays(-1))));
    }
    
    @Test
    public void setEndTimeOfDay_updatesEndProperly()
    {
        DateBuilder end = aDate();
        
        SessionTimesViewModel viewModel = createViewModelWith(aSession().withEnd(end));
        
        Day originalDay = Day.of(valueOf(end));
        
        // 25: change both the day & time of day
        viewModel.setEndTimeOfDay(TimeOfDay.of(valueOf(end.addHours(25))));
        
        LiveData<Date> newEnd = activateLocally(viewModel::getEnd);
        
        // day is left unchanged
        assertThat(Day.of(newEnd.getValue()), is(equalTo(originalDay)));
        // time of day is updated
        TimeUtils timeUtils = new TimeUtils();
        assertThat(timeUtils.getTimeOfDayOf(newEnd.getValue()),
                   is(equalTo(timeUtils.getTimeOfDayOf(valueOf(end)))));
    }
    
    @Test(expected = SessionTimesViewModel.InvalidDateTimeException.class)
    public void setEndTimeOfDay_throwsIfEndIsBeforeStart()
    {
        DateBuilder end = aDate();
        SessionTimesViewModel viewModel =
                createViewModelWith(aSession().withEnd(end).withNoDuration());
        
        viewModel.setEndTimeOfDay(TimeOfDay.of(valueOf(end.addMinutes(-1))));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private SessionTimesViewModel createViewModelWith(SessionBuilder session)
    {
        return new SessionTimesViewModel(valueOf(session), new TimeUtils());
    }
}
