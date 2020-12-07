package com.rbraithwaite.sleepapp.ui_tests;

import com.rbraithwaite.sleepapp.test_utils.ui.DialogTestHelper;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;

public class TimePickerFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void positiveArgs_displayProperInitialTime()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        DialogTestHelper<TimePickerFragment> dialogTestHelper =
                DialogTestHelper.launchDialogWithArgs(
                        TimePickerFragment.class,
                        TimePickerFragment.createArguments(calendar.getTimeInMillis()));
        
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    // TODO [20-12-6 7:21PM] -- OnTimeSetListener_receivesCorrectValues.
}
