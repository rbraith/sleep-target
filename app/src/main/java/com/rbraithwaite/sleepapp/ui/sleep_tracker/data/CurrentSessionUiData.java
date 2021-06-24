package com.rbraithwaite.sleepapp.ui.sleep_tracker.data;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import java.util.List;

public class CurrentSessionUiData
{
//*********************************************************
// public properties
//*********************************************************

    public String start;
    public String end;
    public String duration;
    public MoodUiData mood;
    public String additionalComments;
    public List<Integer> tagIds;

//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionUiData(
            String start,
            String end,
            String duration,
            MoodUiData mood,
            String additionalComments,
            List<Integer> tagIds)
    {
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.mood = mood;
        this.additionalComments = additionalComments;
        this.tagIds = tagIds;
    }
}
