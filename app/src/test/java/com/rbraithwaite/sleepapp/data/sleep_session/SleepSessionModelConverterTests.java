package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepSessionModelConverterTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(SleepSessionModelConverter.convertEntityToModel(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        SleepSessionModel model = SleepSessionModelConverter.convertEntityToModel(entity);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDuration(), is(equalTo(entity.duration)));
        assertThat(model.getWakeTimeGoal(), is(equalTo(entity.wakeTimeGoal)));
    }
    
    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(SleepSessionModelConverter.convertModelToEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        SleepSessionModel model = TestUtils.ArbitraryData.getSleepSessionModel();
        SleepSessionEntity entity = SleepSessionModelConverter.convertModelToEntity(model);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDuration(), is(equalTo(entity.duration)));
        assertThat(model.getWakeTimeGoal(), is(equalTo(entity.wakeTimeGoal)));
    }
}
