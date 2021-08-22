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

package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

public class MoodBuilder
        implements BuilderOf<Mood>
{
//*********************************************************
// private properties
//*********************************************************

    private int mIndex;

//*********************************************************
// constructors
//*********************************************************

    public MoodBuilder()
    {
        mIndex = 2;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public Mood build()
    {
        return new Mood(mIndex);
    }
    
//*********************************************************
// api
//*********************************************************

    public MoodBuilder withIndex(int index)
    {
        mIndex = index;
        return this;
    }
}
