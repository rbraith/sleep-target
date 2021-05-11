package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.test_utils.TestEqualities;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.CurrentSessionUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PostSleepDialogViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    TagRepository mockTagRepository;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockTagRepository = mock(TagRepository.class);
    }
    
    @After
    public void teardown()
    {
        mockTagRepository = null;
    }
    
    @Test
    public void setRating_updates_getPostSleepData()
    {
        PostSleepDialogViewModel viewModel = createViewModel(new PostSleepData(5f), null);
        
        LiveData<PostSleepData> postSleepData = viewModel.getPostSleepData();
        TestUtils.activateLocalLiveData(postSleepData);
        
        // SUT
        float expectedRating = 1f;
        viewModel.setRating(expectedRating);
        
        assertThat(postSleepData.getValue().rating, is(equalTo(expectedRating)));
    }
    
    @Test
    public void ratingIsZeroWhenPostSleepDataIsNull()
    {
        PostSleepDialogViewModel viewModel = createViewModel(null, null);
        assertThat(viewModel.getRating(), is(0f));
    }
    
    @Test
    public void getStartText_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        assertThat(viewModel.getStartText(), is(equalTo(sessionUiData.start)));
    }
    
    @Test
    public void getEndText_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        assertThat(viewModel.getEndText(), is(equalTo(sessionUiData.end)));
    }
    
    @Test
    public void getDurationText_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        assertThat(viewModel.getDurationText(), is(equalTo(sessionUiData.duration)));
    }
    
    @Test
    public void getMood_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        assertThat(viewModel.getMood(), is(equalTo(sessionUiData.mood)));
    }
    
    @Test
    public void getAdditionalComments_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        assertThat(viewModel.getAdditionalComments(),
                   is(equalTo(sessionUiData.additionalComments)));
    }
    
    @Test
    public void getTags_reflectsConstructor()
    {
        CurrentSessionUiData sessionUiData = TestUtils.ArbitraryData.getCurrentSessionUiData();
        LiveData<List<Tag>> expected =
                new MutableLiveData<>(Arrays.asList(
                        new Tag(1, "tag1"),
                        new Tag(2, "tag2"),
                        new Tag(3, "tag3")));
        when(mockTagRepository.getTagsWithIds(sessionUiData.tagIds)).thenReturn(expected);
        
        PostSleepDialogViewModel viewModel = createViewModel(null, sessionUiData);
        
        // SUT
        LiveData<List<TagUiData>> tagUiDataList = viewModel.getTags();
        TestUtils.activateLocalLiveData(tagUiDataList);
        
        assertThat(tagUiDataList.getValue().size(), is(expected.getValue().size()));
        for (int i = 0; i < tagUiDataList.getValue().size(); i++) {
            Tag expectedTag = expected.getValue().get(i);
            TagUiData uiData = tagUiDataList.getValue().get(i);
            assertThat(TestEqualities.TagUiData_equals_Tag(uiData, expectedTag), is(true));
        }
    }
    
//*********************************************************
// private methods
//*********************************************************

    private PostSleepDialogViewModel createViewModel(
            PostSleepData postSleepData,
            CurrentSessionUiData currentSessionUiData)
    {
        return new PostSleepDialogViewModel(
                postSleepData,
                currentSessionUiData,
                TestUtils.getContext())
        {
            @Override
            protected TagRepository createTagRepository()
            {
                return mockTagRepository;
            }
        };
    }
}
