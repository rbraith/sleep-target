package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;

public class CurrentSessionBuilder
        implements BuilderOf<CurrentSession>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private String mAdditionalComments;
    private Mood mMood;
    private List<Integer> mSelectedTagIds;
    private List<Interruption> mInterruptions;
    private Interruption mCurrentInterruption;
    
//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionBuilder()
    {
        DateBuilder date = aDate();
        mStart = date.build();
        mAdditionalComments = "some comments";
        mMood = aMood().build();
        
        mSelectedTagIds = new ArrayList<>();
        mSelectedTagIds.add(1);
        mSelectedTagIds.add(2);
        mSelectedTagIds.add(3);
        
        InterruptionBuilder interruption = anInterruption();
        mInterruptions = new ArrayList<>();
        mInterruptions.add(interruption
                                   .withStart(date.addMinutes(5))
                                   .withDurationMinutes(10)
                                   .build());
        mInterruptions.add(interruption
                                   .withStart(date.addMinutes(15))
                                   .withDurationMinutes(5)
                                   .build());
        
        mCurrentInterruption = interruption
                .withStart(date.addMinutes(5))
                .build();
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public CurrentSession build()
    {
        return new CurrentSession(
                mStart,
                mAdditionalComments,
                mMood,
                mSelectedTagIds,
                mInterruptions,
                mCurrentInterruption);
    }
    
//*********************************************************
// api
//*********************************************************

    public CurrentSessionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    public CurrentSessionBuilder withAdditionalComments(String comments)
    {
        mAdditionalComments = comments;
        return this;
    }
    
    public CurrentSessionBuilder withMood(MoodBuilder mood)
    {
        mMood = mood.build();
        return this;
    }
    
    public CurrentSessionBuilder withSelectedTagIds(List<Integer> selectedTagIds)
    {
        mSelectedTagIds = selectedTagIds;
        return this;
    }
    
    public CurrentSessionBuilder withInterruptions(InterruptionBuilder... interruptions)
    {
        ArrayList<InterruptionBuilder> builderList = new ArrayList<>(Arrays.asList(interruptions));
        
        mInterruptions = builderList.stream()
                .map(InterruptionBuilder::build)
                .collect(Collectors.toList());
        
        return this;
    }
    
    public CurrentSessionBuilder withCurrentInterruption(InterruptionBuilder currentInterruption)
    {
        mCurrentInterruption = currentInterruption.build();
        return this;
    }
    
    public CurrentSessionBuilder withNoInterruptions()
    {
        mInterruptions = null;
        mCurrentInterruption = null;
        return this;
    }
    
    public CurrentSessionBuilder withNoCurrentInterruption()
    {
        mCurrentInterruption = null;
        return this;
    }
}
