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

package com.rbraithwaite.sleepapp.data.repositories;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.data.convert.ConvertTag;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class TagRepositoryImplTests
{
//*********************************************************
// private properties
//*********************************************************

    private TagRepositoryImpl repository;
    private TagDao mockTagDao;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockTagDao = mock(TagDao.class);
        repository = new TagRepositoryImpl(mockTagDao, new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        repository = null;
        mockTagDao = null;
    }
    
    @Test
    public void getTagsWithIds_callsDao()
    {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        repository.getTagsWithIds(expected);
        verify(mockTagDao, times(1)).getTagsWithIds(expected);
    }
    
    @Test
    public void getAllTags_initialValueHasNoChanges()
    {
        Object[][] entitiesData = {
                // id, text
                {1, "test"},
                {2, "test2"},
                {3, "test3"},
                {4, "test4"},
        };
        List<TagEntity> entities = generateEntities(entitiesData);
        
        when(mockTagDao.getAllTags()).thenReturn(new MutableLiveData<>(entities));
        
        LiveData<ListTrackingData<Tag>> tags = repository.getAllTags();
        TestUtils.activateLocalLiveData(tags);
        
        assertThat(tags.getValue().lastChange, is(nullValue()));
        assertThat(tags.getValue().list.size(), is(entities.size()));
    }
    
    @Test
    public void addTag_updatesTags()
    {
        when(mockTagDao.getAllTags()).thenReturn(new MutableLiveData<>(new ArrayList<>()));
        
        LiveData<ListTrackingData<Tag>> tags = repository.getAllTags();
        TestUtils.activateLocalLiveData(tags);
        assertThat(tags.getValue().list.isEmpty(), is(true));
        
        Tag testTag = new Tag(1, "test");
        // SUT
        repository.addTag(testTag);
        shadowOf(Looper.getMainLooper()).idle(); // needed since ListTrackingLiveData uses postValue
        
        assertThat(tags.getValue().list.size(), is(1));
        ListTrackingData.ListChange<Tag> lastChange = tags.getValue().lastChange;
        assertThat(lastChange, is(notNullValue()));
        assertThat(lastChange.index, is(0));
        assertThat(lastChange.changeType, is(ListTrackingData.ChangeType.ADDED));
    }
    
    @Test
    public void deleteTag_updatesTags()
    {
        Object[][] entitiesData = {{1, "test"}};
        List<TagEntity> entities = generateEntities(entitiesData);
        
        when(mockTagDao.getAllTags()).thenReturn(new MutableLiveData<>(entities));
        
        LiveData<ListTrackingData<Tag>> tags = repository.getAllTags();
        TestUtils.activateLocalLiveData(tags);
        
        // SUT
        Tag testTag = ConvertTag.fromEntity(entities.get(0));
        repository.deleteTag(testTag);
        shadowOf(Looper.getMainLooper()).idle(); // needed since ListTrackingLiveData uses postValue
        
        assertThat(tags.getValue().list.isEmpty(), is(true));
        ListTrackingData.ListChange<Tag> lastChange = tags.getValue().lastChange;
        assertThat(lastChange, is(notNullValue()));
        assertThat(lastChange.index, is(0));
        assertThat(lastChange.changeType, is(ListTrackingData.ChangeType.DELETED));
    }
    
    @Test
    public void updateTag_updatesTags()
    {
        Object[][] entitiesData = {{1, "test"}};
        List<TagEntity> entities = generateEntities(entitiesData);
        
        when(mockTagDao.getAllTags()).thenReturn(new MutableLiveData<>(entities));
        
        LiveData<ListTrackingData<Tag>> tags = repository.getAllTags();
        TestUtils.activateLocalLiveData(tags);
        
        Tag updatedTag = ConvertTag.fromEntity(entities.get(0));
        updatedTag.setText("updated text");
        // SUT
        repository.updateTag(updatedTag);
        shadowOf(Looper.getMainLooper()).idle(); // needed since ListTrackingLiveData uses postValue
        
        assertThat(tags.getValue().list.size(), is(1));
        ListTrackingData.ListChange<Tag> lastChange = tags.getValue().lastChange;
        assertThat(lastChange, is(notNullValue()));
        assertThat(lastChange.index, is(0));
        assertThat(lastChange.changeType, is(ListTrackingData.ChangeType.MODIFIED));
    }



//*********************************************************
// private methods
//*********************************************************


    /**
     * entitiesData is {int, String}
     */
    private List<TagEntity> generateEntities(Object[][] entitiesData)
    {
        List<TagEntity> entities = new ArrayList<>();
        for (Object[] data : entitiesData) {
            TagEntity entity = new TagEntity();
            entity.id = (int) data[0];
            entity.text = (String) data[1];
            entities.add(entity);
        }
        return entities;
    }
}
