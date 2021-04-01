package com.rbraithwaite.sleepapp.ui.session_data;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFormatting;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SessionDataFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatSleepDurationGoal_positiveInput()
    {
        Object[][] data = {
                // minutes, expected
                {15, "0h 15m"},
                {120, "2h 00m"},
                {605, "10h 05m"}
        };
        
        for (Object[] d : data) {
            int minutes = (int) d[0];
            String expected = (String) d[1];
            
            assertThat(
                    SessionDataFormatting.formatSleepDurationGoal(new SleepDurationGoal(minutes)),
                    is(equalTo(expected)));
        }
    }
    
    @Test
    public void formatTimeOfDay_positiveInput()
    {
        Object[][] data = {
                // hourOfDay, minutes, expected
                {1, 23, "1:23 AM"},
                {0, 5, "12:05 AM"},
                {12, 0, "12:00 PM"},
                {22, 59, "10:59 PM"},
        };
        
        for (Object[] d : data) {
            int hourOfDay = (int) d[0];
            int minutes = (int) d[1];
            String expected = (String) d[2];
            
            assertThat(
                    SessionDataFormatting.formatTimeOfDay(hourOfDay, minutes),
                    is(equalTo(expected)));
        }
    }
    
    @Test
    public void formatDate_positiveInput()
    {
        Object[][] data = {
                // year, month, day of month, expected
                {2021, 2, 31, "Mar 31 2021"}
        };
        
        for (Object[] d : data) {
            int year = (int) d[0];
            int month = (int) d[1];
            int dayOfMonth = (int) d[2];
            String expected = (String) d[3];
            
            assertThat(
                    SessionDataFormatting.formatDate(year, month, dayOfMonth),
                    is(equalTo(expected)));
        }
    }
    
    @Test
    public void formatSleepDurationGoal_unsetArg()
    {
        assertThat(
                SleepGoalsFormatting.formatSleepDurationGoal(SleepDurationGoal.createWithNoGoal()),
                is(""));
    }
}
