package com.rbraithwaite.sleepapp.ui.interruption_details;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.SleepSession;

import java.io.Serializable;

public class InterruptionDetailsData
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private Interruption mInterruption;
    private SleepSession mParentSleepSession;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210115;

//*********************************************************
// constructors
//*********************************************************

    public InterruptionDetailsData(Interruption interruption, SleepSession parentSleepSession)
    {
        mInterruption = interruption;
        mParentSleepSession = parentSleepSession;
    }

//*********************************************************
// api
//*********************************************************

    public Interruption getInterruption()
    {
        return mInterruption;
    }
    
    public SleepSession getParentSleepSession()
    {
        return mParentSleepSession;
    }
}
