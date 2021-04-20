package com.rbraithwaite.sleepapp.core.models;

import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CurrentSession
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private TimeUtils mTimeUtils;
    
    private String mAdditionalComments;
    private Mood mMood;
    private List<Integer> mSelectedTagIds;

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
        this(start, additionalComments, null, null);
    }
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments,
            @Nullable Mood mood,
            @Nullable List<Integer> selectedTagIds)
    {
        mStart = start;
        mAdditionalComments = additionalComments;
        mTimeUtils = createTimeUtils();
        mMood = mood;
        mSelectedTagIds = selectedTagIds == null ? new ArrayList<>() : selectedTagIds;
    }


//*********************************************************
// api
//*********************************************************

    public Mood getMood()
    {
        return mMood;
    }
    
    public void setMood(Mood mood)
    {
        mMood = mood;
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
    
    // SMELL [21-04-19 10:30PM] -- It's ugly to ignore the tag ids in the current session.
    //  A potential solution to this might be to provide a TagRepository to the CurrentSession
    //  so that it can convert its tag ids to Tag models when it needs to, but this seems really
    //  smelly as well, idk.
    //  maybe not? https://softwareengineering.stackexchange.com/a/318710
    //  https://stackoverflow.com/a/47897704.
    
    /**
     * @return This current session as a distinct sleep session. Note: the returned SleepSession
     * will have no {@link Tag tags}, ignoring any {@link #getSelectedTagIds() selected tag ids} in
     * the CurrentSession.
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
    
    public List<Integer> getSelectedTagIds()
    {
        return mSelectedTagIds;
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
