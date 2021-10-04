/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;

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
public class SleepTrackerFormattingTests
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
                    SleepTrackerFormatting.formatSleepDurationGoal(new SleepDurationGoal(
                            minutes)),
                    is(equalTo(expected)));
        }
    }
}
