package com.rbraithwaite.sleepapp.ui_tests.dialog_tests;

import android.widget.DatePicker;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.dialog.DatePickerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.datePickerWithDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class DatePickerFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void positiveArgs_displayProperInitialDate()
    {
        GregorianCalendar calendar = new GregorianCalendar(2010, 9, 8);
        
        DialogTestHelper<DatePickerFragment> dialogHelper = DialogTestHelper.launchDialogWithArgs(
                DatePickerFragment.class,
                DatePickerFragment.createArguments(calendar.getTimeInMillis()));
        
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    // TODO [20-11-29 7:14PM] -- test null fragment args.
    
    @Test
    public void OnDateSetListener_receivesCorrectValues()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7);
        
        final int testYear = 2010;
        final int testMonth = 5;
        final int testDayOfMonth = 20;
        
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.setArguments(DatePickerFragment.createArguments(calendar.getTimeInMillis()));
        
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        dialog.setOnDateSetListener(new DatePickerFragment.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
            {
                assertThat(year, is(testYear));
                assertThat(month, is(testMonth));
                assertThat(dayOfMonth, is(testDayOfMonth));
                blocker.unblockThread();
            }
        });
        DialogTestHelper<DatePickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(dialog);
        
        // change the date & confirm
        onDatePicker().perform(setDatePickerDate(testYear, testMonth, testDayOfMonth));
        DialogTestUtils.pressOK();
        blocker.blockThread(); // give the callback time to get called
    }
}
