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

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;

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
        DateBuilder date = aDate().now().subtractDays(10);
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
    
    public CurrentSessionBuilder withNoDetails()
    {
        return withNoMood().withNoComments().withNoTags();
    }
    
    public CurrentSessionBuilder withNoTags()
    {
        mSelectedTagIds = new ArrayList<>();
        return this;
    }
    
    public CurrentSessionBuilder withNoComments()
    {
        mAdditionalComments = null;
        return this;
    }
    
    public CurrentSessionBuilder withNoMood()
    {
        mMood = null;
        return this;
    }
    
    public CurrentSessionBuilder withSelectedTagIds(Integer... ids)
    {
        mSelectedTagIds = new ArrayList<>(Arrays.asList(ids));
        return this;
    }
}
