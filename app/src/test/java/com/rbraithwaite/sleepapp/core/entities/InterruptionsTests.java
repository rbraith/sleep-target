package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InterruptionsTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getTotalDuration_returnsCorrectDuration()
    {
        Interruptions interruptions = createInterruptionsFrom(
                anInterruption().withDurationMinutes(10),
                anInterruption().withDurationMinutes(5));
        
        long expectedDuration = 15 * 60 * 1000;
        
        assertThat(interruptions.getTotalDuration(), is(equalTo(expectedDuration)));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private Interruptions createInterruptionsFrom(InterruptionBuilder... interruptionBuilders)
    {
        return new Interruptions(new ArrayList<>(Arrays.asList(interruptionBuilders)).stream()
                                         .map(InterruptionBuilder::build)
                                         .collect(Collectors.toList()));
    }
}
