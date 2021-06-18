package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodDialogFragment;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorViewModel;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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

//*********************************************************
// private methods
//*********************************************************

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
