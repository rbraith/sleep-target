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
package com.rbraithwaite.sleeptarget.ui.common.views.mood_selector;

import androidx.fragment.app.FragmentActivity;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;

public class MoodSelectorViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<MoodUiData> mMood;

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public MoodSelectorViewModel()
    {
        mMood = new MutableLiveData<>();
    }
    
//*********************************************************
// api
//*********************************************************

    public static MoodSelectorViewModel getInstanceFrom(FragmentActivity activity)
    {
        return new ViewModelProvider(activity).get(MoodSelectorViewModel.class);
    }

    public Boolean isMoodSet()
    {
        return mMood.getValue() != null;
    }
    
    public void clearSelectedMood()
    {
        mMood.setValue(null);
    }
    
    public LiveData<MoodUiData> getMood()
    {
        return mMood;
    }
    
    public void setMood(MoodUiData mood)
    {
        // HACK [21-06-14 1:31AM] -- This null check is a stop gap, a better solution would be that
        //  MoodUiData and Mood are never null, only unset. (Right now, null values are allowed
        //  in various places such as CurrentSession, SleepSession, etc).
        mMood.setValue(mood == null ? new MoodUiData() : mood);
    }
}
