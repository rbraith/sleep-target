package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WakeTimeGoal
{
//*********************************************************
// private properties
//*********************************************************

    private Date mEditTime;
    private Integer mGoalMillis; // millis from 12am

//*********************************************************
// constructors
//*********************************************************

    public WakeTimeGoal(Date editTime, int goalMillis)
    {
        mEditTime = editTime;
        mGoalMillis = goalMillis;
    }
    
    private WakeTimeGoal()
    {
        // used for static factories
    }

    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public String toString()
    {
        return "WakeTimeGoal{" +
               "mEditTime=" + mEditTime +
               ", mGoalMillis=" + mGoalMillis +
               '}';
    }
    
//*********************************************************
// api
//*********************************************************

    public static WakeTimeGoal createWithNoGoal(Date editTime)
    {
        WakeTimeGoal model = new WakeTimeGoal();
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
    
    // TEST NEEDED [21-06-26 9:02PM]
    public TimeOfDay asTimeOfDay()
    {
        GregorianCalendar cal = new GregorianCalendar();
        // REFACTOR [21-03-9 3:05AM] -- inject time utils here
        TimeUtils timeUtils = new TimeUtils();
        timeUtils.setCalendarTimeOfDay(cal, getGoalMillis());
        
        return new TimeOfDay(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
    }
}
