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
package com.rbraithwaite.sleeptarget.ui.session_details.controllers;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.common.views.datetime.DateTimeViewModel;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class DateTimeViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private DateTimeViewModel viewModel;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        viewModel = new DateTimeViewModel();
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
    }
    
    @Test
    public void setDate_updatesDate()
    {
        LiveData<Day> date = viewModel.getDate();
        LiveData<String> dateText = viewModel.getDateText();
        TestUtils.activateLocalLiveData(date);
        TestUtils.activateLocalLiveData(dateText);
        
        viewModel.setFormatter(createFormatter());
        
        int expectedYear = 2021;
        int expectedMonth = 2;
        int expectedDay = 31;
        
        viewModel.setDate(new Day(expectedYear, expectedMonth, expectedDay));
        
        assertThat(date.getValue().year, is(expectedYear));
        assertThat(date.getValue().month, is(expectedMonth));
        assertThat(date.getValue().dayOfMonth, is(expectedDay));
        
        assertThat(dateText.getValue(), is(equalTo("2021 2 31")));
    }
    
    @Test
    public void setTimeOfDay_updatesTimeOfDay()
    {
        LiveData<TimeOfDay> timeOfDay = viewModel.getTimeOfDay();
        LiveData<String> timeOfDayText = viewModel.getTimeOfDayText();
        TestUtils.activateLocalLiveData(timeOfDay);
        TestUtils.activateLocalLiveData(timeOfDayText);
        
        viewModel.setFormatter(createFormatter());
        
        int expectedHourOfDay = 23;
        int expectedMinute = 45;
        
        viewModel.setTimeOfDay(new TimeOfDay(expectedHourOfDay, expectedMinute));
        
        assertThat(timeOfDay.getValue().hourOfDay, is(expectedHourOfDay));
        assertThat(timeOfDay.getValue().minute, is(expectedMinute));
        assertThat(timeOfDayText.getValue(), is(equalTo("23 45")));
    }

//*********************************************************
// private methods
//*********************************************************

    private DateTimeViewModel.Formatter createFormatter()
    {
        return new DateTimeViewModel.Formatter()
        {
            @Override
            public String formatTimeOfDay(int hourOfDay, int minute)
            {
                return String.format("%d %d", hourOfDay, minute);
            }
            
            @Override
            public String formatDate(int year, int month, int dayOfMonth)
            {
                return String.format("%d %d %d", year, month, dayOfMonth);
            }
        };
    }
}
