package com.rbraithwaite.sleepapp.utils;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.lifecycle.MutableLiveData;

public abstract class TickingLiveData<T>
        extends MutableLiveData<T>
{
//*********************************************************
// private properties
//*********************************************************

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private long mFrequencyMillis;

//*********************************************************
// private constants
//*********************************************************

    private static final long DEFAULT_FREQUENCY_MILLIS = 100;

//*********************************************************
// public constants
//*********************************************************

    public static final String THREAD_NAME = "TickingLiveData";

//*********************************************************
// constructors
//*********************************************************

    public TickingLiveData()
    {
        this(DEFAULT_FREQUENCY_MILLIS);
    }
    
    public TickingLiveData(long frequencyMillis)
    {
        mFrequencyMillis = frequencyMillis;
    }



//*********************************************************
// abstract
//*********************************************************

    
    /**
     * The return value of this method updates the LiveData on each tick.
     */
    public abstract T onTick();

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
                TickingLiveData.this.postValue(onTick());
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
