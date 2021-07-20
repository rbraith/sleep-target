package com.rbraithwaite.sleepapp.data.prefs;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.utils.CommonUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CurrentSessionPrefs
{
//*********************************************************
// private properties
//*********************************************************

    private Prefs mPrefs;
    private Executor mExecutor;
    private MutableLiveData<CurrentSessionPrefsData> mCurrentSessionCache;

//*********************************************************
// private constants
//*********************************************************

    private static final String KEY_CURRENT_SESSION_EXISTS = "CurrentSessionExists";
    private static final String KEY_SESSION_START = "SessionStart";
    private static final String KEY_ADDITIONAL_COMMENTS = "Comments";
    private static final String KEY_MOOD = "Mood";
    private static final String KEY_SELECTED_TAGS = "SelectedTagIds";
    private static final String KEY_INTERRUPTIONS = "Interruptions";
    private static final String KEY_CURRENT_INTERRUPTION = "CurrentInterruption";
    private static final long DEFAULT_LONG = -1L;
    private static final int DEFAULT_INT = -1;
    private static final String DEFAULT_STRING = "";
    private static final Set<String> DEFAULT_STRING_SET = new HashSet<>();
    private final Object mCurrentSessionLock = new Object();
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentSessionPrefs(Prefs prefs, Executor executor)
    {
        mPrefs = prefs;
        mExecutor = executor;
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<CurrentSessionPrefsData> getCurrentSession()
    {
        return Transformations.map(
                getCurrentSessionCachePersisted(),
                data -> data);
    }
    
    public void setCurrentSession(CurrentSessionPrefsData currentSession)
    {
        mExecutor.execute(() -> {
            synchronized (mCurrentSessionLock) {
                mPrefs.edit()
                        .putBoolean(KEY_CURRENT_SESSION_EXISTS, true)
                        .putLong(KEY_SESSION_START, currentSession.start)
                        .putString(KEY_ADDITIONAL_COMMENTS, currentSession.additionalComments)
                        .putInt(KEY_MOOD, currentSession.moodIndex)
                        .putStringSet(KEY_SELECTED_TAGS, currentSession.selectedTagIds)
                        .putStringSet(KEY_INTERRUPTIONS, currentSession.interruptions)
                        .putString(KEY_CURRENT_INTERRUPTION, currentSession.currentInterruption)
                        .commit();
            }
        });
        // update the cache
        getCurrentSessionCacheLocal().postValue(currentSession);
    }
    
    public void clearCurrentSession()
    {
        mExecutor.execute(() -> {
            synchronized (mCurrentSessionLock) {
                mPrefs.edit().putBoolean(KEY_CURRENT_SESSION_EXISTS, false).commit();
            }
        });
        // clear the cache
        getCurrentSessionCacheLocal().postValue(CurrentSessionPrefsData.empty());
    }
    
//*********************************************************
// private methods
//*********************************************************

    private MutableLiveData<CurrentSessionPrefsData> getCurrentSessionCachePersisted()
    {
        mCurrentSessionCache = CommonUtils.lazyInit(mCurrentSessionCache, () -> {
            MutableLiveData<CurrentSessionPrefsData> liveData = new MutableLiveData<>();
            
            mExecutor.execute(() -> {
                synchronized (mCurrentSessionLock) {
                    CurrentSessionPrefsData data = doesCurrentSessionPrefsExist() ?
                            loadCurrentSessionPrefs() :
                            CurrentSessionPrefsData.empty();
                    liveData.postValue(data);
                }
            });
            
            return liveData;
        });
        return mCurrentSessionCache;
    }
    
    // SMELL [21-07-17 3:03PM] -- This only exists because I thought mCurrentSessionLock
    //  might cause a deadlock in setCurrentSession.
    
    /**
     * Alternative lazy-init getter for the cache, which lazy inits to an empty LiveData. This is
     * for cases where setCurrentSession is called before the cache has been created.
     */
    private MutableLiveData<CurrentSessionPrefsData> getCurrentSessionCacheLocal()
    {
        mCurrentSessionCache = CommonUtils.lazyInit(mCurrentSessionCache, MutableLiveData::new);
        return mCurrentSessionCache;
    }
    
    private boolean doesCurrentSessionPrefsExist()
    {
        return mPrefs.get().getBoolean(KEY_CURRENT_SESSION_EXISTS, false);
    }
    
    private CurrentSessionPrefsData loadCurrentSessionPrefs()
    {
        SharedPreferences prefs = mPrefs.get();
        
        return new CurrentSessionPrefsData(
                prefs.getLong(KEY_SESSION_START, DEFAULT_LONG),
                prefs.getString(KEY_ADDITIONAL_COMMENTS, DEFAULT_STRING),
                prefs.getInt(KEY_MOOD, DEFAULT_INT),
                prefs.getStringSet(KEY_SELECTED_TAGS, DEFAULT_STRING_SET),
                prefs.getStringSet(KEY_INTERRUPTIONS, DEFAULT_STRING_SET),
                prefs.getString(KEY_CURRENT_INTERRUPTION, DEFAULT_STRING));
    }
}
