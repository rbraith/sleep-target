package com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment;

import android.os.Bundle;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

public class SessionEditFragmentTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private SessionEditFragmentTestUtils() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    
    /**
     * Uses TestUtils.ArbitraryData.getDate for the start and end times, so that can reliably be
     * used when checking the values of the fragment.
     */
    public static HiltFragmentTestHelper<SessionEditFragment> launchSessionEditFragmentWithArbitraryDates()
    {
        long dateMillis = TestUtils.ArbitraryData.getDate().getTime();
        Bundle args = SessionEditFragment.createArguments("test",
                                                          new SessionEditData(dateMillis,
                                                                              dateMillis));
        return HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
    }
    
    public static ViewInteraction onStartDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.date)));
    }
    
    public static ViewInteraction onEndDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.date)));
    }
    
    public static ViewInteraction onStartTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.time)));
    }
    
    public static ViewInteraction onEndTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.time)));
    }
}
