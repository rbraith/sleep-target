package com.rbraithwaite.sleepapp.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.utils.interfaces.Factory;

import java.util.concurrent.Executor;

public class AsyncUtils
{
//*********************************************************
// public helpers
//*********************************************************

    public static class AsyncFactory<T>
    {
        private Executor mExecutor;
        
        public AsyncFactory(Executor executor)
        {
            mExecutor = executor;
        }
        
        public LiveData<T> createAsync(Factory<T> factory)
        {
            MutableLiveData<T> liveData = new MutableLiveData<>();
            mExecutor.execute(() -> liveData.postValue(factory.create()));
            return liveData;
        }
    }

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
