package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.models.session.Session;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test(expected = NullPointerException.class)
    public void setStartFixed_throwsIfNull()
    {
        Session session = valueOf(aSession());
        session.setStartFixed((Calendar) null);
    }
    
    @Test(expected = Session.InvalidDateError.class)
    public void setStartFixed_throwsIfStartIsAfterEnd()
    {
        DateBuilder date = aDate();
        Session session = valueOf(aSession().withStart(date).withNoDuration());
        
        session.setStartFixed(valueOf(date.addDays(1)));
    }
    
    @Test
    public void setStartFixed_setsStartDateCorrectly()
    {
        DateBuilder date = aDate();
        Session session = valueOf(aSession().withStart(date).withNoDuration());
        
        Date originalEnd = session.getEnd();
        
        session.setStartFixed(valueOf(date.addDays(-1).addHours(-1)));
        
        assertThat(session.getStart(), is(equalTo(valueOf(date))));
        assertThat(session.getDurationMillis(), is(equalTo(TimeUtils.hoursToMillis(25))));
        assertThat(session.getEnd(), is(equalTo(originalEnd)));
    }
    
    @Test
    public void offsetStartFixed_setStartDateCorrectly()
    {
        Session session = valueOf(aSession().withNoDuration());
        
        Date originalEnd = session.getEnd();
        
        session.offsetStartFixed(-12, -34);
        
        long expectedDuration = TimeUtils.timeToMillis(12, 34, 0, 0);
        assertThat(session.getDurationMillis(), is(equalTo(expectedDuration)));
        assertThat(session.getEnd(), is(equalTo(originalEnd)));
    }
    
    @Test
    public void offsetEndFixed_setsEndDateCorrectly()
    {
        Session session = valueOf(aSession().withNoDuration());
        
        Date originalStart = session.getStart();
        
        session.offsetEndFixed(12, 34);
        
        long expectedDuration = TimeUtils.timeToMillis(12, 34, 0, 0);
        assertThat(session.getDurationMillis(), is(equalTo(expectedDuration)));
        assertThat(session.getStart(), is(equalTo(originalStart)));
    }
    
    @Test(expected = NullPointerException.class)
    public void setEndFixed_throwsIfNull()
    {
        Session session = valueOf(aSession());
        session.setEndFixed((Calendar) null);
    }
    
    @Test(expected = Session.InvalidDateError.class)
    public void setEndFixed_throwsIfStartIsAfterEnd()
    {
        DateBuilder date = aDate();
        Session session = valueOf(aSession().withStart(date).withNoDuration());
        
        session.setEndFixed(valueOf(date.addDays(-1)));
    }
    
    @Test
    public void setEndFixed_setsEndDateCorrectly()
    {
        DateBuilder date = aDate();
        Session session = valueOf(aSession().withStart(date).withNoDuration());
        
        Date originalStart = session.getStart();
        
        session.setEndFixed(valueOf(date.addDays(1).addHours(1)));
        
        assertThat(session.getEnd(), is(equalTo(valueOf(date))));
        assertThat(session.getDurationMillis(), is(equalTo(TimeUtils.hoursToMillis(25))));
        assertThat(session.getStart(), is(equalTo(originalStart)));
    }
    
    @Test
    public void getEnd_getsCorrectValue()
    {
        DateBuilder date = aDate();
        
        int expectedDuration = 12345;
        Session session = valueOf(aSession().withStart(aDate()).withDuration(expectedDuration));
        
        date.addMillis(expectedDuration);
        assertThat(session.getEnd(), is(equalTo(valueOf(date))));
    }
    
    @Test(expected = Session.InvalidDurationError.class)
    public void duration_cannotBeNegative()
    {
        Session session = new Session(valueOf(aDate()), -123);
    }
}
