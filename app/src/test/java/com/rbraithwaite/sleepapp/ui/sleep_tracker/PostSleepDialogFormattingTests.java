package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import org.junit.Test;

import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PostSleepDialogFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatDate_returnsNullIfNullInput()
    {
        assertThat(PostSleepDialogFormatting.formatDate(null), is(nullValue()));
    }
    
    @Test
    public void formatDate_formatsDate()
    {
        GregorianCalendar cal = new GregorianCalendar(2021, 4, 2, 12, 34);
        
        String formatted = PostSleepDialogFormatting.formatDate(cal.getTime());
        
        assertThat(formatted, is(equalTo("12:34 PM, May 2 2021")));
    }
    
    @Test
    public void formatDuration_formatsDuration()
    {
        Object[][] data = {
                // hour, minute, second, expected string
                {1, 23, 45, "1h 23m 45s"},
                {0, 1, 1, "0h 01m 01s"},
                {54, 0, 0, "54h 00m 00s"}
        };
        
        for (Object[] d : data) {
            int hour = (int) d[0];
            int minute = (int) d[1];
            int second = (int) d[2];
            
            String expected = (String) d[3];
            
            assertThat(PostSleepDialogFormatting.formatDuration(toDurationMillis(hour,
                                                                                 minute,
                                                                                 second)),
                       is(equalTo(expected)));
        }
    }

//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-05-5 11:44PM] -- extract this?
    private long toDurationMillis(int hours, int minutes, int seconds)
    {
        long hourMillis = hours * 60 * 60 * 1000;
        long minuteMillis = minutes * 60 * 1000;
        long secondMillis = seconds * 1000;
        
        return hourMillis + minuteMillis + secondMillis;
    }
}
