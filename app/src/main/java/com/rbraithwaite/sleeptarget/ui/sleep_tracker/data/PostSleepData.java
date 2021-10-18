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

import com.rbraithwaite.sleeptarget.utils.Constants;

import java.io.Serializable;

public class PostSleepData implements Serializable
{
//*********************************************************
// public constants
//*********************************************************
    
    public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    public final float rating;

//*********************************************************
// constructors
//*********************************************************

    public PostSleepData(float rating)
    {
        this.rating = rating;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        PostSleepData that = (PostSleepData) o;
        
        return Float.compare(that.rating, rating) == 0;
    }
}
