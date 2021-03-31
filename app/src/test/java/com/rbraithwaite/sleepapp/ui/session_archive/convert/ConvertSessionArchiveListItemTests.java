package com.rbraithwaite.sleepapp.ui.session_archive.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;

import org.junit.Test;

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
        
        SessionArchiveListItem listItem =
                ConvertSessionArchiveListItem.fromSleepSession(sleepSession);
        assertThat(listItem.startTime,
                   is(equalTo(SessionArchiveFormatting.formatFullDate(sleepSession.getStart()))));
        assertThat(listItem.endTime,
                   is(equalTo(SessionArchiveFormatting.formatFullDate(sleepSession.getEnd()))));
        assertThat(listItem.sessionDuration,
                   is(equalTo(SessionArchiveFormatting.formatDuration(sleepSession.getDurationMillis()))));
        assertThat(listItem.hasAdditionalComments, is(true));
    }
}
