package com.rbraithwaite.sleepapp.test_utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.prefs.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

public class TestUtils
{
//*********************************************************
// public helpers
//*********************************************************

    public interface SyncedActivityAction<T extends Activity>
    {
        void perform(T activity);
    }
    
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
        
        public static GregorianCalendar getCalendar()
        {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(getDate());
            return calendar;
        }
        
        public static SleepSessionEntity getSleepSessionEntity()
        {
            SleepSessionEntity sleepSessionEntity = new SleepSessionEntity();
            
            GregorianCalendar cal = getCalendar();
            sleepSessionEntity.startTime = cal.getTime();
            sleepSessionEntity.duration = getDurationMillis();
            
            cal.add(Calendar.MILLISECOND, (int) sleepSessionEntity.duration);
            sleepSessionEntity.endTime = cal.getTime();
            
            sleepSessionEntity.additionalComments = "lol!";
            
            return sleepSessionEntity;
        }
        
        public static SleepSession getSleepSession()
        {
            return new SleepSession(
                    getDate(),
                    getDurationMillis(),
                    "test!",
                    getMood());
        }
        
        public static Mood getMood()
        {
            return Mood.fromIndex(1);
        }
        
        public static Date getWakeTimeGoal()
        {
            GregorianCalendar calendar = getCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 45);
            return calendar.getTime();
        }
        
        public static long getDurationMillis() {return 5000L;}
        
        public static SleepDurationGoal getSleepDurationGoalModel()
        {
            return new SleepDurationGoal(1234);
        }
        
        public static DateRange getDateRange()
        {
            return DateRange.asWeekOf(getDate());
        }
        
        public static WakeTimeGoalEntity getWakeTimeGoalEntity()
        {
            WakeTimeGoalEntity entity = new WakeTimeGoalEntity();
            entity.editTime = getDate();
            entity.wakeTimeGoal = 12345;
            return entity;
        }
        
        public static WakeTimeGoal getWakeTimeGoalModel()
        {
            return new WakeTimeGoal(TestUtils.ArbitraryData.getDate(), 12345);
        }
        
        public static SleepDurationGoalEntity getSleepDurationGoalEntity()
        {
            SleepDurationGoalEntity entity = new SleepDurationGoalEntity();
            entity.editTime = getDate();
            entity.goalMinutes = 1234;
            return entity;
        }
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
// constructors
//*********************************************************

    private TestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static <T extends Activity> void performSyncedActivityAction(
            ActivityScenario<T> scenario,
            final SyncedActivityAction<T> syncedActivityAction)
    {
        final ThreadBlocker blocker = new ThreadBlocker();
        scenario.onActivity(new ActivityScenario.ActivityAction<T>()
        {
            @Override
            public void perform(T activity)
            {
                syncedActivityAction.perform(activity);
                blocker.unblockThread();
            }
        });
        blocker.blockThread();
    }
    
    /**
     * @return false if the activity was already at the desiredOrientation
     */
    public static <T extends Activity> boolean rotateActivitySynced(
            ActivityScenario<T> scenario,
            final int desiredOrientation)
    {
        final TestUtils.DoubleRef<Boolean> inDesiredOrientation = new TestUtils.DoubleRef<>(false);
        TestUtils.SyncedActivityAction<T> checkInDesiredOrientation =
                new TestUtils.SyncedActivityAction<T>()
                {
                    @Override
                    public void perform(T activity)
                    {
                        inDesiredOrientation.ref =
                                (activity.getResources().getConfiguration().orientation ==
                                 desiredOrientation);
                    }
                };
        
        // check if already oriented
        performSyncedActivityAction(scenario, checkInDesiredOrientation);
        // BUG [20-12-8 9:37PM] -- oops this should return false.
        if (inDesiredOrientation.ref) {return true;}
        
        // perform orientation change
        performSyncedActivityAction(scenario, new SyncedActivityAction<T>()
        {
            @Override
            public void perform(T activity)
            {
                activity.setRequestedOrientation(desiredOrientation);
            }
        });
        // wait for orientation change
        while (!inDesiredOrientation.ref &&
               !scenario.getState().isAtLeast(Lifecycle.State.RESUMED)) {
            TestUtils.performSyncedActivityAction(scenario, checkInDesiredOrientation);
        }
        return true;
    }
    
    
    public static void runOnMainSync(Runnable runner)
    {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runner);
    }
    
    public static Context getContext()
    {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
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
