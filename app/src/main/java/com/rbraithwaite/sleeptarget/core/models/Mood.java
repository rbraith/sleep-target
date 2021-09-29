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
package com.rbraithwaite.sleeptarget.core.models;

import com.rbraithwaite.sleeptarget.utils.Constants;

import java.io.Serializable;

// HACK [21-09-28 8:14PM] -- This core model shouldn't be serializable - consider making a
//  parcelable wrapper at the infrastructure level instead.
public class Mood
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMoodIndex;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

//*********************************************************
// constructors
//*********************************************************

    // SMELL [21-06-11 11:03PM] -- Right now Mood is just a wrapper around the index. I left it
    //  this way in order to not break interfaces that use Mood, and to allow for possible
    //  future expansion of functionality. See also ui.common.data.MoodUiData.MoodUiData(int).
    public Mood(Integer moodIndex)
    {
        mMoodIndex = moodIndex;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return mMoodIndex.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Mood mood = (Mood) o;
        
        return (mMoodIndex == null && mood.mMoodIndex == null) ||
               mMoodIndex.equals(mood.mMoodIndex);
    }

//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-06-11 11:07PM] -- legacy method - replace this with Mood(moodIndex).
    @Deprecated
    public static Mood fromIndex(Integer moodIndex)
    {
        return new Mood(moodIndex);
    }
    
    /**
     * Returns the index of the Type of this mood.
     */
    public Integer asIndex()
    {
        return mMoodIndex;
    }
}
