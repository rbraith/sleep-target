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
    
    private String mAdditionalComments;
    private Mood mMood;
    private List<Integer> mSelectedTagIds;
    
    
    private CurrentInterruption mCurrentInterruption;
    
    private List<Interruption> mRecordedInterruptions = new ArrayList<>();


//*********************************************************
// package properties
//*********************************************************

    int mInterruptionsTotalDurationCache = 0;


//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * A static "snapshot" of some CurrentSession's state. This is needed as CurrentSession's
     * duration is always updating in real time - see getOngoingDurationMillis()
     */
    public static class Snapshot
    {
        public Date start;
        public Date end;
        public long durationMillis;
        public Mood mood;
        public String additionalComments;
        public List<Integer> selectedTagIds;
        public List<Interruption> interruptions;
        
        public Snapshot(
                Date start,
                Date end,
                long durationMillis,
                Mood mood,
                String additionalComments,
                List<Integer> selectedTagIds,
                List<Interruption> interruptions)
        {
            this.start = start;
            this.end = end;
            this.durationMillis = durationMillis;
            this.mood = mood;
            this.additionalComments = additionalComments;
            this.selectedTagIds = selectedTagIds;
            this.interruptions = interruptions;
        }
    }


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
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments)
    {
        // TODO [21-06-14 1:30AM] -- mood shouldn't ever be null, only unset.
        this(start, additionalComments, null, null);
    }
    
    public CurrentSession(
            @Nullable Date start,
            @Nullable String additionalComments,
            // TODO [21-06-14 1:30AM] -- mood shouldn't ever be null, only unset.
            @Nullable Mood mood,
            @Nullable List<Integer> selectedTagIds)
    {
        this(start, additionalComments, mood, selectedTagIds, null, null);
    }
    
    public CurrentSession(
            Date start,
            String additionalComments,
            Mood mood,
            List<Integer> selectedTagIds,
            List<Interruption> interruptions,
            Interruption currentInterruption)
    {
        mStart = start;
        mAdditionalComments = additionalComments;
        mMood = mood;
        mSelectedTagIds = selectedTagIds == null ? new ArrayList<>() : selectedTagIds;
        
        if (interruptions != null) {
            mRecordedInterruptions = interruptions;
        }
        
        if (currentInterruption != null) {
            mCurrentInterruption = new CurrentInterruption(
                    currentInterruption.getStart(),
                    currentInterruption.getReason());
        }
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
    public long getOngoingDurationMillis(TimeUtils timeUtils)
    {
        return timeUtils.getNow().getTime() - mStart.getTime();
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
    
    /**
     * Note: if there is an ongoing interruption, a snapshot of that interruption will be appended
     * to the interruptions field of the returned Snapshot.
     */
    public Snapshot createSnapshot(TimeUtils timeUtils)
    {
        if (!isStarted()) {
            return null;
        }
        
        Date start = getStart();
        int durationMillis = (int) getOngoingDurationMillis(timeUtils);
        // HACK [21-05-25 3:35PM] -- this is a temporary bandaid here for a larger app-wide issue.
        //  That issue being the multitude of places where I am casting from long to int for
        //  a sleep session's duration. This behaviour is very far from ideal, what would be much
        //  more preferable would be if the app were able to just handle long duration sleep
        //  sessions.
        //  This hack here is to protect against overflows, but there are plenty of places
        //  elsewhere in the app where no such protections (regardless of how hacky) exist.
        durationMillis = Math.max(0, durationMillis);
        
        Date end = timeUtils.addDurationToDate(start, durationMillis);
        
        // SMELL [21-07-8 9:47PM] -- It's weird that everything else is primitives while this is
        //  a list of Interruptions - tbh I think createSnapshot should maybe just return a
        //  SleepSession? similar to how createCurrentInterruptionSnapshot returns an Interruption.
        List<Interruption> interruptions = new ArrayList<>(getInterruptions());
        if (isInterrupted()) {
            interruptions.add(createCurrentInterruptionSnapshot(timeUtils));
        }
        
        return new Snapshot(
                start,
                end,
                durationMillis,
                getMood(),
                getAdditionalComments(),
                getSelectedTagIds(),
                interruptions);
    }
    
    public void setInterruptionReason(String reason)
    {
        if (isInterrupted()) {
            mCurrentInterruption.reason = reason;
        }
    }
    
    public Interruption createCurrentInterruptionSnapshot(TimeUtils timeUtils)
    {
        if (!isInterrupted()) {
            return null;
        }
        
        return mCurrentInterruption.getSnapshot(timeUtils);
    }
    
    public List<Interruption> getInterruptions()
    {
        return mRecordedInterruptions;
    }
    
    /**
     * Interrupt this CurrentSession if that is possible. It is not possible if this session is not
     * started, or if it is already interrupted. This sets the initial reason for the new
     * interruption as that of the most recent saved interruption.
     *
     * @param timeUtils Needed to start the interruption
     *
     * @return true if a new interruption was started, false if nothing happened.
     */
    public boolean interrupt(TimeUtils timeUtils)
    {
        if (isStarted() && !isInterrupted()) {
            mCurrentInterruption = new CurrentInterruption(
                    timeUtils.getNow(),
                    getLatestRecordedInterruptionReason());
            return true;
        }
        return false;
    }
    
    /**
     * Save the ongoing interruption and resume the session. This only does anything if this
     * CurrentSession was already interrupted.
     *
     * @param timeUtils Needed to save the interruption.
     *
     * @return true if the session resumed, false if nothing happened.
     */
    public boolean resume(TimeUtils timeUtils)
    {
        if (isInterrupted()) {
            Interruption newInterruption = mCurrentInterruption.getSnapshot(timeUtils);
            mRecordedInterruptions.add(newInterruption);
            mInterruptionsTotalDurationCache += newInterruption.getDurationMillis();
            clearCurrentInterruption();
            return true;
        }
        return false;
    }
    
    public boolean isInterrupted()
    {
        return mCurrentInterruption != null;
    }
    
    public long getOngoingInterruptionDurationMillis(TimeUtils timeUtils)
    {
        if (!isInterrupted()) {
            return 0;
        }
        return timeUtils.getNow().getTime() - mCurrentInterruption.startTime.getTime();
    }
    
    /**
     * @return The total interruption time in milliseconds. If there is a current ongoing
     * interruption, that time is also added.
     */
    public long getInterruptionsTotalDuration(TimeUtils timeUtils)
    {
        return mInterruptionsTotalDurationCache + getOngoingInterruptionDurationMillis(timeUtils);
    }
    
    /**
     * @return the duration minus the total interruption time (in millis)
     */
    public long getDurationMinusInterruptions(TimeUtils timeUtils)
    {
        return getOngoingDurationMillis(timeUtils) - getInterruptionsTotalDuration(timeUtils);
    }
    
    // TEST NEEDED [21-07-17 9:08PM] -- .
    
    /**
     * If there is no current interruption, then the latest reason is the latest recorded
     * interruption reason if there are any, or else null.
     */
    public String getLatestInterruptionReason()
    {
        return isInterrupted() ? mCurrentInterruption.reason :
                getLatestRecordedInterruptionReason();
    }

//*********************************************************
// private methods
//*********************************************************

    private String getLatestRecordedInterruptionReason()
    {
        if (!mRecordedInterruptions.isEmpty()) {
            return mRecordedInterruptions.get(mRecordedInterruptions.size() - 1).getReason();
        }
        return null;
    }
    
    private void clearCurrentInterruption()
    {
        mCurrentInterruption = null;
    }

//*********************************************************
// private helpers
//*********************************************************

    private static class CurrentInterruption
    {
        Date startTime;
        String reason;
        
        public CurrentInterruption(Date startTime, String reason)
        {
            this.startTime = startTime;
            this.reason = reason;
        }
        
        public Interruption getSnapshot(TimeUtils timeUtils)
        {
            int duration = 0;
            
            if (timeUtils != null) {
                duration = (int) (timeUtils.getNow().getTime() - startTime.getTime());
            }
            
            return new Interruption(startTime, duration, reason);
        }
    }
}
