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

package com.rbraithwaite.sleepapp.ui.sleep_goals.data;

public class SleepDurationGoalUIData
{
//*********************************************************
// public properties
//*********************************************************

    public int hours;
    public int remainingMinutes;

//*********************************************************
// constructors
//*********************************************************

    public SleepDurationGoalUIData(int hours, int remainingMinutes)
    {
        this.hours = hours;
        this.remainingMinutes = remainingMinutes;
    }
}
