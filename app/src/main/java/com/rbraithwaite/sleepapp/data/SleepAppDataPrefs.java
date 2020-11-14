package com.rbraithwaite.sleepapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;

import java.util.Date;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SleepAppDataPrefs
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Date> mCurrentSession;
    private Executor mExecutor;

//*********************************************************
// private constants
//*********************************************************

    private static final String CURRENT_SESSION_KEY = "current sleep session";
    private static final long NULL_VAL = -1L;

//*********************************************************
// public constants
//*********************************************************

    // made these public to allow tests to reset the shared prefs
    // TODO not ideal, find a better solution
    public static final String PREFS_FILE_KEY = "com.rbraithwaite.sleepapp.PREFS_FILE_KEY";
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepAppDataPrefs(Executor executor)
    {
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

    
//*********************************************************
// private methods
//*********************************************************

    private SharedPreferences getSharedPrefs(Context context)
    {
        return context.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
    }
}
