package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;
import java.util.List;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aTag;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;

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
}
