package com.rbraithwaite.sleepapp.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

// TODO [21-04-18 11:50PM] -- This should actually be a LiveData.
public class LiveDataFuture
{
//*********************************************************
// public helpers
//*********************************************************

    public interface OnValueListener<T>
    {
        void onValue(T value);
    }

//*********************************************************
// constructors
//*********************************************************

    private LiveDataFuture() {/* No instantiation */}



//*********************************************************
// api
//*********************************************************

    
    /**
     * This acts as a kind of "one-off" observer of a particular LiveData. It will activate the
     * LiveData and wait until the LiveData has a value before calling the onValueListener.
     * <p>
     * If lifecycleOwner is null, LiveData.observeForever() is used instead of LiveData.observe().
     */
    public static <T> void getValue(
            @NonNull final LiveData<T> liveData,
            LifecycleOwner lifecycleOwner,
            @NonNull final OnValueListener<T> onValueListener)
    {
        Observer<T> observer = new Observer<T>()
        {
            @Override
            public void onChanged(T t)
            {
                // remove the observer first, in case the client does something like refreshing
                // liveData inside the listener
                liveData.removeObserver(this);
                onValueListener.onValue(t);
            }
        };
        
        if (lifecycleOwner == null) {
            liveData.observeForever(observer);
        } else {
            liveData.observe(lifecycleOwner, observer);
        }
    }
    
    public static <T> void getValue(
            @NonNull final LiveData<T> liveData,
            @NonNull final OnValueListener<T> onValueListener)
    {
        getValue(liveData, null, onValueListener);
    }
}
