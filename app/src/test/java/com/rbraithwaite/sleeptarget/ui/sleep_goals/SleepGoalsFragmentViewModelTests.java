/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.sleep_goals;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class SleepGoalsFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepGoalsFragmentViewModel viewModel;
    CurrentGoalsRepository mockCurrentGoalsRepository;
    SleepSessionRepository mockSleepSessionRepository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockCurrentGoalsRepository = mock(CurrentGoalsRepository.class);
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        viewModel = new SleepGoalsFragmentViewModel(
                mockCurrentGoalsRepository,
                mockSleepSessionRepository,
                new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockCurrentGoalsRepository = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void getSucceededSleepDurationGoalDates_returnsEmptyListWhenNoGoals()
    {
        when(mockCurrentGoalsRepository.getSleepDurationGoalHistory()).thenReturn(
                new MutableLiveData<List<SleepDurationGoal>>(new ArrayList<SleepDurationGoal>()));
        
        // SUT
        LiveData<List<Date>> succeededGoalDates = viewModel.getSucceededSleepDurationGoalDates();
        
        // verify
        TestUtils.activateLocalLiveData(succeededGoalDates);
        // NOTE: apparently this needs to come *after* the live data is activated
        shadowOf(Looper.getMainLooper()).idle();
        assertThat(succeededGoalDates.getValue().isEmpty(), is(true));
    }
    
    @Test
    public void getSucceededSleepDurationGoalDates_returnsCorrectDates()
    {
        // setup
        final int year = 2021;
        final int month = 2;
        final int baseDay = 1;
        when(mockCurrentGoalsRepository.getSleepDurationGoalHistory()).thenReturn(
                new MutableLiveData<>(Arrays.asList(
                        // 2 with a goal set
                        new SleepDurationGoal(
                                new GregorianCalendar(year, month, baseDay).getTime(),
                                50),
                        // a goal edited on the same day as the previous - this one is expected
                        new SleepDurationGoal(
                                new GregorianCalendar(year, month, baseDay, 4, 56).getTime(),
                                123),
                        // 4 with no goal set
                        SleepDurationGoal.createWithNoGoal(
                                new GregorianCalendar(year, month, baseDay + 4).getTime()),
                        // 4 days of a different goal
                        new SleepDurationGoal(
                                new GregorianCalendar(year, month, baseDay + 8).getTime(),
                                321))));
        
        TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return new GregorianCalendar(year, month, baseDay + 12).getTime();
            }
        };
        viewModel.setTimeUtils(stubTimeUtils);
        
        when(mockSleepSessionRepository.getSleepSessionsInRangeSynced(any(Date.class),
                                                                      any(Date.class)))
                // first day, a sleep session starting on that day and ending on the next -
                // should meet the goal
                .thenReturn(Arrays.asList(new SleepSession(
                        new GregorianCalendar(year, month, baseDay, 23, 45).getTime(),
                        123 * 60 * 1000))) // 123 minutes in millis
                // second day, a sleep session starts & ends on the day after this day - should
                // meet the goal
                .thenReturn(Arrays.asList(new SleepSession(
                        new GregorianCalendar(year, month, baseDay + 2, 3, 45).getTime(),
                        123 * 60 * 1000)))
                // ninth day, 2 sleep sessions - neither meet the goal (one too short, one too long)
                .thenReturn(Arrays.asList(
                        new SleepSession(
                                new GregorianCalendar(year, month, baseDay + 9, 3, 45).getTime(),
                                12 * 60 * 1000),
                        new SleepSession(
                                new GregorianCalendar(year, month, baseDay + 9, 5, 45).getTime(),
                                500 * 60 * 1000)));
        
        // SUT
        LiveData<List<Date>> succeededGoalDates = viewModel.getSucceededSleepDurationGoalDates();
        
        // verify
        TestUtils.activateLocalLiveData(succeededGoalDates);
        shadowOf(Looper.getMainLooper()).idle();
        
        assertThat(succeededGoalDates.getValue().size(), is(2));
        
        List<GregorianCalendar> expectedSucceededDates = Arrays.asList(
                new GregorianCalendar(year, month, baseDay),
                new GregorianCalendar(year, month, baseDay + 1));
        
        GregorianCalendar succeededCal = new GregorianCalendar();
        for (int i = 0; i < expectedSucceededDates.size(); i++) {
            succeededCal.setTime(succeededGoalDates.getValue().get(i));
            assertThat(succeededCal.get(Calendar.DAY_OF_YEAR),
                       is(equalTo(expectedSucceededDates.get(i).get(Calendar.DAY_OF_YEAR))));
        }
    }
    
    @Test
    public void getSucceededWakeTimeGoalDates_returnsEmptyListWhenNoWakeTimeGoals()
    {
        when(mockCurrentGoalsRepository.getWakeTimeGoalHistory()).thenReturn(
                new MutableLiveData<List<WakeTimeGoal>>(new ArrayList<WakeTimeGoal>()));
        
        // SUT
        LiveData<List<Date>> succeededGoalDates = viewModel.getSucceededWakeTimeGoalDates();
        
        // verify
        TestUtils.activateLocalLiveData(succeededGoalDates);
        shadowOf(Looper.getMainLooper()).idle();
        assertThat(succeededGoalDates.getValue().size(), is(0));
    }
    
    @Test
    public void hasSleepDurationGoal_updatesProperly()
    {
        MutableLiveData<SleepDurationGoal> mockSleepDurationGoal = new MutableLiveData<>(
                SleepDurationGoal.createWithNoGoal());
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(mockSleepDurationGoal);
        
        // SUT
        LiveData<Boolean> hasSleepDurationGoal = viewModel.hasSleepDurationGoal();
        TestUtils.activateLocalLiveData(hasSleepDurationGoal);
        
        // REFACTOR [21-01-29 1:47PM] -- should this be broken off into a separate test?
        //  hasSleepDurationGoal_isFalseWhenNoGoal
        assertThat(hasSleepDurationGoal.getValue(), is(false));
        
        // simulate setting a new goal
        mockSleepDurationGoal.setValue(new SleepDurationGoal(123));
        assertThat(hasSleepDurationGoal.getValue(), is(true));
    }
    
    @Test
    public void getSleepDurationGoalText_updatesFromRepository()
    {
        int testMinutes = 123;
        SleepDurationGoal testSleepDurationGoal = new SleepDurationGoal(testMinutes);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(
                new MutableLiveData<>(testSleepDurationGoal));
        
        LiveData<String> sleepDurationGoalText = viewModel.getSleepDurationGoalText();
        TestUtils.activateLocalLiveData(sleepDurationGoalText);
        
        assertThat(sleepDurationGoalText.getValue(), is(equalTo(
                SleepGoalsFormatting.formatSleepDurationGoal(new SleepDurationGoal(testMinutes)))));
    }
    
    @Test
    public void getSleepDurationGoal_callRepository()
    {
        LiveData<SleepDurationGoal> expected =
                new MutableLiveData<>(new SleepDurationGoal(123));
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(expected);
        LiveData<SleepDurationGoalUIData> goal = viewModel.getSleepDurationGoal();
        TestUtils.activateLocalLiveData(goal);
        assertThat(goal.getValue().hours, is(expected.getValue().getHours()));
        assertThat(goal.getValue().remainingMinutes, is(expected.getValue().getRemainingMinutes()));
    }
    
    @Test
    public void clearSleepDurationGoal_callRepo()
    {
        viewModel.clearSleepDurationGoal();
        verify(mockCurrentGoalsRepository, times(1)).clearSleepDurationGoal();
    }
    
    @Test
    public void clearWakeTime_callsRepo()
    {
        viewModel.clearWakeTime();
        verify(mockCurrentGoalsRepository, times(1)).clearWakeTimeGoal();
    }
    
    @Test
    public void hasWakeTime_updatesProperly()
    {
        MutableLiveData<WakeTimeGoal> mockWakeTimeGoal = new MutableLiveData<>(null);
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(mockWakeTimeGoal);
        
        LiveData<Boolean> hasWakeTime = viewModel.hasWakeTime();
        TestUtils.LocalLiveDataSynchronizer<Boolean> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(hasWakeTime);
        
        assertThat(hasWakeTime.getValue(), is(false));
        
        mockWakeTimeGoal.setValue(TestUtils.ArbitraryData.getWakeTimeGoal());
        synchronizer.sync();
        assertThat(hasWakeTime.getValue(), is(true));
        
        // TODO [20-12-23 2:10AM] -- check with clearWakeTime().
    }
    
    @Test
    public void setWakeTime_updatesRepository()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setWakeTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        ArgumentCaptor<WakeTimeGoal> arg = ArgumentCaptor.forClass(WakeTimeGoal.class);
        verify(mockCurrentGoalsRepository, times(1))
                .setWakeTimeGoal(arg.capture());
        
        WakeTimeGoal wakeTime = arg.getValue();
        GregorianCalendar calendar2 = new GregorianCalendar();
        calendar2.setTime(wakeTime.asDate());
        assertThat(calendar.get(Calendar.HOUR_OF_DAY), is(calendar2.get(Calendar.HOUR_OF_DAY)));
        assertThat(calendar.get(Calendar.MINUTE), is(calendar2.get(Calendar.MINUTE)));
    }
    
    @Test
    public void getWakeTimeText_updatesFromRepository()
    {
        WakeTimeGoal expectedWakeTime = TestUtils.ArbitraryData.getWakeTimeGoal();
        
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(
                new MutableLiveData<>(expectedWakeTime));
        
        LiveData<String> wakeTimeText = viewModel.getWakeTimeText();
        TestUtils.activateLocalLiveData(wakeTimeText);
        
        assertThat(wakeTimeText.getValue(),
                   is(equalTo(SleepGoalsFormatting.formatWakeTimeGoal(expectedWakeTime))));
    }
    
    @Test
    public void getWakeTimeGoalDateMillis_callsRepository()
    {
        LiveData<WakeTimeGoal> expected =
                new MutableLiveData<>(TestUtils.ArbitraryData.getWakeTimeGoal());
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(expected);
        
        LiveData<Long> wakeTimeGoalMillis = viewModel.getWakeTimeGoalDateMillis();
        TestUtils.activateLocalLiveData(wakeTimeGoalMillis);
        assertThat(wakeTimeGoalMillis.getValue(),
                   is(equalTo((long) expected.getValue().asDate().getTime())));
    }
    
    @Test
    public void getDefaultWakeTime_returnsCorrectValue()
    {
        long defaultWakeTime = viewModel.getDefaultWakeTime();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_HOUR);
        calendar.set(Calendar.MINUTE, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_MINUTE);
        
        GregorianCalendar defaultWakeTimeCalendar = new GregorianCalendar();
        defaultWakeTimeCalendar.setTimeInMillis(defaultWakeTime);
        
        assertThat(defaultWakeTimeCalendar.get(Calendar.HOUR_OF_DAY),
                   is(equalTo(calendar.get(Calendar.HOUR_OF_DAY))));
        assertThat(defaultWakeTimeCalendar.get(Calendar.MINUTE),
                   is(equalTo(calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void setSleepDurationGoal_callsRepository()
    {
        SleepDurationGoal testGoal = new SleepDurationGoal(1234);
        
        ArgumentCaptor<SleepDurationGoal> repoArg =
                ArgumentCaptor.forClass(SleepDurationGoal.class);
        viewModel.setSleepDurationGoal(testGoal.getHours(), testGoal.getRemainingMinutes());
        verify(mockCurrentGoalsRepository, times(1))
                .setSleepDurationGoal(repoArg.capture());
        
        SleepDurationGoal sleepDurationGoal = repoArg.getValue();
        assertThat(sleepDurationGoal.inMinutes(), is(equalTo(testGoal.inMinutes())));
    }
}
