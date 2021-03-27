package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

public class SleepDurationGoal
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMinutes;
    private Date mEditTime;

//*********************************************************
// constructors
//*********************************************************

    private SleepDurationGoal(Date editTime)
    {
        mMinutes = null;
        mEditTime = editTime;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromMinutes().
    public SleepDurationGoal(int minutes)
    {
        this(new TimeUtils().getNow(), minutes);
    }
    
    public SleepDurationGoal(Date editTime, int minutes)
    {
        mMinutes = minutes;
        mEditTime = editTime;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromHoursAndMinutes().
    public SleepDurationGoal(int hours, int minutes)
    {
        this(new TimeUtils().getNow(), hours, minutes);
    }
    
    public SleepDurationGoal(Date editTime, int hours, int minutes)
    {
        // TODO [21-02-2 1:38AM] -- arg validity checks: >= 0.
        // REFACTOR [21-02-2 1:38AM] -- extract this conversion logic.
        mMinutes = (hours * 60) + minutes;
        mEditTime = editTime;
    }

//*********************************************************
// api
//*********************************************************

    public static SleepDurationGoal createWithNoGoal(Date editTime)
    {
        return new SleepDurationGoal(editTime);
    }
    
    public static SleepDurationGoal createWithNoGoal()
    {
        return new SleepDurationGoal(new TimeUtils().getNow());
    }
    
    
    /**
     * If isSet() returns false, this will return null.
     */
    public Integer inMinutes()
    {
        return mMinutes;
    }
    
    public boolean isSet()
    {
        return (mMinutes != null);
    }
    
    public Date getEditTime()
    {
        return mEditTime;
    }
    
    public Integer getHours()
    {
        if (!isSet()) {
            return null;
        }
        
        return getHoursUnsafe();
    }
    
    public Integer getRemainingMinutes()
    {
        if (!isSet()) {
            return null;
        }
        
        // using unsafe here so that isSet isn't checked redundantly
        return mMinutes - (getHoursUnsafe() * 60);
    }



//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Does not check for null.
     */
    private int getHoursUnsafe()
    {
        return mMinutes / 60;
    }
}
