package com.rbraithwaite.sleepapp.ui_tests.dialog_tests;

import android.content.DialogInterface;
import android.widget.NumberPicker;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DurationPickerTestUtils;
import com.rbraithwaite.sleepapp.ui.dialog.DurationPickerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class DurationPickerFragmentTests
{
//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-01-30 11:53PM] -- project wide: change positive/negative to valid/invalid?.
    @Test
    public void positiveArgs_displayProperValues()
    {
        final int expectedHour = 8;
        final int expectedMinute = 30;
        
        DurationPickerFragment fragment = DurationPickerFragment.createInstance(
                expectedHour,
                expectedMinute,
                null);
        
        DialogTestHelper<DurationPickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(fragment);
        
        helper.performSyncedDialogAction(new DialogTestHelper.SyncedDialogAction<DurationPickerFragment>()
        {
            @Override
            public void perform(DurationPickerFragment dialogFragment)
            {
                NumberPicker hourPicker =
                        dialogFragment.requireDialog().findViewById(R.id.hour_picker);
                NumberPicker minutePicker =
                        dialogFragment.requireDialog().findViewById(R.id.minute_picker);
                
                assertThat(hourPicker.getValue(), is(expectedHour));
                assertThat(minutePicker.getValue(), is(expectedMinute));
            }
        });
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void negativeHourArg_throwsException()
    {
        int invalidHour = -1;
        int validMinute = 23;
        DurationPickerFragment fragment = DurationPickerFragment.createInstance(
                invalidHour,
                validMinute,
                null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void negativeMinuteArg_throwsException()
    {
        int validHour = 5;
        int invalidMinute = -1;
        DurationPickerFragment fragment = DurationPickerFragment.createInstance(
                validHour,
                invalidMinute,
                null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void negativeMinuteArg_cannotBeGreaterThan60()
    {
        int validHour = 5;
        int invalidMinute = 60;
        DurationPickerFragment fragment = DurationPickerFragment.createInstance(
                validHour,
                invalidMinute,
                null);
    }
    
    // BUG [21-03-26 11:07PM] -- flaky test?
    @Test
    public void OnDurationSetListener_ReceivesCorrectValues()
    {
        int startHour = 1;
        final int expectedHour = 2;
        int startMinute = 23;
        final int expectedMinute = 24;
        
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        // SUT
        DurationPickerFragment.OnDurationSetListener testListener =
                new DurationPickerFragment.OnDurationSetListener()
                {
                    @Override
                    public void onDurationSet(
                            DialogInterface dialog,
                            int which,
                            int hour,
                            int minute)
                    {
                        assertThat(hour, is(expectedHour));
                        assertThat(minute, is(expectedMinute));
                        blocker.unblockThread();
                    }
                };
        
        DurationPickerFragment fragment = DurationPickerFragment.createInstance(
                startHour,
                startMinute,
                testListener);
        
        DialogTestHelper<DurationPickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(fragment);
        
        DurationPickerTestUtils.setDuration(expectedHour, expectedMinute);
        DialogTestUtils.pressOK();
        
        blocker.blockThread();
    }
}
