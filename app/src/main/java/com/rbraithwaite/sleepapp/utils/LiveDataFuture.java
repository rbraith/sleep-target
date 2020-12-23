package com.rbraithwaite.sleepapp.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
     * This acts as a kind of "one-off" observer of a particular LiveData. It will observe the
     * provided LiveData until a valid value is obtained, then it will stop observing.
     * <p>
     * It is meant to be used with LiveData whose valid values cannot be null, as it waits until the
     * LiveData's value is not null before triggering its callback.
     */
    public static <T> void getValue(
            @NonNull final LiveData<T> liveData,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull final OnValueListener<T> onValueListener)
    {
        liveData.observe(
                lifecycleOwner,
                new Observer<T>()
                {
                    @Override
                    public void onChanged(T t)
                    {
                        if (t != null) {
                            onValueListener.onValue(t);
                            liveData.removeObserver(this);
                        }
                    }
                });
    }
}
