package com.rbraithwaite.sleepapp.data.current_goals;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepDurationGoalModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getHours_returnCorrectValue()
    {
        assertThat(new SleepDurationGoalModel(130).getHours(), is(2));
    }
    
    @Test
    public void getHours_nullIfUnsetModel()
    {
        assertThat(new SleepDurationGoalModel().getHours(), is(nullValue()));
    }
    
    @Test
    public void getRemainingMinutes_returnsCorrectValue()
    {
        assertThat(new SleepDurationGoalModel(165).getRemainingMinutes(), is(45));
    }
    
    @Test
    public void getRemainingMinutes_nullIfUnsetModel()
    {
        assertThat(new SleepDurationGoalModel().getRemainingMinutes(), is(nullValue()));
    }
    
    @Test
    public void isSet_isFalseIfEmptyModel()
    {
        SleepDurationGoalModel model = new SleepDurationGoalModel();
        assertThat(model.isSet(), is(false));
    }
    
    @Test
    public void isSet_isTrueIfModelHasMinutes()
    {
        SleepDurationGoalModel model = new SleepDurationGoalModel(123);
        assertThat(model.isSet(), is(true));
    }
    
    @Test
    public void inMinutes_returnNullIfModelIsNotSet()
    {
        SleepDurationGoalModel model = new SleepDurationGoalModel();
        assertThat(model.inMinutes(), is(nullValue()));
    }
    
    @Test
    public void inMinutes_returnsMinutes()
    {
        int expectedMinutes = 123;
        SleepDurationGoalModel model = new SleepDurationGoalModel(expectedMinutes);
        assertThat(model.inMinutes(), is(equalTo(expectedMinutes)));
    }
    
    @Test
    public void inMinutes_matchesConstructor()
    {
        assertThat(new SleepDurationGoalModel(2, 34).inMinutes(), is(154));
    }
}