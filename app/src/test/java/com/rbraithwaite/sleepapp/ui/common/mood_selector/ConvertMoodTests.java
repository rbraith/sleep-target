package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertMoodTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toUiData_createsCorrectObj()
    {
        Mood testMood = new Mood(0);
        
        MoodUiData uiData = ConvertMood.toUiData(testMood);
        
        assertThat(uiData.asIndex(), is(equalTo(testMood.asIndex())));
    }
    
    @Test
    public void toUiData_returnsNullOnNullInput()
    {
        assertThat(ConvertMood.toUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_returnsNullOnNullInput()
    {
        assertThat(ConvertMood.fromUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_createsCorrectObj()
    {
        MoodUiData uiData = new MoodUiData(0);
        
        Mood mood = ConvertMood.fromUiData(uiData);
        
        assertThat(mood.asIndex(), is(equalTo(uiData.asIndex())));
    }
}
