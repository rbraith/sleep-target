package com.rbraithwaite.sleepapp.ui_tests.session_data_fragment;

import android.content.pm.ActivityInfo;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFragment;

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
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.launchSessionDataFragmentWithArbitraryData;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onEndDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onEndTimeTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onStartDateTextView;
import static com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils.onStartTimeTextView;

@RunWith(AndroidJUnit4.class)
public class SessionDataFragmentRotationTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void startDate_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the start date
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchSessionDataFragmentWithArbitraryData();
        
        onStartDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the start date retains its updated value
        onStartDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(calendar.getTime()))));
    }
    
    @Test
    public void startTime_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the start time
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchSessionDataFragmentWithArbitraryData();
        
        onStartTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, -5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the start time retains its updated value
        onStartTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
    }
    
    @Test
    public void endDate_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the end date
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchSessionDataFragmentWithArbitraryData();
        
        onEndDateTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        onDatePicker().perform(setDatePickerDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the end date retains its updated value
        onEndDateTextView().check(matches(withText(new DateTimeFormatter().formatDate(calendar.getTime()))));
    }
    
    @Test
    public void endTime_persistsAcrossOrientationChange()
    {
        // GIVEN the user updates the end time
        HiltFragmentTestHelper<SessionDataFragment> testHelper =
                launchSessionDataFragmentWithArbitraryData();
        
        onEndTimeTextView().perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.MINUTE, 5);
        onTimePicker().perform(PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)));
        
        UITestUtils.pressDialogOK();
        
        // WHEN the device is rotated
        TestUtils.rotateActivitySynced(testHelper.getScenario(),
                                       ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // THEN the end time retains its updated value
        onEndTimeTextView().check(matches(withText(new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
}
