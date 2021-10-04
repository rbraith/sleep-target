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

package com.rbraithwaite.sleeptarget.ui.interruption_details;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;

import java.io.Serializable;

public class InterruptionDetailsData
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private Interruption mInterruption;
    private SleepSession mParentSleepSession;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210115;

//*********************************************************
// constructors
//*********************************************************

    public InterruptionDetailsData(Interruption interruption, SleepSession parentSleepSession)
    {
        mInterruption = interruption;
        mParentSleepSession = parentSleepSession;
    }

//*********************************************************
// api
//*********************************************************

    public Interruption getInterruption()
    {
        return mInterruption;
    }
    
    public SleepSession getParentSleepSession()
    {
        return mParentSleepSession;
    }
}
