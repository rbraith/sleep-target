package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(Enclosed.class)
public class CommonFormattingTests
{
//*********************************************************
// public helpers
//*********************************************************

    @RunWith(Parameterized.class)
    public static class FormatSleepDurationGoal_PositiveArgs
    {
        private int minutes;
        private String expected;
        
        public FormatSleepDurationGoal_PositiveArgs(int minutes, String expected)
        {
            this.minutes = minutes;
            this.expected = expected;
        }
        
        @Parameterized.Parameters
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][] {
                    // minutes, expected
                    {15, "0h 15m"},
                    {120, "2h 00m"},
                    {605, "10h 05m"}
            });
        }
        
        @Test
        public void runTest()
        {
            assertThat(
                    CommonFormatting.formatSleepDurationGoal(new SleepDurationGoalModel(minutes)),
                    is(equalTo(expected)));
        }
    }

//*********************************************************
// api
//*********************************************************

    @Test
    public void formatSleepDurationGoal_unsetArg()
    {
        assertThat(
                CommonFormatting.formatSleepDurationGoal(SleepDurationGoalModel.createWithNoGoal()),
                is(""));
    }
}
