package com.rbraithwaite.sleepapp.ui.session_archive.data;

// ui pojo, to be converted within a viewmodel
public class SessionArchiveListItem
{
//*********************************************************
// public properties
//*********************************************************

    public String startTime;
    public String endTime;
    public String sessionDuration;

//*********************************************************
// api
//*********************************************************

    public static SessionArchiveListItem create(
            String startTime,
            String endTime,
            String sessionDuration)
    {
        SessionArchiveListItem listItem = new SessionArchiveListItem();
        listItem.startTime = startTime;
        listItem.endTime = endTime;
        listItem.sessionDuration = sessionDuration;
        return listItem;
    }
}
