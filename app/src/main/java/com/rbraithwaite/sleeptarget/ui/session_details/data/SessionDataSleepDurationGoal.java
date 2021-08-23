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

package com.rbraithwaite.sleeptarget.ui.session_details.data;

// REFACTOR [21-02-9 10:35PM] -- this duplicates ui.sleep_goals.data.SleepDurationGoalUIData.java
//  -- should I make some ui.common_data package or something? Or should I leave this as is to
//  keep things separated.
public class SessionDataSleepDurationGoal
{
//*********************************************************
// public properties
//*********************************************************

    public int hours;
    public int remainingMinutes;

//*********************************************************
// constructors
//*********************************************************

    public SessionDataSleepDurationGoal(int hours, int remainingMinutes)
    {
        this.hours = hours;
        this.remainingMinutes = remainingMinutes;
    }
}
