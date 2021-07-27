package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ConvertInterruptionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toEntity_positiveInput()
    {
        int id = 2;
        Date start = TestUtils.ArbitraryData.getDate();
        int durationMillis = 500;
        String reason = "reason";
        
        Interruption interruption = new Interruption(id, start, durationMillis, reason);
        
        SleepInterruptionEntity entity = ConvertInterruption.toEntity(interruption);
        
        assertThat(entity.id, is(equalTo(id)));
        assertThat(entity.sessionId, is(equalTo(0L)));
        assertThat(entity.startTime, is(equalTo(start)));
        assertThat(entity.durationMillis, is(durationMillis));
        assertThat(entity.reason, is(equalTo(reason)));
    }
}
