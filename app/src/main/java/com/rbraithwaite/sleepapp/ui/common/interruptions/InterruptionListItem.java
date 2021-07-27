package com.rbraithwaite.sleepapp.ui.common.interruptions;

public class InterruptionListItem
{
//*********************************************************
// public constants
//*********************************************************

    public final int interruptionId;
    public final String start;
    public final String duration;
    public final String reason;

//*********************************************************
// constructors
//*********************************************************

    public InterruptionListItem(int interruptionId, String start, String duration, String reason)
    {
        this.interruptionId = interruptionId;
        this.start = start;
        this.duration = duration;
        this.reason = reason;
    }
}
