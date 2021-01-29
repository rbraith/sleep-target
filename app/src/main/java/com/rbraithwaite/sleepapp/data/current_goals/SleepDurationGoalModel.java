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

    public SleepDurationGoalModel(Integer minutes)
    {
        mMinutes = minutes;
    }

//*********************************************************
// api
//*********************************************************

    
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
}
