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
