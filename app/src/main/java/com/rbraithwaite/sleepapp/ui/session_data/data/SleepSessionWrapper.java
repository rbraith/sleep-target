package com.rbraithwaite.sleepapp.ui.session_data.data;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

// REFACTOR [21-01-9 2:43AM] -- consider just using Bundles instead?


/**
 * A simple wrapper used to transport sleep session data between view models without polluting the
 * view layer (where the transportation occurs) with references to that data (the view layer should
 * only care about UI data representations).
 */
public class SleepSessionWrapper
{
//*********************************************************
// public properties
//*********************************************************

    public SleepSessionEntity entity;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepSessionWrapper(SleepSessionEntity entity)
    {
        this.entity = entity;
    }
}
