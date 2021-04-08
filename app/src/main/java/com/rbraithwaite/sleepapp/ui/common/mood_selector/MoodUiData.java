package com.rbraithwaite.sleepapp.ui.common.mood_selector;

public class MoodUiData
{
//*********************************************************
// public properties
//*********************************************************

    public Type type;

//*********************************************************
// public helpers
//*********************************************************

    public enum Type
    {
        MOOD_1,
        MOOD_2,
        MOOD_3,
        MOOD_4,
        MOOD_5,
        MOOD_6,
        MOOD_7,
        MOOD_8,
        MOOD_9,
        MOOD_10,
        MOOD_11,
        MOOD_12
    }

//*********************************************************
// constructors
//*********************************************************

    public MoodUiData(Type type)
    {
        this.type = type;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + type.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        MoodUiData moodUiData = (MoodUiData) o;
        return type == moodUiData.type;
    }
}
