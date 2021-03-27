package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepDurationGoalTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void createWithoutSettingGoal_has_isSet_returnFalse()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().isSet(), is(false));
    }
    
    @Test
    public void getHours_returnCorrectValue()
    {
        assertThat(new SleepDurationGoal(130).getHours(), is(2));
    }
    
    @Test
    public void getHours_nullIfUnsetModel()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().getHours(), is(nullValue()));
    }
    
    @Test
    public void getRemainingMinutes_returnsCorrectValue()
    {
        assertThat(new SleepDurationGoal(165).getRemainingMinutes(), is(45));
    }
    
    @Test
    public void getRemainingMinutes_nullIfUnsetModel()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().getRemainingMinutes(),
                   is(nullValue()));
    }
    
    @Test
    public void isSet_isTrueIfModelHasMinutes()
    {
        SleepDurationGoal model = new SleepDurationGoal(123);
        assertThat(model.isSet(), is(true));
    }
    
    @Test
    public void inMinutes_returnNullIfModelIsNotSet()
    {
        SleepDurationGoal model = SleepDurationGoal.createWithNoGoal();
        assertThat(model.inMinutes(), is(nullValue()));
    }
    
    @Test
    public void inMinutes_returnsMinutes()
    {
        int expectedMinutes = 123;
        SleepDurationGoal model = new SleepDurationGoal(expectedMinutes);
        assertThat(model.inMinutes(), is(equalTo(expectedMinutes)));
    }
    
    @Test
    public void inMinutes_matchesConstructor()
    {
        assertThat(new SleepDurationGoal(2, 34).inMinutes(), is(154));
    }
}
