package com.rbraithwaite.sleepapp.data.prefs;

import java.util.Date;
import java.util.List;

public class CurrentSessionPrefsData
{
//*********************************************************
// public properties
//*********************************************************

    public List<Integer> selectedTagIds;

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_MOOD = -1;
    public final Date start;
    public final String additionalComments;
    public final int moodIndex;

//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionPrefsData(
            Date start,
            String additionalComments,
            int moodIndex,
            List<Integer> selectedTagIds)
    {
        this.start = start;
        this.additionalComments = additionalComments;
        this.moodIndex = moodIndex;
        this.selectedTagIds = selectedTagIds;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + (start == null ? 0 : start.hashCode());
        hash = prime * hash + (additionalComments == null ? 0 : additionalComments.hashCode());
        hash = prime * hash + moodIndex;
        hash = prime * hash + (selectedTagIds == null ? 0 : selectedTagIds.hashCode());
        
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CurrentSessionPrefsData that = (CurrentSessionPrefsData) o;
        return ((start == null && that.start == null) || start.equals(that.start)) &&
               ((additionalComments == null && that.additionalComments == null) ||
                additionalComments.equals(that.additionalComments)) &&
               moodIndex == that.moodIndex &&
               ((selectedTagIds == null && that.selectedTagIds == null) ||
                selectedTagIds.equals(that.selectedTagIds));
    }
}
