package com.rbraithwaite.sleepapp.ui.session_archive.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.ConvertMood;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;

public class ConvertSessionArchiveListItem
{
    private ConvertSessionArchiveListItem() {/* No instantiation */}
    
    /**
     * Converts a SleepSession to a SessionArchiveListItem.
     *
     * @param sleepSession The SleepSession to convert.
     * @return  The converted SessionArchiveListItem. Returns null if sleepSession is null.
     */
    public static SessionArchiveListItem fromSleepSession(SleepSession sleepSession)
    {
        if (sleepSession == null) {
            return null;
        }
        return SessionArchiveListItem.create(
                SessionArchiveFormatting.formatFullDate(sleepSession.getStart()),
                SessionArchiveFormatting.formatFullDate(sleepSession.getEnd()),
                SessionArchiveFormatting.formatDuration(sleepSession.getDurationMillis()),
                (sleepSession.getAdditionalComments() != null),
                ConvertMood.toUiData(sleepSession.getMood()));
    }
}
