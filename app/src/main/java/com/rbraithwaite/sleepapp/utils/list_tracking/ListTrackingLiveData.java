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

package com.rbraithwaite.sleepapp.utils.list_tracking;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Predicate;



/**
 * A form of mutable LiveData, designed to provide atomic updates about a list. In order to receive
 * atomic updates about a list (individual add, delete, & modify operations) you need to manipulate
 * the list provided in ListTrackingLiveData's constructor through LiveTrackingLiveData's
 * list-analogous operations (add(), delete(), set()).
 */
public class ListTrackingLiveData<T>
        extends LiveData<ListTrackingData<T>>
{
//*********************************************************
// private properties
//*********************************************************

    private List<T> mList;
    private long mChangeId = -1L; // -1 so it increments to 0 the first time

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "ListTrackingLiveData";

//*********************************************************
// constructors
//*********************************************************

    public ListTrackingLiveData(List<T> list)
    {
        mList = list;
        setValue(new ListTrackingData<>(getNextChangeId(), mList, null));
    }

//*********************************************************
// api
//*********************************************************

    public List<T> getList()
    {
        return mList;
    }
    
    public void add(T elem)
    {
        mList.add(elem);
        postValue(new ListTrackingData<>(
                getNextChangeId(),
                mList,
                new ListTrackingData.ListChange<>(
                        elem,
                        mList.size() - 1,
                        ListTrackingData.ChangeType.ADDED)));
    }
    
    public void delete(int index)
    {
        T deleted = mList.remove(index);
        postValue(new ListTrackingData<>(
                getNextChangeId(),
                mList,
                new ListTrackingData.ListChange<>(
                        deleted,
                        index,
                        ListTrackingData.ChangeType.DELETED)));
    }
    
    public void delete(T elem)
    {
        delete(mList.indexOf(elem));
    }
    
    // TEST NEEDED [21-06-30 3:03AM]
    
    /**
     * Deletes the first element matching the predicate. Does nothing if there is no match.
     */
    public void delete(Predicate<T> predicate)
    {
        // REFACTOR [21-06-29 9:58PM] -- there might be a better stream-based way of doing this.
        for (int i = 0; i < mList.size(); i++) {
            if (predicate.test(mList.get(i))) {
                delete(i);
                break;
            }
        }
    }
    
    public void set(int index, T elem)
    {
        mList.set(index, elem);
        postValue(new ListTrackingData<T>(
                getNextChangeId(),
                mList,
                new ListTrackingData.ListChange<T>(
                        elem,
                        index,
                        ListTrackingData.ChangeType.MODIFIED)));
    }
    
    // TEST NEEDED [21-06-30 3:03AM]
    
    /**
     * Set the first element that matches the predicate.
     */
    public void set(T elem, Predicate<T> predicate)
    {
        // REFACTOR [21-06-29 9:58PM] -- there might be a better stream-based way of doing this.
        int elemIndex = -1; // -1 so that if the elem isn't found, the set() call will crash
        for (int i = 0; i < mList.size(); i++) {
            if (predicate.test(mList.get(i))) {
                elemIndex = i;
                break;
            }
        }
        set(elemIndex, elem);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private long getNextChangeId()
    {
        mChangeId++;
        return mChangeId;
    }
}
