package com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils;


/**
 * Generic failed assertion exception.
 */
public class AssertionFailed
        extends RuntimeException
{
//*********************************************************
// constructors
//*********************************************************

    public AssertionFailed(String message)
    {
        super(message);
    }
}
