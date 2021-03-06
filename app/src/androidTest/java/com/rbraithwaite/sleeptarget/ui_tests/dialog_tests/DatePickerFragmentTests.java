/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui_tests.dialog_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestHelper;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DatePickerFragment;
import com.rbraithwaite.sleeptarget.utils.time.Day;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.datePickerWithDate;
import static com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils.onDatePicker;
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
        
        DialogTestHelper.launchDialogWithArgs(
                DatePickerFragment.class,
                DatePickerFragment.createArguments("unused", Day.of(calendar)));
        
        onDatePicker().check(matches(datePickerWithDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))));
    }
    
    // TODO [20-11-29 7:14PM] -- test null fragment args.
    
    @Test
    public void DatePickerViewModel_receivesCorrectValues()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7);
        
        DatePickerFragment dialog = DatePickerFragment.createInstance("unused", Day.of(calendar));
        
        DialogTestHelper<DatePickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(dialog);
    
        final int testYear = 2010;
        final int testMonth = 5;
        final int testDayOfMonth = 20;
    
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        TestUtils.performSyncedActivityAction(helper.getScenario(), activity -> {
            dialog.getViewModel(activity).onDateSet().observe(activity, dateEvent -> {
                if (dateEvent.isFresh()) {
                    Day day = dateEvent.getExtra();
                    assertThat(day.year, is(testYear));
                    assertThat(day.month, is(testMonth));
                    assertThat(day.dayOfMonth, is(testDayOfMonth));
                    blocker.unblockThread();
                }
            });
        });
        
        // change the date & confirm
        onDatePicker().perform(setDatePickerDate(testYear, testMonth, testDayOfMonth));
        DialogTestUtils.pressPositiveButton();
        blocker.blockThread(); // give the callback time to get called
    }
}
