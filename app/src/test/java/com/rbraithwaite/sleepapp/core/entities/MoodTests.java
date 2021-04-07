package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.Mood;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class MoodTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void fromIndex_returnsNullOnNullInput()
    {
        assertThat(Mood.fromIndex(null), is(nullValue()));
    }
    
    @Test
    public void fromIndex_createsMoodWithCorrectType()
    {
        assertThat(Mood.fromIndex(0).getType(), is(equalTo(Mood.Type.MOOD_1)));
    }
    
    @Test
    public void toIndex_reflects_fromIndex()
    {
        int expected = 2;
        assertThat(Mood.fromIndex(expected).toIndex(), is(equalTo(expected)));
    }
}
