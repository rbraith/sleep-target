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

import android.os.Handler;
import android.os.HandlerThread;

import androidx.lifecycle.LiveData;

public class TickingLiveData<T>
        extends LiveData<T>
{
//*********************************************************
// private properties
//*********************************************************

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private long mFrequencyMillis;
    
    private OnTick<T> mOnTick;
    
//*********************************************************
// private constants
//*********************************************************

    private static final long DEFAULT_FREQUENCY_MILLIS = 100;
    
//*********************************************************
// public constants
//*********************************************************

    public static final String THREAD_NAME = "TickingLiveData";
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnTick<T>
    {
        T onTick();
    }
    
//*********************************************************
// constructors
//*********************************************************

    public TickingLiveData()
    {
        this(DEFAULT_FREQUENCY_MILLIS, null);
    }
    
    public TickingLiveData(OnTick<T> onTick)
    {
        this(DEFAULT_FREQUENCY_MILLIS, onTick);
    }
    
    public TickingLiveData(long frequencyMillis, OnTick<T> onTick)
    {
        mFrequencyMillis = frequencyMillis;
        mOnTick = onTick;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected void onActive()
    {
        startTicking();
    }

    @Override
    protected void onInactive()
    {
        stopTicking();
    }
    
//*********************************************************
// api
//*********************************************************

    public synchronized void setOnTick(OnTick<T> onTick)
    {
        mOnTick = onTick;
    }

//*********************************************************
// private methods
//*********************************************************

    private synchronized void startTicking()
    {
        mHandlerThread = new HandlerThread(THREAD_NAME);
        mHandlerThread.start();
        
        // the thread needs to be started before accessing its looper
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mOnTick != null) {
                    TickingLiveData.this.postValue(mOnTick.onTick());
                }
                mHandler.postDelayed(this, mFrequencyMillis);
            }
        });
    }
    
    private synchronized void stopTicking()
    {
        // null token removes all
        mHandler.removeCallbacksAndMessages(null);
        mHandlerThread.quit();
        
        mHandler = null;
        mHandlerThread = null;
    }
}
