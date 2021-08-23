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

package com.rbraithwaite.sleeptarget.data.convert;

import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagEntity;

public class ConvertTag
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertTag() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static Tag fromEntity(TagEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        return new Tag(entity.id, entity.text);
    }
    
    public static TagEntity toEntity(Tag tag)
    {
        if (tag == null) {
            return null;
        }
        
        TagEntity entity = new TagEntity();
        entity.id = tag.getTagId();
        entity.text = tag.getText();
        
        return entity;
    }
}
