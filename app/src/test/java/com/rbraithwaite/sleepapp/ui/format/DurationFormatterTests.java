package com.rbraithwaite.sleepapp.ui.format;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(Enclosed.class)
public class DurationFormatterTests
{
//*********************************************************
// public helpers
//*********************************************************

    @RunWith(Parameterized.class)
    public static class FormatDurationMillis_positiveInput
    {
        private Long inputDuration;
        private String expected;
        
        public FormatDurationMillis_positiveInput(Long inputDuration, String expected)
        {
            this.inputDuration = inputDuration;
            this.expected = expected;
        }
        
        @Parameterized.Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] {
                    {0L, "0h 00m 00s"},
                    {300000L, "0h 05m 00s"},
                    {13543000L, "3h 45m 43s"},
                    {64861000L, "18h 01m 01s"}
            });
        }
        
        @Test
        public void formatDurationMillis_positiveInput()
        {
            DurationFormatter formatter = new DurationFormatter();
            assertThat(formatter.formatDurationMillis(inputDuration), is(equalTo(expected)));
        }
    }
    
    public static class OtherTests
    {
        @Test(expected = IllegalArgumentException.class)
        public void formatDurationMillis_negativeInput()
        {
            DurationFormatter formatter = new DurationFormatter();
            String formatted = formatter.formatDurationMillis(-1L);
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void constructor_negativeFormatInput()
        {
            String invalidFormat = "%d %d";
            DurationFormatter formatter = new DurationFormatter(Locale.CANADA, invalidFormat);
        }
        
        /**
         * Just testing that no exceptions are thrown.
         */
        @Test
        public void constructor_positiveFormatInput()
        {
            String validFormat = "%d %d %d";
            DurationFormatter formatter = new DurationFormatter(Locale.CANADA, validFormat);
        }
    }
}

//*********************************************************
// api
//*********************************************************

