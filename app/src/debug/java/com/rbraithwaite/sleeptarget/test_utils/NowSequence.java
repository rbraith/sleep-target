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

package com.rbraithwaite.sleeptarget.test_utils;

import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Date;
import java.util.List;

public class NowSequence
        extends TimeUtils
{
//*********************************************************
// private properties
//*********************************************************

    private int currentIndex = -1; // -1 so that the first call increments to 0
    private List<Date> mNows;
    private boolean mShouldRepeat;
    
//*********************************************************
// constructors
//*********************************************************

    public NowSequence(List<Date> nows, boolean shouldRepeat)
    {
        mNows = nows;
        mShouldRepeat = shouldRepeat;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Date getNow()
    {
        if (mNows == null || mNows.isEmpty() || currentIndex >= mNows.size()) {
            // REFACTOR [21-07-15 12:10AM] -- this should maybe throw an exception here instead.
            return null;
        }
        
        currentIndex++;
        if (currentIndex >= mNows.size() && mShouldRepeat) {
            currentIndex = 0;
        }
        
        return mNows.get(currentIndex);
    }
}
