package com.rbraithwaite.sleepapp.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingData;

public interface TagRepository
{
//*********************************************************
// abstract
//*********************************************************

    LiveData<ListTrackingData<Tag>> getAllTags();
    void addTag(Tag newTag);
    void deleteTag(Tag tag);
    void updateTag(Tag tag);
}
