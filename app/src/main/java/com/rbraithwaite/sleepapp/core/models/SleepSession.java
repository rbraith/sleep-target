package com.rbraithwaite.sleepapp.core.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class SleepSession
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private Date mStart;
    private long mDurationMillis;
    private TimeUtils mTimeUtils;
    
    private String mAdditionalComments;
    private Mood mMood;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;

//*********************************************************
// public helpers
//*********************************************************

    public static class InvalidDateError
            extends RuntimeException
    {
        public InvalidDateError(String message)
        {
            super(message);
        }
    }
    
    public static class InvalidDurationError
            extends RuntimeException
    {
        public InvalidDurationError(String message)
        {
            super(message);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SleepSession(
            int id,
            @NonNull Date start,
            long durationMillis)
    {
        this(id, start, durationMillis, null);
    }
    
    public SleepSession(
            Date start,
            long durationMillis)
    {
        this(start, durationMillis, null);
    }
    
    public SleepSession(
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments)
    {
        this(0, start, durationMillis, additionalComments, null);
    }
    
    public SleepSession(
            int id,
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments)
    {
        this(id, start, durationMillis, additionalComments, null);
    }
    
    public SleepSession(
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments,
            @Nullable Mood mood)
    {
        this(0, start, durationMillis, additionalComments, mood);
    }
    
    public SleepSession(
            int id,
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments,
            @Nullable Mood mood)
    {
        // OPTIMIZE [21-03-26 2:03AM] -- It's not ideal to always & blindly be validating the inputs
        //  inside here - there are many cases where I can be confident that the input data is
        //  already valid. I need to develop a general & flexible strategy for input validation.
        if (durationMillis < 0) {
            throw new InvalidDurationError("durationMillis cannot be < 0.");
        }
        
        mId = id;
        mStart = start;
        mDurationMillis = durationMillis;
        mAdditionalComments = additionalComments;
        mMood = mood;
        
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    // SMELL [21-03-29 9:50PM] the id is a storage implementation detail - it is not
    //  relevant to the domain model.
    public int getId()
    {
        return mId;
    }
    
    public void setId(int id)
    {
        mId = id;
    }
    
    public Date getStart()
    {
        return mStart;
    }
    
    /**
     * Set the start date & time.
     */
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public long getDurationMillis()
    {
        return mDurationMillis;
    }
    
    public void setDurationMillis(long durationMillis)
    {
        mDurationMillis = durationMillis;
    }
    
    public Date getEnd()
    {
        Date start = getStart();
        if (start == null) {
            return null;
        }
        long durationMillis = getDurationMillis();
        
        return mTimeUtils.getDateFromMillis(
                start.getTime() + durationMillis);
    }
    
    /**
     * Set the start of the sleep session in a fixed way. In other words, change the start while
     * keeping the current end date the same (such that it is the duration that changes).
     *
     * @param start The new start date. If this comes after the current end date an {@link
     *              InvalidDateError} is thrown.
     */
    public void setStartFixed(@NonNull Calendar start)
    {
        if (!isValidStartAndEnd(start.getTime(), getEnd())) {
            throw new InvalidDateError(String.format(
                    "Start date (%s) cannot be after end date (%s)",
                    start.toString(), getEnd().toString()));
        }
        
        mDurationMillis = getEnd().getTime() - start.getTimeInMillis();
        mStart = start.getTime();
    }
    
    /**
     * Set the end of the sleep session in a fixed way. In other words, change the end while keeping
     * the current start date the same (such that it is the duration that changes).
     *
     * @param end The new end date. If this comes before the current start date an {@link
     *            InvalidDateError} is thrown.
     */
    public void setEndFixed(@NonNull Calendar end)
    {
        if (!isValidStartAndEnd(getStart(), end.getTime())) {
            throw new InvalidDateError(String.format(
                    "Start date (%s) cannot be after end date (%s)",
                    getStart().toString(), end.toString()));
        }
        
        mDurationMillis = end.getTimeInMillis() - getStart().getTime();
    }
    
    public String getAdditionalComments()
    {
        return mAdditionalComments;
    }
    
    public void setAdditionalComments(String additionalComments)
    {
        mAdditionalComments = additionalComments;
    }
    
    public Mood getMood()
    {
        return mMood;
    }
    
    public void setMood(Mood mood)
    {
        mMood = mood;
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    private boolean isValidStartAndEnd(Date start, Date end)
    {
        return start.getTime() <= end.getTime();
    }
}
