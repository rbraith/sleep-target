package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SleepSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void hasNoInterruptions_returnsCorrectValue()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        
        assertThat(sleepSession.hasNoInterruptions(), is(true));
        
        sleepSession.setInterruptions(new Interruptions(aListOf(anInterruption())));
        
        assertThat(sleepSession.hasNoInterruptions(), is(false));
    }
    
    @Test
    public void getRating_isZeroIfSetToNull()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setRating(null);
        assertThat(sleepSession.getRating(), is(0f));
    }
}
