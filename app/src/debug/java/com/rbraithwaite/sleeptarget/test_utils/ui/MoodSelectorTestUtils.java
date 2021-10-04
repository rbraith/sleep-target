/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodDialogFragment;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.tagValue;
import static org.hamcrest.Matchers.allOf;

@Deprecated
public class MoodSelectorTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private MoodSelectorTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void addMood(Matcher<View> moodSelector, int moodIndex)
    {
        onView(allOf(withParent(moodSelector),
                     withId(R.id.mood_selector_add_btn))).perform(click());
        
        onView(withTagValue(tagValue(MoodDialogFragment.RECYCLER_TAG))).perform(
                RecyclerViewActions.actionOnItemAtPosition(moodIndex, click()));
        DialogTestUtils.pressPositiveButton();
    }
    
    public static void selectMood(Matcher<View> moodSelector, int moodIndex)
    {
        onView(allOf(withParent(moodSelector),
                     withId(R.id.mood_selector_mood_value))).perform(click());
        
        onView(withTagValue(tagValue(MoodDialogFragment.formatMoodTag(moodIndex)))).perform(click());
        DialogTestUtils.pressPositiveButton();
    }
}
