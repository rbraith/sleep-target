package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;

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

    public Long startDateTime;
    public Long endDateTime;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20201213L;

//*********************************************************
// constructors
//*********************************************************

    SessionEditData() {}
    
    public SessionEditData(Long startDateTime, Long endDateTime)
    {
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
}
