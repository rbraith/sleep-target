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

package com.rbraithwaite.sleeptarget.ui.sleep_tracker.data;

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.utils.Constants;

import java.io.Serializable;

public class StoppedSessionData implements Serializable
{
//*********************************************************
// public constants
//*********************************************************
    
    public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    public final CurrentSession.Snapshot currentSessionSnapshot;
    public final PostSleepData postSleepData;
    
//*********************************************************
// constructors
//*********************************************************

    public StoppedSessionData(
            CurrentSession.Snapshot currentSessionSnapshot,
            PostSleepData postSleepData)
    {
        this.currentSessionSnapshot = currentSessionSnapshot;
        this.postSleepData = postSleepData;
    }
}
