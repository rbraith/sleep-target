package com.rbraithwaite.sleepapp.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;

import java.util.Date;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SleepAppDataPrefs
{
//*********************************************************
// private properties
//*********************************************************

    private Executor mExecutor;
    private Context mContext;
    
    private MutableLiveData<Date> mCurrentSession;
    private MutableLiveData<Long> mWakeTimeGoal;
    private MutableLiveData<Integer> mSleepDurationGoal;

//*********************************************************
// private constants
//*********************************************************

    private static final long NULL_LONG_VAL = -1L;
    private static final int NULL_INT_VAL = -1;
    
    private static final String WAKE_TIME_GOAL_KEY = "wake time goal";
    private static final String CURRENT_SESSION_KEY = "current sleep session";
    private static final String SLEEP_DURATION_GOAL_KEY = "sleep duration goal";

//*********************************************************
// public constants
//*********************************************************

    // HACK [20-11-14 8:06PM] -- made this public to allow tests to reset the shared prefs
    //  not ideal, find a better solution.
    public static final String PREFS_FILE_KEY = "com.rbraithwaite.sleepapp.PREFS_FILE_KEY";


//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepAppDataPrefs(
            @ApplicationContext Context context,
            Executor executor)
    {
        mContext = context;
        mExecutor = executor;
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Returns a null LiveData immediately, then updates asynchronously (replicates Room behaviour)
     */
    public LiveData<Date> getCurrentSession()
    {
        return createTrackingMediator(getCurrentSessionMutable());
    }
    
    /**
     * Executes asynchronously
     */
    public void setCurrentSession(final Date startTime)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                commitLong(CURRENT_SESSION_KEY,
                           (startTime == null) ? NULL_LONG_VAL : startTime.getTime());
                if (mCurrentSession != null) {
                    mCurrentSession.postValue(startTime);
                }
            }
        });
    }
    
    public LiveData<Long> getWakeTimeGoal()
    {
        return createTrackingMediator(getWakeTimeGoalMutable());
    }
    
    // REFACTOR [21-01-11 9:59PM]
    //  It's not ideal to be persisting to the prefs every time setWakeTimeGoal()
    //  is called, although this would require a significant design change - would I need
    //  something like commitWakeTimeGoal()?
    public void setWakeTimeGoal(final long wakeTimeGoalMillis)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                // REFACTOR [21-02-6 2:01AM] -- change all this to behave like
                //  setSleepDurationGoal().
                //  This includes writing a new unit test for the new null arg behaviour.
                commitLong(WAKE_TIME_GOAL_KEY, wakeTimeGoalMillis);
                if (mWakeTimeGoal != null) {
                    Long val = wakeTimeGoalMillis == NULL_LONG_VAL ? null : wakeTimeGoalMillis;
                    mWakeTimeGoal.postValue(val);
                }
            }
        });
    }
    
    public synchronized void clearWakeTimeGoal()
    {
        setWakeTimeGoal(NULL_LONG_VAL);
    }
    
    public LiveData<Integer> getSleepDurationGoal()
    {
        // REFACTOR [21-01-29 3:24PM] -- should I just use Transformations.map() here? I think
        //  it does essentially the same thing as createTrackingMediator, but I don't need to
        //  transform the data at all here - it would be a 1:1 mapping.
        return createTrackingMediator(getSleepDurationGoalMutable());
    }
    
    /**
     * Setting to null clears the current goal.
     */
    public void setSleepDurationGoal(final Integer goalMinutes)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                commitInt(SLEEP_DURATION_GOAL_KEY,
                          (goalMinutes == null) ? NULL_INT_VAL : goalMinutes);
                if (mSleepDurationGoal != null) {
                    mSleepDurationGoal.postValue(goalMinutes);
                }
            }
        });
    }
    
    public void clearSleepDurationGoal()
    {
        setSleepDurationGoal(null);
    }


//*********************************************************
// private methods
//*********************************************************

    @SuppressLint("ApplySharedPref") // suppress commit() warning
    private void commitInt(String key, int value)
    {
        getSharedPrefs().edit().putInt(key, value).commit();
    }
    
    @SuppressLint("ApplySharedPref") // suppress commit() warning
    private void commitLong(String key, long value)
    {
        getSharedPrefs().edit().putLong(key, value).commit();
    }
    
    // REFACTOR [21-01-6 12:59AM] -- This does not fully mirror Room behaviour - to do better, I
    //  should not post any value until the LiveData has an observer (override onActive()). This
    //  isn't a big deal, it's just that it's not as lazy as it could be.
    // REFACTOR [21-01-29 3:22PM] -- extract this as a utility?
    private <T> MediatorLiveData<T> createTrackingMediator(LiveData<T> source)
    {
        final MediatorLiveData<T> mediator = new MediatorLiveData<>();
        
        mediator.addSource(source, new Observer<T>()
        {
            @Override
            public void onChanged(T value)
            {
                mediator.setValue(value);
            }
        });
        
        return mediator;
    }
    
    // REFACTOR [21-01-29 3:00PM] -- duplicates logic from getWakeTimeGoalMutable and
    //  getCurrentSessionMutable. I can't see a clean way to fix this, since I would need to
    //  parameterize both the prefs value retrieval behaviour and the postValue formatting
    //  behaviour, which would make calls to whatever interface I made really verbose.
    private MutableLiveData<Integer> getSleepDurationGoalMutable()
    {
        if (mSleepDurationGoal == null) {
            mSleepDurationGoal = new MutableLiveData<>();
            // asynchronously initialize the in-memory cache from the preferences
            mExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    int sleepDurationGoalMinutes =
                            getSharedPrefs().getInt(SLEEP_DURATION_GOAL_KEY, NULL_INT_VAL);
                    mSleepDurationGoal.postValue(
                            sleepDurationGoalMinutes == NULL_INT_VAL ? null :
                                    sleepDurationGoalMinutes);
                }
            });
        }
        return mSleepDurationGoal;
    }
    
    private MutableLiveData<Long> getWakeTimeGoalMutable()
    {
        if (mWakeTimeGoal == null) {
            mWakeTimeGoal = new MutableLiveData<>();
            // asynchronously initialize the in-memory cache from the preferences
            mExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    // REFACTOR [21-01-11 10:04PM] -- This should be getWakeTimeGoalPersisted.
                    long wakeTimeGoal =
                            getSharedPrefs().getLong(WAKE_TIME_GOAL_KEY, NULL_LONG_VAL);
                    mWakeTimeGoal.postValue(wakeTimeGoal == NULL_LONG_VAL ? null : wakeTimeGoal);
                }
            });
        }
        return mWakeTimeGoal;
    }
    
    // REFACTOR [21-01-13 11:59PM] -- duplicates logic from getWakeTimeGoalMutable.
    private MutableLiveData<Date> getCurrentSessionMutable()
    {
        if (mCurrentSession == null) {
            mCurrentSession = new MutableLiveData<>();
            mExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    // SMELL [20-11-14 4:58PM] -- race condition between here and setCurrentSession?
                    //  consider making some or all of the methods in this class 'synchronized'.
                    long currentSessionStartDate =
                            getSharedPrefs().getLong(CURRENT_SESSION_KEY, NULL_LONG_VAL);
                    
                    mCurrentSession.postValue(
                            currentSessionStartDate == NULL_LONG_VAL ?
                                    null :
                                    DateConverter.convertDateFromMillis(currentSessionStartDate));
                }
            });
        }
        return mCurrentSession;
    }
    
    private SharedPreferences getSharedPrefs()
    {
        return mContext.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
    }
}
