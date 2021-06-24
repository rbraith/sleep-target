package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.CurrentSessionUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;

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
    private CurrentSessionUiData mSessionUiData;
    private Context mContext;
    private TagRepository mTagRepository;
    private PostSleepDialogViewModel.EntryPoint mEntryPoint;

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
            PostSleepData initialData,
            CurrentSessionUiData currentSessionUiData,
            Context context)
    {
        mContext = context;
        mTagRepository = createTagRepository();
        
        mPostSleepData.setValue(initialData);
        mSessionUiData = currentSessionUiData;
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
        return mSessionUiData.mood;
    }
    
    public LiveData<List<TagUiData>> getTags()
    {
        return Transformations.map(
                mTagRepository.getTagsWithIds(mSessionUiData.tagIds),
                tags -> tags.stream().map(ConvertTag::toUiData).collect(Collectors.toList()));
    }
    
    public String getAdditionalComments()
    {
        return mSessionUiData.additionalComments;
    }
    
    public String getDurationText()
    {
        return mSessionUiData.duration;
    }
    
    public String getEndText()
    {
        return mSessionUiData.end;
    }
    
    public String getStartText()
    {
        return mSessionUiData.start;
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
