package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class InterruptionsTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void consumeUpdates_removesExistingUpdate()
    {
        List<Interruption> interruptionList = aListOf(anInterruption());
        Interruptions interruptions = new Interruptions(interruptionList);
        
        assertThat(interruptions.hasUpdates(), is(false));
        
        interruptions.delete(interruptionList.get(0).getId());
        
        assertThat(interruptions.hasUpdates(), is(true));
        
        Interruptions.Updates updates = interruptions.consumeUpdates();
        
        assertThat(interruptions.hasUpdates(), is(false));
    }
    
    @Test
    public void delete_deletesInterruption()
    {
        InterruptionBuilder expectedInterruption = anInterruption();
        Interruption expected = anInterruption().build();
        
        Interruptions interruptions = new Interruptions(aListOf(expectedInterruption));
        
        interruptions.delete(expected.getId());
        
        assertThat(interruptions.get(expected.getId()), is(nullValue()));
        
        assertThat(interruptions.hasUpdates(), is(true));
        
        Interruptions.Updates updates = interruptions.consumeUpdates();
        assertThat(updates.deleted.size(), is(1));
        assertThat(updates.deleted.get(0), is(equalTo(expected)));
    }
    
    @Test
    public void get_returnsCorrectInterruption()
    {
        InterruptionBuilder expectedInterruption = anInterruption();
        Interruption expected = anInterruption().build();
        
        Interruptions interruptions = new Interruptions(aListOf(expectedInterruption));
        
        assertThat(interruptions.get(expected.getId()), is(equalTo(expected)));
    }
    
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

    // REFACTOR [21-07-26 9:57PM] -- use aListOf instead.
    @Deprecated
    private Interruptions createInterruptionsFrom(InterruptionBuilder... interruptionBuilders)
    {
        return new Interruptions(new ArrayList<>(Arrays.asList(interruptionBuilders)).stream()
                                         .map(InterruptionBuilder::build)
                                         .collect(Collectors.toList()));
    }
}
