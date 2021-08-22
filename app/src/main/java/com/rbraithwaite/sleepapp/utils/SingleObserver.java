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
