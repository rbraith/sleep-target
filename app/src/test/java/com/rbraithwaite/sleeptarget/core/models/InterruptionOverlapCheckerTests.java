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

import com.rbraithwaite.sleeptarget.core.models.overlap_checker.InterruptionOverlapChecker;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;

import org.junit.Test;

import java.util.List;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class InterruptionOverlapCheckerTests
{
    @Test
    public void noOverlap()
    {
        DateBuilder date = aDate();
        List<Interruption> interruptions = aListOf(
                anInterruption()
                        .withId(1)
                        .withDurationHours(1)
                        .withStart(date)
                        .withReason("existing interruption"));
        Interruption nonOverlapping = valueOf(
                anInterruption()
                        .withId(2)
                        .withDurationHours(1)
                        .withStart(date.addHours(2)));
    
        InterruptionOverlapChecker checker = new InterruptionOverlapChecker(interruptions);
        
        assertThat(checker.checkForOverlapExclusive(nonOverlapping), is(nullValue()));
    }
}
