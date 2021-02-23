package com.rbraithwaite.sleepapp.utils;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimeUtilsTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void setCalendarTimeOfDay_positiveInput()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        TimeUtils.setCalendarTimeOfDay(cal, 0);
        
        checkCalendarTimeOfDay(cal, 0, 0, 0, 0);
        
        TimeUtils.setCalendarTimeOfDay(cal, TimeUtils.timeToMillis(
                1, 2, 3, 4));
        
        checkCalendarTimeOfDay(cal, 1, 2, 3, 4);
    }
    
    @Test
    public void getTimeOfDay_reflects_setCalendarTimeOfDay()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        long expectedTimeOfDay = TimeUtils.timeToMillis(1, 2, 3, 4);
        TimeUtils.setCalendarTimeOfDay(cal, expectedTimeOfDay);
        
        assertThat(TimeUtils.getTimeOfDay(cal), is(expectedTimeOfDay));
        assertThat(TimeUtils.getTimeOfDay(cal.getTime()), is(expectedTimeOfDay));
    }
    
    @Test
    public void timeToMillis_positiveInput()
    {
        assertThat(TimeUtils.timeToMillis(1, 2, 3, 4), is(3723004L));
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
