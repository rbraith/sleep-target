/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
