package com.rbraithwaite.sleepapp.ui.session_archive.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;

import java.util.List;
import java.util.stream.Collectors;

public class ConvertSessionArchiveListItem
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertSessionArchiveListItem() {/* No instantiation */}
    
    // TEST NEEDED [21-05-14 5:09PM] -- update tests with rating data, interruption data.


//*********************************************************
// api
//*********************************************************

    
    /**
     * Converts a SleepSession to a SessionArchiveListItem.
     *
     * @param sleepSession The SleepSession to convert.
     *
     * @return The converted SessionArchiveListItem. Returns null if sleepSession is null.
     */
    public static SessionArchiveListItem fromSleepSession(SleepSession sleepSession)
    {
        if (sleepSession == null) {
            return null;
        }
        
        return SessionArchiveListItem.create(
                sleepSession.getId(),
                SessionArchiveFormatting.formatFullDate(sleepSession.getStart()),
                SessionArchiveFormatting.formatFullDate(sleepSession.getEnd()),
                SessionArchiveFormatting.formatDuration(sleepSession.getDurationMillis()),
                (sleepSession.getAdditionalComments() != null),
                ConvertMood.toUiData(sleepSession.getMood()),
                convertTags(sleepSession.getTags()),
                sleepSession.getRating(),
                SessionArchiveFormatting.formatInterruptions(sleepSession.getInterruptions()));
    }

//*********************************************************
// private methods
//*********************************************************

    private static List<String> convertTags(List<Tag> tags)
    {
        if (tags == null) {
            return null;
        }
        
        return tags.stream().map(Tag::getText).collect(Collectors.toList());
    }
}
