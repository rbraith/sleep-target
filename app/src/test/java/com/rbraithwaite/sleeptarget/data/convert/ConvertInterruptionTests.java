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

package com.rbraithwaite.sleeptarget.data.convert;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ConvertInterruptionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toEntity_positiveInput()
    {
        int id = 2;
        Date start = TestUtils.ArbitraryData.getDate();
        long durationMillis = 500L;
        String reason = "reason";
        
        Interruption interruption = new Interruption(id, start, durationMillis, reason);
        
        SleepInterruptionEntity entity = ConvertInterruption.toEntity(interruption);
        
        assertThat(entity.id, is(equalTo(id)));
        assertThat(entity.sessionId, is(equalTo(0L)));
        assertThat(entity.startTime, is(equalTo(start)));
        assertThat(entity.durationMillis, is(durationMillis));
        assertThat(entity.reason, is(equalTo(reason)));
    }
}
