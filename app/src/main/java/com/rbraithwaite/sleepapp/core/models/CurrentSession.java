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
// public helpers
//*********************************************************

    
    /**
     * A static "snapshot" of some CurrentSession's state. This is needed as CurrentSession's
     * duration is always updating in real time - see {@link #getOngoingDurationMillis()}
     */
    public static class Snapshot
    {
        public Date start;
        public Date end;
        public long durationMillis;
        public Mood mood;
        public String additionalComments;
        public List<Integer> selectedTagIds;
        
        public Snapshot(
                Date start,
                Date end,
                long durationMillis,
                Mood mood, String additionalComments, List<Integer> selectedTagIds)
        {
            this.start = start;
            this.end = end;
            this.durationMillis = durationMillis;
            this.mood = mood;
            this.additionalComments = additionalComments;
            this.selectedTagIds = selectedTagIds;
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public CurrentSession(TimeUtils timeUtils)
    {
        this(null, timeUtils);
    }
    
    public CurrentSession(@Nullable Date start, TimeUtils timeUtils)
    {
        this(start, null, timeUtils);
    }
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments,
            TimeUtils timeUtils)
    {
        // TODO [21-06-14 1:30AM] -- mood shouldn't ever be null, only unset.
        this(start, additionalComments, null, null, timeUtils);
    }
    
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments,
            // TODO [21-06-14 1:30AM] -- mood shouldn't ever be null, only unset.
            @Nullable Mood mood,
            @Nullable List<Integer> selectedTagIds,
            TimeUtils timeUtils)
    {
        mStart = start;
        mAdditionalComments = additionalComments;
        mTimeUtils = timeUtils;
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
    
    public void setSelectedTagIds(List<Integer> selectedTagIds)
    {
        mSelectedTagIds = selectedTagIds;
    }
    
    public Snapshot createSnapshot()
    {
        Date start = getStart();
        int durationMillis = (int) getOngoingDurationMillis();
        // HACK [21-05-25 3:35PM] -- this is a temporary bandaid here for a larger app-wide issue.
        //  That issue being the multitude of places where I am casting from long to int for
        //  a sleep session's duration. This behaviour is very far from ideal, what would be much
        //  more preferable would be if the app were able to just handle long duration sleep
        //  sessions.
        //  This hack here is to protect against overflows, but there are plenty of places
        //  elsewhere in the app where no such protections (regardless of how hacky) exist.
        durationMillis = Math.max(0, durationMillis);
        
        Date end = mTimeUtils.addDurationToDate(start, durationMillis);
        
        return new Snapshot(
                start,
                end,
                durationMillis,
                getMood(),
                getAdditionalComments(),
                getSelectedTagIds());
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
}
