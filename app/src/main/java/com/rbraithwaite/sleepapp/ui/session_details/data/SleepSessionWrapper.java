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

package com.rbraithwaite.sleepapp.ui.session_details.data;

import com.rbraithwaite.sleepapp.core.models.SleepSession;

import java.io.Serializable;

// REFACTOR [21-01-9 2:43AM] -- consider just using Bundles instead?


// SMELL [21-03-26 1:28AM] -- SessionDataFragment shouldn't be using domain models for its data.
//  It should be using simple data structures for its input & output interfaces. I should deprecate
//  this class & replace it.



/**
 * A simple wrapper used to transport sleep session data between view models without polluting the
 * view layer (where the transportation occurs) with references to that data (the view layer should
 * only care about UI data representations).
 */
public class SleepSessionWrapper
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSession mSleepSession;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210115;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionWrapper(SleepSession sleepSession)
    {
        mSleepSession = sleepSession;
    }

//*********************************************************
// api
//*********************************************************

    public SleepSession getModel()
    {
        return mSleepSession;
    }
}
