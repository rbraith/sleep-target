package com.rbraithwaite.sleepapp.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.rbraithwaite.sleepapp.utils.CommonUtils;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class Prefs
{
//*********************************************************
// private properties
//*********************************************************

    private Context mContext;
    private SharedPreferences mPrefs;

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
    public Prefs(@ApplicationContext Context context)
    {
        mContext = context;
    }
    
//*********************************************************
// api
//*********************************************************

    public SharedPreferences get()
    {
        mPrefs = CommonUtils.lazyInit(mPrefs, () -> {
            return mContext.getSharedPreferences(PREFS_FILE_KEY, Context.MODE_PRIVATE);
        });
        return mPrefs;
    }
    
    public SharedPreferences.Editor edit()
    {
        return get().edit();
    }
}
