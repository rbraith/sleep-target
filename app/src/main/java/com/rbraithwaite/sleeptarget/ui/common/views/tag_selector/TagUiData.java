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

public class TagUiData
{
//*********************************************************
// public properties
//*********************************************************

    public String text;
    public int tagId;

//*********************************************************
// constructors
//*********************************************************

    public TagUiData(int tagId, String text)
    {
        this.tagId = tagId;
        this.text = text;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + tagId;
        hash = prime * hash + (text == null ? 0 : text.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        TagUiData tagUiData = (TagUiData) o;
        return tagId == tagUiData.tagId &&
               ((text == null && tagUiData.text == null) ||
                (text != null && text.equals(tagUiData.text)));
    }
}
