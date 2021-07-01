package com.rbraithwaite.sleepapp.ui.session_archive.data;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// ui pojo, to be converted within a viewmodel
public class SessionArchiveListItem
{
//*********************************************************
// public properties
//*********************************************************

    public int sleepSessionId;
    public String startTime;
    public String endTime;
    public String sessionDuration;
    public boolean hasAdditionalComments;
    public MoodUiData mood;
    public List<String> tags;
    public float rating;

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        return Objects.hash(startTime,
                            endTime,
                            sessionDuration,
                            hasAdditionalComments,
                            mood,
                            tags,
                            rating,
                            sleepSessionId);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SessionArchiveListItem that = (SessionArchiveListItem) o;
        return hasAdditionalComments == that.hasAdditionalComments &&
               Float.compare(that.rating, rating) == 0 &&
               startTime.equals(that.startTime) &&
               endTime.equals(that.endTime) &&
               sessionDuration.equals(that.sessionDuration) &&
               Objects.equals(mood, that.mood) &&
               Objects.equals(tags, that.tags) &&
               sleepSessionId == that.sleepSessionId;
    }
    
//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-06-29 10:17PM] -- why did I use a static factory here?
    public static SessionArchiveListItem create(
            int sleepSessionId,
            String startTime,
            String endTime,
            String sessionDuration,
            boolean hasAdditionalComments,
            MoodUiData mood,
            List<String> tags,
            float rating)
    {
        SessionArchiveListItem listItem = new SessionArchiveListItem();
        listItem.sleepSessionId = sleepSessionId;
        listItem.startTime = startTime;
        listItem.endTime = endTime;
        listItem.sessionDuration = sessionDuration;
        listItem.hasAdditionalComments = hasAdditionalComments;
        listItem.mood = mood;
        listItem.tags = tags == null ? new ArrayList<>() : tags;
        listItem.rating = rating;
        return listItem;
    }
}
