package com.rbraithwaite.sleepapp.ui.session_archive.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;

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
