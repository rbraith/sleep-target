package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

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
    public void fromResult_matches_toResult()
    {
        long expectedStartDateTime = 100;
        long expectedEndDateTime = 200;
        
        SessionEditData data = new SessionEditData(expectedStartDateTime, expectedEndDateTime);
        Bundle result = data.toResult();
        
        SessionEditData data2 = SessionEditData.fromResult(result);
        assertThat(data2.startDateTime, is(equalTo(expectedStartDateTime)));
        assertThat(data2.endDateTime, is(equalTo(expectedEndDateTime)));
    }
}
