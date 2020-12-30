package com.rbraithwaite.sleepapp.ui.session_data;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SessionEditDataTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toSleepSessionData_worksWithValidInput()
    {
        int expectedId = 2;
        long expectedStartDateTime = 100;
        long expectedEndDateTime = 200;
        
        SessionEditData sessionEditData =
                new SessionEditData(expectedId, expectedStartDateTime, expectedEndDateTime);
        
        SleepSessionData sleepSessionData = sessionEditData.toSleepSessionData();
        
        assertThat(sleepSessionData.id, is(sessionEditData.sessionId));
        assertThat(sleepSessionData.startTime.getTime(), is(sessionEditData.startDateTime));
        assertThat(sleepSessionData.duration,
                   is(sessionEditData.endDateTime - sessionEditData.startDateTime));
    }
    
    @Test
    public void fromResult_matches_toResult()
    {
        int expectedId = 2;
        long expectedStartDateTime = 100;
        long expectedEndDateTime = 200;
        
        SessionEditData data =
                new SessionEditData(expectedId, expectedStartDateTime, expectedEndDateTime);
        Bundle result = data.toResult();
        
        SessionEditData data2 = SessionEditData.fromResult(result);
        assertThat(data2.sessionId, is(equalTo(expectedId)));
        assertThat(data2.startDateTime, is(equalTo(expectedStartDateTime)));
        assertThat(data2.endDateTime, is(equalTo(expectedEndDateTime)));
    }
}
