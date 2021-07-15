package com.rbraithwaite.sleepapp.utils;

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
