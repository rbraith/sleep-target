package com.rbraithwaite.sleepapp.ui_tests;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragmentDirections;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class SessionEditFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void argsAreProperlyDisplayed()
    {
        // TODO [20-11-22 9:52PM] -- this test will need to be updated with comment, etc args
        //  eventually.
        
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        Date testStartTime = calendar.getTime();
        calendar.add(GregorianCalendar.MINUTE, 25);
        Date testEndTime = calendar.getTime();
        
        Bundle args = SessionArchiveFragmentDirections
                .actionSessionArchiveToSessionEdit(testStartTime.getTime(), testEndTime.getTime())
                .getArguments();
        
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.date)))
                .check(matches(withText(formatter.formatDate(testStartTime))));
        onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.date)))
                .check(matches(withText(formatter.formatDate(testEndTime))));
        
        onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.time)))
                .check(matches(withText(formatter.formatTimeOfDay(testStartTime))));
        onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.time)))
                .check(matches(withText(formatter.formatTimeOfDay(testEndTime))));
        
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        testEndTime.getTime() - testStartTime.getTime()))));
    }
}
