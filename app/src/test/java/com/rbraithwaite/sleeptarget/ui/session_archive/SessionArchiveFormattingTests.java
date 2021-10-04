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

package com.rbraithwaite.sleeptarget.ui.session_archive;

import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;

import org.junit.Test;

import java.util.GregorianCalendar;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SessionArchiveFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatInterruptions_positiveInput()
    {
        SleepSessionBuilder parent = aSleepSession().withDurationHours(123);
        
        parent.withInterruptions(anInterruption().withDuration(1, 10, 10),
                                 anInterruption().withDuration(2, 5, 5));
        
        assertThat(SessionArchiveFormatting.formatInterruptionsOf(valueOf(parent)),
                   is(equalTo("3h 15m 15s (2)")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void formatDuration_throwsOnNegativeInput()
    {
        SessionArchiveFormatting.formatDuration(-1);
    }
    
    @Test
    public void formatDuration_positiveInput()
    {
        long duration = (2 * 60 * 60 * 1000) +  // 2  hr
                        (34 * 60 * 1000) +      // 34 min
                        (56 * 1000);            // 56 sec
        
        String formattedDuration = SessionArchiveFormatting.formatDuration(duration);
        
        assertThat(formattedDuration, is(equalTo("2h 34m 56s")));
    }
    
    @Test
    public void formatFullDate_returnsNullOnNullInput()
    {
        assertThat(SessionArchiveFormatting.formatFullDate(null), is(nullValue()));
    }
    
    @Test
    public void formatFullDate_positiveInput()
    {
        GregorianCalendar date = new GregorianCalendar(2021, 2, 31, 4, 56);
        
        assertThat(
                SessionArchiveFormatting.formatFullDate(date.getTime()),
                is(equalTo("4:56 AM, Mar 31 2021")));
    }
}
