package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

public class MoodBuilder
        implements BuilderOf<Mood>
{
//*********************************************************
// private properties
//*********************************************************

    private int mIndex;

//*********************************************************
// constructors
//*********************************************************

    public MoodBuilder()
    {
        mIndex = 2;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public Mood build()
    {
        return new Mood(mIndex);
    }
    
//*********************************************************
// api
//*********************************************************

    public MoodBuilder withIndex(int index)
    {
        mIndex = index;
        return this;
    }
}
