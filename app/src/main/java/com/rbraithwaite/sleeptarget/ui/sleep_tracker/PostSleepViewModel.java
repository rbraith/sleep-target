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
package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.repositories.TagRepository;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.ConvertInterruption;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;

import java.util.List;
import java.util.stream.Collectors;

public class PostSleepViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private StoppedSessionData mStoppedSessionData;
    private MutableLiveData<Integer> mAction = new MutableLiveData<>(NO_ACTION);
    private boolean mInitialized;
    private TagRepository mTagRepository;
    private MutableLiveData<PostSleepData> mPostSleepData = new MutableLiveData<>();

//*********************************************************
// public constants
//*********************************************************

    // action types
    public static final int KEEP = 0;
    public static final int DISCARD = 1;
    public static final int NO_ACTION = 2;

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public PostSleepViewModel(TagRepository tagRepository)
    {
        mTagRepository = tagRepository;
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * Get the current action then reset the action to NO_ACTION
     */
    public int consumeAction()
    {
        mInitialized = false;
        int temp = mAction.getValue();
        mAction.setValue(NO_ACTION);
        return temp;
    }
    
    // TEST NEEDED [21-08-20 5:19PM]
    public void init(StoppedSessionData stoppedSessionData)
    {
        if (!mInitialized) {
            mStoppedSessionData = stoppedSessionData;
            mPostSleepData.setValue(stoppedSessionData.postSleepData);
            mInitialized = true;
        }
    }
    
    /**
     * De-initialize and return the data.
     */
    public StoppedSessionData consumeData()
    {
        mInitialized = false;
        return new StoppedSessionData(
                mStoppedSessionData.currentSessionSnapshot,
                getPostSleepData().getValue());
    }
    
    // TEST NEEDED [21-08-20 5:19PM]
    
    // TEST NEEDED [21-08-20 5:19PM]
    public void discardData()
    {
        mInitialized = false;
    }
    
    // TEST NEEDED [21-09-6 4:31PM]
    public void onDiscardConfirmed()
    {
        setAction(DISCARD);
    }
    
    public LiveData<Integer> getAction()
    {
        return mAction;
    }
    
    public void setAction(int action)
    {
        mAction.setValue(action);
    }
    
    public LiveData<PostSleepData> getPostSleepData()
    {
        return mPostSleepData;
    }
    
    public float getRating()
    {
        PostSleepData postSleepData = getPostSleepData().getValue();
        return postSleepData == null ? 0f : postSleepData.rating;
    }
    
    public void setRating(float rating)
    {
        mPostSleepData.setValue(new PostSleepData(rating));
    }
    
    public LiveData<List<TagUiData>> getTags()
    {
        return Transformations.map(
                mTagRepository.getTagsWithIds(mStoppedSessionData.currentSessionSnapshot.selectedTagIds),
                tags -> tags.stream().map(ConvertTag::toUiData).collect(Collectors.toList()));
    }
    
    public String getAdditionalComments()
    {
        return mStoppedSessionData.currentSessionSnapshot.additionalComments;
    }
    
    public String getEndText()
    {
        return PostSleepFormatting.formatDate(mStoppedSessionData.currentSessionSnapshot.end);
    }
    
    public String getStartText()
    {
        return PostSleepFormatting.formatDate(mStoppedSessionData.currentSessionSnapshot.start);
    }
    
    public String getDurationText()
    {
        return PostSleepFormatting.formatDuration(mStoppedSessionData.currentSessionSnapshot.durationMillis);
    }
    
    public MoodUiData getMood()
    {
        return ConvertMood.toUiData(mStoppedSessionData.currentSessionSnapshot.mood);
    }
    
    public boolean hasNoInterruptions()
    {
        List<Interruption> interruptions = mStoppedSessionData.currentSessionSnapshot.interruptions;
        return interruptions == null || interruptions.isEmpty();
    }
    
    public String getInterruptionsCountText()
    {
        return PostSleepFormatting.formatInterruptionsCount(
                mStoppedSessionData.currentSessionSnapshot.interruptions);
    }
    
    public String getInterruptionsTotalTimeText()
    {
        return PostSleepFormatting.formatDuration(
                // REFACTOR [21-07-19 7:02PM] -- CurrentSession.Snapshot should have an
                //  Interruptions instead.
                new Interruptions(mStoppedSessionData.currentSessionSnapshot.interruptions).getTotalDuration());
    }
    
    public List<InterruptionListItem> getInterruptionsListItems()
    {
        return mStoppedSessionData.currentSessionSnapshot.interruptions.stream()
                .map(ConvertInterruption::toListItem)
                .collect(Collectors.toList());
    }
}
