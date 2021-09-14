/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rbraithwaite.sleeptarget.ui.session_archive.convert;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleeptarget.ui.session_archive.data.SessionArchiveListItem;

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
                SessionArchiveFormatting.formatInterruptionsOf(sleepSession));
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
