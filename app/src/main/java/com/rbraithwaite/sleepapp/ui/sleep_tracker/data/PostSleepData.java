package com.rbraithwaite.sleepapp.ui.sleep_tracker.data;

public class PostSleepData
{
//*********************************************************
// public constants
//*********************************************************

    public final float rating;
    
//*********************************************************
// constructors
//*********************************************************

    public PostSleepData(float rating)
    {
        this.rating = rating;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        PostSleepData that = (PostSleepData) o;
        
        return Float.compare(that.rating, rating) == 0;
    }
}
