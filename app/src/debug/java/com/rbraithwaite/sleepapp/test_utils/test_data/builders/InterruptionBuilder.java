package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;

// REFACTOR [21-07-31 3:16AM] -- derive this from SessionBuilder.
public class InterruptionBuilder
        implements BuilderOf<Interruption>
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private Date mStart;
    private int mDuration;
    private String mReason;

//*********************************************************
// constructors
//*********************************************************

    public InterruptionBuilder()
    {
        mId = 0;
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
        return new Interruption(mId, mStart, mDuration, mReason);
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
        withDuration(0, minutes, 0);
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
    
    public InterruptionBuilder withId(int id)
    {
        mId = id;
        return this;
    }
    
    public InterruptionBuilder withDurationHours(int hours)
    {
        withDuration(hours, 0, 0);
        return this;
    }
}
