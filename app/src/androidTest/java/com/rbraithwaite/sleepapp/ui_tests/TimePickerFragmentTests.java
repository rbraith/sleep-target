package com.rbraithwaite.sleepapp.ui_tests;

import android.widget.TimePicker;

import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.DialogTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
    
    @Test
    public void OnTimeSetListener_receivesCorrectValues()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        
        // different from calendar
        final int testHourOfDay = 3;
        final int testMinute = 15;
        
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.setArguments(TimePickerFragment.createArguments(calendar.getTimeInMillis()));
        
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        dialog.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
            {
                assertThat(hourOfDay, is(equalTo(testHourOfDay)));
                assertThat(minute, is(equalTo(testMinute)));
                blocker.unblockThread();
            }
        });
        DialogTestHelper<TimePickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(dialog);
        
        // change the time & confirm
        onTimePicker().perform(PickerActions.setTime(testHourOfDay, testMinute));
        UITestUtils.pressDialogOK();
        blocker.blockThread(); // give the callback time to get called
    }
}
