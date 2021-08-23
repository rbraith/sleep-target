/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.core.models;

import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.InterruptionBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
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
    public void update_updatesUpdatesUpdated()
    {
        // Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo.
        
        int id = 1;
        InterruptionBuilder interruption = anInterruption().withId(id).withReason("derp");
        
        Interruptions interruptions = new Interruptions(aListOf(interruption));
        
        String expectedReason = "updated";
        Interruption expected = valueOf(interruption.withReason(expectedReason));
        interruptions.update(expected);
        assertThat(interruptions.get(expected.getId()), is(equalTo(expected)));
        
        Interruptions.Updates updates = interruptions.consumeUpdates();
        assertThat(updates.updated.size(), is(1));
        assertThat(updates.updated.get(0), is(equalTo(expected)));
    }
    
    @Test
    public void update_doesNothingIfInterruptionIdNotFound()
    {
        InterruptionBuilder interruption = anInterruption().withId(1).withReason("derp");
        
        Interruptions interruptions = new Interruptions(aListOf(interruption));
        
        interruption.withId(2).withReason("updated");
        interruptions.update(valueOf(interruption));
        
        assertThat(interruptions.hasUpdates(), is(false));
        assertThat(interruptions.consumeUpdates(), is(nullValue()));
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
