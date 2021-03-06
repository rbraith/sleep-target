package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

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
    private SleepDurationGoalModel mSleepDurationGoal;
    private TimeUtils mTimeUtils;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionModel(
            int id,
            Date start,
            long duration,
            Date wakeTimeGoal,
            SleepDurationGoalModel sleepDurationGoal)
    {
        mId = id;
        mStart = start;
        mDuration = duration;
        mWakeTimeGoal = wakeTimeGoal;
        mSleepDurationGoal = sleepDurationGoal;
        
        mTimeUtils = createTimeUtils();
    }
    
    public SleepSessionModel(
            Date start,
            long duration,
            Date wakeTimeGoal,
            SleepDurationGoalModel sleepDurationGoal)
    {
        this(0, start, duration, wakeTimeGoal, sleepDurationGoal);
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
        
        return mTimeUtils.getDateFromMillis(
                start.getTime() + durationMillis);
    }
    
    public SleepDurationGoalModel getSleepDurationGoal()
    {
        return mSleepDurationGoal;
    }
    
    public void setSleepDurationGoal(SleepDurationGoalModel sleepDurationGoal)
    {
        mSleepDurationGoal = sleepDurationGoal;
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
