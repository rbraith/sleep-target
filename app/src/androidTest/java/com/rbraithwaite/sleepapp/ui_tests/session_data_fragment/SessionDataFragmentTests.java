package com.rbraithwaite.sleepapp.ui_tests.session_data_fragment;

import android.os.Bundle;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DurationPickerTestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFormatting;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragment;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

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
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.launchWithSleepSession;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onEndDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onEndTimeTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onStartDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onStartTimeTextView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SessionDataFragmentTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Test
    public void addSleepDurationGoalButton_addsGoal()
    {
        // GIVEN the user is on a session data screen with no sleep duration goal
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setSleepDurationGoal(SleepDurationGoalModel.createWithNoGoal()); //
        // set to empty goal
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // WHEN the user adds a sleep duration goal
        onView(withId(R.id.session_data_add_duration_btn)).perform(click());
        int testHours = 12;
        int testMinutes = 34;
        DurationPickerTestUtils.setDuration(testHours, testMinutes);
        DialogTestUtils.pressOK();
        
        // THEN that new sleep duration goal is displayed on the screen
        onView(withId(R.id.session_data_duration_value)).check(matches(withText(
                SessionDataFormatting.formatSleepDurationGoal(
                        new SleepDurationGoalModel(testHours, testMinutes)))));
    }
    
    @Test
    public void addSleepDurationGoalButton_isDisplayedWhenGoalIsNotSet()
    {
        // GIVEN the user is on the session data screen
        // WHEN no sleep duration goal has been set for the displayed session
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setSleepDurationGoal(SleepDurationGoalModel.createWithNoGoal());
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // THEN the "add new sleep duration goal" button is displayed
        onView(withId(R.id.session_data_duration_layout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.session_data_add_duration_btn)).check(matches(isDisplayed()));
    }
    
    @Test
    public void addSleepDurationGoal_hasCorrectDefaultValue()
    {
        // GIVEN the user is on the session data screen
        // AND no sleep duration goal is set for the session
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setSleepDurationGoal(SleepDurationGoalModel.createWithNoGoal());
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // WHEN the user goes to add a sleep duration goal to the session
        onView(withId(R.id.session_data_add_duration_btn)).perform(click());
        
        // THEN the correct default value is displayed in the dialog
        DurationPickerTestUtils.checkMatchesDuration(
                SessionDataFragmentViewModel.DEFAULT_SLEEP_DURATION_GOAL_HOURS,
                SessionDataFragmentViewModel.DEFAULT_SLEEP_DURATION_GOAL_MINUTES);
    }
    
    @Test
    public void deleteSleepDurationGoalButton_deletesGoal()
    {
        // GIVEN the user is on the session data screen
        // AND there is a set sleep duration goal
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setSleepDurationGoal(TestUtils.ArbitraryData.getSleepDurationGoalModel());
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // WHEN the user deletes the sleep duration goal
        onView(withId(R.id.session_data_delete_duration_btn)).perform(click());
        DialogTestUtils.pressOK();
        
        // THEN the sleep duration goal is deleted
        onView(withId(R.id.session_data_duration_layout)).check(matches(not(isDisplayed())));
    }
    
    @Test
    public void sleepDurationGoal_isDisplayedCorrectlyWhenProvided()
    {
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setSleepDurationGoal(new SleepDurationGoalModel(123));
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(initialData);
        
        ViewInteraction onDurationValue =
                onView(allOf(
                        withId(R.id.session_data_duration_value),
                        withParent(withId(R.id.session_data_duration_layout))));
        
        onDurationValue.check(matches(isDisplayed()));
        onDurationValue.check(matches(withText(
                SessionDataFormatting.formatSleepDurationGoal(initialData.getSleepDurationGoal()))));
        onView(withId(R.id.session_data_add_duration_btn)).check(matches(not(isDisplayed())));
    }
    
    @Test
    public void editSleepDurationGoal_displaysProperDefaultValue()
    {
        // GIVEN the user has set a sleep duration goal in the session data fragment
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(sleepSession);
        
        // WHEN the user goes to edit the sleep duration goal
        onView(withId(R.id.session_data_duration_value)).perform(click());
        
        // THEN the edit dialog displays the correct default value (which is the set goal value)
        SleepDurationGoalModel expected = TestUtils.ArbitraryData.getSleepDurationGoalModel();
        DurationPickerTestUtils.checkMatchesDuration(expected.getHours(),
                                                     expected.getRemainingMinutes());
    }
    
    @Test
    public void editSleepDurationGoal_editsGoal()
    {
        // GIVEN the user goes to edit the sleep duration goal
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        SleepDurationGoalModel initialGoal = new SleepDurationGoalModel(123);
        sleepSession.setSleepDurationGoal(initialGoal);
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(sleepSession);
        
        // WHEN the user edits the values and confirms the dialog
        SleepDurationGoalModel updatedGoal = new SleepDurationGoalModel(321);
        onView(withId(R.id.session_data_duration_value)).perform(click());
        DurationPickerTestUtils.setDuration(updatedGoal.getHours(),
                                            updatedGoal.getRemainingMinutes());
        DialogTestUtils.pressOK();
        
        // THEN the sleep duration goal is updated
        onView(withId(R.id.session_data_duration_value)).check(matches(withText(
                SessionDataFormatting.formatSleepDurationGoal(updatedGoal))));
    }
    
    @Test
    public void wakeTimeDialog_reflectsSetWakeTime()
    {
        // GIVEN the user has set a wake-time goal in the session data fragment
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(sleepSession);
        
        // WHEN the user goes to edit the wake-time goal
        onView(withId(R.id.session_data_goal_waketime)).perform(click());
        
        // THEN the time picker displays the set wake-time goal value (as opposed to the default
        //  value)
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(sleepSession.getWakeTimeGoal());
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void wakeTime_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user goes to edit the wake-time goal
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(TestUtils.ArbitraryData.getSleepSessionModel());
        
        onView(withId(R.id.session_data_goal_waketime)).perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.HOUR_OF_DAY, 5);
        
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        // WHEN the user confirms the dialog
        DialogTestUtils.pressOK();
        
        // THEN the wake-time goal is updated
        onView(withId(R.id.session_data_goal_waketime)).check(matches(withText(
                // REFACTOR [21-01-15 10:46PM] -- this should be SessionDataFragment's
                //  DateTimeFormatter.
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void addWakeTimeButton_isDisplayedWhenWakeTimeIsNull()
    {
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setWakeTimeGoal(null);
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchWithSleepSession(initialData);
        
        onView(withId(R.id.session_data_add_waketime_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.session_data_waketime_layout)).check(matches(not(isDisplayed())));
    }
    
    @Test
    public void deleteWakeTimeButton_deletesWakeTime()
    {
        // GIVEN the user is on the session data screen
        // AND there is a set wake-time goal
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        initialData.setWakeTimeGoal(TestUtils.ArbitraryData.getWakeTimeGoal());
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // WHEN the user deletes the wake-time goal
        onView(withId(R.id.session_data_delete_waketime_btn)).perform(click());
        DialogTestUtils.pressOK();
        
        // THEN the wake-time goal is deleted
        // AND the 'add wake-time goal' button is displayed
        onView(withId(R.id.session_data_waketime_layout)).check(matches(not(isDisplayed())));
        onView(withId(R.id.session_data_add_waketime_btn)).check(matches(isDisplayed()));
    }
    
    @Test
    public void addWakeTimeButton_addsWakeTime()
    {
        // GIVEN the user is on the session data screen
        SleepSessionModel initialData = TestUtils.ArbitraryData.getSleepSessionModel();
        // AND there is no wake-time
        initialData.setWakeTimeGoal(null);
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithSleepSession(initialData);
        
        // WHEN the user clicks the "add wake-time" button
        onView(withId(R.id.session_data_add_waketime_btn)).perform(click());
        // AND confirms the dialog
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        DialogTestUtils.pressOK();
        
        // THEN the display is updated with the new wake-time
        onView(withId(R.id.session_data_goal_waketime)).check(matches(withText(
                // REFACTOR [21-01-15 9:01PM] -- this should be SessionDataFragment's
                //  DateTimeFormatter dependency.
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void startTime_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the start time dialog open
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onStartTimeTextView().perform(click());
        
        // WHEN the user changes the time and confirms the dialog
        // positive change, the start time remains before the end time
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
        
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        DialogTestUtils.pressOK();
        
        // THEN the start time is updated
        onStartTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_data_duration))
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
        // REFACTOR [21-12-29 2:52AM] -- call this SleepSessionData.create().
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession.setStart(calendar.getTime());
        
        Bundle args = SessionDataFragment.createArguments(
                new SessionDataFragment.ArgsBuilder(new SleepSessionWrapper(sleepSession)).build());
        HiltFragmentTestHelper<SessionDataFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionDataFragment.class, args);
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onStartTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, -5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressOK();
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onStartTimeTextView().perform(click());
        
        // WHEN the user confirms an invalid start time (start > end)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalStartTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10); // set start after end, making it invalid
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressOK();
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
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
        
        DialogTestUtils.pressOK();
        
        // THEN the start date text is updated
        onStartDateTextView().check(matches(withText(new DateTimeFormatter().formatDate
                (calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_data_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        originalDate.getTime() - newDate.getTime()))));
    }
    
    @Test
    public void startDate_displaysCorrectDialogWhenPressed()
    {
        // GIVEN the user has the session edit fragment open
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession.setStart(calendar.getTime());
        
        Bundle args = SessionDataFragment.createArguments(
                new SessionDataFragment.ArgsBuilder(new SleepSessionWrapper(sleepSession)).build());
        HiltFragmentTestHelper<SessionDataFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionDataFragment.class, args);
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onStartDateTextView().perform(click());
        
        // WHEN the user confirms an invalid start date (start > end)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalStartDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressOK();
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onStartDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressOK();
        
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
        Bundle args = SessionDataFragment.createArguments(
                new SessionDataFragment.ArgsBuilder(
                        new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSessionModel()))
                        .build());
        HiltFragmentTestHelper<SessionDataFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionDataFragment.class, args);
        
        // WHEN the user presses the end date text view
        onEndDateTextView().perform(click());
        
        // THEN a DatePickerDialog is displayed
        onDatePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start date text
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    @Test
    public void endDate_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the end date dialog open
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
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
        
        DialogTestUtils.pressOK();
        
        // THEN the end date text is updated
        onEndDateTextView().check(matches(withText(
                new DateTimeFormatter().formatDate(calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_data_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        newDate.getTime() - originalDate.getTime()))));
    }
    
    @Test
    public void endDateDialog_reflectsUpdatedEndDate()
    {
        // GIVEN the user updates the end date from the dialog
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onEndDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressOK();
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onEndDateTextView().perform(click());
        
        // WHEN the user confirms an invalid end date (end < start)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalEndDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressOK();
        
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
        Bundle args = SessionDataFragment.createArguments(
                new SessionDataFragment.ArgsBuilder(
                        new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSessionModel()))
                        .build());
        HiltFragmentTestHelper<SessionDataFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(SessionDataFragment.class, args);
        
        // WHEN the user presses the end time text view
        onEndTimeTextView().perform(click());
        
        // THEN a TimePickerDialog is displayed
        onTimePicker().check(matches(isDisplayed()));
        // AND the dialog values match the start time text
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void endTime_updatesWhenPositiveDialogIsConfirmed()
    {
        // GIVEN the user has the end time dialog open
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onEndTimeTextView().perform(click());
        
        // WHEN the user changes the time and confirms the dialog
        // positive change, the end time remains after the start time
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalDate = calendar.getTime();
        
        calendar.add(Calendar.HOUR_OF_DAY, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        DialogTestUtils.pressOK();
        
        // THEN the end time is updated
        onEndTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
        // AND the session duration text is updated
        onView(withId(R.id.session_data_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        calendar.getTime().getTime() - originalDate.getTime()))));
    }
    
    @Test
    public void endTimeDialog_reflectsUpdatedEndTime()
    {
        // GIVEN the user updates the end time from the dialog
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onEndTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressOK();
        
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
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                SessionDataFragmentTestUtils.launchWithZeroDuration();
        
        onEndTimeTextView().perform(click());
        
        // WHEN the user confirms an invalid end time (end < start)
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date originalEndTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -10); // set end before start, making it invalid
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressOK();
        
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
        
        SleepSessionModel sleepSession = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession.setStart(testStartTime);
        sleepSession.setDuration(testEndTime.getTime() - testStartTime.getTime());
        
        HiltFragmentTestHelper<SessionDataFragment> testHelper
                = HiltFragmentTestHelper.launchFragmentWithArgs(
                SessionDataFragment.class,
                SessionDataFragment.createArguments(
                        new SessionDataFragment.ArgsBuilder(new SleepSessionWrapper(sleepSession))
                                .setPositiveIcon(R.drawable.ic_baseline_bar_chart_24)
                                .setNegativeIcon(R.drawable.ic_baseline_nights_stay_24)
                                .build()));
        
        DateTimeFormatter formatter = new DateTimeFormatter();
        
        onStartDateTextView().check(matches(withText(formatter.formatDate(testStartTime))));
        onEndDateTextView().check(matches(withText(formatter.formatDate(testEndTime))));
        
        onStartTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testStartTime))));
        onEndTimeTextView().check(matches(withText(formatter.formatTimeOfDay(testEndTime))));
        
        onView(withId(R.id.session_data_duration))
                .check(matches(withText(new DurationFormatter().formatDurationMillis(
                        testEndTime.getTime() - testStartTime.getTime()))));
        
        // check menu item icons
        // TODO [21-12-30 1:40PM] -- figure out some way to verify that the dynamic menu icons
        //  display correctly.
    }
}
