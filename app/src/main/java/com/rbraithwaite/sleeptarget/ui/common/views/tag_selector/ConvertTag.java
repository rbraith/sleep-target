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

package com.rbraithwaite.sleeptarget.ui.common.views.tag_selector;

import com.rbraithwaite.sleeptarget.core.models.Tag;

public class ConvertTag
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertTag() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static TagUiData toUiData(Tag tag)
    {
        if (tag == null) {
            return null;
        }
        
        return new TagUiData(tag.getTagId(), tag.getText());
    }
    
    public static Tag fromUiData(TagUiData tagUiData)
    {
        if (tagUiData == null) {
            return null;
        }
        
        return new Tag(tagUiData.tagId, tagUiData.text);
    }
}
