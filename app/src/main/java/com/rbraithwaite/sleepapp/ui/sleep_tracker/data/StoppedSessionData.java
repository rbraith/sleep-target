package com.rbraithwaite.sleepapp.ui.sleep_tracker.data;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;

public class StoppedSessionData
{
//*********************************************************
// public constants
//*********************************************************

    public final CurrentSession.Snapshot currentSessionSnapshot;
    public final PostSleepData postSleepData;
    
//*********************************************************
// constructors
//*********************************************************

    public StoppedSessionData(
            CurrentSession.Snapshot currentSessionSnapshot,
            PostSleepData postSleepData)
    {
        this.currentSessionSnapshot = currentSessionSnapshot;
        this.postSleepData = postSleepData;
    }
}
