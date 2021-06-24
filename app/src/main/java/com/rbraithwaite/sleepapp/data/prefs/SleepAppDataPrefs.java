package com.rbraithwaite.sleepapp.data.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.data.database.convert.ConvertDate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
    
    private MutableLiveData<CurrentSessionPrefsData> mCurrentSession;
    
    private SharedPreferences mSharedPrefs;
    
//*********************************************************
// private constants
//*********************************************************

    private static final long NULL_LONG_VAL = -1L;
    private static final int NULL_INT_VAL = -1;
    
    private static final String SESSION_START_KEY = "SessionStart";
    private static final String ADDITIONAL_COMMENTS_KEY = "Comments";
    private static final String MOOD_KEY = "Mood";
    private static final String SELECTED_TAGS_KEY = "SelectedTagIds";
    
    
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
    public LiveData<CurrentSessionPrefsData> getCurrentSession()
    {
        // REFACTOR [21-01-29 3:24PM] -- should I just use Transformations.map() here? I think
        //  it does essentially the same thing as createTrackingMediator, but I don't need to
        //  transform the data at all here - it would be a 1:1 mapping.
        return createTrackingMediator(getCurrentSessionMutable());
    }
    
    public void setCurrentSession(@NonNull final CurrentSessionPrefsData currentSession)
    {
        mExecutor.execute(() -> {
            synchronized (SleepAppDataPrefs.this) {
                commitCurrentSession(currentSession);
                
                // not using getCurrentSession() here so that the async postValue() call in
                // that method is not called after this one.
                if (mCurrentSession != null) {
                    mCurrentSession.postValue(currentSession);
                }
            }
        });
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void commitCurrentSession(CurrentSessionPrefsData currentSession)
    {
        // REFACTOR [21-04-2 10:24PM] -- prefs data should take the millis directly - move getTime()
        //  out of this class.
        commitLong(SESSION_START_KEY,
                   (currentSession.start == null) ? NULL_LONG_VAL : currentSession.start.getTime());
        
        commitString(ADDITIONAL_COMMENTS_KEY, currentSession.additionalComments);
        
        commitInt(MOOD_KEY,
                  currentSession.moodIndex == CurrentSessionPrefsData.NO_MOOD ? NULL_INT_VAL :
                          currentSession.moodIndex);
        
        commitSelectedTagIds(currentSession.selectedTagIds);
    }
    
    private Date retrieveStart()
    {
        long startMillis = getSharedPrefs().getLong(SESSION_START_KEY, NULL_LONG_VAL);
        return startMillis == NULL_LONG_VAL ? null : ConvertDate.fromMillis(startMillis);
    }
    
    private String retrieveAdditionalComments()
    {
        return getSharedPrefs().getString(ADDITIONAL_COMMENTS_KEY, null);
    }

    private int retrieveMoodIndex()
    {
        int moodIndex = getSharedPrefs().getInt(MOOD_KEY, NULL_INT_VAL);
        return moodIndex == NULL_INT_VAL ? CurrentSessionPrefsData.NO_MOOD : moodIndex;
    }
    
    private List<Integer> retrieveSelectedTagIds()
    {
        Set<String> storedTagIdData =
                getSharedPrefs().getStringSet(SELECTED_TAGS_KEY, new HashSet<>());
        return storedTagIdData.stream().map(Integer::valueOf).collect(Collectors.toList());
    }
    
    @SuppressLint("ApplySharedPref") // suppress commit() warning
    private void commitSelectedTagIds(List<Integer> selectedTagIds)
    {
        getSharedPrefs().edit()
                .putStringSet(
                        SELECTED_TAGS_KEY,
                        selectedTagIds.stream().map(Object::toString).collect(Collectors.toSet()))
                .commit();
    }
    
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
    
    @SuppressLint("ApplySharedPref") // suppress commit() warning
    private void commitString(String key, String value)
    {
        getSharedPrefs().edit().putString(key, value).commit();
    }
    
    // REFACTOR [21-01-6 12:59AM] -- This does not fully mirror Room behaviour - to do better, I
    //  should not post any value until the LiveData has an observer (override onActive()). This
    //  isn't a big deal, it's just that it's not as lazy as it could be.
    // REFACTOR [21-01-29 3:22PM] -- extract this as a utility?
    private <T> MediatorLiveData<T> createTrackingMediator(LiveData<T> source)
    {
        final MediatorLiveData<T> mediator = new MediatorLiveData<>();
        
        mediator.addSource(source, mediator::setValue);
        
        return mediator;
    }
    
    private MutableLiveData<CurrentSessionPrefsData> getCurrentSessionMutable()
    {
        if (mCurrentSession == null) {
            mCurrentSession = new MutableLiveData<>();
            mExecutor.execute(() -> {
                synchronized (SleepAppDataPrefs.this) {
                    mCurrentSession.postValue(new CurrentSessionPrefsData(
                            retrieveStart(),
                            retrieveAdditionalComments(),
                            retrieveMoodIndex(),
                            retrieveSelectedTagIds()));
                }
            });
        }
        return mCurrentSession;
    }
    
    private SharedPreferences getSharedPrefs()
    {
        if (mSharedPrefs == null) {
            mSharedPrefs = mContext.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
        }
        return mSharedPrefs;
    }
}
