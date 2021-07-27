package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.utils.CommonUtils;

import java.util.ArrayList;
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
    
    private Updates mUpdates;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Updates
    {
        public List<Interruption> added = new ArrayList<>();
        public List<Interruption> updated = new ArrayList<>();
        public List<Interruption> deleted = new ArrayList<>();
    }
    
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
    
    public int getCount()
    {
        return mInterruptions.size();
    }
    
    public boolean isEmpty()
    {
        return mInterruptions == null || mInterruptions.isEmpty();
    }
    
    public List<Interruption> asList()
    {
        return mInterruptions;
    }
    
    public Interruption get(int interruptionId)
    {
        return mInterruptions.stream()
                .filter(interruption -> interruption.getId() == interruptionId)
                .findFirst()
                .orElse(null);
    }
    
    public void delete(int interruptionId)
    {
        if (mInterruptions.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < mInterruptions.size(); i++) {
            if (mInterruptions.get(i).getId() == interruptionId) {
                getUpdates().deleted.add(mInterruptions.remove(i));
                break;
            }
        }
    }
    
    public boolean hasUpdates()
    {
        return mUpdates != null;
    }
    
    public Updates consumeUpdates()
    {
        Updates temp = mUpdates;
        mUpdates = null;
        return temp;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private Updates getUpdates()
    {
        mUpdates = CommonUtils.lazyInit(mUpdates, Updates::new);
        return mUpdates;
    }
}
