package com.rbraithwaite.sleepapp.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.data.database.convert.ConvertDate;

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

//*********************************************************
// private constants
//*********************************************************

    private static final long NULL_LONG_VAL = -1L;
    private static final int NULL_INT_VAL = -1;
    
    private static final String CURRENT_SESSION_KEY = "current sleep session";

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
        // REFACTOR [21-01-29 3:24PM] -- should I just use Transformations.map() here? I think
        //  it does essentially the same thing as createTrackingMediator, but I don't need to
        //  transform the data at all here - it would be a 1:1 mapping.
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
                // REFACTOR [21-01-11 9:59PM]
                //  It's not ideal to be persisting to the prefs every time setCurrentSession()
                //  is called, although this would require a significant design change - would I
                //  need
                //  something like commitCurrentSession()?
                commitLong(CURRENT_SESSION_KEY,
                           (startTime == null) ? NULL_LONG_VAL : startTime.getTime());
                if (mCurrentSession != null) {
                    mCurrentSession.postValue(startTime);
                }
            }
        });
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
                                    ConvertDate.fromMillis(currentSessionStartDate));
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
