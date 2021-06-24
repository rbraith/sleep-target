package com.rbraithwaite.sleepapp.core.models;

public class Mood
{
//*********************************************************
// private properties
//*********************************************************

    private Integer mMoodIndex;

//*********************************************************
// constructors
//*********************************************************

    // SMELL [21-06-11 11:03PM] -- Right now Mood is just a wrapper around the index. I left it
    //  this way in order to not break interfaces that use Mood, and to allow for possible
    //  future expansion of functionality. See also ui.common.data.MoodUiData.MoodUiData(int).
    public Mood(Integer moodIndex)
    {
        mMoodIndex = moodIndex;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return mMoodIndex.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Mood mood = (Mood) o;
        
        return (mMoodIndex == null && mood.mMoodIndex == null) ||
               mMoodIndex.equals(mood.mMoodIndex);
    }

//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-06-11 11:07PM] -- legacy method - replace this with Mood(moodIndex).
    @Deprecated
    public static Mood fromIndex(Integer moodIndex)
    {
        return new Mood(moodIndex);
    }
    
    /**
     * Returns the index of the Type of this mood.
     */
    public Integer asIndex()
    {
        return mMoodIndex;
    }
}
