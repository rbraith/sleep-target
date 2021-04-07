package com.rbraithwaite.sleepapp.core.models;

import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

public class CurrentSession
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private TimeUtils mTimeUtils;
    
    private String mAdditionalComments;
    private Mood mMood;

//*********************************************************
// constructors
//*********************************************************

    public CurrentSession()
    {
        this(null);
    }
    
    public CurrentSession(@Nullable Date start)
    {
        this(start, null);
    }
    
    public CurrentSession(@Nullable Date start, @Nullable String additionalComments)
    {
        this(start, additionalComments, null);
    }
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments,
            @Nullable Mood mood)
    {
        mStart = start;
        mAdditionalComments = additionalComments;
        mTimeUtils = createTimeUtils();
        mMood = mood;
    }

    
//*********************************************************
// api
//*********************************************************

    public Mood getMood()
    {
        return mMood;
    }
    
    public Date getStart()
    {
        return mStart;
    }
    
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public boolean isStarted()
    {
        return mStart != null;
    }
    
    /**
     * This returns a dynamic value - the duration from the start of the current session to whenever
     * this method was called. Do not expect any two calls of this method to return the same value.
     */
    public long getOngoingDurationMillis()
    {
        return mTimeUtils.getNow().getTime() - mStart.getTime();
    }
    
    /**
     * @return This current session as a distinct sleep session.
     */
    public SleepSession toSleepSession()
    {
        return new SleepSession(
                getStart(),
                getOngoingDurationMillis(),
                getAdditionalComments(),
                getMood());
    }
    
    public String getAdditionalComments()
    {
        return mAdditionalComments;
    }
    
    public void setAdditionalComments(String additionalComments)
    {
        mAdditionalComments = additionalComments;
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
