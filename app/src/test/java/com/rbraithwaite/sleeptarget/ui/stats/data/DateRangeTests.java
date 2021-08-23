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

package com.rbraithwaite.sleeptarget.ui.stats.data;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DateRangeTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void asYearOf_createsCorrectObj()
    {
        // date the test was written, is the reason for this value
        GregorianCalendar testDay = new GregorianCalendar(2021, 1, 26);
        DateRange testRange =
                DateRange.asYearOf(testDay.getTime(), new TimeUtils().hoursToMillis(2));
        
        assertThat(
                testRange.getStart(),
                is(equalTo(new GregorianCalendar(2021, 0, 1, 2, 0).getTime())));
        
        assertThat(
                testRange.getEnd(),
                is(equalTo(new GregorianCalendar(2022, 0, 1, 2, 0).getTime())));
        
        assertThat(testRange.getDifferenceInDays(), is(equalTo(365)));
    }
    
    @Test
    public void asMonthOf_createsCorrectObj()
    {
        // date the test was written, is the reason for this value
        GregorianCalendar testDay = new GregorianCalendar(2021, 1, 26);
        DateRange testRange =
                DateRange.asMonthOf(testDay.getTime(), new TimeUtils().hoursToMillis(2));
        
        assertThat(
                testRange.getStart(),
                is(equalTo(new GregorianCalendar(2021, 1, 1, 2, 0).getTime())));
        
        assertThat(
                testRange.getEnd(),
                is(equalTo(new GregorianCalendar(2021, 2, 1, 2, 0).getTime())));
        
        assertThat(testRange.getDifferenceInDays(), is(equalTo(28)));
    }
    
    @Test
    public void asWeekOf_createsCorrectObj()
    {
        // I picked this day because I knew it was a Wednesday (it was the week when I made this
        // test)
        GregorianCalendar testDay = new GregorianCalendar(2021, 1, 17, 18, 19);
        
        GregorianCalendar expectedStart = new GregorianCalendar(2021, 1, 15);
        GregorianCalendar expectedEnd = new GregorianCalendar(2021, 1, 22);
        
        DateRange dateRange = DateRange.asWeekOf(testDay.getTime());
        assertThat(dateRange.getStart(), is(equalTo(expectedStart.getTime())));
        assertThat(dateRange.getEnd(), is(equalTo(expectedEnd.getTime())));
    }
    
    @Test
    public void asWeekOf_hasCorrectDifferenceInDays()
    {
        DateRange range = DateRange.asWeekOf(TestUtils.ArbitraryData.getDate());
        assertThat(range.getDifferenceInDays(), is(7));
    }
    
    @Test
    public void offsetDays_offsetsCorrectly()
    {
        DateRange testRange = TestUtils.ArbitraryData.getDateRange();
        
        int[] offsets = {5, -5};
        for (int offset : offsets) {
            Date start = testRange.getStart();
            Date end = testRange.getEnd();
            
            // SUT
            testRange.offsetDays(offset);
            
            // verify
            GregorianCalendar offsetDate = new GregorianCalendar();
            offsetDate.setTime(testRange.getStart());
            offsetDate.add(Calendar.DAY_OF_WEEK, offset * -1);
            assertThat(start, is(equalTo(offsetDate.getTime())));
            
            offsetDate.setTime(testRange.getEnd());
            offsetDate.add(Calendar.DAY_OF_WEEK, offset * -1);
            assertThat(end, is(equalTo(offsetDate.getTime())));
        }
    }
}
