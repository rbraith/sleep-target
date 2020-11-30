package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.ui.DialogTestHelper;
import com.rbraithwaite.sleepapp.ui.dialog.DatePickerFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.datePickerWithDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;

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
    
    // TODO [20-11-29 7:14PM] -- test null args.
    
    // TODO [20-11-29 7:15PM] -- callback test.
}
