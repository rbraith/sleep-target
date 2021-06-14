package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

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
        
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel(mood);
        
        LiveData<MoodUiData> moodLiveData = viewModel.getMood();
        TestUtils.activateLocalLiveData(moodLiveData);
        
        assertThat(moodLiveData.getValue(), is(equalTo(mood)));
    }
    
    @Test
    public void getMood_reflects_setMood()
    {
        MoodUiData mood = new MoodUiData(0);
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel(mood);
        
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
        MoodSelectorViewModel viewModel = new MoodSelectorViewModel(mood);
        
        assertThat(viewModel.isMoodSet(), is(true));
        
        viewModel.clearSelectedMood();
        
        assertThat(viewModel.isMoodSet(), is(false));
    }
}
