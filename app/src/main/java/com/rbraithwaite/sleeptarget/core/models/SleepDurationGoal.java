/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.core.models;

import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Date;

// REFACTOR [21-08-29 5:30PM] -- Rename all internal instances of "goal" to "target".
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
