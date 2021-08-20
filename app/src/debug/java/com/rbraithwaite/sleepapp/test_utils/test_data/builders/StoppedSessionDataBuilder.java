package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aCurrentSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aPostSleepData;

public class StoppedSessionDataBuilder
        implements BuilderOf<StoppedSessionData>
{
//*********************************************************
// private properties
//*********************************************************

    private CurrentSession mCurrentSession;
    private PostSleepData mPostSleepData;
    private TimeUtils mTimeUtils;

//*********************************************************
// constructors
//*********************************************************

    public StoppedSessionDataBuilder()
    {
        mCurrentSession = aCurrentSession().build();
        mPostSleepData = aPostSleepData().build();
        mTimeUtils = new TimeUtils();
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public StoppedSessionData build()
    {
        return new StoppedSessionData(mCurrentSession.createSnapshot(mTimeUtils), mPostSleepData);
    }

//*********************************************************
// api
//*********************************************************

    public StoppedSessionDataBuilder with(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
        return this;
    }
    
    public StoppedSessionDataBuilder with(CurrentSessionBuilder currentSession)
    {
        mCurrentSession = currentSession.build();
        return this;
    }
    
    public StoppedSessionDataBuilder with(PostSleepDataBuilder postSleepData)
    {
        mPostSleepData = postSleepData.build();
        return this;
    }
}
