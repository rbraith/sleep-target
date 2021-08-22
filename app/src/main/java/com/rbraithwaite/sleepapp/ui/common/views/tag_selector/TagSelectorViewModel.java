/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleepapp.ui.common.views.tag_selector;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.components.ApplicationComponent;

public class TagSelectorViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private Context mContext;
    private TagRepository mTagRepository;
    
    private boolean mInitializedListItems = false;
    
    private MutableLiveData<List<ListItemData>> mListItems = new MutableLiveData<>();
    private LiveData<ListTrackingData<ListItemData>> mLastListItemChange;
    private EntryPoint mEntryPoint;
    private MutableLiveData<Set<Integer>> mTagEditChangeIndices = new MutableLiveData<>();
    private MutableLiveData<Set<Integer>> mTagExpansionChangedIndices = new MutableLiveData<>();
    private MutableLiveData<Integer> mLastTagSelectionChangeIndex = new MutableLiveData<>();
    private MutableLiveData<List<TagUiData>> mSelectedTags =
            new MutableLiveData<>(new ArrayList<>());
    private int mCurrentActiveEditTagIndex = NO_ACTIVE_EDIT_TAG;
    private int mCurrentExpandedTagIndex = NO_TAG_EXPANDED;

//*********************************************************
// private constants
//*********************************************************

    private static final int NO_TAG_EXPANDED = -1;
    private static final String TAG = "TagSelectorViewModel";

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_ACTIVE_EDIT_TAG = -1;

//*********************************************************
// public helpers
//*********************************************************

    public static class ListItemData
    {
        public TagUiData tagUiData;
        public boolean expanded = false;
        public boolean selected = false;
        public boolean beingEdited = false;
        
        public ListItemData(TagUiData tagUiData)
        {
            this.tagUiData = tagUiData;
        }
    }

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

    public TagSelectorViewModel(Context context)
    {
        mContext = context;
        mTagRepository = createTagRepository();
        
        LiveDataFuture.getValue(
                mTagRepository.getAllTags(), null, lastTagsChange -> {
                    if (!mInitializedListItems) {
                        initializeListItems(lastTagsChange.list);
                    }
                });
    }

//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-06-29 8:43PM] -- make this like SessionArchiveFragmentViewModel
    //  .getLastListItemsChange.
    public LiveData<ListTrackingData<ListItemData>> getLastListItemChange()
    {
        if (mLastListItemChange == null) {
            mLastListItemChange = Transformations.map(
                    mTagRepository.getAllTags(),
                    lastTagsChange -> {
                        if (lastTagsChange == null) {
                            return null;
                        }
                        if (!mInitializedListItems) {
                            initializeListItems(lastTagsChange.list);
                            // HACK [21-06-30 8:54PM] -- I actually shouldn't be creating
                            //  ListTrackingData
                            //  directly like this.
                            return new ListTrackingData<>(0, mListItems.getValue(), null);
                        } else {
                            return updateListItemsWith(lastTagsChange.lastChange);
                        }
                    });
        }
        return mLastListItemChange;
    }
    
    public void setSelectedTagIds(List<Integer> selectedTagIds)
    {
        LiveDataFuture.getValue(mListItems, null, listItems -> {
            List<TagUiData> selectedTags = new ArrayList<>();
            
            Set<Integer> selectedTagIdsSet = new HashSet<>(selectedTagIds);
            for (ListItemData listItem : listItems) {
                if (selectedTagIdsSet.contains(listItem.tagUiData.tagId)) {
                    listItem.selected = true;
                    selectedTags.add(listItem.tagUiData);
                } else {
                    listItem.selected = false;
                }
            }
            
            mSelectedTags.setValue(selectedTags);
        });
    }
    
    public void clearSelectedTags()
    {
        setSelectedTagIds(new ArrayList<>());
    }
    
    public LiveData<Set<Integer>> getTagExpansionChangedIndices()
    {
        return mTagExpansionChangedIndices;
    }
    
    public LiveData<Integer> getLastTagSelectionChangeIndex()
    {
        return mLastTagSelectionChangeIndex;
    }
    
    public LiveData<Set<Integer>> getTagEditChangeIndices()
    {
        return mTagEditChangeIndices;
    }
    
    /**
     * Toggles the edit state of a tag (active/inactive). Like tag expansion, only one tag may be
     * actively edited at a time - if another tag is being actively edited, that tag will become
     * inactive. This also toggles the expansion of the newly active tag.
     */
    // REFACTOR [21-04-19 12:10AM] -- duplicates logic / system w/ toggleTagExpansion.
    public void toggleTagEditState(int tagIndex)
    {
        Set<Integer> changedTags = new HashSet<>();
        changedTags.addAll(_toggleTagEditState(tagIndex));
        if (mListItems.getValue().get(tagIndex).expanded) {
            changedTags.addAll(_toggleTagExpansion(tagIndex));
        }
        mTagEditChangeIndices.setValue(changedTags);
    }
    
    /**
     * Only one tag is expanded at a time. If another tag is already expanded that tag will be
     * collapsed.
     *
     * @param tagIndex The index of the tag to toggle the expansion of.
     */
    public void toggleTagExpansion(int tagIndex)
    {
        mTagExpansionChangedIndices.setValue(_toggleTagExpansion(tagIndex));
    }
    
    public void addTagFromText(String tagText)
    {
        mTagRepository.addTag(new Tag(tagText));
    }
    
    public void deleteTag(int tagIndex)
    {
        TagUiData tag = mListItems.getValue().get(tagIndex).tagUiData;
        mTagRepository.deleteTag(ConvertTag.fromUiData(tag));
    }
    
    public void updateTagText(int tagIndex, String newText)
    {
        TagUiData tag = mListItems.getValue().get(tagIndex).tagUiData;
        tag.text = newText;
        mTagRepository.updateTag(ConvertTag.fromUiData(tag));
    }
    
    public void toggleTagSelection(int tagIndex)
    {
        // REFACTOR [21-04-20 2:45PM] -- should I create a Maybe monad for all these
        //  LiveData.getValue() calls? Or is this solving the wrong problem? I should
        //  consider alternatives to this LiveData situation before I make a Maybe.
        ListItemData listItem = mListItems.getValue().get(tagIndex);
        listItem.selected = !listItem.selected;
        
        mLastTagSelectionChangeIndex.setValue(tagIndex);
        
        if (listItem.selected) {
            mSelectedTags.getValue().add(listItem.tagUiData);
        } else {
            // remove the id value, not the list index
            mSelectedTags.getValue().remove(listItem.tagUiData);
        }
        LiveDataUtils.refresh(mSelectedTags);
    }
    
    public LiveData<List<TagUiData>> getSelectedTags()
    {
        return mSelectedTags;
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

    private void initializeListItems(List<Tag> initialTags)
    {
        List<ListItemData> result = new ArrayList<>();
        for (Tag tag : initialTags) {
            result.add(new ListItemData(ConvertTag.toUiData(tag)));
        }
        mListItems.setValue(result);
        mInitializedListItems = true;
    }
    
    private EntryPoint getEntryPoint()
    {
        if (mEntryPoint == null) {
            mEntryPoint = EntryPointAccessors.fromApplication(
                    mContext.getApplicationContext(),
                    EntryPoint.class);
        }
        return mEntryPoint;
    }
    
    /**
     * Manage updating the locally cached list items, based on changes to the repository tags list.
     */
    private ListTrackingData<ListItemData> updateListItemsWith(ListTrackingData.ListChange<Tag> change)
    {
        if (change == null) {
            // HACK [21-06-30 8:54PM] -- I actually shouldn't be creating ListTrackingData
            //  directly like this.
            return new ListTrackingData<>(0, mListItems.getValue(), null);
        }
        
        // update the ViewModel's cached list to reflect the repo
        ListItemData listItem = null;
        switch (change.changeType) {
        case ADDED:
            listItem = new ListItemData(ConvertTag.toUiData(change.elem));
            mListItems.getValue().add(listItem);
            break;
        
        case DELETED:
            listItem = mListItems.getValue().remove(change.index);
            if (change.index == mCurrentExpandedTagIndex) {
                mCurrentExpandedTagIndex = NO_TAG_EXPANDED;
            }
            if (listItem.selected) {
                mSelectedTags.getValue().remove(listItem.tagUiData);
                LiveDataUtils.refresh(mSelectedTags);
            }
            break;
        
        case MODIFIED:
            listItem = mListItems.getValue().get(change.index);
            listItem.tagUiData = ConvertTag.toUiData(change.elem);
        }
        
        // HACK [21-06-30 8:54PM] -- I actually shouldn't be creating ListTrackingData
        //  directly like this.
        return new ListTrackingData<>(0, mListItems.getValue(), new ListTrackingData.ListChange<>(
                listItem, change.index, change.changeType));
    }
    
    // REFACTOR [21-04-19 12:28AM] -- Come up with a better name for this.
    private Set<Integer> _toggleTagEditState(int tagIndex)
    {
        ListItemData listItem = mListItems.getValue().get(tagIndex);
        listItem.beingEdited = !listItem.beingEdited;
        
        Set<Integer> changedTags = new HashSet<>();
        changedTags.add(tagIndex);
        
        if (tagIndex != mCurrentActiveEditTagIndex) {
            if (mCurrentActiveEditTagIndex != NO_ACTIVE_EDIT_TAG) {
                mListItems.getValue().get(mCurrentActiveEditTagIndex).beingEdited = false;
                changedTags.add(mCurrentActiveEditTagIndex);
            }
            mCurrentActiveEditTagIndex = tagIndex;
        } else {
            mCurrentActiveEditTagIndex = NO_ACTIVE_EDIT_TAG;
        }
        
        return changedTags;
    }
    
    // REFACTOR [21-04-19 12:21AM] -- Come up with a better name for this.
    private Set<Integer> _toggleTagExpansion(int tagIndex)
    {
        ListItemData listItem = mListItems.getValue().get(tagIndex);
        listItem.expanded = !listItem.expanded;
        
        Set<Integer> changedTags = new HashSet<>();
        changedTags.add(tagIndex);
        
        if (tagIndex != mCurrentExpandedTagIndex) {
            // If a different item was expanded, collapse that item so that only one item is
            // expanded at a time.
            if (mCurrentExpandedTagIndex != NO_TAG_EXPANDED) {
                mListItems.getValue().get(mCurrentExpandedTagIndex).expanded = false;
                changedTags.add(mCurrentExpandedTagIndex);
            }
            mCurrentExpandedTagIndex = tagIndex;
        } else {
            // The same item was toggled, so now no items are expanded.
            mCurrentExpandedTagIndex = NO_TAG_EXPANDED;
        }
        
        return changedTags;
    }
}
