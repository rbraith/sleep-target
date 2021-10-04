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

package com.rbraithwaite.sleeptarget.test_utils.test_data.builders;

import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

public class PostSleepDataBuilder
        implements BuilderOf<PostSleepData>
{
//*********************************************************
// private properties
//*********************************************************

    private float mRating;

//*********************************************************
// constructors
//*********************************************************

    public PostSleepDataBuilder()
    {
        mRating = 4.5f;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public PostSleepData build()
    {
        return new PostSleepData(mRating);
    }
    
//*********************************************************
// api
//*********************************************************

    public PostSleepDataBuilder withRating(float rating)
    {
        mRating = rating;
        return this;
    }
}
