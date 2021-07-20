package com.rbraithwaite.sleepapp.core.models;

import java.util.List;


/**
 * Behaviour relating to an {@link Interruption} collection.
 */
public class Interruptions
{
//*********************************************************
// private properties
//*********************************************************

    private List<Interruption> mInterruptions;
    
//*********************************************************
// constructors
//*********************************************************

    public Interruptions(List<Interruption> interruptions)
    {
        mInterruptions = interruptions;
    }
    
    
//*********************************************************
// api
//*********************************************************

    public long getTotalDuration()
    {
        return mInterruptions.stream()
                .mapToLong(Interruption::getDurationMillis)
                .sum();
    }
}
