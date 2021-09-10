/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.ui.common.views.session_times;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SessionBuilder;
import com.rbraithwaite.sleeptarget.utils.LiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static com.rbraithwaite.sleeptarget.test_utils.LiveDataTestUtils.activateLocally;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class SessionTimesViewModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void errorDialogEvent_properlyTriggers()
    {
        SessionTimesViewModel viewModel = createViewModelWith(aSession());
        
        LiveData<LiveDataEvent<Integer>> event = viewModel.errorDialogEvent();
        TestUtils.activateLocalLiveData(event);
        
        assertThat(event.getValue(), is(nullValue()));
        
        viewModel.triggerErrorDialogEvent(123);
        
        assertThat(event.getValue().getExtra(), is(123));
    }
    
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
        SessionTimesViewModel viewModel = new SessionTimesViewModel(new TimeUtils());
        viewModel.init(valueOf(session));
        return viewModel;
    }
}
