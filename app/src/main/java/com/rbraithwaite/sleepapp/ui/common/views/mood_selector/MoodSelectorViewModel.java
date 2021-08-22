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

package com.rbraithwaite.sleepapp.ui.common.views.mood_selector;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

public class MoodSelectorViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<MoodUiData> mMood;

//*********************************************************
// constructors
//*********************************************************

    public MoodSelectorViewModel(MoodUiData mood)
    {
        mMood = new MutableLiveData<>(mood);
    }
    
    public MoodSelectorViewModel()
    {
        this(null);
    }

//*********************************************************
// api
//*********************************************************

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
