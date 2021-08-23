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

package com.rbraithwaite.sleeptarget.test_utils.test_data.builders;

import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

public class TagBuilder
        implements BuilderOf<Tag>
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private String mText;
    
//*********************************************************
// constructors
//*********************************************************

    public TagBuilder()
    {
        mId = 0;
        mText = "some tag";
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Tag build()
    {
        return new Tag(mId, mText);
    }
    
//*********************************************************
// api
//*********************************************************

    public TagBuilder withId(int id)
    {
        mId = id;
        return this;
    }
    
    public TagBuilder withText(String text)
    {
        mText = text;
        return this;
    }
}
