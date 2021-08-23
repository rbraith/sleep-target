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

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aCurrentSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aPostSleepData;

public class StoppedSessionDataBuilder
        implements BuilderOf<StoppedSessionData>
{
//*********************************************************
// private properties
//*********************************************************

    private CurrentSession mCurrentSession;
    private PostSleepData mPostSleepData;
    private TimeUtils mTimeUtils;

//*********************************************************
// constructors
//*********************************************************

    public StoppedSessionDataBuilder()
    {
        mCurrentSession = aCurrentSession().build();
        mPostSleepData = aPostSleepData().build();
        mTimeUtils = new TimeUtils();
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public StoppedSessionData build()
    {
        return new StoppedSessionData(mCurrentSession.createSnapshot(mTimeUtils), mPostSleepData);
    }

//*********************************************************
// api
//*********************************************************

    public StoppedSessionDataBuilder with(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
        return this;
    }
    
    public StoppedSessionDataBuilder with(CurrentSessionBuilder currentSession)
    {
        mCurrentSession = currentSession.build();
        return this;
    }
    
    public StoppedSessionDataBuilder with(PostSleepDataBuilder postSleepData)
    {
        mPostSleepData = postSleepData.build();
        return this;
    }
}
