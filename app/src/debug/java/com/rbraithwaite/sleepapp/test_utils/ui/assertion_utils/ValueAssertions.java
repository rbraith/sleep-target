package com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils;

import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;

public class ValueAssertions<T>
{
//*********************************************************
// private properties
//*********************************************************

    private T mValue;
    
//*********************************************************
// constructors
//*********************************************************

    public ValueAssertions(T value)
    {
        mValue = value;
    }
    
//*********************************************************
// api
//*********************************************************

    public void matches(Matcher<T> matcher)
    {
        assertThat(mValue, matcher);
    }
}
