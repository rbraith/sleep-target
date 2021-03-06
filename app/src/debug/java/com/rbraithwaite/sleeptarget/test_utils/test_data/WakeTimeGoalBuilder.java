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

package com.rbraithwaite.sleeptarget.test_utils.test_data;

import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;

public class WakeTimeGoalBuilder
        implements BuilderOf<WakeTimeGoal>
{
    private Date mEditTime;
    private int mGoalMillis;
    private Boolean hasGoal = true;
    
    public WakeTimeGoalBuilder()
    {
        mEditTime = valueOf(aDate());
        mGoalMillis = (int) TimeUtils.hoursToMillis(8);
    }
    
    @Override
    public WakeTimeGoal build()
    {
        if (hasGoal) {
            return new WakeTimeGoal(mEditTime, mGoalMillis);
        } else {
            return WakeTimeGoal.createWithNoGoal(mEditTime);
        }
    }
    
    public WakeTimeGoalBuilder withEditTime(DateBuilder editTime)
    {
        mEditTime = editTime.build();
        return this;
    }
    
    public WakeTimeGoalBuilder withGoal(int hourOfDay, int minutes)
    {
        mGoalMillis = (int) TimeUtils.timeToMillis(hourOfDay, minutes, 0, 0);
        hasGoal = true;
        return this;
    }
    
//*********************************************************
// api
//*********************************************************

    public WakeTimeGoalBuilder withNoGoalSet()
    {
        hasGoal = false;
        return this;
    }
}
