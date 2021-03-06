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

import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.rules.RetryInstrumentedTestRule;
import com.rbraithwaite.sleeptarget.test_utils.rules.RetryTestRule;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestHelper;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TimePickerFragmentTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
    @Rule
    public RetryInstrumentedTestRule RetryInstrumentedTestRule = new RetryInstrumentedTestRule();

//*********************************************************
// api
//*********************************************************

    @RetryTestRule.Retry
    @Test
    public void positiveArgs_displayProperInitialTime()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        DialogTestHelper<TimePickerFragment> dialogTestHelper =
                DialogTestHelper.launchDialogWithArgs(
                        TimePickerFragment.class,
                        TimePickerFragment.createArguments("unused", TimeOfDay.of(calendar)));
        
        onTimePicker().check(matches(timePickerWithTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void OnTimeSetListener_receivesCorrectValues()
    {
        GregorianCalendar calendar = new GregorianCalendar(2019, 8, 7, 6, 5);
        
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.setArguments(TimePickerFragment.createArguments("unused", TimeOfDay.of(calendar)));
        
        DialogTestHelper<TimePickerFragment> helper =
                DialogTestHelper.launchProvidedInstance(dialog);
        
        // different from calendar
        final int testHourOfDay = 3;
        final int testMinute = 15;
        
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        TestUtils.performSyncedActivityAction(helper.getScenario(), activity -> {
            dialog.getViewModel(activity).onTimeSet().observe(activity, timeEvent -> {
                if (timeEvent.isFresh()) {
                    TimeOfDay timeOfDay = timeEvent.getExtra();
                    assertThat(timeOfDay.hourOfDay, is(equalTo(testHourOfDay)));
                    assertThat(timeOfDay.minute, is(equalTo(testMinute)));
                    blocker.unblockThread();
                }
            });
        });
        
        // change the time & confirm
        onTimePicker().perform(PickerActions.setTime(testHourOfDay, testMinute));
        DialogTestUtils.pressPositiveButton();
        blocker.blockThread(); // give the callback time to get called
    }
}
