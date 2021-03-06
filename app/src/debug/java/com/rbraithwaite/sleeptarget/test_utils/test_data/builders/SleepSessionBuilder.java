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
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aTag;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;

// REFACTOR [21-07-31 3:16AM] -- derive this from SessionBuilder.
public class SleepSessionBuilder
        implements BuilderOf<SleepSession>
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private Date mStart;
    private long mDuration;
    private String mComments;
    private Mood mMood;
    private List<Tag> mTags;
    private float mRating;
    private List<Interruption> mInterruptions;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionBuilder()
    {
        mId = 0;
        mStart = aDate().build();
        mDuration = 123456;
        mComments = "some comments";
        mMood = aMood().build();
        mTags = aListOf(
                aTag().withId(1).withText("tag 1"),
                aTag().withId(2).withText("tag 2"));
        mRating = 2.5f;
        mInterruptions = aListOf(
                anInterruption()
                        .withStart(aDate().copying(mStart))
                        .withDurationMinutes(1));
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public SleepSession build()
    {
        SleepSession sleepSession = new SleepSession(
                mId,
                mStart,
                mDuration,
                mComments,
                mMood,
                mTags,
                mRating);
        if (mInterruptions != null) {
            sleepSession.setInterruptions(new Interruptions(mInterruptions));
        }
        return sleepSession;
    }

//*********************************************************
// api
//*********************************************************

    public SleepSessionBuilder withComments(String comments)
    {
        mComments = comments;
        return this;
    }
    
    public SleepSessionBuilder withInterruptions(InterruptionBuilder... interruptions)
    {
        mInterruptions = aListOf(interruptions);
        return this;
    }
    
    public SleepSessionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    public SleepSessionBuilder withDurationHours(int hours)
    {
        mDuration = TimeUtils.hoursToMillis(hours);
        return this;
    }
    
    public SleepSessionBuilder offsetStartByHours(int hours)
    {
        mStart = new TimeUtils().addDurationToDate(
                mStart,
                (int) TimeUtils.hoursToMillis(hours));
        return this;
    }
    
    public SleepSessionBuilder withId(int id)
    {
        mId = id;
        return this;
    }
    
    public SleepSessionBuilder withNoInterruptions()
    {
        mInterruptions = null;
        return this;
    }
    
    public SleepSessionBuilder withStart(Date start)
    {
        mStart = start;
        return this;
    }
    
    public SleepSessionBuilder withDurationMinutes(int minutes)
    {
        mDuration = TimeUtils.minutesToMillis(minutes);
        return this;
    }
    
    public SleepSessionBuilder withNoTags()
    {
        mTags = new ArrayList<>();
        return this;
    }
    
    public SleepSessionBuilder withNoAdditionalComments()
    {
        mComments = "";
        return this;
    }
    
    public SleepSessionBuilder withRating(float rating)
    {
        mRating = rating;
        return this;
    }
}
