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
    public void isSet_isFalseIfMinutesIsNull()
    {
        SleepDurationGoalModel model = new SleepDurationGoalModel(null);
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
        SleepDurationGoalModel model = new SleepDurationGoalModel(null);
        assertThat(model.inMinutes(), is(nullValue()));
    }
    
    @Test
    public void inMinutes_returnsMinutes()
    {
        int expectedMinutes = 123;
        SleepDurationGoalModel model = new SleepDurationGoalModel(expectedMinutes);
        assertThat(model.inMinutes(), is(equalTo(expectedMinutes)));
    }
}
