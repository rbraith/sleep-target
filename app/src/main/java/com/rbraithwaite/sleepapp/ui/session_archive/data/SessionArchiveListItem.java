package com.rbraithwaite.sleepapp.ui.session_archive.data;

import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodUiData;

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

//*********************************************************
// api
//*********************************************************

    public static SessionArchiveListItem create(
            String startTime,
            String endTime,
            String sessionDuration,
            boolean hasAdditionalComments,
            MoodUiData mood)
    {
        SessionArchiveListItem listItem = new SessionArchiveListItem();
        listItem.startTime = startTime;
        listItem.endTime = endTime;
        listItem.sessionDuration = sessionDuration;
        listItem.hasAdditionalComments = hasAdditionalComments;
        listItem.mood = mood;
        return listItem;
    }
}
