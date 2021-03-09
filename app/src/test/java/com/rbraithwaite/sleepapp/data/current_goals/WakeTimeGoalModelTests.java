package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class WakeTimeGoalModelTests
{
    @Test
    public void isSet_isFalseWith_createWithNoGoal()
    {
        WakeTimeGoalModel model = WakeTimeGoalModel.createWithNoGoal(TestUtils.ArbitraryData.getDate());
        assertThat(model.isSet(), is(false));
    }
    
    @Test
    public void isSet_isTrue_whenGoalIsSet()
    {
        WakeTimeGoalModel model = new WakeTimeGoalModel(TestUtils.ArbitraryData.getDate(), 12345);
        assertThat(model.isSet(), is(true));
    }
    
    @Test
    public void asDate_returnsCorrectDate()
    {
        WakeTimeGoalModel model = new WakeTimeGoalModel(
                TestUtils.ArbitraryData.getDate(),
                5 * 60 * 60 * 1000); // 5am
    
        // SUT
        Date testDate = model.asDate();
        
        // verify
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(testDate);
        assertThat(cal.get(Calendar.HOUR_OF_DAY), is(5));
        assertThat(cal.get(Calendar.MINUTE), is(0));
        assertThat(cal.get(Calendar.SECOND), is(0));
        assertThat(cal.get(Calendar.MILLISECOND), is(0));
    }
    
    @Test
    public void asDate_returnsNullIfNotSet()
    {
        WakeTimeGoalModel model = WakeTimeGoalModel.createWithNoGoal(TestUtils.ArbitraryData.getDate());
        assertThat(model.asDate(), is(nullValue()));
    }
}
