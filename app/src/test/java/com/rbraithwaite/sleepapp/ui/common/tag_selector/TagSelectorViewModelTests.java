package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.list_tracking.ListTrackingData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TagSelectorViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private TagRepository mockTagRepository;
    
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
    public void getSelectedTags_reflects_setSelectedTagIds()
    {
        Tag tag1 = new Tag(1, "test1");
        Tag tag2 = new Tag(2, "test2");
        Tag tag3 = new Tag(3, "test3");
        
        TagSelectorViewModel viewModel = createViewModel(Arrays.asList(tag1, tag2, tag3));
        
        LiveData<List<TagUiData>> selectedTags = viewModel.getSelectedTags();
        TestUtils.activateLocalLiveData(selectedTags);
        
        assertThat(selectedTags.getValue().isEmpty(), is(true));
        
        // SUT
        viewModel.setSelectedTagIds(Arrays.asList(2, 3));
        
        assertThat(selectedTags.getValue().size(), is(2));
        assertThat(selectedTags.getValue().get(0), is(equalTo(ConvertTag.toUiData(tag2))));
        assertThat(selectedTags.getValue().get(1), is(equalTo(ConvertTag.toUiData(tag3))));
    }
    
    @Test
    public void getSelectedTags_hearsDeletedTags()
    {
        Tag tag1 = new Tag(1, "test1");
        Tag tag2 = new Tag(2, "test2");
        Tag tag3 = new Tag(3, "test3");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);
        
        setup_deleteTag_onMockRepoWith(setupMockRepoWithTags(tags));
        
        // start with 2 selected tags
        TagSelectorViewModel viewModel = createViewModel(mockTagRepository);
        viewModel.setSelectedTagIds(Arrays.asList(2, 3));
        
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        LiveData<List<TagUiData>> selectedTags = viewModel.getSelectedTags();
        TestUtils.activateLocalLiveData(selectedTags);
        
        // SUT
        // delete one of the selected tags
        viewModel.deleteTag(1);
        
        assertThat(selectedTags.getValue().size(), is(1));
        assertThat(selectedTags.getValue().get(0), is(equalTo(ConvertTag.toUiData(tag3))));
    }
    
    @Test
    public void toggleTagSelection_updatesSelectedTags()
    {
        Tag tag1 = new Tag(1, "test1");
        
        TagSelectorViewModel viewModel = createViewModel(Arrays.asList(tag1));
        
        // REFACTOR [21-04-14 10:48PM] -- maybe make TestUtils.activateLocalLiveDatas(LiveData...).
        LiveData<List<TagUiData>> selectedTags = viewModel.getSelectedTags();
        TestUtils.activateLocalLiveData(selectedTags);
        LiveData<Integer> lastSelectedTagIndex = viewModel.getLastTagSelectionChangeIndex();
        TestUtils.activateLocalLiveData(lastSelectedTagIndex);
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> listItems =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(listItems);
        
        // SUT
        viewModel.toggleTagSelection(0);
        
        assertThat(selectedTags.getValue().size(), is(1));
        assertThat(selectedTags.getValue().get(0), is(equalTo(ConvertTag.toUiData(tag1))));
        
        assertThat(lastSelectedTagIndex.getValue(), is(equalTo(0)));
        
        assertThat(listItems.getValue().list.get(0).selected, is(true));
    }
    
    @Test
    public void updateTagText_updatesTag()
    {
        Tag tag1 = new Tag(1, "test1");
        
        setup_updateTag_onMockRepoWith(
                setupMockRepoWithTags(Arrays.asList(tag1)));
        
        TagSelectorViewModel viewModel = createViewModel(mockTagRepository);
        
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        
        // SUT
        String expectedText = "updated text";
        viewModel.updateTagText(0, expectedText);
        
        ListTrackingData.ListChange<TagSelectorViewModel.ListItemData> change =
                lastChange.getValue().lastChange;
        assertThat(change.index, is(0));
        assertThat(change.changeType, is(ListTrackingData.ChangeType.MODIFIED));
        assertThat(change.elem.tagUiData.text, is(equalTo(expectedText)));
    }
    
    @Test
    public void addTag_addsTag()
    {
        Tag tag1 = new Tag(1, "test1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag1);
        
        setup_addTag_onMockRepoWith(setupMockRepoWithTags(tags));
        
        TagSelectorViewModel viewModel = createViewModel(mockTagRepository);
        
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        
        // SUT
        String expectedText = "new tag";
        viewModel.addTagFromText(expectedText);
        
        
        ListTrackingData.ListChange<TagSelectorViewModel.ListItemData> change =
                lastChange.getValue().lastChange;
        assertThat(change.index, is(1));
        assertThat(change.changeType, is(ListTrackingData.ChangeType.ADDED));
        assertThat(change.elem.tagUiData.text, is(equalTo(expectedText)));
    }
    
    @Test
    public void deleteTag_deletesTag()
    {
        Tag tag1 = new Tag(1, "test1");
        List<Tag> tags = new ArrayList<>();
        tags.add(tag1);
        
        setup_deleteTag_onMockRepoWith(setupMockRepoWithTags(tags));
        
        TagSelectorViewModel viewModel = createViewModel(mockTagRepository);
        
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        
        // SUT
        viewModel.deleteTag(0);
        
        ListTrackingData.ListChange<TagSelectorViewModel.ListItemData> change =
                lastChange.getValue().lastChange;
        assertThat(change.index, is(0));
        assertThat(change.changeType, is(ListTrackingData.ChangeType.DELETED));
    }
    
    @Test
    public void toggleTagExpansion_togglesExpansion()
    {
        Tag tag1 = new Tag(1, "test1");
        Tag tag2 = new Tag(2, "test2");
        
        TagSelectorViewModel viewModel = createViewModel(Arrays.asList(tag1, tag2));
        
        LiveData<Set<Integer>> changedIndices = viewModel.getTagExpansionChangedIndices();
        TestUtils.activateLocalLiveData(changedIndices);
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        
        List<TagSelectorViewModel.ListItemData> listItems = lastChange.getValue().list;
        
        // expand first tag
        viewModel.toggleTagExpansion(0);
        assertThat(changedIndices.getValue().size(), is(1));
        assertThat(changedIndices.getValue().contains(0), is(true));
        assertThat(listItems.get(0).expanded, is(true));
        
        // expand second tag while first is already expanded, collapsing first tag
        viewModel.toggleTagExpansion(1);
        assertThat(changedIndices.getValue().size(), is(2));
        assertThat(changedIndices.getValue().containsAll(Arrays.asList(1, 0)), is(true));
        assertThat(listItems.get(0).expanded, is(false));
        assertThat(listItems.get(1).expanded, is(true));
        
        // collapse second tag
        viewModel.toggleTagExpansion(1);
        assertThat(changedIndices.getValue().size(), is(1));
        assertThat(changedIndices.getValue().contains(1), is(true));
        assertThat(listItems.get(1).expanded, is(false));
    }
    
    @Test
    public void toggleEditState_togglesEditState()
    {
        Tag tag1 = new Tag(1, "test1");
        Tag tag2 = new Tag(2, "test2");
        
        TagSelectorViewModel viewModel = createViewModel(Arrays.asList(tag1, tag2));
        
        LiveData<Set<Integer>> changedIndices = viewModel.getTagEditChangeIndices();
        TestUtils.activateLocalLiveData(changedIndices);
        LiveData<ListTrackingData<TagSelectorViewModel.ListItemData>> lastChange =
                viewModel.getLastListItemChange();
        TestUtils.activateLocalLiveData(lastChange);
        
        List<TagSelectorViewModel.ListItemData> listItems = lastChange.getValue().list;
        
        // edit first tag
        viewModel.toggleTagEditState(0);
        assertThat(changedIndices.getValue().size(), is(1));
        assertThat(changedIndices.getValue().contains(0), is(true));
        assertThat(listItems.get(0).beingEdited, is(true));
        
        // edit second tag while first is already being edited
        viewModel.toggleTagEditState(1);
        assertThat(changedIndices.getValue().size(), is(2));
        assertThat(changedIndices.getValue().containsAll(Arrays.asList(1, 0)), is(true));
        assertThat(listItems.get(0).beingEdited, is(false));
        assertThat(listItems.get(1).beingEdited, is(true));
        
        // finish editing second tag
        viewModel.toggleTagEditState(1);
        assertThat(changedIndices.getValue().size(), is(1));
        assertThat(changedIndices.getValue().contains(1), is(true));
        assertThat(listItems.get(1).beingEdited, is(false));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private TagSelectorViewModel createViewModel(List<Tag> initialTags)
    {
        when(mockTagRepository.getAllTags()).thenReturn(new MutableLiveData<>(
                new ListTrackingData<>(initialTags, null)));
        
        return createViewModel(mockTagRepository);
    }
    
    private TagSelectorViewModel createViewModel(TagRepository tagRepository)
    {
        return new TagSelectorViewModel(TestUtils.getContext())
        {
            @Override
            protected TagRepository createTagRepository()
            {
                return tagRepository;
            }
        };
    }
    
    /**
     * Set up the mock repo to provide deleted-tag list updates.
     */
    private void setup_deleteTag_onMockRepoWith(MutableLiveData<ListTrackingData<Tag>> tags)
    {
        doAnswer(invocation -> {
            List<Tag> tagList = tags.getValue().list;
            
            Tag tag = (Tag) invocation.getArguments()[0];
            int index = tagList.indexOf(tag);
            
            tagList.remove(index);
            
            tags.setValue(new ListTrackingData<>(
                    tagList,
                    new ListTrackingData.ListChange<>(tag,
                                                      index,
                                                      ListTrackingData.ChangeType.DELETED)));
            
            return null;
        }).when(mockTagRepository).deleteTag(any(Tag.class));
    }
    
    private void setup_updateTag_onMockRepoWith(MutableLiveData<ListTrackingData<Tag>> tags)
    {
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                List<Tag> tagList = tags.getValue().list;
                
                Tag tag = (Tag) invocation.getArguments()[0];
                int index = tagList.indexOf(tag);
                
                tagList.set(index, tag);
                
                tags.setValue(new ListTrackingData<>(
                        tagList,
                        new ListTrackingData.ListChange<>(tag,
                                                          index,
                                                          ListTrackingData.ChangeType.MODIFIED)));
                
                return null;
            }
        }).when(mockTagRepository).updateTag(any(Tag.class));
    }
    
    private void setup_addTag_onMockRepoWith(MutableLiveData<ListTrackingData<Tag>> tags)
    {
        doAnswer(invocation -> {
            List<Tag> tagList = tags.getValue().list;
            
            Tag tag = (Tag) invocation.getArguments()[0];
            
            tagList.add(tag);
            
            tags.setValue(new ListTrackingData<>(
                    tagList,
                    new ListTrackingData.ListChange<>(tag,
                                                      tagList.size() - 1,
                                                      ListTrackingData.ChangeType.ADDED)));
            
            return null;
        }).when(mockTagRepository).addTag(any(Tag.class));
    }
    
    private MutableLiveData<ListTrackingData<Tag>> setupMockRepoWithTags(List<Tag> tagList)
    {
        MutableLiveData<ListTrackingData<Tag>> tags = new MutableLiveData<>(
                new ListTrackingData<>(tagList, null));
        when(mockTagRepository.getAllTags()).thenReturn(tags);
        return tags;
    }
}
