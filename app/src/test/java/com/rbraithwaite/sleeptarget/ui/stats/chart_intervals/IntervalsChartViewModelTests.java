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

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalDataSetGenerator;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class IntervalsChartViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    IntervalsChartViewModel viewModel;
    SleepSessionRepository mockSleepSessionRepository;
    CurrentGoalsRepository mockCurrentGoalsRepository;
    IntervalDataSetGenerator mockSleepIntervalsDataSetGenerator;
    Executor executor;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        mockCurrentGoalsRepository = mock(CurrentGoalsRepository.class);
        mockSleepIntervalsDataSetGenerator = mock(IntervalDataSetGenerator.class);
        executor = new TestUtils.SynchronizedExecutor();
        viewModel = new IntervalsChartViewModel(
                mockSleepSessionRepository,
                mockCurrentGoalsRepository,
                mockSleepIntervalsDataSetGenerator,
                executor);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        executor = null;
        mockSleepSessionRepository = null;
        mockSleepIntervalsDataSetGenerator = null;
    }
    
    @Test
    public void hasAnyData_returnsFalseIfRepoIsEmpty()
    {
        when(mockSleepSessionRepository.getTotalSleepSessionCount()).thenReturn(
                new MutableLiveData<>(0));
        
        LiveData<Boolean> hasAnyData = viewModel.hasAnyData();
        TestUtils.activateLocalLiveData(hasAnyData);
        
        assertThat(hasAnyData.getValue(), is(false));
    }
    
    @Test
    public void hasAnyData_returnsTrueWhenThereIsData()
    {
        when(mockSleepSessionRepository.getTotalSleepSessionCount()).thenReturn((
                                                                                        new MutableLiveData<>(
                                                                                                1)));
        
        LiveData<Boolean> hasAnyData = viewModel.hasAnyData();
        TestUtils.activateLocalLiveData(hasAnyData);
        
        assertThat(hasAnyData.getValue(), is(true));
    }
    
    @Test
    public void getIntervalsResolution_returns_WEEK_byDefault()
    {
        assertThat(viewModel.getIntervalsResolution(),
                   is(equalTo(IntervalsDataSet.Resolution.WEEK)));
    }
    
    @Test
    public void getIntervalsResolution_reflects_setIntervalsResolution()
    {
        viewModel.setIntervalsResolution(IntervalsDataSet.Resolution.MONTH);
        assertThat(viewModel.getIntervalsResolution(),
                   is(equalTo(IntervalsDataSet.Resolution.MONTH)));
    }
    
    @Test
    public void intervalsRangeReflectsResolution()
    {
        avoidIntervalsDataSetNullPointer();
        
        LiveData<IntervalsDataSet> intervalsDataSet = viewModel.getIntervalsDataSet();
        TestUtils.activateLocalLiveData(intervalsDataSet);
        
        viewModel.setIntervalsResolution(IntervalsDataSet.Resolution.MONTH);
        
        // These are called twice: first from the activation of the LiveData, then again when
        // the resolution changes.
        verify(mockSleepSessionRepository, times(2))
                .getSleepSessionsInRange(any(Date.class), any(Date.class));
        // TODO [21-10-12 11:51PM] -- upgrading mockito revealed that sleepSessions has been
        //  null the whole time - double check that this is the proper behaviour.
        verify(mockSleepIntervalsDataSetGenerator, times(2))
                .generateFromConfig(isNull(), anyList(), any(IntervalsDataSet.Config.class));
    }
    
    @Test
    public void configureIntervalsDataSet_updates_getIntervalsDataSet()
    {
        avoidIntervalsDataSetNullPointer();
        
        LiveData<IntervalsDataSet> intervalsDataSet = viewModel.getIntervalsDataSet();
        TestUtils.activateLocalLiveData(intervalsDataSet);
        
        // SUT
        IntervalsDataSet.Config testConfig = new IntervalsDataSet.Config(
                TestUtils.ArbitraryData.getDateRange(),
                12345,
                false,
                IntervalsDataSet.Resolution.WEEK);
        viewModel.configureIntervalsDataSet(testConfig);
        shadowOf(Looper.getMainLooper()).idle();
        
        verify(mockSleepSessionRepository, times(1))
                .getSleepSessionsInRange(
                        testConfig.dateRange.getStart(),
                        testConfig.dateRange.getEnd());
        // TODO [21-10-12 11:51PM] -- upgrading mockito revealed that sleepSessions has been
        //  null the whole time - double check that this is the proper behaviour.
        verify(mockSleepIntervalsDataSetGenerator, times(1))
                .generateFromConfig(isNull(), anyList(), eq(testConfig));
    }
    
    @Test
    public void stepIntervalsRange_updatesIntervalsConfig()
    {
        final GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        final TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return date.getTime();
            }
        };
        viewModel.setTimeUtils(stubTimeUtils);
        
        IntervalsDataSet.Config config1 = viewModel.getIntervalsDataSetConfig();
        long start1 = config1.dateRange.getStart().getTime();
        int diffDays1 = config1.dateRange.getDifferenceInDays();
        
        // SUT forward
        viewModel.stepIntervalsRange(IntervalsChartViewModel.Step.FORWARD);
        
        // verify
        IntervalsDataSet.Config config2 = viewModel.getIntervalsDataSetConfig();
        long start2 = config2.dateRange.getStart().getTime();
        int diffDays2 = config2.dateRange.getDifferenceInDays();
        assertThat(
                // REFACTOR [21-02-28 9:46PM] -- I should be using TimeUnit in TimeUtils.
                TimeUnit.DAYS.convert(start2 - start1, TimeUnit.MILLISECONDS),
                is(equalTo(7L)));
        assertThat(diffDays1, is(equalTo(diffDays2)));
        
        // SUT backward
        viewModel.stepIntervalsRange(IntervalsChartViewModel.Step.BACKWARD);
        
        // verify
        IntervalsDataSet.Config config3 = viewModel.getIntervalsDataSetConfig();
        assertThat(config1, is(equalTo(config3)));
    }

//*********************************************************
// private methods
//*********************************************************

    private void avoidIntervalsDataSetNullPointer()
    {
        when(mockSleepSessionRepository.getSleepSessionsInRange(any(Date.class), any(Date.class)))
                .thenReturn(new MutableLiveData<>(null));
    }
}
