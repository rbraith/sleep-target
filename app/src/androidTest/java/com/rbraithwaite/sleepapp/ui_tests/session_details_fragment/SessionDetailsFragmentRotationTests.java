package com.rbraithwaite.sleepapp.ui_tests.session_details_fragment;

import android.content.pm.ActivityInfo;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFormatting;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils.launchSessionDetailsFragmentWithArbitraryData;
import static com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils.onEndDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils.onEndTimeTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils.onStartDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils.onStartTimeTextView;

@RunWith(AndroidJUnit4.class)
public class SessionDetailsFragmentRotationTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void startDate_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the start date
        HiltFragmentTestHelper<SessionDetailsFragment> testHelper =
                launchSessionDetailsFragmentWithArbitraryData();
        
        onStartDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressPositiveButton();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the start date retains its updated value
        onStartDateTextView().check(matches(withText(formatDate(calendar))));
    }
    
    @Test
    public void startTime_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the start time
        HiltFragmentTestHelper<SessionDetailsFragment> testHelper =
                launchSessionDetailsFragmentWithArbitraryData();
        
        onStartTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, -5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressPositiveButton();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the start time retains its updated value
        onStartTimeTextView().check(matches(withText(SessionDetailsFormatting.formatTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)))));
    }
    
    @Test
    public void endDate_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the end date
        HiltFragmentTestHelper<SessionDetailsFragment> testHelper =
                launchSessionDetailsFragmentWithArbitraryData();
        
        onEndDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        DialogTestUtils.pressPositiveButton();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the end date retains its updated value
        onEndDateTextView().check(matches(withText(formatDate(calendar))));
    }
    
    @Test
    public void endTime_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the end time
        HiltFragmentTestHelper<SessionDetailsFragment> testHelper =
                launchSessionDetailsFragmentWithArbitraryData();
        
        onEndTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        DialogTestUtils.pressPositiveButton();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the end time retains its updated value
        onEndTimeTextView().check(matches(withText(SessionDetailsFormatting.formatTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)))));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private String formatDate(GregorianCalendar calendar)
    {
        return SessionDetailsFormatting.formatDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
