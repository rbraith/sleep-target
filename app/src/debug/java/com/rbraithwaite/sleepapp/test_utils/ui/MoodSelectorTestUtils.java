package com.rbraithwaite.sleepapp.test_utils.ui;

import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodDialogFragment;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.tagValue;
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
