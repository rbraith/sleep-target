package com.rbraithwaite.sleepapp.data;

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

//*********************************************************
// private constants
//*********************************************************

    private static final String CURRENT_SESSION_KEY = "current sleep session";
    
    private static final long NULL_VAL = -1L;
    
    private static final String WAKE_TIME_GOAL_KEY = "wake time goal";


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
    // REFACTOR [21-01-6 12:59AM] -- This does not fully mirror Room behaviour - to do better, I
    //  should not post any value until the LiveData has an observer (override onActive()). This
    //  isn't a big deal, it's just that it's not as lazy as it could be.
    // REFACTOR [21-01-14 12:13AM] -- this duplicates logic in getWakeTimeGoal.
    public LiveData<Date> getCurrentSession()
    {
        final MediatorLiveData<Date> currentSessionLiveData = new MediatorLiveData<>();
        
        currentSessionLiveData.addSource(getCurrentSessionMutable(), new Observer<Date>()
        {
            @Override
            public void onChanged(Date currentSession)
            {
                currentSessionLiveData.setValue(currentSession);
            }
        });
        
        return currentSessionLiveData;
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
                SharedPreferences prefs = getSharedPrefs(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                
                editor.putLong(CURRENT_SESSION_KEY,
                               (startTime == null) ? NULL_VAL : startTime.getTime());
                editor.commit();
                if (mCurrentSession != null) {
                    mCurrentSession.postValue(startTime);
                }
            }
        });
    }
    
    public LiveData<Long> getWakeTimeGoal()
    {
        final MediatorLiveData<Long> wakeTimeGoalLiveData = new MediatorLiveData<>();
        
        wakeTimeGoalLiveData.addSource(getWakeTimeGoalMutable(), new Observer<Long>()
        {
            @Override
            public void onChanged(Long wakeTimeGoal)
            {
                wakeTimeGoalLiveData.setValue(wakeTimeGoal);
            }
        });
        
        return wakeTimeGoalLiveData;
    }
    
    // REFACTOR [21-01-11 9:59PM]
    //  It's not ideal to be persisting to the prefs every time setWakeTimeGoal()
    //  is called, although this would require a significant design change - would I need
    //  something like commitWakeTimeGoal()?
    public void setWakeTimeGoal(final long wakeTimeGoalMillis)
    {
        // REFACTOR [20-12-22 1:47AM] -- this logic duplicates setCurrentSession()
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                SharedPreferences prefs = getSharedPrefs(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                
                editor.putLong(WAKE_TIME_GOAL_KEY, wakeTimeGoalMillis);
                editor.commit();
                if (mWakeTimeGoal != null) {
                    Long val = wakeTimeGoalMillis == NULL_VAL ? null : wakeTimeGoalMillis;
                    mWakeTimeGoal.postValue(val);
                }
            }
        });
    }
    
    public synchronized void clearWakeTimeGoal()
    {
        setWakeTimeGoal(NULL_VAL);
    }


//*********************************************************
// private methods
//*********************************************************

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
                            getSharedPrefs(mContext).getLong(WAKE_TIME_GOAL_KEY, NULL_VAL);
                    mWakeTimeGoal.postValue(wakeTimeGoal == NULL_VAL ? null : wakeTimeGoal);
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
                            getSharedPrefs(mContext).getLong(CURRENT_SESSION_KEY, NULL_VAL);
                    
                    mCurrentSession.postValue(
                            currentSessionStartDate == NULL_VAL ?
                                    null :
                                    DateConverter.convertDateFromMillis(currentSessionStartDate));
                }
            });
        }
        return mCurrentSession;
    }
    
    private SharedPreferences getSharedPrefs(Context context)
    {
        return context.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
    }
}
