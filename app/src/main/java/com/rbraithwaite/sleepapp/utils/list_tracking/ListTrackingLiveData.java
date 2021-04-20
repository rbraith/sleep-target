package com.rbraithwaite.sleepapp.utils.list_tracking;

import androidx.lifecycle.LiveData;

import java.util.List;

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
        setValue(new ListTrackingData<>(mList, null));
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
    
    public void set(int index, T elem)
    {
        mList.set(index, elem);
        postValue(new ListTrackingData<T>(
                mList,
                new ListTrackingData.ListChange<T>(
                        elem,
                        index,
                        ListTrackingData.ChangeType.MODIFIED)));
    }
}
