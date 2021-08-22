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
