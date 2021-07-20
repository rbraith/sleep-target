package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;

public class InterruptionBuilder
        implements BuilderOf<Interruption>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private int mDuration;
    private String mReason;
    
//*********************************************************
// constructors
//*********************************************************

    public InterruptionBuilder()
    {
        mStart = aDate().build();
        mDuration = 5 * 60 * 1000; // 5 min
        mReason = "some reason";
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Interruption build()
    {
        return new Interruption(mStart, mDuration, mReason);
    }
    
//*********************************************************
// api
//*********************************************************

    public InterruptionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    public InterruptionBuilder withDurationMinutes(int minutes)
    {
        mDuration = minutes * 60 * 1000;
        return this;
    }
    
    public InterruptionBuilder withReason(String reason)
    {
        mReason = reason;
        return this;
    }
    
    public InterruptionBuilder withDuration(int hours, int minutes, int seconds)
    {
        mDuration =
                hours * 60 * 60 * 1000 +
                minutes * 60 * 1000 +
                seconds * 1000;
        return this;
    }
}
