/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
