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

package com.rbraithwaite.sleeptarget.ui.common.views.tag_selector;

import com.rbraithwaite.sleeptarget.core.models.Tag;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertTagTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toUiData_nullInput()
    {
        assertThat(ConvertTag.toUiData(null), is(nullValue()));
    }
    
    @Test
    public void toUiData_positiveInput()
    {
        Tag testTag = new Tag(2, "test");
        TagUiData tagUiData = ConvertTag.toUiData(testTag);
        
        assertThat(tagUiData.tagId, is(testTag.getTagId()));
        assertThat(tagUiData.text, is(equalTo(testTag.getText())));
    }
    
    @Test
    public void fromUiData_nullInput()
    {
        assertThat(ConvertTag.fromUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_positiveInput()
    {
        TagUiData tagUiData = new TagUiData(2, "test");
        Tag tag = ConvertTag.fromUiData(tagUiData);
        
        assertThat(tag.getTagId(), is(tagUiData.tagId));
        assertThat(tag.getText(), is(equalTo(tagUiData.text)));
    }
}
