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

import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;

import org.junit.Test;

import java.util.Date;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InterruptionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getEnd_returnsCorrectValue()
    {
        DateBuilder date = aDate();
        Interruption interruption = anInterruption()
                .withStart(date)
                .withDurationMinutes(1234)
                .build();
        
        Date end = interruption.getEnd();
        
        assertThat(end, is(equalTo(date.addMinutes(1234).build())));
    }
    
    @Test
    public void getDurationMillisInBounds_returnsCorrectDuration()
    {
        DateBuilder date = aDate();
        Interruption interruption = valueOf(anInterruption().withStart(date).withDurationHours(2));
        
        DateBuilder startDate = date.copy();
        SleepSessionBuilder sleepSession = aSleepSession();
        Object[][] data = {
                // sleep session, expected duration in hrs
                // fully out of bounds (before)
                // REFACTOR [21-09-13 9:40PM] -- subtractHours.
                {valueOf(sleepSession.withStart(startDate.addHours(-2)).withDurationHours(1)), 0},
                // partially out of bounds (before)
                {valueOf(sleepSession.withStart(startDate.addHours(1)).withDurationHours(2)), 1},
                // fully in bounds
                {valueOf(sleepSession.withStart(startDate.addMinutes(90)).withDurationHours(1)), 1},
                // partially out of bounds (after)
                {valueOf(sleepSession.withStart(startDate.addMinutes(30)).withDurationHours(2)), 1},
                // full out of bounds (after)
                {valueOf(sleepSession.withStart(startDate.addHours(2)).withDurationHours(2)), 0}
        };
        
        for (Object[] testData : data) {
            SleepSession bounds = (SleepSession) testData[0];
            int expectedDurationHours = (int) testData[1];
            
            long expectedDurationMillis = expectedDurationHours * 60 * 60 * 1000;
            
            assertThat(interruption.getDurationMillisInBounds(bounds), is(expectedDurationMillis));
        }
    }
}
