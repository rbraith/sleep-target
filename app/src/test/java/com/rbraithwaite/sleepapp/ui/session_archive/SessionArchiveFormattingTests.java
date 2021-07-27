package com.rbraithwaite.sleepapp.ui.session_archive;

import com.rbraithwaite.sleepapp.core.models.Interruptions;

import org.junit.Test;

import java.util.GregorianCalendar;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SessionArchiveFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatInterruptions_positiveInput()
    {
        Interruptions interruptions = new Interruptions(aListOf(
                anInterruption().withDuration(1, 10, 10),
                anInterruption().withDuration(2, 5, 5)));
        
        assertThat(SessionArchiveFormatting.formatInterruptions(interruptions),
                   is(equalTo("3h 15m 15s (2)")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void formatDuration_throwsOnNegativeInput()
    {
        SessionArchiveFormatting.formatDuration(-1);
    }
    
    @Test
    public void formatDuration_positiveInput()
    {
        long duration = (2 * 60 * 60 * 1000) +  // 2  hr
                        (34 * 60 * 1000) +      // 34 min
                        (56 * 1000);            // 56 sec
        
        String formattedDuration = SessionArchiveFormatting.formatDuration(duration);
        
        assertThat(formattedDuration, is(equalTo("2h 34m 56s")));
    }
    
    @Test
    public void formatFullDate_returnsNullOnNullInput()
    {
        assertThat(SessionArchiveFormatting.formatFullDate(null), is(nullValue()));
    }
    
    @Test
    public void formatFullDate_positiveInput()
    {
        GregorianCalendar date = new GregorianCalendar(2021, 2, 31, 4, 56);
        
        assertThat(
                SessionArchiveFormatting.formatFullDate(date.getTime()),
                is(equalTo("4:56 AM, Mar 31 2021")));
    }
}
