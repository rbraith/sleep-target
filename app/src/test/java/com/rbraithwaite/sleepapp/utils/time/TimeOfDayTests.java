package com.rbraithwaite.sleepapp.utils.time;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimeOfDayTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void ofCalendar_returnsCorrectTimeOfDay()
    {
        int expectedHourOfDay = 11;
        int expectedMinute = 22;
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        cal.set(Calendar.HOUR_OF_DAY, expectedHourOfDay);
        cal.set(Calendar.MINUTE, expectedMinute);
        
        TimeOfDay timeOfDay = TimeOfDay.of(cal);
        
        assertThat(timeOfDay.hourOfDay, is(expectedHourOfDay));
        assertThat(timeOfDay.minute, is(expectedMinute));
    }
}
