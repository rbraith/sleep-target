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

package com.rbraithwaite.sleeptarget.utils.time;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimeOfDayTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void ofCalendar_returnsCorrectTimeOfDay()
    {
        int expectedHourOfDay = 11;
        int expectedMinute = 22;
        GregorianCalendar cal = TestUtils.ArbitraryData.getCalendar();
        cal.set(Calendar.HOUR_OF_DAY, expectedHourOfDay);
        cal.set(Calendar.MINUTE, expectedMinute);
        
        TimeOfDay timeOfDay = TimeOfDay.of(cal);
        
        assertThat(timeOfDay.hourOfDay, is(expectedHourOfDay));
        assertThat(timeOfDay.minute, is(expectedMinute));
    }
}
