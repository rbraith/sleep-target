package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;
import java.util.GregorianCalendar;

public class WakeTimeGoalModel
{
//*********************************************************
// private properties
//*********************************************************

    private Date mEditTime;
    private Integer mGoalMillis; // millis from 12am
    
//*********************************************************
// constructors
//*********************************************************

    public WakeTimeGoalModel(Date editTime, int goalMillis)
    {
        mEditTime = editTime;
        mGoalMillis = goalMillis;
    }
    
    private WakeTimeGoalModel()
    {
        // used for static factories
    }
    
//*********************************************************
// api
//*********************************************************

    public static WakeTimeGoalModel createWithNoGoal(Date editTime)
    {
        WakeTimeGoalModel model = new WakeTimeGoalModel();
        model.mEditTime = editTime;
        return model;
    }
    
    public Date getEditTime()
    {
        return mEditTime;
    }
    
    /**
     * Returns null if isSet() is false.
     */
    public Integer getGoalMillis()
    {
        return mGoalMillis;
    }
    
    public boolean isSet()
    {
        return mGoalMillis != null;
    }
    
    /**
     * Returns null if isSet() is false.
     */
    public Date asDate()
    {
        if (!isSet()) {
            return null;
        }
        
        // REFACTOR [21-03-9 3:05AM] -- I should probably be injecting calendars wherever I'm
        //  using them :/
        GregorianCalendar cal = new GregorianCalendar();
        // REFACTOR [21-03-9 3:05AM] -- inject time utils here
        TimeUtils timeUtils = new TimeUtils();
        timeUtils.setCalendarTimeOfDay(cal, getGoalMillis());
        return cal.getTime();
    }
}
