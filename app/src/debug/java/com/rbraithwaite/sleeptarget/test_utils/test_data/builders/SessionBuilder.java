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

import com.rbraithwaite.sleeptarget.core.models.session.Session;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;

public class SessionBuilder
        implements BuilderOf<Session>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private Date mEnd;
    private long mDurationMillis;
    
//*********************************************************
// constructors
//*********************************************************

    public SessionBuilder()
    {
        mStart = aDate().build();
        mDurationMillis = 123456;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Session build()
    {
        Session session = new Session(mStart, mDurationMillis);
        if (mEnd != null) {
            session.setStart(new TimeUtils().getDateFromMillis(
                    mEnd.getTime() - mDurationMillis));
        }
        return session;
    }
    
//*********************************************************
// api
//*********************************************************

    public SessionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    /**
     * This takes priority over start.
     */
    public SessionBuilder withEnd(DateBuilder end)
    {
        mEnd = end.build();
        return this;
    }
    
    public SessionBuilder withNoDuration()
    {
        return withDuration(0);
    }
    
    public SessionBuilder withDuration(int durationMillis)
    {
        mDurationMillis = durationMillis;
        return this;
    }
}
