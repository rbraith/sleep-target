package com.rbraithwaite.sleepapp.ui.session_archive.data;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import java.util.ArrayList;
import java.util.List;

// ui pojo, to be converted within a viewmodel
public class SessionArchiveListItem
{
//*********************************************************
// public properties
//*********************************************************

    public String startTime;
    public String endTime;
    public String sessionDuration;
    public boolean hasAdditionalComments;
    public MoodUiData mood;
    public List<String> tags;
    public float rating;

//*********************************************************
// api
//*********************************************************

    public static SessionArchiveListItem create(
            String startTime,
            String endTime,
            String sessionDuration,
            boolean hasAdditionalComments,
            MoodUiData mood,
            List<String> tags,
            float rating)
    {
        SessionArchiveListItem listItem = new SessionArchiveListItem();
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
