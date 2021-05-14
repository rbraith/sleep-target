package com.rbraithwaite.sleepapp.ui_tests.session_details_fragment;

import android.os.Bundle;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

// REFACTOR [20-12-16 9:55PM] -- move this to debug test_utils?
// REFACTOR [20-12-16 10:06PM] -- maybe rename this SessionEditTestUtils? (current name is kinda
//  long)
@Deprecated
public class SessionDetailsFragmentTestUtils
{
    private SessionDetailsFragmentTestUtils() {/* No instantiation */}


    public static void checkStartDateTimeDoesNotMatch(GregorianCalendar datetime)
    {
        DateTimeFormatter formatter = new DateTimeFormatter();
        // REFACTOR [20-12-16 10:02PM] -- call this checkStartDateDoesNotMatch.
        SessionDetailsFragmentTestUtils.onStartDateTextView()
                .check(matches(not(withText(formatter.formatDate(datetime.getTime())))));
        // REFACTOR [20-12-16 10:02PM] -- call this checkStartTimeDoesNotMatch.
        SessionDetailsFragmentTestUtils.onStartTimeTextView()
                .check(matches(not(withText(formatter.formatTimeOfDay(datetime.getTime())))));
    }
    
//*********************************************************
// api
//*********************************************************

    public static void pressPositive()
    {
        onView(withId(R.id.session_data_action_positive)).perform(click());
    }
    
    public static void pressNegative()
    {
        onView(withId(R.id.session_data_action_negative)).perform(click());
    }
    
    public static void setStartDateTime(GregorianCalendar startDateTime)
    {
        setDateTime(onStartDateTextView(), onStartTimeTextView(), startDateTime);
    }
    
    public static void setEndDateTime(GregorianCalendar endDateTime)
    {
        setDateTime(onEndDateTextView(), onEndTimeTextView(), endDateTime);
    }
    
    public static HiltFragmentTestHelper<SessionDetailsFragment> launchWithSleepSession(
            SleepSession sleepSession)
    {
        return HiltFragmentTestHelper.launchFragmentWithArgs(
                SessionDetailsFragment.class,
                SessionDetailsFragment.createArguments(new SessionDetailsFragment.ArgsBuilder(
                        new SleepSessionWrapper(sleepSession)).build()));
    }
    
    /**
     * Uses TestUtils.ArbitraryData.getDate for the start and end times, so that can reliably be
     * used when checking the values of the fragment.
     */
    public static HiltFragmentTestHelper<SessionDetailsFragment> launchSessionDetailsFragmentWithArbitraryData()
    {
        Bundle args = SessionDetailsFragment.createArguments(
                new SessionDetailsFragment.ArgsBuilder(
                        new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSession()))
                        .build());
        return HiltFragmentTestHelper.launchFragmentWithArgs(SessionDetailsFragment.class, args);
    }
    
    /**
     * Uses TestUtils.ArbitraryData.getDate for the start and end times, so that can reliably be
     * used when checking the values of the fragment.
     */
    public static HiltFragmentTestHelper<SessionDetailsFragment> launchWithZeroDuration()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setDurationMillis(0);
        return HiltFragmentTestHelper.launchFragmentWithArgs(
                SessionDetailsFragment.class,
                SessionDetailsFragment.createArguments(
                        new SessionDetailsFragment.ArgsBuilder(
                                new SleepSessionWrapper(sleepSession)).build()));
    }
    
    public static ViewInteraction onStartDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_data_start_time)), withId(R.id.date)));
    }
    
    public static ViewInteraction onEndDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_data_end_time)), withId(R.id.date)));
    }
    
    public static ViewInteraction onStartTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_data_start_time)), withId(R.id.time)));
    }
    
    public static ViewInteraction onEndTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_data_end_time)), withId(R.id.time)));
    }

    private static void setDateTime(
            ViewInteraction date,
            ViewInteraction time,
            GregorianCalendar datetime)
    {
        // REFACTOR [20-12-16 9:58PM] -- call this setDate(GregorianCalendar).
        // set date
        date.perform(click());
        onDatePicker().perform(setDatePickerDate(
                datetime.get(Calendar.YEAR),
                datetime.get(Calendar.MONTH),
                datetime.get(Calendar.DAY_OF_MONTH)));
        DialogTestUtils.pressPositiveButton();
        // REFACTOR [20-12-16 9:58PM] -- call this setTime(GregorianCalendar).
        // set time
        time.perform(click());
        onTimePicker().perform(PickerActions.setTime(
                datetime.get(Calendar.HOUR_OF_DAY),
                datetime.get(Calendar.MINUTE)));
        DialogTestUtils.pressPositiveButton();
    }
}
