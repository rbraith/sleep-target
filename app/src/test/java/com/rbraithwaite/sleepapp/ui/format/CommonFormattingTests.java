package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CommonFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatFullDate_returnsNullIfNullInput()
    {
        assertThat(CommonFormatting.formatFullDate(null), is(nullValue()));
    }
    
    @Test
    public void formatFullDate_formatsDate()
    {
        GregorianCalendar cal = new GregorianCalendar(2021, 4, 2, 12, 34);
        
        String formatted = CommonFormatting.formatFullDate(cal.getTime());
        
        assertThat(formatted, is(equalTo("12:34 PM, May 2 2021")));
    }
    
    @Test
    public void formatSleepDurationGoal_positiveInput()
    {
        Collection<Object[]> data = Arrays.asList(new Object[][] {
                // minutes, expected
                {15, "0h 15m"},
                {120, "2h 00m"},
                {605, "10h 05m"}
        });
        
        for (Object[] d : data) {
            int minutes = (int) d[0];
            String expected = (String) d[1];
            
            assertThat(
                    CommonFormatting.formatSleepDurationGoal(new SleepDurationGoal(minutes)),
                    is(equalTo(expected)));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void formatDurationMillis_throwsIfNegative()
    {
        CommonFormatting.formatDurationMillis(-1);
    }
    
    @Test
    public void formatDuration_positiveInput()
    {
        long duration = (2 * 60 * 60 * 1000) +  // 2  hr
                        (34 * 60 * 1000) +      // 34 min
                        (56 * 1000);            // 56 sec
        
        String formattedDuration = CommonFormatting.formatDurationMillis(duration);
        
        assertThat(formattedDuration, is(equalTo("2h 34m 56s")));
    }
    
    @Test
    public void formatSleepDurationGoal_unsetArg()
    {
        assertThat(
                CommonFormatting.formatSleepDurationGoal(SleepDurationGoal.createWithNoGoal()),
                is(""));
    }
}
