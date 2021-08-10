package com.rbraithwaite.sleepapp.core.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.core.models.session.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SleepSession
        extends Session
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private String mAdditionalComments;
    private Mood mMood;
    private List<Tag> mTags;
    private float mRating;
    private Interruptions mInterruptions;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;
    
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
        super(start, durationMillis);
        
        mId = id;
        mAdditionalComments = additionalComments;
        mMood = mood;
        setTags(tags);
        setRating(rating);
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = mId;
        result = 31 * result + super.hashCode();
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
        if (!super.equals(o)) { return false; }
        if (Float.compare(that.mRating, mRating) != 0) { return false; }
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
    
    public boolean hasNoInterruptions()
    {
        return mInterruptions == null || mInterruptions.isEmpty();
    }
    
    public Interruption getInterruption(int interruptionId)
    {
        if (mInterruptions == null) {
            return null;
        }
        
        return mInterruptions.get(interruptionId);
    }
    
    public void deleteInterruption(int interruptionId)
    {
        if (mInterruptions == null) {
            return;
        }
        
        mInterruptions.delete(interruptionId);
    }
    
    public void updateInterruption(Interruption updated)
    {
        if (mInterruptions != null) {
            mInterruptions.update(updated);
        }
    }
    
    public void addInterruption(Interruption interruption)
    {
        if (mInterruptions != null) {
            mInterruptions.add(interruption);
        }
    }
    
    /**
     * Check whether the provided interruption overlaps with any existing interruptions in this
     * sleep session. "Exclusive" means that an existing interruption with the same id as the one to
     * check will be skipped (this is useful for interruptions which have been updated).
     *
     * @param interruptionToCheck The interruption... to check.
     *
     * @return The first overlapping interruption found, or null if no interruptions were
     * overlapping.
     */
    public Interruption checkForInterruptionOverlapExclusive(Interruption interruptionToCheck)
    {
        return mInterruptions == null ? null :
                mInterruptions.checkForOverlapExclusive(interruptionToCheck);
    }
    
    /**
     * @return The sleep duration minus the total interruption time.
     */
    public long getNetDurationMillis()
    {
        Interruptions interruptions = getInterruptions();
        return getDurationMillis() - (interruptions == null ? 0 : interruptions.getTotalDuration());
    }
}
