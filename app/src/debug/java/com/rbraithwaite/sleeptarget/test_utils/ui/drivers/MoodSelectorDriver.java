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

package com.rbraithwaite.sleeptarget.test_utils.ui.drivers;

import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodDialogFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.tagValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class MoodSelectorDriver
{
//*********************************************************
// private properties
//*********************************************************

    private Matcher<View> mMoodSelectorView;
    private MoodSelectorViewModel mViewModel;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;

//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
    {
        private MoodSelectorDriver mOwningMoodSelector;
        
        public Assertions(MoodSelectorDriver owningMoodSelector)
        {
            mOwningMoodSelector = owningMoodSelector;
        }
        
        public void moodIsUnset()
        {
            onView(allOf(withParent(mOwningMoodSelector.mMoodSelectorView),
                         withId(R.id.mood_selector_add_btn))).check(matches(isDisplayed()));
        }
        
        public void selectedMoodMatches(int expectedMoodIndex)
        {
            MoodUiData mood = mOwningMoodSelector.mViewModel.getMood().getValue();
            assertThat(mood, is(notNullValue()));
            assertThat(mood.asIndex(), is(equalTo(expectedMoodIndex)));
        }
        
        public void selectedMoodMatches(Mood mood)
        {
            if (mood == null) {
                moodIsUnset();
            } else {
                selectedMoodMatches(mood.asIndex());
            }
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public MoodSelectorDriver(Matcher<View> moodSelectorView, MoodSelectorViewModel viewModel)
    {
        mMoodSelectorView = moodSelectorView;
        mViewModel = viewModel;
        assertThat = new Assertions(this);
    }

//*********************************************************
// api
//*********************************************************

    public void addNewMood(int moodIndex)
    {
        openMoodDialog();
        selectMoodInDialog(moodIndex);
        DialogTestUtils.pressPositiveButton();
    }
    
    // REFACTOR [21-06-25 3:33PM] -- this makes addNewMood redundant.
    public void setMood(Mood mood)
    {
        if (selectedMoodEquals(mood)) {
            return;
        }
        openMoodDialog();
        if (mood == null) {
            // delete the mood
            DialogTestUtils.pressNegativeButton();
        } else {
            selectMoodInDialog(mood.asIndex());
            DialogTestUtils.pressPositiveButton();
        }
    }
    
//*********************************************************
// private methods
//*********************************************************

    private boolean selectedMoodEquals(Mood mood)
    {
        if (mood == null) {
            return isMoodUnset();
        } else {
            try {
                assertThat.selectedMoodMatches(mood.asIndex());
                return true;
            } catch (AssertionError e) {
                return false;
            }
        }
    }
    
    private boolean isMoodUnset()
    {
        try {
            assertThat.moodIsUnset();
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    private void selectMoodInDialog(int moodIndex)
    {
        onView(withTagValue(tagValue(MoodDialogFragment.RECYCLER_TAG))).perform(
                RecyclerViewActions.actionOnItemAtPosition(moodIndex, click()));
    }
    
    private void openMoodDialog()
    {
        try {
            onView(allOf(withParent(mMoodSelectorView),
                         withId(R.id.mood_selector_add_btn))).perform(click());
        } catch (RuntimeException e) {
            onView(allOf(withParent(mMoodSelectorView),
                         withId(R.id.mood_selector_mood_value))).perform(click());
        }
    }
}
