package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.MoodSelectorTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.TagSelectorTestUtils;

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
    
    public static void openTagDialog()
    {
        TagSelectorTestUtils.openTagDialog();
    }
    
    public static void addTag(String tagText)
    {
        TagSelectorTestUtils.addTag(tagText);
    }
    
    public static void checkTagCountMatches(int expectedCount)
    {
        TagSelectorTestUtils.checkTagCountMatches(expectedCount);
    }
    
    public static void toggleTagSelection(int tagIndex)
    {
        TagSelectorTestUtils.toggleTagSelection(tagIndex);
    }
}
