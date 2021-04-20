package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.MoodSelectorTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorController;
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

public class SleepTrackerFragmentTestUtils
{
//*********************************************************
// api
//*********************************************************

    public static void addMood(int moodIndex)
    {
        MoodSelectorTestUtils.addMood(withId(R.id.more_context_mood), moodIndex);
    }
    
    public static void openTagDialog()
    {
        try {
            onView(withTagValue(tagValue(TagSelectorController.SELECTED_TAGS_TAG))).perform(click());
        } catch (NoMatchingViewException e) {
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
        DialogTestUtils.pressOK();
    }
    
    public static void checkTagCountMatches(int expectedCount)
    {
        openTagDialog();
        onView(withTagValue(tagValue(TagSelectorDialogFragment.RECYCLER_TAG)))
                .check(matches(recyclerViewWithCount(
                        expectedCount + 1))); // + 1 for the 'add new tag' btn
        DialogTestUtils.pressOK();
    }
    
    public static void toggleTagSelection(int tagIndex)
    {
        openTagDialog();
        onView(withTagValue(tagValue(TagSelectorDialogFragment.RECYCLER_TAG)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(tagIndex, click()));
        DialogTestUtils.pressOK();
    }
}
