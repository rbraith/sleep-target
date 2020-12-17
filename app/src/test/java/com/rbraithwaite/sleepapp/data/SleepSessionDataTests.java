package com.rbraithwaite.sleepapp.data;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SleepSessionDataTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toSleepSessionEntity_createsProperSleepSessionEntity()
    {
        SleepSessionData sleepSessionData = TestUtils.ArbitraryData.getSleepSessionData();
        SleepSessionEntity sleepSessionEntity = sleepSessionData.toSleepSessionEntity();
        
        assertThat(sleepSessionEntity.id, is(sleepSessionData.id));
        assertThat(sleepSessionEntity.startTime, is(sleepSessionData.startTime));
        assertThat(sleepSessionEntity.duration, is(sleepSessionData.duration));
    }
}
