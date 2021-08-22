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
