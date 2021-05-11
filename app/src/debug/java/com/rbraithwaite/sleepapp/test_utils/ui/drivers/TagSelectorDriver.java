package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagUiData;

import org.hamcrest.Matcher;

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
import static org.hamcrest.Matchers.notNullValue;

public class TagSelectorDriver
{
//*********************************************************
// private properties
//*********************************************************

    private Matcher<View> mTagSelectorView;
    private TagSelectorViewModel mViewModel;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;
    
//*********************************************************
// package properties
//*********************************************************

    // HACK [21-05-8 1:08AM] -- This is no good - it makes a strong assumption about how the
    //  database handles ids. For example, this will become invalid anytime a tag is deleted.
    //  A better solution would be to somehow access the selected tag id from the db or the
    //  TagSelectorViewModel.
    // 0 so that the first time a tag is added this increments to 1 (lowest db id)
    int greatestTagId = 0;
    
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
            List<TagUiData> selectedTags =
                    mOwningTagSelector.mViewModel.getSelectedTags().getValue();
            assertThat(selectedTags, is(notNullValue()));
            assertThat(selectedTags.size(), is(equalTo(expectedSelectedTagIds.size())));
            for (int i = 0; i < selectedTags.size(); i++) {
                assertThat(selectedTags.get(i).tagId, is(equalTo(expectedSelectedTagIds.get(i))));
            }
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public TagSelectorDriver(Matcher<View> tagSelectorView, TagSelectorViewModel viewModel)
    {
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
        List<Integer> addedIds =
                tagTexts.stream()
                        .map(this::addTagInDialog)
                        .collect(Collectors.toList());
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
        for (Integer tagIndex : tagIndices) {
            toggleTagSelectionInDialog(tagIndex);
        }
        closeTagDialog();
    }
    
//*********************************************************
// private methods
//*********************************************************

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
        greatestTagId++;
        return greatestTagId;
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
}
