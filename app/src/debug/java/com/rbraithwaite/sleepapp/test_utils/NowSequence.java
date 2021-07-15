package com.rbraithwaite.sleepapp.test_utils;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;
import java.util.List;

public class NowSequence
        extends TimeUtils
{
//*********************************************************
// private properties
//*********************************************************

    private int currentIndex = -1; // -1 so that the first call increments to 0
    private List<Date> mNows;
    private boolean mShouldRepeat;
    
//*********************************************************
// constructors
//*********************************************************

    public NowSequence(List<Date> nows, boolean shouldRepeat)
    {
        mNows = nows;
        mShouldRepeat = shouldRepeat;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Date getNow()
    {
        if (mNows == null || mNows.isEmpty() || currentIndex >= mNows.size()) {
            // REFACTOR [21-07-15 12:10AM] -- this should maybe throw an exception here instead.
            return null;
        }
        
        currentIndex++;
        if (currentIndex >= mNows.size() && mShouldRepeat) {
            currentIndex = 0;
        }
        
        return mNows.get(currentIndex);
    }
}
