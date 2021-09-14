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
package com.rbraithwaite.sleeptarget.ui.common.interruptions;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;

public class ConvertInterruption
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertInterruption() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static InterruptionListItem toListItem(Interruption interruption)
    {
        return new InterruptionListItem(
                interruption.getId(),
                InterruptionFormatting.formatListItemStart(interruption.getStart()),
                InterruptionFormatting.formatDuration(interruption.getDurationMillis()),
                InterruptionFormatting.formatListItemReason(interruption.getReason()),
                false);
    }
    
    public static InterruptionListItem toListItem(Interruption interruption, SleepSession parent)
    {
        // OPTIMIZE [21-09-12 8:23PM] -- It's not great constantly recalculating whether an
        //  Interruption is out of its parent's bounds - the parent sleep session should keep
        //  a cache of its oob interruptions.
        
        if (interruption.isOutsideBoundsOf(parent).either()) {
            return new InterruptionListItem(
                    interruption.getId(),
                    InterruptionFormatting.formatListItemStart(interruption.getStart()),
                    InterruptionFormatting.formatOutOfBoundsDuration(
                            interruption.getDurationMillisInBounds(parent)),
                    InterruptionFormatting.formatListItemReason(interruption.getReason()),
                    true);
        } else {
            return toListItem(interruption);
        }
    }
}
