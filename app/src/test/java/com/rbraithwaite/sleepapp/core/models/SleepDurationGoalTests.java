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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class SleepDurationGoalTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void createWithoutSettingGoal_has_isSet_returnFalse()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().isSet(), is(false));
    }
    
    @Test
    public void getHours_returnCorrectValue()
    {
        assertThat(new SleepDurationGoal(130).getHours(), is(2));
    }
    
    @Test
    public void getHours_nullIfUnsetModel()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().getHours(), is(nullValue()));
    }
    
    @Test
    public void getRemainingMinutes_returnsCorrectValue()
    {
        assertThat(new SleepDurationGoal(165).getRemainingMinutes(), is(45));
    }
    
    @Test
    public void getRemainingMinutes_nullIfUnsetModel()
    {
        assertThat(SleepDurationGoal.createWithNoGoal().getRemainingMinutes(),
                   is(nullValue()));
    }
    
    @Test
    public void isSet_isTrueIfModelHasMinutes()
    {
        SleepDurationGoal model = new SleepDurationGoal(123);
        assertThat(model.isSet(), is(true));
    }
    
    @Test
    public void inMinutes_returnNullIfModelIsNotSet()
    {
        SleepDurationGoal model = SleepDurationGoal.createWithNoGoal();
        assertThat(model.inMinutes(), is(nullValue()));
    }
    
    @Test
    public void inMinutes_returnsMinutes()
    {
        int expectedMinutes = 123;
        SleepDurationGoal model = new SleepDurationGoal(expectedMinutes);
        assertThat(model.inMinutes(), is(equalTo(expectedMinutes)));
    }
    
    @Test
    public void inMinutes_matchesConstructor()
    {
        assertThat(new SleepDurationGoal(2, 34).inMinutes(), is(154));
    }
}
