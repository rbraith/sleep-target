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

package com.rbraithwaite.sleeptarget.data.prefs;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CurrentSessionPrefsData
{
//*********************************************************
// public constants
//*********************************************************

    /**
     * Use this for the nullable string properties
     */
    public static final String NULL = "";
    
    public static final long NOT_STARTED = -1L;
    public static final String NO_COMMENTS = NULL;
    public static final int NO_MOOD = -1;
    public static final Set<String> NO_SELECTED_TAGS = new HashSet<>();
    public static final Set<String> NO_INTERRUPTIONS = NO_SELECTED_TAGS;
    public static final String NO_CURRENT_INTERRUPTION = NULL;
    /**
     * Millis from epoch
     */
    public final long start;
    /**
     * Nullable
     */
    public final String additionalComments;
    public final int moodIndex;
    /**
     * The integer ids as strings
     */
    public final Set<String> selectedTagIds;
    /**
     * json strings
     */
    public final Set<String> interruptions;
    /**
     * Nullable - json string
     */
    public final String currentInterruption;
    
//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionPrefsData(
            long start,
            String additionalComments,
            int moodIndex,
            Set<String> selectedTagIds,
            Set<String> interruptions,
            String currentInterruption)
    {
        this.start = start;
        this.additionalComments = additionalComments;
        this.moodIndex = moodIndex;
        this.selectedTagIds = selectedTagIds;
        this.interruptions = interruptions;
        this.currentInterruption = currentInterruption;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = (int) (start ^ (start >>> 32));
        result = 31 * result + (additionalComments != null ? additionalComments.hashCode() : 0);
        result = 31 * result + moodIndex;
        result = 31 * result + (selectedTagIds != null ? selectedTagIds.hashCode() : 0);
        result = 31 * result + (interruptions != null ? interruptions.hashCode() : 0);
        result = 31 * result + (currentInterruption != null ? currentInterruption.hashCode() : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        CurrentSessionPrefsData that = (CurrentSessionPrefsData) o;
        
        if (start != that.start) { return false; }
        if (moodIndex != that.moodIndex) { return false; }
        if (!Objects.equals(additionalComments, that.additionalComments)) { return false; }
        if (!Objects.equals(selectedTagIds, that.selectedTagIds)) { return false; }
        if (!Objects.equals(interruptions, that.interruptions)) { return false; }
        return Objects.equals(currentInterruption, that.currentInterruption);
    }
    
//*********************************************************
// api
//*********************************************************

    public static CurrentSessionPrefsData empty()
    {
        return new CurrentSessionPrefsData(
                NOT_STARTED,
                NO_COMMENTS,
                NO_MOOD,
                NO_SELECTED_TAGS,
                NO_INTERRUPTIONS,
                NO_CURRENT_INTERRUPTION);
    }
}
