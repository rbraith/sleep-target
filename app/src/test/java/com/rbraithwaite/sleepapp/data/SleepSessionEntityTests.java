package com.rbraithwaite.sleepapp.data;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SleepSessionEntityTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void create_returnsProperInstance()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date expectedStartDateTime = calendar.getTime();
        long expectedDuration = TestUtils.ArbitraryData.getDurationMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        Date expectedWakeTimeGoal = calendar.getTime();
        
        //SUT
        SleepSessionEntity sleepSessionEntity = SleepSessionEntity.create(
                expectedStartDateTime,
                expectedDuration,
                expectedWakeTimeGoal);
        
        // verify
        assertThat(sleepSessionEntity.id, is(0));
        assertThat(sleepSessionEntity.startTime, is(expectedStartDateTime));
        assertThat(sleepSessionEntity.duration, is(expectedDuration));
        assertThat(sleepSessionEntity.wakeTimeGoal, is(expectedWakeTimeGoal));
    }
}
