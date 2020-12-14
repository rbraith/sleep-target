package com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment;

import android.os.Bundle;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;
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
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.datePickerWithDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils.launchSessionEditFragmentWithArbitraryDates;
import static com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils.onEndDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils.onEndTimeTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils.onStartDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils.onStartTimeTextView;

//import static androidx.core.content.res.TypedArrayUtils.getString;

@RunWith(AndroidJUnit4.class)
public class SessionEditFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void startTime_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the start time dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onStartTimeTextView().perform(click());
        
        // WHEN the user changes the time and confirms the dialog
        // positive change, the start time remains before the end time
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
        
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        UITestUtils.pressDialogOK();
        
        // THEN the start time is updated
        onStartTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        originalDate.getTime() - calendar.getTime().getTime()))));
    }
    
    // REFACTOR [20-12-5 7:54PM] -- i should separate the different dialog tests to different
    //  modules - start date, start time, end date, end time
    @Test
    public void startTime_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments("test",
                                                          new SessionEditData(testDate, testDate));
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
    public void startTimeDialog_reflectsUpdatedStartTime()
    {
        // GIVEN the user updates the start time from the dialog
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onStartTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, -5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the user reopens the dialog
        onStartTimeTextView().perform(click());
        
        // THEN the dialog reflects the current start time
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void invalidStartTimeDialog_showsError()
    {
        // GIVEN the user has the start time dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper =
                launchSessionEditFragmentWithArbitraryDates();
        
        onStartTimeTextView().perform(click());
        
        // WHEN the user confirms an invalid start time (start > end)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalStartTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10); // set start after end, making it invalid
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the start time is not updated
        onStartTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                originalStartTime))));
        // AND an error message is displayed
        UITestUtils.checkSnackbarIsDisplayedWithMessage(R.string.error_session_edit_start_datetime);
    }
    
    @Test
    public void startDate_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the start date dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onStartDateTextView().perform(click());
        
        // WHEN the user changes the date and confirms the dialog
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
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
    
    @Test
    public void startDate_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments("test",
                                                          new SessionEditData(testDate, testDate));
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
    
    @Test
    public void invalidStartDateDialog_showsError()
    {
        // GIVEN the user has the start date dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper =
                launchSessionEditFragmentWithArbitraryDates();
        
        onStartDateTextView().perform(click());
        
        // WHEN the user confirms an invalid start date (start > end)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalStartDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the start date is not updated
        onStartDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(
                originalStartDate))));
        // AND an error message is displayed
        UITestUtils.checkSnackbarIsDisplayedWithMessage(R.string.error_session_edit_start_datetime);
    }
    
    @Test
    public void startDateDialog_reflectsUpdatedStartDate()
    {
        // GIVEN the user updates the start date from the dialog
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onStartDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
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
    
    @Test
    public void endDate_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments("test",
                                                          new SessionEditData(testDate, testDate));
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        // WHEN the user presses the end date text view
        onEndDateTextView().perform(click());
        
        // THEN a DatePickerDialog is displayed
        onDatePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start date text
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    @Test
    public void endDate_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the end date dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onEndDateTextView().perform(click());
        
        // WHEN the user changes the date and confirms the dialog
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date newDate = calendar.getTime();
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the end date text is updated
        onEndDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        newDate.getTime() - originalDate.getTime()))));
    }
    
    @Test
    public void endDateDialog_reflectsUpdatedEndDate()
    {
        // GIVEN the user updates the end date from the dialog
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onEndDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the user reopens the dialog
        onEndDateTextView().perform(click());
        
        // THEN the dialog reflects the current start date
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    @Test
    public void invalidEndDate_showsError()
    {
        // GIVEN the user has the end date dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper =
                launchSessionEditFragmentWithArbitraryDates();
        
        onEndDateTextView().perform(click());
        
        // WHEN the user confirms an invalid end date (end < start)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalEndDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the end date is not updated
        onEndDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(
                originalEndDate))));
        // AND an error message is displayed
        UITestUtils.checkSnackbarIsDisplayedWithMessage(R.string.error_session_edit_end_datetime);
    }
    
    @Test
    public void endTime_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        long testDate = calendar.getTimeInMillis();
        
        Bundle args = SessionEditFragment.createArguments("test",
                                                          new SessionEditData(testDate, testDate));
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionEditFragment.class, args);
        
        // WHEN the user presses the end time text view
        onEndTimeTextView().perform(click());
        
        // THEN a TimePickerDialog is displayed
        onTimePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start time text
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void endTime_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the end time dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onEndTimeTextView().perform(click());
        
        // WHEN the user changes the time and confirms the dialog
        // positive change, the end time remains after the start time
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
        
        calendar.add(Calendar.HOUR_OF_DAY, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        UITestUtils.pressDialogOK();
        
        // THEN the end time is updated
        onEndTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        calendar.getTime().getTime() - originalDate.getTime()))));
    }
    
    @Test
    public void endTimeDialog_reflectsUpdatedEndTime()
    {
        // GIVEN the user updates the end time from the dialog
        HiltFragmentTestHelper<SessionEditFragment> testHelper
                = launchSessionEditFragmentWithArbitraryDates();
        
        onEndTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the user reopens the dialog
        onEndTimeTextView().perform(click());
        
        // THEN the dialog reflects the current end time
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void invalidEndTimeDialog_showsError()
    {
        // GIVEN the user has the end time dialog open
        HiltFragmentTestHelper<SessionEditFragment> testHelper =
                launchSessionEditFragmentWithArbitraryDates();
        
        onEndTimeTextView().perform(click());
        
        // WHEN the user confirms an invalid end time (end < start)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalEndTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -10); // set end before start, making it invalid
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // THEN the end time is not updated
        onEndTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                originalEndTime))));
        // AND an error message is displayed
        UITestUtils.checkSnackbarIsDisplayedWithMessage(R.string.error_session_edit_end_datetime);
    }
    
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
                SessionEditFragment.createArguments("test",
                                                    new SessionEditData(testStartTime.getTime(),
                                                                        testEndTime.getTime())));
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        onStartDateTextView().check(matches(withText(formatter.formatDate(testStartTime))));
        onEndDateTextView().check(matches(withText(formatter.formatDate(testEndTime))));
        
        onStartTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testStartTime))));
        onEndTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testEndTime))));
        
        onView(withId(R.id.session_edit_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        testEndTime.getTime() - testStartTime.getTime()))));
    }
}
