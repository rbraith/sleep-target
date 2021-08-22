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

package com.rbraithwaite.sleepapp.test_utils.test_data;

import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;

public class WakeTimeGoalBuilder
        implements BuilderOf<WakeTimeGoal>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mEditTime;
    private int mGoalMillis;
    
//*********************************************************
// constructors
//*********************************************************

    public WakeTimeGoalBuilder()
    {
        mEditTime = valueOf(aDate());
        mGoalMillis = (int) TimeUtils.hoursToMillis(8);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public WakeTimeGoal build()
    {
        return new WakeTimeGoal(mEditTime, mGoalMillis);
    }
    
//*********************************************************
// api
//*********************************************************

    public WakeTimeGoalBuilder withEditTime(DateBuilder editTime)
    {
        mEditTime = editTime.build();
        return this;
    }
    
    public WakeTimeGoalBuilder withGoal(int hourOfDay, int minutes)
    {
        mGoalMillis = (int) TimeUtils.timeToMillis(hourOfDay, minutes, 0, 0);
        return this;
    }
}
