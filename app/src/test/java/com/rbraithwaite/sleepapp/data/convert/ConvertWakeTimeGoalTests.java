package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertWakeTimeGoalTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(ConvertWakeTimeGoal.fromEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        WakeTimeGoalEntity entity = TestUtils.ArbitraryData.getWakeTimeGoalEntity();
        WakeTimeGoal model = ConvertWakeTimeGoal.fromEntity(entity);
        
        assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
        assertThat(model.getGoalMillis(), is(equalTo(entity.wakeTimeGoal)));
    }
    
    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(ConvertWakeTimeGoal.toEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        WakeTimeGoal model = TestUtils.ArbitraryData.getWakeTimeGoalModel();
        
        // SUT
        WakeTimeGoalEntity entity = ConvertWakeTimeGoal.toEntity(model);
        
        // TODO [21-03-9 3:29PM] -- verify id.
        assertThat(entity.editTime, is(equalTo(model.getEditTime())));
        assertThat(entity.wakeTimeGoal, is(equalTo(model.getGoalMillis())));
    }
}
