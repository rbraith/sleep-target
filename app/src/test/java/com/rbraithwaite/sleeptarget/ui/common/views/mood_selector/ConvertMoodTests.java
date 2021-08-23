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

package com.rbraithwaite.sleeptarget.ui.common.views.mood_selector;

import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertMoodTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toUiData_createsCorrectObj()
    {
        Mood testMood = new Mood(0);
        
        MoodUiData uiData = ConvertMood.toUiData(testMood);
        
        assertThat(uiData.asIndex(), is(equalTo(testMood.asIndex())));
    }
    
    @Test
    public void toUiData_returnsNullOnNullInput()
    {
        assertThat(ConvertMood.toUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_returnsNullOnNullInput()
    {
        assertThat(ConvertMood.fromUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_createsCorrectObj()
    {
        MoodUiData uiData = new MoodUiData(0);
        
        Mood mood = ConvertMood.fromUiData(uiData);
        
        assertThat(mood.asIndex(), is(equalTo(uiData.asIndex())));
    }
}
