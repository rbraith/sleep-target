package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import com.rbraithwaite.sleepapp.core.models.Mood;

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
        Mood testMood = new Mood(Mood.Type.MOOD_1);
        
        MoodUiData uiData = ConvertMood.toUiData(testMood);
        
        assertThat(uiData.type, is(equalTo(MoodUiData.Type.MOOD_1)));
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
        MoodUiData uiData = new MoodUiData(MoodUiData.Type.MOOD_1);
        
        Mood mood = ConvertMood.fromUiData(uiData);
        
        assertThat(mood.getType(), is(equalTo(Mood.Type.MOOD_1)));
    }
}
