package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertSleepSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepSession.fromEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        SleepSession model = ConvertSleepSession.fromEntity(entity);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDurationMillis(), is(equalTo(entity.duration)));
        assertThat(model.getAdditionalComments(), is(equalTo(entity.additionalComments)));
    }
    
    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepSession.toEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        SleepSession model = TestUtils.ArbitraryData.getSleepSession();
        SleepSessionEntity entity = ConvertSleepSession.toEntity(model);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDurationMillis(), is(equalTo(entity.duration)));
        assertThat(model.getAdditionalComments(), is(equalTo(entity.additionalComments)));
    }
}
