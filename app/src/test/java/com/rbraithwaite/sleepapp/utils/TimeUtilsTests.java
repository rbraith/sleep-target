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

package com.rbraithwaite.sleepapp.utils;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TimeUtilsTests
{
//*********************************************************
// package properties
//*********************************************************

    TimeUtils timeUtils;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        timeUtils = new TimeUtils();
    }
    
    @After
    public void teardown()
    {
        timeUtils = null;
    }
    
    @Test
    public void getCalendarFromDate_returnsCorrectCalendar()
    {
        Date expected = TestUtils.ArbitraryData.getDate();
        assertThat(TimeUtils.getCalendarFrom(expected).getTime(), is(equalTo(expected)));
    }
    
    @Test
    public void addDurationToDate_returnsCorrectDate()
    {
        Date date = TestUtils.ArbitraryData.getDate();
        int expectedDuration = 123456;
        Date newDate = new TimeUtils().addDurationToDate(date, expectedDuration);
        
        assertThat(newDate.getTime() - date.getTime(), is(equalTo((long) expectedDuration)));
    }
    
    @Test
    public void setCalendarTimeOfDay_positiveInput()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        timeUtils.setCalendarTimeOfDay(cal, 0);
        
        checkCalendarTimeOfDay(cal, 0, 0, 0, 0);
        
        timeUtils.setCalendarTimeOfDay(cal, timeUtils.timeToMillis(
                1, 2, 3, 4));
        
        checkCalendarTimeOfDay(cal, 1, 2, 3, 4);
    }
    
    @Test
    public void getTimeOfDay_reflects_setCalendarTimeOfDay()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        long expectedTimeOfDay = timeUtils.timeToMillis(1, 2, 3, 4);
        timeUtils.setCalendarTimeOfDay(cal, expectedTimeOfDay);
        
        assertThat(timeUtils.getTimeOfDayOf(cal), is(expectedTimeOfDay));
        assertThat(timeUtils.getTimeOfDayOf(cal.getTime()), is(expectedTimeOfDay));
    }
    
    @Test
    public void timeToMillis_positiveInput()
    {
        assertThat(timeUtils.timeToMillis(1, 2, 3, 4), is(3723004L));
    }

//*********************************************************
// private methods
//*********************************************************

    private void checkCalendarTimeOfDay(
            GregorianCalendar cal,
            int hour,
            int minute,
            int second,
            int milli)
    {
        assertThat(cal.get(Calendar.HOUR_OF_DAY), is(hour));
        assertThat(cal.get(Calendar.MINUTE), is(minute));
        assertThat(cal.get(Calendar.SECOND), is(second));
        assertThat(cal.get(Calendar.MILLISECOND), is(milli));
    }
}
