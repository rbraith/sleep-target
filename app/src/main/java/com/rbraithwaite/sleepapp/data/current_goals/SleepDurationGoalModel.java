package com.rbraithwaite.sleepapp.data.current_goals;

public class SleepDurationGoalModel
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMinutes;

//*********************************************************
// constructors
//*********************************************************

    private SleepDurationGoalModel()
    {
        mMinutes = null;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromMinutes().
    public SleepDurationGoalModel(int minutes)
    {
        mMinutes = minutes;
    }
    
    // REFACTOR [21-02-2 1:43AM] -- should maybe use a static factory here instead.
    //  createFromHoursAndMinutes().
    public SleepDurationGoalModel(int hours, int minutes)
    {
        // TODO [21-02-2 1:38AM] -- arg validity checks: >= 0.
        // REFACTOR [21-02-2 1:38AM] -- extract this conversion logic.
        mMinutes = (hours * 60) + minutes;
    }
    
//*********************************************************
// api
//*********************************************************

    public static SleepDurationGoalModel createWithoutSettingGoal()
    {
        return new SleepDurationGoalModel();
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
