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

package com.rbraithwaite.sleeptarget.test_utils.ui;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorDialogFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.recyclerViewWithCount;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.tagValue;

@Deprecated
public class TagSelectorTestUtils
{
//*********************************************************
// api
//*********************************************************

    public static void openTagDialog()
    {
        try {
            onView(withId(R.id.tag_selector_selected_recycler)).perform(RecyclerViewActions.actionOnItemAtPosition(
                    0,
                    click()));
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
