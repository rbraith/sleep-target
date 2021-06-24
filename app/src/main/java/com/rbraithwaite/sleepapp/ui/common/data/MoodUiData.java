package com.rbraithwaite.sleepapp.ui.common.data;

public class MoodUiData
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMoodIndex;

//*********************************************************
// constructors
//*********************************************************

    // SMELL [21-06-11 10:37PM] -- It's weird that MoodUiData right now is basically just a wrapper
    //  around the index. I kept it this way for several reasons:
    //  - not breaking interfaces which are using MoodUiData
    //  - flexibility for future expansion of mood data/behaviour?
    //  Idk, like I said its weird - revisit this later.
    public MoodUiData(Integer moodIndex)
    {
        mMoodIndex = moodIndex;
    }
    
    public MoodUiData()
    {
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + (mMoodIndex == null ? 0 : mMoodIndex.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        MoodUiData entity = (MoodUiData) o;
        return ((mMoodIndex == null && entity.mMoodIndex == null) ||
                (mMoodIndex != null && mMoodIndex.equals(entity.mMoodIndex)));
    }


//*********************************************************
// api
//*********************************************************


    /**
     * @return if isSet() is false, this returns null
     */
    public Integer asIndex()
    {
        return mMoodIndex;
    }
    
    // SMELL [21-06-13 2:47AM] -- Is it weird for this behaviour to be attached to what is
    //  ostensibly a data class?
    public boolean isSet()
    {
        return mMoodIndex != null;
    }
}
