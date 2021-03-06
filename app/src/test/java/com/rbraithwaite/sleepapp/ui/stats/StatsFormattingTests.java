package com.rbraithwaite.sleepapp.ui.stats;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class StatsFormattingTests
{
//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-03-5 1:59AM] -- consider changing other tests that use
    //  @RunWith(Parameterized.class) to use this pattern? Or change this
    //  to @RunWith(Parameterized.class)?
    @Test
    public void formatIntervalsYLabel_positiveInput()
    {
        // input, expected
        Object[][] testDataSets = {
                {29, "5am"},
                {9, "9am"},
                {15, "3pm"},
                {22, "10pm"},
                {12, "12pm"},
                {0, "12am"},
                {24, "12am"}
        };
        
        for (Object[] testDataSet : testDataSets) {
            int input = (int) testDataSet[0];
            String expected = (String) testDataSet[1];
            
            assertThat(StatsFormatting.formatIntervalsYLabel(input), is(equalTo(expected)));
        }
    }
}
