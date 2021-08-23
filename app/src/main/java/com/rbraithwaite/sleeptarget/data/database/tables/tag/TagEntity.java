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

package com.rbraithwaite.sleeptarget.data.database.tables.tag;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = TagContract.TABLE_NAME)
public class TagEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = TagContract.Columns.ID)
    public int id;
    
    @ColumnInfo(name = TagContract.Columns.TAG_TEXT)
    public String text;

//*********************************************************
// constructors
//*********************************************************

    public TagEntity(int id, String text)
    {
        this.id = id;
        this.text = text;
    }
    
    public TagEntity(String text)
    {
        this.text = text;
    }
    
    public TagEntity()
    {
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + id;
        hash = prime * hash + (text == null ? 0 : text.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        TagEntity entity = (TagEntity) o;
        return id == entity.id &&
               ((text == null && entity.text == null) || text.equals(entity.text));
    }
}
