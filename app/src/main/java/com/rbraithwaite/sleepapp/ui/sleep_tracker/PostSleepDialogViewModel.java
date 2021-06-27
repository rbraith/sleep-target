package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;

import java.util.List;
import java.util.stream.Collectors;

import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.components.ApplicationComponent;

public class PostSleepDialogViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<PostSleepData> mPostSleepData = new MutableLiveData<>();
    private Context mContext;
    private TagRepository mTagRepository;
    private PostSleepDialogViewModel.EntryPoint mEntryPoint;

//*********************************************************
// private constants
//*********************************************************

    private final StoppedSessionData mStoppedSessionData;

//*********************************************************
// package helpers
//*********************************************************

    @dagger.hilt.EntryPoint
    @InstallIn(ApplicationComponent.class)
    interface EntryPoint
    {
        TagRepository tagRepository();
    }

//*********************************************************
// constructors
//*********************************************************

    public PostSleepDialogViewModel(
            StoppedSessionData stoppedSessionData,
            Context context)
    {
        mContext = context;
        // REFACTOR [21-06-27 2:52PM] -- This is being applied to a DialogFragment, I should
        //  be using a framework ViewModel and ctor-injecting this.
        mTagRepository = createTagRepository();
        
        mPostSleepData.setValue(stoppedSessionData.postSleepData);
        mStoppedSessionData = stoppedSessionData;
    }

//*********************************************************
// api
//*********************************************************

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
    
    public MoodUiData getMood()
    {
        return ConvertMood.toUiData(mStoppedSessionData.currentSessionSnapshot.mood);
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
    
    public String getDurationText()
    {
        return PostSleepDialogFormatting.formatDuration(mStoppedSessionData.currentSessionSnapshot.durationMillis);
    }
    
    public String getEndText()
    {
        return PostSleepDialogFormatting.formatDate(mStoppedSessionData.currentSessionSnapshot.end);
    }
    
    public String getStartText()
    {
        return PostSleepDialogFormatting.formatDate(mStoppedSessionData.currentSessionSnapshot.start);
    }
    
    public StoppedSessionData getKeptSessionData()
    {
        return new StoppedSessionData(
                mStoppedSessionData.currentSessionSnapshot,
                getPostSleepData().getValue());
    }

//*********************************************************
// protected api
//*********************************************************

    protected TagRepository createTagRepository()
    {
        return getEntryPoint().tagRepository();
    }

//*********************************************************
// private methods
//*********************************************************

    private PostSleepDialogViewModel.EntryPoint getEntryPoint()
    {
        if (mEntryPoint == null) {
            mEntryPoint = EntryPointAccessors.fromApplication(
                    mContext.getApplicationContext(),
                    PostSleepDialogViewModel.EntryPoint.class);
        }
        return mEntryPoint;
    }
}
