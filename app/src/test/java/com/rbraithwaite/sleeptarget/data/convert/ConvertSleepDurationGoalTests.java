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

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertSleepDurationGoalTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepDurationGoal.toEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        SleepDurationGoal model = TestUtils.ArbitraryData.getSleepDurationGoal();
        
        // SUT
        SleepDurationGoalEntity entity =
                ConvertSleepDurationGoal.toEntity(model);
        
        // TODO [21-03-9 3:29PM] -- verify id.
        assertThat(entity.editTime, is(equalTo(model.getEditTime())));
        assertThat(entity.goalMinutes, is(equalTo(model.inMinutes())));
    }
    
    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepDurationGoal.fromEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        SleepDurationGoalEntity entity = TestUtils.ArbitraryData.getSleepDurationGoalEntity();
        SleepDurationGoal model = ConvertSleepDurationGoal.fromEntity(entity);
        
        assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
        assertThat(model.inMinutes(), is(equalTo(entity.goalMinutes)));
    }
}
