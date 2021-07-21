package com.rbraithwaite.sleepapp.ui.common.interruptions;

public class InterruptionListItem
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

    public InterruptionListItem(String start, String duration, String reason)
    {
        this.start = start;
        this.duration = duration;
        this.reason = reason;
    }
}
