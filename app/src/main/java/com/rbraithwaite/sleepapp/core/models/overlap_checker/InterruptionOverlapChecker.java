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

package com.rbraithwaite.sleepapp.core.models.overlap_checker;

import com.rbraithwaite.sleepapp.core.models.Interruption;

import java.util.Date;
import java.util.List;

public class InterruptionOverlapChecker
        extends SessionOverlapChecker<Interruption>
{
//*********************************************************
// private constants
//*********************************************************

    private final List<Interruption> mInterruptions;
    
//*********************************************************
// constructors
//*********************************************************

    public InterruptionOverlapChecker(List<Interruption> interruptions)
    {
        mInterruptions = interruptions;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected Interruption getFirstStartingBefore(Date date)
    {
        // REFACTOR [21-07-31 9:20PM] -- this would be nicer if I could guarantee the elems
        //  were sorted by start.
        long dateMillis = date.getTime();
        if (mInterruptions == null || mInterruptions.isEmpty()) {
            return null;
        }
        Interruption firstStartingBefore = mInterruptions.get(0);
        for (int i = 1; i < mInterruptions.size(); i++) {
            Interruption interruption = mInterruptions.get(i);
            long interruptionStartMillis = interruption.getStart().getTime();
            long firstStartingBeforeMillis = firstStartingBefore.getStart().getTime();
            if (interruptionStartMillis <= dateMillis &&
                interruptionStartMillis > firstStartingBeforeMillis) {
                firstStartingBefore = interruption;
            }
        }
        return firstStartingBefore;
    }
    
    @Override
    protected Interruption getFirstStartingAfter(Date date)
    {
        // REFACTOR [21-07-31 9:20PM] -- this would be nicer if I could guarantee the elems
        //  were sorted by start.
        long dateMillis = date.getTime();
        if (mInterruptions == null || mInterruptions.isEmpty()) {
            return null;
        }
        Interruption firstStartingAfter = mInterruptions.get(0);
        for (int i = 1; i < mInterruptions.size(); i++) {
            Interruption interruption = mInterruptions.get(i);
            long interruptionStartMillis = interruption.getStart().getTime();
            long firstStartingAfterMillis = firstStartingAfter.getStart().getTime();
            if (interruptionStartMillis >= dateMillis &&
                interruptionStartMillis < firstStartingAfterMillis) {
                firstStartingAfter = interruption;
            }
        }
        return firstStartingAfter;
    }
    
    @Override
    protected boolean isDistinct(
            Interruption session, Interruption possibleOverlap)
    {
        return session.getId() != possibleOverlap.getId();
    }
}
