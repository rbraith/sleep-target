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

package com.rbraithwaite.sleeptarget.ui.session_details;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFormatting;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SessionDetailsFormattingTests
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
                    SessionDetailsFormatting.formatSleepDurationGoal(new SleepDurationGoal(minutes)),
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
                    SessionDetailsFormatting.formatTimeOfDay(hourOfDay, minutes),
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
                    SessionDetailsFormatting.formatDate(year, month, dayOfMonth),
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
