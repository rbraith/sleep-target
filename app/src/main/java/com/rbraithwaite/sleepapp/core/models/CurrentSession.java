package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

public class CurrentSession
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private TimeUtils mTimeUtils;

//*********************************************************
// constructors
//*********************************************************

    public CurrentSession()
    {
        this(null);
    }
    
    public CurrentSession(Date start)
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
    
    /**
     * @return This current session as a distinct sleep session.
     */
    public SleepSession toSleepSession()
    {
        return new SleepSession(getStart(), getOngoingDurationMillis());
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
