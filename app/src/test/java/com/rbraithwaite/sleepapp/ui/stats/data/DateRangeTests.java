package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

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
}
