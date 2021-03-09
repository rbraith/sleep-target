package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class WakeTimeGoalModelConverterTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(WakeTimeGoalModelConverter.convertEntityToModel(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        WakeTimeGoalEntity entity = TestUtils.ArbitraryData.getWakeTimeGoalEntity();
        WakeTimeGoalModel model = WakeTimeGoalModelConverter.convertEntityToModel(entity);
        
        assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
        assertThat(model.getGoalMillis(), is(equalTo(entity.wakeTimeGoal)));
    }
    
    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(WakeTimeGoalModelConverter.convertModelToEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        WakeTimeGoalModel model = TestUtils.ArbitraryData.getWakeTimeGoalModel();
        
        // SUT
        WakeTimeGoalEntity entity = WakeTimeGoalModelConverter.convertModelToEntity(model);
        
        // TODO [21-03-9 3:29PM] -- verify id.
        assertThat(entity.editTime, is(equalTo(model.getEditTime())));
        assertThat(entity.wakeTimeGoal, is(equalTo(model.getGoalMillis())));
    }
}
