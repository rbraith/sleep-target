package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.utils.DateUtils;

import java.io.Serializable;
import java.util.Date;

public class SleepSessionModel
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private Date mStart;
    private long mDuration;
    private Date mWakeTimeGoal;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepSessionModel(int id, Date start, long duration, Date wakeTimeGoal)
    {
        mId = id;
        mStart = start;
        mDuration = duration;
        mWakeTimeGoal = wakeTimeGoal;
    }
    
    public SleepSessionModel(Date start, long duration, Date wakeTimeGoal)
    {
        mId = 0;
        mStart = start;
        mDuration = duration;
        mWakeTimeGoal = wakeTimeGoal;
    }
    
//*********************************************************
// api
//*********************************************************

    public int getId()
    {
        return mId;
    }
    
    public void setId(int id)
    {
        mId = id;
    }
    
    public Date getStart()
    {
        return mStart;
    }
    
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public long getDuration()
    {
        return mDuration;
    }
    
    public void setDuration(long duration)
    {
        mDuration = duration;
    }
    
    public Date getWakeTimeGoal()
    {
        return mWakeTimeGoal;
    }
    
    public void setWakeTimeGoal(Date wakeTimeGoal)
    {
        mWakeTimeGoal = wakeTimeGoal;
    }
    
    public Date getEnd()
    {
        Date start = getStart();
        if (start == null) {
            return null;
        }
        long durationMillis = getDuration();
        
        return DateUtils.getDateFromMillis(
                start.getTime() + durationMillis);
    }
}
