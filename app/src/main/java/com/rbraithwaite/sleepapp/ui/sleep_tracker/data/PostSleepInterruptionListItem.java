package com.rbraithwaite.sleepapp.ui.sleep_tracker.data;

public class PostSleepInterruptionListItem
{
//*********************************************************
// public constants
//*********************************************************

    public final String start;
    public final String duration;
    public final String reason;
    
//*********************************************************
// constructors
//*********************************************************

    public PostSleepInterruptionListItem(String start, String duration, String reason)
    {
        this.start = start;
        this.duration = duration;
        this.reason = reason;
    }
}
