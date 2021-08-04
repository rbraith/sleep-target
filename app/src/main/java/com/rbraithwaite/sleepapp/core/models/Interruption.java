package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.models.session.Session;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// REFACTOR [21-08-2 5:30PM] -- This and SleepSession should not be implementing Serializable
//  This is an infrastructure concern & should be dealt with outside the domain layer.
public class Interruption
        extends Session
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private String mReason;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;

//*********************************************************
// constructors
//*********************************************************

    public Interruption(Date startTime, int durationMillis, String reason)
    {
        this(0, startTime, durationMillis, reason);
    }
    
    public Interruption(int id, Date startTime, int durationMillis, String reason)
    {
        super(startTime, durationMillis);
        mReason = reason;
        mId = id;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public String toString()
    {
        return "Interruption{" +
               "mStartTime=" + getStart() +
               ", mDurationMillis=" + getDurationMillis() +
               ", mReason='" + mReason + '\'' +
               '}';
    }
    
    public Date getEnd()
    {
        return new TimeUtils().getDateFromMillis(getStart().getTime() + getDurationMillis());
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Interruption that = (Interruption) o;
        
        if (mId != that.mId) { return false; }
        if (!super.equals(o)) { return false; }
        return Objects.equals(mReason, that.mReason);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (mReason != null ? mReason.hashCode() : 0);
        result = 31 * result + mId;
        return result;
    }
    
//*********************************************************
// api
//*********************************************************

    public String getReason()
    {
        return mReason;
    }
    
    public void setReason(String reason)
    {
        mReason = reason;
    }
    
    public int getId()
    {
        return mId;
    }
}
