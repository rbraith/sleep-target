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
    private boolean mAllHaveValues = false;
    private boolean mUsePost = false;

//*********************************************************
// public properties
//*********************************************************

    public static Object NO_VALUE = new Object();

//*********************************************************
// public helpers
//*********************************************************

    public static class Update
    {
        public List<Object> values;
        public int updatedIndex;
        
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
        initValues(liveData.length);
        
        for (int i = 0; i < liveData.length; i++) {
            int j = i; // "effectively final"
            addSource(liveData[i], value -> {
                mValues.set(j, value);
                MergedLiveData.this.setValue(new Update(mValues, j));
            });
        }
    }
    
    /**
     * Overload of MergedLiveData which can wait until all the provided live data instances have a
     * real value before updating itself. (I.e. ilf waitForAll is true, it's guaranteed that none of
     * the update values will be NO_VALUE)
     */
    public MergedLiveData(
            boolean waitForAll,
            LiveData<?>... liveData)
    {
        initValues(liveData.length);
        
        for (int i = 0; i < liveData.length; i++) {
            int j = i; // "effectively final"
            addSource(liveData[i], value -> {
                mValues.set(j, value);
                if (!waitForAll || allHaveValues()) {
                    // SMELL [21-10-4 9:11PM] -- the update uses a ref to the private values list
                    MergedLiveData.this.setValue(new Update(mValues, j));
                }
            });
        }
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initValues(int size)
    {
        mValues = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            mValues.add(NO_VALUE);
        }
    }
    
    private boolean allHaveValues()
    {
        if (mAllHaveValues) {
            return true;
        }
        
        for (Object val : mValues) {
            if (val == NO_VALUE) {
                return false;
            }
        }
        
        mAllHaveValues = true;
        return true;
    }
}
