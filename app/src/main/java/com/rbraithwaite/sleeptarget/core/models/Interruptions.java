/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.core.models;

import com.rbraithwaite.sleeptarget.core.models.overlap_checker.InterruptionOverlapChecker;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


// HACK [21-09-28 8:09PM] -- This shouldn't need to implement serializable.

/**
 * Behaviour relating to an {@link Interruption} collection.
 */
public class Interruptions
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private List<Interruption> mInterruptions;
    private Updates mUpdates;
    private AddedIdGenerator mAddedIdGenerator = new AddedIdGenerator();
    private int ID_NOT_FOUND = -1; // used with getIndexOfIdInList

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

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
    
    public Interruptions()
    {
        this(new ArrayList<>());
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
    
    public long getTotalDurationInBounds(SleepSession sleepSession)
    {
        return mInterruptions.stream()
                .mapToLong(interruption -> interruption.getDurationMillisInBounds(sleepSession))
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
        
        int i = getIndexOfIdInList(interruptionId, mInterruptions);
        if (i != ID_NOT_FOUND) {
            // if the interruption was newly added, simply remove it from that list instead of
            // appending it to the deleted list.
            List<Interruption> added = getUpdates().added;
            int addedIndex = getIndexOfIdInList(interruptionId, added);
            if (addedIndex != ID_NOT_FOUND) {
                mInterruptions.remove(i);
                added.remove(addedIndex);
            } else {
                getUpdates().deleted.add(mInterruptions.remove(i));
            }
        }
    }
    
    public void update(Interruption updated)
    {
        if (!mInterruptions.isEmpty()) {
            int id = updated.getId();
            int index = getIndexOfIdInList(id, mInterruptions);
            if (index != ID_NOT_FOUND) {
                mInterruptions.set(index, updated);
                // update Updates cache
                List<Interruption> updatedList = getUpdates().updated;
                index = getIndexOfIdInList(id, updatedList);
                if (index == ID_NOT_FOUND) {
                    updatedList.add(updated);
                } else {
                    updatedList.set(index, updated);
                }
            }
        }
    }
    
    // TEST NEEDED [21-08-4 4:22PM]
    public void add(Interruption interruption)
    {
        // HACK [21-09-14 3:36PM] -- side effect (intentional to allow clients to delete the
        //  interruption they just added).
        interruption.setId(mAddedIdGenerator.getNextId());
        
        mInterruptions.add(interruption);
        getUpdates().added.add(interruption);
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
    
    /**
     * Check whether the provided interruption overlaps with any existing interruptions. "Exclusive"
     * means that an existing interruption with the same id as the one to check will be skipped
     * (this is useful for interruptions which have been updated).
     *
     * @param interruptionToCheck The interruption... to check.
     *
     * @return The first overlapping interruption found, or null if no interruptions were
     * overlapping.
     */
    public Interruption checkForOverlapExclusive(Interruption interruptionToCheck)
    {
        // REFACTOR [21-07-31 9:31PM] -- This should be injected.
        InterruptionOverlapChecker overlapChecker = new InterruptionOverlapChecker(mInterruptions);
        return overlapChecker.checkForOverlapExclusive(interruptionToCheck);
    }

//*********************************************************
// private methods
//*********************************************************

    private int getIndexOfIdInList(int id, List<Interruption> interruptions)
    {
        for (int i = 0; i < interruptions.size(); i++) {
            if (interruptions.get(i).getId() == id) {
                return i;
            }
        }
        return ID_NOT_FOUND;
    }
    
    private Updates getUpdates()
    {
        mUpdates = CommonUtils.lazyInit(mUpdates, Updates::new);
        return mUpdates;
    }


//*********************************************************
// private helpers
//*********************************************************

    
    /**
     * Generate placeholder unique ids for newly added interruptions. These ids decrement down from
     * 0.
     */
    private static class AddedIdGenerator
            implements Serializable
    {
        private int mNextId = 1;
        public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;
        
        public int getNextId()
        {
            mNextId--;
            return mNextId;
        }
    }
}
//!en
