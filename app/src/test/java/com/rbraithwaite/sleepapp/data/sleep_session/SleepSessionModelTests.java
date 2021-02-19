package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepSessionModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getEnd_getsCorrectValue()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        int sessionDurationMillis = 12345;
        
        SleepSessionModel sleepSessionModel = new SleepSessionModel(
                calendar.getTime(),
                sessionDurationMillis,
                null,
                null);
        
        calendar.add(Calendar.MILLISECOND, sessionDurationMillis);
        Date expectedEnd = calendar.getTime();
        
        assertThat(sleepSessionModel.getEnd(), is(equalTo(expectedEnd)));
    }
    
    @Test
    public void getEnd_returnsNullIfStartIsNull()
    {
        SleepSessionModel sleepSessionModel = new SleepSessionModel(
                null,
                1234,
                null,
                null);
        
        assertThat(sleepSessionModel.getEnd(), is(nullValue()));
    }
}
