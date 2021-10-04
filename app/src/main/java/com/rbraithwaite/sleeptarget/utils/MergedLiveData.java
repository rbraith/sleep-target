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

package com.rbraithwaite.sleeptarget.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;

// REFACTOR [21-07-14 9:29PM] -- I ended up not needing this class for what I originally created
//  it for - consider deleting this class.

/**
 * Clients will need to recast the types of the values in the update.
 */
public class MergedLiveData
        extends MediatorLiveData<MergedLiveData.Update>
{
//*********************************************************
// private properties
//*********************************************************

    private List<Object> mValues;

//*********************************************************
// public properties
//*********************************************************

    public static Object NO_VALUE = new Object();
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Update
    {
        List<Object> values;
        int updatedIndex;
        
        public Update(List<Object> values, int updatedIndex)
        {
            this.values = values;
            this.updatedIndex = updatedIndex;
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public MergedLiveData(LiveData<?>... liveData)
    {
        mValues = new ArrayList<>(liveData.length);
        
        for (int i = 0; i < liveData.length; i++) {
            // BUG [21-07-14 9:31PM] -- I should probably set the entire values list to NO_VALUE
            //  before adding sources.
            mValues.set(i, NO_VALUE);
            int j = i; // "effectively final"
            addSource(liveData[i], value -> {
                mValues.set(j, value);
                MergedLiveData.this.postValue(new Update(mValues, j));
            });
        }
    }
}
