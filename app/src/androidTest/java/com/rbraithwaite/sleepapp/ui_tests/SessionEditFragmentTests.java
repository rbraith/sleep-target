package com.rbraithwaite.sleepapp.ui_tests;

import android.os.Bundle;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.datePickerWithDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class SessionEditFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void startDate_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the start date dialog open
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        Date originalDate = calendar.getTime();
        
        Bundle args = SessionEditFragment.createArguments(calendar.getTimeInMillis(),
                                                          calendar.getTimeInMillis());
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        onStartDateTextView().perform(click());
        
        // WHEN the user changes the date and confirms the dialog
        calendar.set(Calendar.DAY_OF_MONTH,
                     calendar.get(Calendar.DAY_OF_MONTH) - 1); // set start back one day
        Date newDate = calendar.getTime();
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the start date text is updated
        onStartDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        originalDate.getTime() - newDate.getTime()))));
    }
    
    // TODO [20-12-1 12:03AM] -- dialog reflects new set start date when opened.
    
    @Test
    public void startDate_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments(testDate, testDate);
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        // WHEN the user presses the start date text view
        onStartDateTextView().perform(click());
        
        // THEN a DatePickerDialog is displayed
        onDatePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start date text
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    // REFACTOR [20-12-5 7:54PM] -- i should separate the different dialog tests to different
    //  modules - start date, start time, end date, end time
    @Test
    public void startTime_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments(testDate, testDate);
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        // WHEN the user presses the start time text view
        onStartTimeTextView().perform(click());
        
        // THEN a TimePickerDialog is displayed
        onTimePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start time text
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void startDateDialog_reflectsUpdatedStartDate()
    {
        // GIVEN the user updates the start date from the dialog
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        Bundle args = SessionEditFragment.createArguments(calendar.getTimeInMillis(),
                                                          calendar.getTimeInMillis());
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        onStartDateTextView().perform(click());
        
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the user reopens the dialog
        onStartDateTextView().perform(click());
        
        // THEN the dialog reflects the current start date
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    // TODO [20-11-28 9:10PM] -- endDate_displaysCorrectDialogWhenPressed.
    // TODO [20-11-28 9:10PM] -- endTime_displaysCorrectDialogWhenPressed.
    // TODO [20-11-29 7:21PM] -- test that dialog is retained (with correct values) across device
    //  rotation.
    
    // TODO [20-11-28 10:17PM] -- test fragment arg variations
    //  start null, end null, both null, start after end
    
    @Test
    public void argsAreProperlyDisplayed()
    {
        // TODO [20-11-22 9:52PM] -- this test will need to be updated with comment, etc args
        //  eventually.
        
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        Date testStartTime = calendar.getTime();
        calendar.add(GregorianCalendar.MINUTE, 25);
        Date testEndTime = calendar.getTime();
        
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(
                SessionEditFragment.class,
                SessionEditFragment.createArguments(testStartTime.getTime(),
                                                    testEndTime.getTime()));
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        onStartDateTextView().check(matches(withText(formatter.formatDate(testStartTime))));
        onEndDateTextView().check(matches(withText(formatter.formatDate(testEndTime))));
        
        onStartTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testStartTime))));
        onEndTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testEndTime))));
        
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        testEndTime.getTime() - testStartTime.getTime()))));
    }

//*********************************************************
// private methods
//*********************************************************

    private ViewInteraction onStartDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.date)));
    }
    
    private ViewInteraction onEndDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.date)));
    }
    
    private ViewInteraction onStartTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_start_time)), withId(R.id.time)));
    }
    
    private ViewInteraction onEndTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_edit_end_time)), withId(R.id.time)));
    }
}
