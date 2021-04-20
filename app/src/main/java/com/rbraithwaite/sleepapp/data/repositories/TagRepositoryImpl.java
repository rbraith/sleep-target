package com.rbraithwaite.sleepapp.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.data.convert.ConvertTag;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingData;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

public class TagRepositoryImpl
        implements TagRepository
{
//*********************************************************
// private properties
//*********************************************************

    private TagDao mTagDao;
    private Executor mExecutor;
    
    private ListTrackingLiveData<Tag> mTrackedTags;
    private MediatorLiveData<ListTrackingData<Tag>> mTags;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "TagRepositoryImpl";
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public TagRepositoryImpl(TagDao tagDao, Executor executor)
    {
        mTagDao = tagDao;
        mExecutor = executor;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public LiveData<ListTrackingData<Tag>> getAllTags()
    {
        // SMELL [21-04-17 3:34PM] -- This is all to get around the db LiveData being constantly
        //  updated from the db (I only wanted the first update). It would probably be better to
        //  use an async executor here.
        if (mTags == null) {
            mTags = new MediatorLiveData<>();
            
            LiveData<List<TagEntity>> persistedTags = mTagDao.getAllTags();
            mTags.addSource(persistedTags, entities -> {
                mTags.removeSource(persistedTags); // This source is a one-off, to read the
                // entities value
                // REFACTOR [21-04-18 2:29AM] -- I should probably put this conversion work in a
                //  background thread.
                mTrackedTags = new ListTrackingLiveData<>(convertEntitiesToTags(entities));
                mTags.addSource(mTrackedTags,
                                listTrackingData -> mTags.setValue(listTrackingData));
            });
        }
        return mTags;
    }
    
    @Override
    public void addTag(Tag newTag)
    {
        mExecutor.execute(() -> {
            synchronized (TagRepositoryImpl.this) {
                int newId = (int) mTagDao.addTag(ConvertTag.toEntity(newTag));
                
                if (mTrackedTags != null) {
                    mTrackedTags.add(new Tag(newId, newTag.getText()));
                }
            }
        });
    }
    
    @Override
    public void deleteTag(Tag tag)
    {
        mExecutor.execute(() -> {
            synchronized (TagRepositoryImpl.this) {
                mTagDao.deleteTag(ConvertTag.toEntity(tag));
                
                if (mTrackedTags != null) {
                    mTrackedTags.delete(tag);
                }
            }
        });
    }
    
    @Override
    public void updateTag(Tag tag)
    {
        mExecutor.execute(() -> {
            synchronized (TagRepositoryImpl.this) {
                mTagDao.updateTag(ConvertTag.toEntity(tag));
                
                if (mTrackedTags != null) {
                    mTrackedTags.set(mTrackedTags.getList().indexOf(tag), tag);
                }
            }
        });
    }
    
//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-04-13 10:20PM] -- consider moving this to ConvertTag.
    private List<Tag> convertEntitiesToTags(List<TagEntity> entities)
    {
        List<Tag> tags = new ArrayList<>();
        for (TagEntity entity : entities) {
            tags.add(ConvertTag.fromEntity(entity));
        }
        return tags;
    }
}
