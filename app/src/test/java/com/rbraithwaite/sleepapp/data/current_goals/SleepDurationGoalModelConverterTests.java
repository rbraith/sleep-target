package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepDurationGoalModelConverterTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(SleepDurationGoalModelConverter.convertModelToEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        SleepDurationGoalModel model = TestUtils.ArbitraryData.getSleepDurationGoalModel();
        
        // SUT
        SleepDurationGoalEntity entity =
                SleepDurationGoalModelConverter.convertModelToEntity(model);
        
        // TODO [21-03-9 3:29PM] -- verify id.
        assertThat(entity.editTime, is(equalTo(model.getEditTime())));
        assertThat(entity.goalMinutes, is(equalTo(model.inMinutes())));
    }
    
    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(SleepDurationGoalModelConverter.convertEntityToModel(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        SleepDurationGoalEntity entity = TestUtils.ArbitraryData.getSleepDurationGoalEntity();
        SleepDurationGoalModel model = SleepDurationGoalModelConverter.convertEntityToModel(entity);
        
        assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
        assertThat(model.inMinutes(), is(equalTo(entity.goalMinutes)));
    }
}
