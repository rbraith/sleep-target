package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

public class SleepDurationGoalModel
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMinutes;
    private Date mEditTime;

//*********************************************************
// constructors
//*********************************************************

    private SleepDurationGoalModel(Date editTime)
    {
        mMinutes = null;
        mEditTime = editTime;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromMinutes().
    public SleepDurationGoalModel(int minutes)
    {
        this(new TimeUtils().getNow(), minutes);
    }
    
    public SleepDurationGoalModel(Date editTime, int minutes)
    {
        mMinutes = minutes;
        mEditTime = editTime;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromHoursAndMinutes().
    public SleepDurationGoalModel(int hours, int minutes)
    {
        this(new TimeUtils().getNow(), hours, minutes);
    }
    
    public SleepDurationGoalModel(Date editTime, int hours, int minutes)
    {
        // TODO [21-02-2 1:38AM] -- arg validity checks: >= 0.
        // REFACTOR [21-02-2 1:38AM] -- extract this conversion logic.
        mMinutes = (hours * 60) + minutes;
        mEditTime = editTime;
    }

//*********************************************************
// api
//*********************************************************
    
    public static SleepDurationGoalModel createWithNoGoal(Date editTime)
    {
        return new SleepDurationGoalModel(editTime);
    }

    public static SleepDurationGoalModel createWithNoGoal()
    {
        return new SleepDurationGoalModel(new TimeUtils().getNow());
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
