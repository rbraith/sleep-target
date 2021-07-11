package com.rbraithwaite.sleepapp.test_utils.value_matchers;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SleepInterruptionEntityMatchers
{
//*********************************************************
// api
//*********************************************************

    public static Matcher<SleepInterruptionEntity> interruptionWithReason(String reason)
    {
        return new TypeSafeMatcher<SleepInterruptionEntity>()
        {
            @Override
            protected boolean matchesSafely(SleepInterruptionEntity item)
            {
                return (item.reason == null && reason == null) ||
                       (item.reason != null && item.reason.equals(reason));
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("SleepInterruptionEntity has reason: " + reason);
            }
        };
    }
}
