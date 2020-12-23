package com.rbraithwaite.sleepapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.data.convert.DateConverter;

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

    private MutableLiveData<Date> mCurrentSession;
    private Executor mExecutor;
    private Context mContext;
    
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
        // REFACTOR [20-12-22 1:50AM] -- replace various context args in methods w/ mContext.
        mContext = context;
        mExecutor = executor;
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * Executes asynchronously
     */
    public void setCurrentSession(final Context context, final Date startTime)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                SharedPreferences prefs = getSharedPrefs(context);
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
    
    /**
     * Returns a null LiveData immediately, then updates asynchronously (replicates Room behaviour)
     */
    public LiveData<Date> getCurrentSession(final Context context)
    {
        mCurrentSession = new MutableLiveData<>(null);
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Date result = null;
                // SMELL [20-11-14 4:58PM] -- race condition between here and setCurrentSession?
                long currentSessionStartDate =
                        getSharedPrefs(context).getLong(CURRENT_SESSION_KEY, NULL_VAL);
                if (currentSessionStartDate != NULL_VAL) {
                    result = DateConverter.convertDateFromMillis(currentSessionStartDate);
                }
                mCurrentSession.postValue(result);
            }
        });
        return mCurrentSession;
    }
    
    public LiveData<Long> getWakeTimeGoal()
    {
        // REFACTOR [20-12-22 1:53AM] -- this duplicates logic in getCurrentSession()
        // REFACTOR [20-12-22 1:53AM] -- I don't think I need to be keeping a private reference
        //  to the
        //  LiveData, same goes for getCurrentSession() - it's ref'd in the executor, that should
        //  keep
        //  it from being gc'd
        mWakeTimeGoal = new MutableLiveData<>(null);
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                // SMELL [20-12-22 1:55AM] -- race condition between here and setWakeTimeGoal?
                long wakeTimeGoal =
                        getSharedPrefs(mContext).getLong(WAKE_TIME_GOAL_KEY, NULL_VAL);
                
                if (wakeTimeGoal != NULL_VAL) {
                    mWakeTimeGoal.postValue(wakeTimeGoal);
                } else {
                    mWakeTimeGoal.postValue(null);
                }
            }
        });
        return mWakeTimeGoal;
    }
    
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
                    mWakeTimeGoal.postValue(wakeTimeGoalMillis);
                }
            }
        });
    }


//*********************************************************
// private methods
//*********************************************************

    private SharedPreferences getSharedPrefs(Context context)
    {
        return context.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
    }
}
