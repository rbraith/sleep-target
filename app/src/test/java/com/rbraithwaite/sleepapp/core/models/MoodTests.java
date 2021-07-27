package com.rbraithwaite.sleepapp.core.models;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MoodTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toIndex_reflects_fromIndex()
    {
        int expected = 2;
        assertThat(Mood.fromIndex(expected).asIndex(), is(equalTo(expected)));
    }
}
