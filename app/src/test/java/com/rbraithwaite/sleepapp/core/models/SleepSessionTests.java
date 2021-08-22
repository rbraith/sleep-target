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
