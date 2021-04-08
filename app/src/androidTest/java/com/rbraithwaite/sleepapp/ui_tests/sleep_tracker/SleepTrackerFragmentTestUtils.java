package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.MoodSelectorTestUtils;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SleepTrackerFragmentTestUtils
{
//*********************************************************
// api
//*********************************************************

    public static void addMood(int moodIndex)
    {
        MoodSelectorTestUtils.addMood(withId(R.id.more_context_mood), moodIndex);
    }
}
