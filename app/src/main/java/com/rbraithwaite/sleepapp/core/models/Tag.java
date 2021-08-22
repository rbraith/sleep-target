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

public class Tag
{
//*********************************************************
// private properties
//*********************************************************

    private int mTagId;
    private String mText;

//*********************************************************
// constructors
//*********************************************************

    public Tag(int tagId, String text)
    {
        mTagId = tagId;
        mText = text;
    }
    
    public Tag(String text)
    {
        this(0, text);
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + mTagId;
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Tag tag = (Tag) o;
        return mTagId == tag.mTagId;
    }

//*********************************************************
// api
//*********************************************************

    public int getTagId()
    {
        return mTagId;
    }
    
    public String getText()
    {
        return mText;
    }
    
    public void setText(String text)
    {
        mText = text;
    }
}
