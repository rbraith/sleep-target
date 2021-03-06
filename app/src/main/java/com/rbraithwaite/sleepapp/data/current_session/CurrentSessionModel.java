package com.rbraithwaite.sleepapp.data.current_session;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

public class CurrentSessionModel
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private TimeUtils mTimeUtils;

//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionModel()
    {
        this(null);
    }
    
    public CurrentSessionModel(Date start)
    {
        mStart = start;
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public Date getStart()
    {
        return mStart;
    }
    
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public boolean isSet()
    {
        return mStart != null;
    }
    
    /**
     * This returns a dynamic value - the duration from the start of the current session to whenever
     * this method was called. Do not expect any two calls of this method to return the same value.
     */
    public long getOngoingDurationMillis()
    {
        return mTimeUtils.getNow().getTime() - mStart.getTime();
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
