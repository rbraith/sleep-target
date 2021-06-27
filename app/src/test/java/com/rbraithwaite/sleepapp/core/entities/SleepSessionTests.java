package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SleepSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getRating_isZeroIfSetToNull()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setRating(null);
        assertThat(sleepSession.getRating(), is(0f));
    }
    
    @Test(expected = NullPointerException.class)
    public void setStartFixed_throwsIfNull()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setStartFixed(null);
    }
    
    @Test(expected = SleepSession.InvalidDateError.class)
    public void setStartFixed_throwsIfStartIsAfterEnd()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        GregorianCalendar newStart = new GregorianCalendar();
        newStart.setTime(sleepSession.getEnd());
        newStart.add(Calendar.DAY_OF_MONTH, 1);
        
        // SUT
        sleepSession.setStartFixed(newStart);
    }
    
    @Test
    public void setStartFixed_setsStartDateCorrectly()
    {
        GregorianCalendar originalStart = TestUtils.ArbitraryData.getCalendar();
        SleepSession sleepSession = new SleepSession(originalStart.getTime(), 12345);
        
        GregorianCalendar newStart = new GregorianCalendar();
        newStart.setTime(originalStart.getTime());
        newStart.add(Calendar.DAY_OF_MONTH, -1);
        newStart.add(Calendar.HOUR, 1);
        
        // SUT
        sleepSession.setStartFixed(newStart);
        
        // verify
        assertThat(sleepSession.getStart(), is(equalTo(newStart.getTime())));
    }
    
    @Test
    public void offsetStartFixed_setStartDateCorrectly()
    {
        GregorianCalendar originalStart = TestUtils.ArbitraryData.getCalendar();
        SleepSession sleepSession = new SleepSession(originalStart.getTime(), 12345);
        
        sleepSession.offsetStartFixed(-12, -34);
        
        GregorianCalendar offsetStart = TimeUtils.getCalendarFrom(sleepSession.getStart());
        
        int expectedOffset = (int) TimeUtils.timeToMillis(12, 34, 0, 0);
        assertThat((int) (originalStart.getTimeInMillis() - offsetStart.getTimeInMillis()),
                   is(expectedOffset));
    }
    
    @Test
    public void offsetEndFixed_setsEndDateCorrectly()
    {
        GregorianCalendar originalEnd = TestUtils.ArbitraryData.getCalendar();
        SleepSession sleepSession = new SleepSession(originalEnd.getTime(), 0);
        
        sleepSession.offsetEndFixed(12, 34);
        
        GregorianCalendar offsetEnd = TimeUtils.getCalendarFrom(sleepSession.getEnd());
        
        int expectedOffset = (int) TimeUtils.timeToMillis(12, 34, 0, 0);
        assertThat((int) (offsetEnd.getTimeInMillis() - originalEnd.getTimeInMillis()),
                   is(expectedOffset));
    }
    
    @Test
    public void setStartFixed_leavesEndDateUnchanged()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        SleepSession sleepSession = new SleepSession(cal.getTime(), 12345);
        
        Date expectedEnd = sleepSession.getEnd();
        
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.add(Calendar.HOUR, 1);
        
        // SUT
        sleepSession.setStartFixed(cal);
        
        assertThat(sleepSession.getEnd(), is(equalTo(expectedEnd)));
    }
    
    @Test(expected = NullPointerException.class)
    public void setEndFixed_throwsIfNull()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setEndFixed(null);
    }
    
    @Test(expected = SleepSession.InvalidDateError.class)
    public void setEndFixed_throwsIfStartIsAfterEnd()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        GregorianCalendar newEnd = new GregorianCalendar();
        newEnd.setTime(sleepSession.getEnd());
        newEnd.add(Calendar.DAY_OF_MONTH, -1);
        
        // SUT
        sleepSession.setEndFixed(newEnd);
    }
    
    @Test
    public void setEndFixed_setsEndDateCorrectly()
    {
        SleepSession sleepSession =
                new SleepSession(TestUtils.ArbitraryData.getCalendar().getTime(), 12345);
        
        GregorianCalendar originalEnd = new GregorianCalendar();
        originalEnd.setTime(sleepSession.getEnd());
        
        GregorianCalendar newEnd = new GregorianCalendar();
        newEnd.setTime(originalEnd.getTime());
        newEnd.add(Calendar.DAY_OF_MONTH, 1);
        newEnd.add(Calendar.HOUR, 1);
        
        // SUT
        sleepSession.setEndFixed(newEnd);
        
        // verify
        assertThat(sleepSession.getEnd(), is(equalTo(newEnd.getTime())));
    }
    
    @Test
    public void setEndFixed_leavesStartDateUnchanged()
    {
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        SleepSession sleepSession = new SleepSession(cal.getTime(), 12345);
        
        Date expectedStart = sleepSession.getStart();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.HOUR, 1);
        
        // SUT
        sleepSession.setEndFixed(cal);
        
        assertThat(sleepSession.getStart(), is(equalTo(expectedStart)));
    }
    
    @Test
    public void getEnd_getsCorrectValue()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        int sessionDurationMillis = 12345;
        
        SleepSession sleepSession = new SleepSession(
                calendar.getTime(),
                sessionDurationMillis);
        
        calendar.add(Calendar.MILLISECOND, sessionDurationMillis);
        Date expectedEnd = calendar.getTime();
        
        assertThat(sleepSession.getEnd(), is(equalTo(expectedEnd)));
    }
    
    @Test(expected = SleepSession.InvalidDurationError.class)
    public void duration_cannotBeNegative()
    {
        SleepSession sleepSession = new SleepSession(TestUtils.ArbitraryData.getDate(), -123);
    }
}
