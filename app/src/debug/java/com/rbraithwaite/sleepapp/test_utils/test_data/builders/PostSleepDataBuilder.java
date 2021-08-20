package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

public class PostSleepDataBuilder
        implements BuilderOf<PostSleepData>
{
//*********************************************************
// private properties
//*********************************************************

    private float mRating;

//*********************************************************
// constructors
//*********************************************************

    public PostSleepDataBuilder()
    {
        mRating = 4.5f;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public PostSleepData build()
    {
        return new PostSleepData(mRating);
    }
    
//*********************************************************
// api
//*********************************************************

    public PostSleepDataBuilder withRating(float rating)
    {
        mRating = rating;
        return this;
    }
}
