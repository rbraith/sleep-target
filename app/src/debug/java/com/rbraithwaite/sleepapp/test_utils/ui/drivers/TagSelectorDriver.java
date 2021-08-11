package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertionFailed;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorRecyclerAdapter;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.utils.interfaces.Action;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.tagValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class TagSelectorDriver
{
//*********************************************************
// private properties
//*********************************************************

    private FragmentTestHelper<?> mOwningFragment;
    private Matcher<View> mTagSelectorView;
    private TagSelectorViewModel mViewModel;
    
//*********************************************************
// private constants
//*********************************************************

    private static final int TAG_NOT_FOUND = -1;
    
//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
    {
        private TagSelectorDriver mOwningTagSelector;
        
        public Assertions(TagSelectorDriver owningTagSelector)
        {
            mOwningTagSelector = owningTagSelector;
        }
        
        public void thereAreNoSelectedTags()
        {
            onView(allOf(withParent(mOwningTagSelector.mTagSelectorView),
                         withId(R.id.tag_selector_add_tags_btn))).check(matches(isDisplayed()));
        }
        
        public void selectedTagsMatch(List<Integer> expectedSelectedTagIds)
        {
            List<TagUiData> selectedTags = mOwningTagSelector.getSelectedTags();
            assertThat(selectedTags, is(notNullValue()));
            assertThat(selectedTags.size(), is(equalTo(expectedSelectedTagIds.size())));
            for (int i = 0; i < selectedTags.size(); i++) {
                assertThat(selectedTags.get(i).tagId, is(equalTo(expectedSelectedTagIds.get(i))));
            }
        }
        
        public void selectedTagsMatchText(List<String> tagTexts)
        {
            List<TagUiData> selectedTags = mOwningTagSelector.getSelectedTags();
            assertThat(selectedTags.size(), is(tagTexts.size()));
            for (int i = 0; i < selectedTags.size(); i++) {
                assertThat(selectedTags.get(i).text, is(equalTo(tagTexts.get(i))));
            }
        }
        
        /**
         * Check that there are tags with these texts, in order.
         */
        public void hasTagsMatching(String... tagTexts)
        {
            mOwningTagSelector.openTagDialog();
            hasTagCount(tagTexts.length);
            for (int i = 0; i < tagTexts.length; i++) {
                tagHasText(tagTexts.length - 1 - i, tagTexts[i]);
            }
            mOwningTagSelector.closeTagDialog();
        }
        
        public void hasTagCount(int expectedCount)
        {
            assertThat(mOwningTagSelector.getTagCount(), is(expectedCount));
        }
        
        public void tagHasText(int tagIndex, String tagText)
        {
            TagUiData tag = mOwningTagSelector.getTagAtIndex(tagIndex);
            assertThat(tag, is(not(nullValue())));
            assertThat(tag.text, is(equalTo(tagText)));
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public TagSelectorDriver(
            FragmentTestHelper<?> owningFragment,
            Matcher<View> tagSelectorView,
            TagSelectorViewModel viewModel)
    {
        mOwningFragment = owningFragment;
        mTagSelectorView = tagSelectorView;
        mViewModel = viewModel;
        assertThat = new Assertions(this);
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Add new tags to the tag selector.
     *
     * @param tagTexts The text values for each new tag.
     *
     * @return A list of the ids of the newly added tags.
     */
    public List<Integer> addTags(List<String> tagTexts)
    {
        openTagDialog();
        List<Integer> addedIds = addTagsInDialog(tagTexts);
        closeTagDialog();
        return addedIds;
    }
    
    /**
     * toggle the selections of the provided tags.
     *
     * @param tagIndices The indices of the tags to toggle.
     */
    public void toggleTagSelections(List<Integer> tagIndices)
    {
        openTagDialog();
        toggleTagSelectionsInDialog(tagIndices);
        closeTagDialog();
    }
    
    public void toggleTagSelectionsById(List<Integer> tagIds)
    {
        openTagDialog();
        toggleTagSelectionsInDialog(convertIdsToIndices(tagIds));
        closeTagDialog();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private List<TagUiData> getSelectedTags()
    {
        return mViewModel.getSelectedTags().getValue();
    }
    
    public void setSelectedTags(List<Tag> tags)
    {
        boolean deselectAll = (tags == null || tags.isEmpty()) && thereAreSelectedTags();
        
        openTagDialog();
        deselectAllTagsInDialog();
        
        if (deselectAll) {
            closeTagDialog();
            return;
        }
        
        List<Integer> tagIdsToSelect = addMissingInDialog(tags);
        
        for (Integer index : getIndicesFromIds(tagIdsToSelect)) {
            toggleTagSelectionInDialog(index);
        }
        closeTagDialog();
    }
    
    private void toggleTagSelectionsInDialog(List<Integer> tagIndices)
    {
        for (Integer tagIndex : tagIndices) {
            toggleTagSelectionInDialog(tagIndex);
        }
    }
    
    private List<Integer> convertIdsToIndices(List<Integer> ids)
    {
        return ids.stream().map(this::getPositionOfTagId).collect(Collectors.toList());
    }

    private int getPositionOfTagId(int id)
    {
        int[] resultWrapper = {TAG_NOT_FOUND};
        // OPTIMIZE [21-08-10 5:41PM] -- this iters over all the tags regardless if the id
        //  has been found.
        forEachTagInDialog(tagForEachData -> {
            if (tagForEachData.listItemData.tagUiData.tagId == id) {
                resultWrapper[0] = tagForEachData.position;
            }
        });
        
        return resultWrapper[0];
    }
    
    private List<Integer> addTagsInDialog(List<String> tagTexts)
    {
        return tagTexts.stream()
                .map(this::addTagInDialog)
                .collect(Collectors.toList());
    }
    
    private List<Integer> getIndicesFromIds(List<Integer> tagIds)
    {
        List<Integer> result = new ArrayList<>();
        
        if (tagIds == null || tagIds.isEmpty()) {
            return result;
        }
        
        forEachTagInDialog(tagForEachData -> {
            if (tagIds.contains(tagForEachData.listItemData.tagUiData.tagId)) {
                result.add(tagForEachData.position);
            }
        });
        
        return result;
    }
    
    /**
     * Add any missing tags from the list
     *
     * @return A list of the existing and new tag ids from the arg list.
     */
    private List<Integer> addMissingInDialog(List<Tag> tags)
    {
        List<Integer> existingTagIds = getExistingTagIds();
        
        List<Integer> existingIdsInTags = new ArrayList<>();
        List<String> tagTextToAdd = new ArrayList<>();
        
        for (Tag tag : tags) {
            if (existingTagIds.contains(tag.getTagId())) {
                existingIdsInTags.add(tag.getTagId());
            } else {
                tagTextToAdd.add(tag.getText());
            }
        }
        
        List<Integer> newIds = addTagsInDialog(tagTextToAdd);
        
        existingIdsInTags.addAll(newIds);
        
        return existingIdsInTags;
    }
    
    private List<Integer> getExistingTagIds()
    {
        List<Integer> result = new ArrayList<>();
        forEachTagInDialog(tagForEachData -> result.add(tagForEachData.listItemData.tagUiData.tagId));
        return result;
    }
    
    private boolean thereAreSelectedTags()
    {
        try {
            assertThat.thereAreNoSelectedTags();
            return false;
        } catch (AssertionError e) {
            return true;
        }
    }
    
    private void performOnTagDialog(Action<TagSelectorDialogFragment> action)
    {
        mOwningFragment.performSyncedFragmentAction(fragment -> {
            TagSelectorDialogFragment dialog =
                    (TagSelectorDialogFragment) ((BaseFragment<?>) fragment).getDialogByTag(
                            TagSelectorController.DIALOG_TAG);
            
            if (dialog == null) {
                // REFACTOR [21-05-7 11:56PM] -- this could be less generic.
                throw new AssertionFailed("Tag selector dialog does not exist.");
            }
            
            action.performOn(dialog);
        });
    }
    
    private void deselectAllTagsInDialog()
    {
        ArrayList<Integer> tagIndicesToDeselect = new ArrayList<>();
        
        forEachTagInDialog(tagForEachData -> {
            if (tagForEachData.listItemData.selected) {
                tagIndicesToDeselect.add(tagForEachData.position);
            }
        });
        
        for (Integer tagIndex : tagIndicesToDeselect) {
            toggleTagSelectionInDialog(tagIndex);
        }
    }
    
    private void forEachTagInDialog(Action<TagForEachData> action)
    {
        performOnTagDialog(dialog -> {
            RecyclerView tagRecycler = dialog.getTagRecycler();
            TagSelectorRecyclerAdapter tagAdapter =
                    (TagSelectorRecyclerAdapter) tagRecycler.getAdapter();
            for (int i = 0; i < tagAdapter.getItemCount(); i++) {
                if (tagAdapter.getItemViewType(i) == TagSelectorRecyclerAdapter.VIEW_TYPE_TAG) {
                    TagSelectorRecyclerAdapter.TagViewHolder vh =
                            (TagSelectorRecyclerAdapter.TagViewHolder) tagRecycler.findViewHolderForAdapterPosition(
                                    i);
                    action.performOn(new TagForEachData(vh.getListItemData(), i));
                }
            }
        });
    }
    
    private TagUiData getTagAtIndex(int tagIndex)
    {
        TagUiData[] resultWrapper = {null};
        
        performOnTagDialog(dialog -> {
            RecyclerView tagRecycler = dialog.getTagRecycler();
            TagSelectorRecyclerAdapter tagAdapter =
                    (TagSelectorRecyclerAdapter) tagRecycler.getAdapter();
            
            if (tagAdapter.getItemViewType(tagIndex) ==
                TagSelectorRecyclerAdapter.VIEW_TYPE_ADD_CUSTOM_BUTTON) {
                return;
            }
            
            resultWrapper[0] = ((TagSelectorRecyclerAdapter.TagViewHolder)
                    tagRecycler.findViewHolderForAdapterPosition(tagIndex)).getListItemData().tagUiData;
        });
        
        return resultWrapper[0];
    }
    
    private int getTagCount()
    {
        int[] resultWrapper = {0};
        
        performOnTagDialog(dialog -> {
            RecyclerView tagRecycler = dialog.getTagRecycler();
            TagSelectorRecyclerAdapter tagAdapter =
                    (TagSelectorRecyclerAdapter) tagRecycler.getAdapter();
            
            resultWrapper[0] = tagAdapter.getItemCount() - 1;
        });
        
        return resultWrapper[0];
    }
    
    private TagUiData getMostRecentTag()
    {
        int tagCount = getTagCount();
        if (tagCount == 0) { return null; }
        return getTagAtIndex(tagCount - 1);
    }
    
    private void toggleTagSelectionInDialog(int tagIndex)
    {
        onView(withTagValue(tagValue(TagSelectorDialogFragment.RECYCLER_TAG)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(tagIndex, click()));
    }
    
    /**
     * Add a tag in the tag dialog.
     *
     * @param tagText the text to give the new tag.
     *
     * @return The id of the new tag.
     */
    private Integer addTagInDialog(String tagText)
    {
        onView(withId(R.id.tag_add_btn)).perform(click());
        onView(withId(R.id.tag_add_btn_edittext)).perform(typeText(tagText),
                                                          pressImeActionButton());
        
        return getMostRecentTag().tagId;
    }
    
    private void openTagDialog()
    {
        try {
            onView(withTagValue(tagValue(TagScrollController.TAGS_TAG))).perform(click());
        } catch (RuntimeException e) {
            // In case there were no currently selected tags
            onView(allOf(withParent(mTagSelectorView),
                         withId(R.id.tag_selector_add_tags_btn))).perform(click());
        }
    }
    
    private void closeTagDialog()
    {
        DialogTestUtils.pressPositiveButton();
    }

//*********************************************************
// private helpers
//*********************************************************

    private static class TagForEachData
    {
        TagSelectorViewModel.ListItemData listItemData;
        int position;
        
        public TagForEachData(TagSelectorViewModel.ListItemData listItemData, int position)
        {
            this.listItemData = listItemData;
            this.position = position;
        }
    }
}
