package com.rbraithwaite.sleepapp.ui.session_details.controllers;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

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
        LiveData<DateTimeViewModel.Date> date = viewModel.getDate();
        LiveData<String> dateText = viewModel.getDateText();
        TestUtils.activateLocalLiveData(date);
        TestUtils.activateLocalLiveData(dateText);
        
        viewModel.setFormatter(createFormatter());
        
        int expectedYear = 2021;
        int expectedMonth = 2;
        int expectedDay = 31;
        
        viewModel.setDate(expectedYear, expectedMonth, expectedDay);
        
        assertThat(date.getValue().year, is(expectedYear));
        assertThat(date.getValue().month, is(expectedMonth));
        assertThat(date.getValue().dayOfMonth, is(expectedDay));
        
        assertThat(dateText.getValue(), is(equalTo("2021 2 31")));
    }
    
    @Test
    public void setTimeOfDay_updatesTimeOfDay()
    {
        LiveData<DateTimeViewModel.TimeOfDay> timeOfDay = viewModel.getTimeOfDay();
        LiveData<String> timeOfDayText = viewModel.getTimeOfDayText();
        TestUtils.activateLocalLiveData(timeOfDay);
        TestUtils.activateLocalLiveData(timeOfDayText);

        viewModel.setFormatter(createFormatter());
        
        int expectedHourOfDay = 23;
        int expectedMinute = 45;
        
        viewModel.setTimeOfDay(expectedHourOfDay, expectedMinute);
        
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
