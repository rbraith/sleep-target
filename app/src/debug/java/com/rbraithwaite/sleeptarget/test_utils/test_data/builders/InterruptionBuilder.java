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

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;

// REFACTOR [21-07-31 3:16AM] -- derive this from SessionBuilder.
public class InterruptionBuilder
        implements BuilderOf<Interruption>
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private Date mStart;
    private long mDuration;
    private String mReason;

//*********************************************************
// constructors
//*********************************************************

    public InterruptionBuilder()
    {
        mId = 0;
        mStart = aDate().build();
        mDuration = 5 * 60 * 1000; // 5 min
        mReason = "some reason";
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public Interruption build()
    {
        return new Interruption(mId, mStart, mDuration, mReason);
    }

//*********************************************************
// api
//*********************************************************

    public InterruptionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    public InterruptionBuilder withStart(Date start)
    {
        mStart = start;
        return this;
    }
    
    public InterruptionBuilder withDurationMinutes(int minutes)
    {
        withDuration(0, minutes, 0);
        return this;
    }
    
    public InterruptionBuilder withReason(String reason)
    {
        mReason = reason;
        return this;
    }
    
    public InterruptionBuilder withDuration(int hours, int minutes, int seconds)
    {
        mDuration =
                hours * 60 * 60 * 1000 +
                minutes * 60 * 1000 +
                seconds * 1000;
        return this;
    }
    
    public InterruptionBuilder withId(int id)
    {
        mId = id;
        return this;
    }
    
    public InterruptionBuilder withDurationHours(int hours)
    {
        withDuration(hours, 0, 0);
        return this;
    }
}
