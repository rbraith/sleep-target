package com.rbraithwaite.sleepapp.utils;

import android.os.Handler;
import android.os.Looper;

public class AsyncUtils
{
//*********************************************************
// constructors
//*********************************************************

    private AsyncUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static void postUIThreadTask(Runnable task)
    {
        Handler uiThreadHandler = new Handler(Looper.getMainLooper());
        uiThreadHandler.post(task);
    }
}
