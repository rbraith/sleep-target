package com.rbraithwaite.sleepapp.ui.session_archive.data;

// TODO [20-12-24 3:35PM] -- rename this SessionArchiveListItem.
// ui pojo, to be converted within a viewmodel
public class UISleepSessionData
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

    public static UISleepSessionData create(
            String startTime,
            String endTime,
            String sessionDuration)
    {
        UISleepSessionData sleepSessionData = new UISleepSessionData();
        sleepSessionData.startTime = startTime;
        sleepSessionData.endTime = endTime;
        sleepSessionData.sessionDuration = sessionDuration;
        return sleepSessionData;
    }
}
