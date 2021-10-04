/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleeptarget.ui.session_archive.data.SessionArchiveListItem;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertSessionArchiveListItemTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void fromSleepSession_returnsNullOnNullInput()
    {
        assertThat(ConvertSessionArchiveListItem.fromSleepSession(null), is(nullValue()));
    }
    
    @Test
    public void fromSleepSession_returnsCorrectValues()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setAdditionalComments("test");
        sleepSession.setTags(Arrays.asList(
                new Tag(1, "tag 1"),
                new Tag(2, "tag 2")
        ));
        
        // SUT
        SessionArchiveListItem listItem =
                ConvertSessionArchiveListItem.fromSleepSession(sleepSession);
        
        assertThat(listItem.startTime,
                   is(equalTo(SessionArchiveFormatting.formatFullDate(sleepSession.getStart()))));
        assertThat(listItem.endTime,
                   is(equalTo(SessionArchiveFormatting.formatFullDate(sleepSession.getEnd()))));
        assertThat(listItem.sessionDuration,
                   is(equalTo(SessionArchiveFormatting.formatDuration(sleepSession.getDurationMillis()))));
        assertThat(listItem.hasAdditionalComments, is(true));
        
        assertThat(listItem.tags.size(), is(sleepSession.getTags().size()));
        for (int i = 0; i < listItem.tags.size(); i++) {
            assertThat(listItem.tags.get(i), is(equalTo(sleepSession.getTags().get(i).getText())));
        }
    }
}
