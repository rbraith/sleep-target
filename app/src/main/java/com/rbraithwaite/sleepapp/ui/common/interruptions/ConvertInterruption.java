package com.rbraithwaite.sleepapp.ui.common.interruptions;

import com.rbraithwaite.sleepapp.core.models.Interruption;

public class ConvertInterruption
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertInterruption() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static InterruptionListItem toListItem(Interruption interruption)
    {
        return new InterruptionListItem(
                interruption.getId(),
                InterruptionFormatting.formatListItemStart(interruption.getStart()),
                InterruptionFormatting.formatDuration(interruption.getDurationMillis()),
                InterruptionFormatting.formatListItemReason(interruption.getReason()));
    }
}
