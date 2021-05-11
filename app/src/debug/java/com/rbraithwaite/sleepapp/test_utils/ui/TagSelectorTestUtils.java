package com.rbraithwaite.sleepapp.test_utils.ui;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorDialogFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.recyclerViewWithCount;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.tagValue;

@Deprecated
public class TagSelectorTestUtils
{
//*********************************************************
// api
//*********************************************************

    public static void openTagDialog()
    {
        try {
            onView(withTagValue(tagValue(TagScrollController.TAGS_TAG))).perform(click());
        } catch (RuntimeException e) {
            // In case there were no currently selected tags
            onView(withId(R.id.tag_selector_add_tags_btn)).perform(click());
        }
    }
    
    public static void addTag(String tagText)
    {
        openTagDialog();
        onView(withId(R.id.tag_add_btn)).perform(click());
        onView(withId(R.id.tag_add_btn_edittext)).perform(typeText(tagText),
                                                          pressImeActionButton());
        DialogTestUtils.pressPositiveButton();
    }
    
    public static void checkTagCountMatches(int expectedCount)
    {
        openTagDialog();
        onView(withTagValue(tagValue(TagSelectorDialogFragment.RECYCLER_TAG)))
                .check(matches(recyclerViewWithCount(
                        expectedCount + 1))); // + 1 for the 'add new tag' btn
        DialogTestUtils.pressPositiveButton();
    }
    
    public static void toggleTagSelection(int tagIndex)
    {
        openTagDialog();
        onView(withTagValue(tagValue(TagSelectorDialogFragment.RECYCLER_TAG)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(tagIndex, click()));
        DialogTestUtils.pressPositiveButton();
    }
}
