package com.rbraithwaite.sleepapp.core.models;

import java.util.Arrays;

public class Mood
{
//*********************************************************
// private properties
//*********************************************************

    private Type type;

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

    public Mood(Type type)
    {
        this.type = type;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return type.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Mood mood = (Mood) o;
        
        return type == mood.type;
    }
    
//*********************************************************
// api
//*********************************************************

    public static Mood fromIndex(Integer typeIndex)
    {
        // TODO [21-04-3 1:52AM] -- handle out of bounds indices (return null?)
        if (typeIndex == null) {
            return null;
        }
        return new Mood(Type.values()[typeIndex]);
    }
    
    public Type getType()
    {
        return type;
    }
    
    /**
     * Returns the index of the Type of this mood.
     */
    public int toIndex()
    {
        return Arrays.asList(Type.values()).indexOf(type);
    }
}
