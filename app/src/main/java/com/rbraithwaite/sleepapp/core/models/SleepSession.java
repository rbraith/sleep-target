package com.rbraithwaite.sleepapp.core.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

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
    private List<Tag> mTags;
    private float mRating;
    private Interruptions mInterruptions;

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
    
    // REFACTOR [21-05-10 3:09PM] -- There are so many properties now - I should probably replace
    //  all these ctors with a builder.

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
        this(id, start, durationMillis, additionalComments, mood, null);
    }
    
    public SleepSession(
            int id,
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments,
            @Nullable Mood mood,
            @Nullable List<Tag> tags)
    {
        this(id, start, durationMillis, additionalComments, mood, tags, null);
    }
    
    public SleepSession(
            int id,
            @NonNull Date start,
            long durationMillis,
            @Nullable String additionalComments,
            @Nullable Mood mood,
            @Nullable List<Tag> tags,
            Float rating)
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
        setTags(tags);
        setRating(rating);
        // REFACTOR [21-05-10 10:01PM] -- this should be ctor injected instead probably.
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = mId;
        result = 31 * result + mStart.hashCode();
        result = 31 * result + (int) (mDurationMillis ^ (mDurationMillis >>> 32));
        result = 31 * result + (mAdditionalComments != null ? mAdditionalComments.hashCode() : 0);
        result = 31 * result + (mMood != null ? mMood.hashCode() : 0);
        result = 31 * result + mTags.hashCode();
        result = 31 * result + (mRating != +0.0f ? Float.floatToIntBits(mRating) : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        SleepSession that = (SleepSession) o;
        
        if (mId != that.mId) { return false; }
        if (mDurationMillis != that.mDurationMillis) { return false; }
        if (Float.compare(that.mRating, mRating) != 0) { return false; }
        if (!mStart.equals(that.mStart)) { return false; }
        if (!Objects.equals(mAdditionalComments, that.mAdditionalComments)) { return false; }
        if (!Objects.equals(mMood, that.mMood)) { return false; }
        return mTags.equals(that.mTags);
    }
    
    @NonNull
    @Override
    public String toString()
    {
        return "SleepSession id:" + getId();
    }

//*********************************************************
// api
//*********************************************************

    public static SleepSession copyOf(SleepSession sleepSession)
    {
        return new SleepSession(
                sleepSession.getId(),
                sleepSession.getStart(),
                sleepSession.getDurationMillis(),
                sleepSession.getAdditionalComments(),
                sleepSession.getMood(),
                sleepSession.getTags(),
                sleepSession.getRating());
    }
    
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
     * Offset the session start by the given hours and minutes in a fixed way (The end date stays
     * the same, so the duration is changed).
     */
    public void offsetStartFixed(int hours, int minutes)
    {
        GregorianCalendar start = TimeUtils.getCalendarFrom(getStart());
        start.add(Calendar.MILLISECOND,
                  (int) TimeUtils.timeToMillis(hours, minutes, 0, 0));
        setStartFixed(start);
    }
    
    /**
     * Offset the session end by the given hours and minutes in a fixed way (The start date stays
     * the same, so the duration is changed).
     */
    public void offsetEndFixed(int hours, int minutes)
    {
        GregorianCalendar end = TimeUtils.getCalendarFrom(getEnd());
        end.add(Calendar.MILLISECOND,
                (int) TimeUtils.timeToMillis(hours, minutes, 0, 0));
        setEndFixed(end);
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
    
    public List<Tag> getTags()
    {
        return mTags;
    }
    
    // TODO [21-05-10 4:19PM] -- test needed for null input behaviour.
    public void setTags(List<Tag> tags)
    {
        mTags = tags == null ? new ArrayList<>() : tags;
    }
    
    public float getRating()
    {
        return mRating;
    }
    
    public void setRating(Float rating)
    {
        mRating = rating == null ? 0f : rating;
    }
    
    public Interruptions getInterruptions()
    {
        return mInterruptions;
    }
    
    public void setInterruptions(Interruptions interruptions)
    {
        mInterruptions = interruptions;
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
