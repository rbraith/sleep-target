package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.mood.MoodDialogFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.tagValue;

public class SleepTrackerFragmentTestUtils
{
//*********************************************************
// api
//*********************************************************

    public static void addMood(int moodIndex)
    {
        onView(withId(R.id.mood_selector_add_btn)).perform(click());
        
        onView(withTagValue(tagValue(MoodDialogFragment.formatMoodTag(moodIndex)))).perform(click());
        DialogTestUtils.pressOK();
    }
}
