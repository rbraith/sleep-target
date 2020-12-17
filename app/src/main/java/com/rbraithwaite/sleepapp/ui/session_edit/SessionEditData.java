package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;

import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import java.io.Serializable;

// REFACTOR [20-12-13 4:10AM] -- consider making this SessionEditFragment.Data



/**
 * DTO representing the values set in a SessionEditFragment
 */
public class SessionEditData
        implements Serializable
{
//*********************************************************
// private constants
//*********************************************************

    private static final String RESULT = "result";

//*********************************************************
// public properties
//*********************************************************

    public int sessionId;
    public long startDateTime;
    public long endDateTime;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20201213L;

//*********************************************************
// constructors
//*********************************************************

    SessionEditData() {}
    
    public SessionEditData(int sessionId, long startDateTime, long endDateTime)
    {
        this.sessionId = sessionId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
    
    public SessionEditData(long startDateTime, long endDateTime)
    {
        this.sessionId = 0;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * Create a SessionEditData instance from a SessionEditFragment result.
     *
     * @param result the return value of SessionEditFragmentViewModel.getResult(), to be parsed into
     *               a new SessionEditData instance
     *
     * @return a new SessionEditData instance
     */
    public static SessionEditData fromResult(Bundle result)
    {
        return (SessionEditData) result.getSerializable(RESULT);
    }
    
    /**
     * @return a bundle for use by FragmentResultListeners listening to SessionEditFragment
     */
    public Bundle toResult()
    {
        Bundle result = new Bundle();
        result.putSerializable(RESULT, this);
        return result;
    }
    
    public SleepSessionData toSleepSessionData()
    {
        SleepSessionData sleepSessionData = new SleepSessionData();
        sleepSessionData.id = sessionId;
        sleepSessionData.startTime = DateUtils.getDateFromMillis(startDateTime);
        sleepSessionData.duration = endDateTime - startDateTime;
        return sleepSessionData;
    }
}
