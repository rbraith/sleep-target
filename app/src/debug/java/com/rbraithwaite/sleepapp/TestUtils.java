package com.rbraithwaite.sleepapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

public class TestUtils
{
//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * Mainly use this to set external objs from within anon class overrides. Analogous to double
     * pointer behavior.
     */
    public static class DoubleRef<T>
    {
        public T ref;
        
        public DoubleRef(T ref)
        {
            this.ref = ref;
        }
    }
    
    public static class SynchronizedExecutor
            implements Executor
    {
        @Override
        public void execute(Runnable command)
        {
            command.run();
        }
    }
    
    public static class ThreadBlocker
    {
        private volatile boolean blocking = true;
        
        public void blockThread()
        {
            while (blocking) {/*block*/}
            reset();
        }
        
        public void unblockThread() {blocking = false;}
        
        private void reset() {blocking = true;}
    }
    
    /**
     * Synchronizes asynchronous LiveData updates
     */
    public static class LocalLiveDataSynchronizer<T>
            extends LiveDataSynchronizerBase<T>
    {
        public LocalLiveDataSynchronizer(LiveData<T> liveData)
        {
            super(liveData);
        }
        
        @Override
        public void attachObserver(LiveData<T> liveData, Observer<T> observer)
        {
            liveData.observeForever(observer);
        }
    }
    
    public static class InstrumentationLiveDataSynchronizer<T>
            extends LiveDataSynchronizerBase<T>
    {
        public InstrumentationLiveDataSynchronizer(LiveData<T> liveData)
        {
            super(liveData);
        }
        
        @Override
        public void attachObserver(LiveData<T> liveData, Observer<T> observer)
        {
            observeLiveDataOnMainThread(liveData, observer, null);
        }
    }
    
    /**
     * Provides fixed, arbitrary data for cases where the specific details of the data aren't
     * important.
     */
    public static class ArbitraryData
    {
        public static List<Integer> getIdList() { return Arrays.asList(1, 2, 3, 4, 5); }
        
        public static Date getDate()
        {
            return new GregorianCalendar(2019, 8, 7, 6, 5).getTime();
        }
        
        public static SleepSessionEntity getSleepSessionEntity()
        {
            SleepSessionEntity sleepSessionEntity = new SleepSessionEntity();
            sleepSessionEntity.startTime = getDate();
            sleepSessionEntity.duration = getDurationMillis();
            
            return sleepSessionEntity;
        }
        
        public static SleepSessionData getSleepSessionData()
        {
            SleepSessionData sleepSessionData = new SleepSessionData();
            sleepSessionData.duration = getDurationMillis();
            sleepSessionData.startTime = getDate();
            
            return sleepSessionData;
        }
        
        public static long getDurationMillis() {return 5000L;}
    }



//*********************************************************
// package helpers
//*********************************************************

    
    /**
     * Guarantees that LiveData will be synchronized on the current thread.
     */
    static abstract class LiveDataSynchronizerBase<T>
    {
        private ThreadBlocker mBlocker = new ThreadBlocker();

        
        /**
         * @param liveData The LiveData to synchronize. Will be activated if not already.
         */
        public LiveDataSynchronizerBase(LiveData<T> liveData)
        {
            Observer<T> dummyObserver = new Observer<T>()
            {
                @Override
                public void onChanged(T t)
                {
                    mBlocker.unblockThread();
                }
            };
            attachObserver(liveData, dummyObserver);
            sync(); // sync any initial LiveData activation
        }
        
        // when an observer is attached, onChanged is triggered
        //      https://developer.android.com/reference/androidx/lifecycle/LiveData?hl=en#observe
        //      (androidx.lifecycle.LifecycleOwner,%20androidx.lifecycle
        //      .Observer%3C?%20super%20T%3E)
        //      "If LiveData already has data set, it will be delivered to the observer."
        public abstract void attachObserver(LiveData<T> liveData, Observer<T> observer);

        
        /**
         * Blocks the current thread until the database has updated the LiveData.
         */
        public void sync() { mBlocker.blockThread(); }
    }


//*********************************************************
// api
//*********************************************************

    public static void runOnMainSync(Runnable runner)
    {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runner);
    }
    
    /**
     * Resets the database for instumentation tests.
     */
    public static void resetDatabase()
    {
        InstrumentationRegistry.getInstrumentation().getTargetContext()
                .deleteDatabase(SleepAppDatabase.NAME);
    }
    
    public static void resetSharedPreferences()
    {
        SharedPreferences.Editor editor =
                ApplicationProvider.getApplicationContext()
                        .getSharedPreferences(SleepAppDataPrefs.PREFS_FILE_KEY,
                                              Context.MODE_PRIVATE)
                        .edit();
        
        editor.clear();
        editor.commit();
    }
    
    // can't run observeForever on instrumentation thread
    // https://developer.android.com/reference/androidx/test/annotation/UiThreadTest
    // https://stackoverflow.com/a/19427987
    public static <T> void observeLiveDataOnMainThread(
            final LiveData<T> liveData,
            final Observer<T> observer,
            final ThreadBlocker blocker)
    {
        final Observer<T> unblockingObserver = new Observer<T>()
        {
            @Override
            public void onChanged(T t)
            {
                observer.onChanged(t);
                if (blocker != null) {
                    blocker.unblockThread();
                }
            }
        };
        runOnMainSync(new Runnable()
        {
            @Override
            public void run()
            {
                liveData.observeForever(unblockingObserver);
            }
        });
        if (blocker != null) {
            blocker.blockThread();
        }
    }
    
    public static <T> void removeObserverOnMainThread(
            final LiveData<T> liveData,
            final Observer<T> observer)
    {
        final ThreadBlocker blocker = new ThreadBlocker();
        runOnMainSync(new Runnable()
        {
            @Override
            public void run()
            {
                liveData.removeObserver(observer);
                blocker.unblockThread();
            }
        });
        blocker.blockThread();
    }
    
    /**
     * LiveData returned from RoomDatabase is not initialized until it has at least one observer.
     * This method provides a dummy observer to force that activation. It also blocks until the
     * database has had time to properly update the LiveData. (This is done asynchronously, so
     * unless you block there are no guarantees)
     * <p>
     * An assumption is made that the provided LiveData came from a Dao.
     */
    public static <T> void activateInstrumentationLiveData(LiveData<T> liveData)
    {
        final TestUtils.ThreadBlocker dummyBlocker = new TestUtils.ThreadBlocker();
        Observer<T> dummyObserver = new Observer<T>()
        {
            @Override
            public void onChanged(T t) {/* do nothing */}
        };
        observeLiveDataOnMainThread(liveData, dummyObserver, dummyBlocker);
    }
    
    public static <T> void activateLocalLiveData(LiveData<T> liveData)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            public void onChanged(T t) {/* do nothing */}
        });
    }
}
