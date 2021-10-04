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

package com.rbraithwaite.sleeptarget.test_utils.value_matchers;

import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SleepInterruptionEntityMatchers
{
//*********************************************************
// api
//*********************************************************

    public static Matcher<SleepInterruptionEntity> interruptionWithReason(String reason)
    {
        return new TypeSafeMatcher<SleepInterruptionEntity>()
        {
            @Override
            protected boolean matchesSafely(SleepInterruptionEntity item)
            {
                return (item.reason == null && reason == null) ||
                       (item.reason != null && item.reason.equals(reason));
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("SleepInterruptionEntity has reason: " + reason);
            }
        };
    }
}
