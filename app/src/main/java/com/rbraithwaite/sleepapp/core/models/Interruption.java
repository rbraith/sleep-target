package com.rbraithwaite.sleepapp.core.models;

import java.util.Date;
import java.util.Objects;

public class Interruption
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStartTime;
    private int mDurationMillis;
    private String mReason;
    
//*********************************************************
// constructors
//*********************************************************

    public Interruption(Date startTime, int durationMillis, String reason)
    {
        mStartTime = startTime;
        mDurationMillis = durationMillis;
        mReason = reason;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = mStartTime.hashCode();
        result = 31 * result + mDurationMillis;
        result = 31 * result + (mReason != null ? mReason.hashCode() : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Interruption that = (Interruption) o;
        
        if (mDurationMillis != that.mDurationMillis) { return false; }
        if (!mStartTime.equals(that.mStartTime)) { return false; }
        return Objects.equals(mReason, that.mReason);
    }
    
    @Override
    public String toString()
    {
        return "Interruption{" +
               "mStartTime=" + mStartTime +
               ", mDurationMillis=" + mDurationMillis +
               ", mReason='" + mReason + '\'' +
               '}';
    }
    
//*********************************************************
// api
//*********************************************************

    public Date getStart()
    {
        return mStartTime;
    }
    
    public String getReason()
    {
        return mReason;
    }
    
    public int getDurationMillis()
    {
        return mDurationMillis;
    }
}
