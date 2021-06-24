package com.rbraithwaite.sleepapp.utils;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

// SMELL [21-04-22 11:32PM] -- This is still kind of ugly, look for a better way.



/**
 * An Observer meant to facilitate observing a single LiveData at a time, in case you lose track of
 * the LiveData it was observing before. Use like this: myLiveData.observe(...MySingleObserver
 * .transferTo(myLiveData))
 */
public abstract class SingleObserver<T>
        implements Observer<T>
{
//*********************************************************
// private properties
//*********************************************************

    private LiveData<T> mLiveData;

//*********************************************************
// api
//*********************************************************

    public SingleObserver<T> transferTo(LiveData<T> liveData)
    {
        if (mLiveData != null) {
            mLiveData.removeObserver(this);
        }
        mLiveData = liveData;
        
        return this;
    }
}
