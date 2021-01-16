package com.rbraithwaite.sleepapp.ui.session_data.data;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;

import java.io.Serializable;

// REFACTOR [21-01-9 2:43AM] -- consider just using Bundles instead?


/**
 * A simple wrapper used to transport sleep session data between view models without polluting the
 * view layer (where the transportation occurs) with references to that data (the view layer should
 * only care about UI data representations).
 */
public class SleepSessionWrapper
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionModel mSleepSession;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210115;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionWrapper(SleepSessionModel sleepSession)
    {
        mSleepSession = sleepSession;
    }
    
//*********************************************************
// api
//*********************************************************

    public SleepSessionModel getValue()
    {
        return mSleepSession;
    }
}
