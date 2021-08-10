package com.rbraithwaite.sleepapp.test_utils.test_data;

import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;

public class WakeTimeGoalBuilder
        implements BuilderOf<WakeTimeGoal>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mEditTime;
    private int mGoalMillis;
    
//*********************************************************
// constructors
//*********************************************************

    public WakeTimeGoalBuilder()
    {
        mEditTime = valueOf(aDate());
        mGoalMillis = (int) TimeUtils.hoursToMillis(8);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public WakeTimeGoal build()
    {
        return new WakeTimeGoal(mEditTime, mGoalMillis);
    }
    
//*********************************************************
// api
//*********************************************************

    public WakeTimeGoalBuilder withEditTime(DateBuilder editTime)
    {
        mEditTime = editTime.build();
        return this;
    }
    
    public WakeTimeGoalBuilder withGoal(int hourOfDay, int minutes)
    {
        mGoalMillis = (int) TimeUtils.timeToMillis(hourOfDay, minutes, 0, 0);
        return this;
    }
}
