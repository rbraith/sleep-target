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

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MoodSelectorViewModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void constructorTest()
    {
        MoodUiData mood = new MoodUiData(2);
        
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel();
        viewModel.setMood(mood);
        
        LiveData<MoodUiData> moodLiveData = viewModel.getMood();
        TestUtils.activateLocalLiveData(moodLiveData);
        
        assertThat(moodLiveData.getValue(), is(equalTo(mood)));
    }
    
    @Test
    public void getMood_reflects_setMood()
    {
        MoodUiData mood = new MoodUiData(0);
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel();
        viewModel.setMood(mood);
        
        MoodUiData expected = new MoodUiData(1);
        // SUT
        viewModel.setMood(expected);
        
        LiveData<MoodUiData> moodLiveData = viewModel.getMood();
        TestUtils.activateLocalLiveData(moodLiveData);
        assertThat(moodLiveData.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void isMoodSet_test()
    {
        MoodUiData mood = new MoodUiData(0);
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel();
        viewModel.setMood(mood);
        
        assertThat(viewModel.isMoodSet(), is(true));
        
        viewModel.clearSelectedMood();
        
        assertThat(viewModel.isMoodSet(), is(false));
    }
}
