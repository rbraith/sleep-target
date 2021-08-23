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

package com.rbraithwaite.sleeptarget.ui.stats.chart_durations;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DurationsChartViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    DurationsChartViewModel viewModel;
    SleepSessionRepository mockSleepSessionRepository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        viewModel = new DurationsChartViewModel(mockSleepSessionRepository);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void getRangeDistance_returnsDefaultValueToStart()
    {
        assertThat(viewModel.getRangeDistance(), is(equalTo(viewModel.DEFAULT_RANGE_DISTANCE)));
    }
    
    @Test
    public void getRangeDistance_reflects_setRangeDistance()
    {
        int expected = viewModel.DEFAULT_RANGE_DISTANCE + 5;
        viewModel.setRangeDistance(expected);
        assertThat(viewModel.getRangeDistance(), is(equalTo(expected)));
    }
    
    @Test
    public void getRangeText_reflectsRangeDistance()
    {
        LiveData<String> rangeTextLive = viewModel.getRangeText();
        TestUtils.activateLocalLiveData(rangeTextLive);
        
        assertThat(Integer.valueOf(rangeTextLive.getValue()),
                   is(equalTo(viewModel.DEFAULT_RANGE_DISTANCE)));
    }
    
    @Test
    public void getDataSet_returnsCorrectData()
    {
        // setup
        float expectedRating0 = 1.5f;
        String expectedLabel0 = "5/17";
        double expectedHours0 = 2;
        SleepSession sleepSession0 = new SleepSession(
                new GregorianCalendar(2021, 4, 17, 12, 34).getTime(),
                (long) (expectedHours0 * 60 * 60 * 1000));
        sleepSession0.setRating(expectedRating0);
        
        float expectedRating1 = 2.5f;
        String expectedLabel1 = "5/18";
        double expectedHours1 = 3;
        SleepSession sleepSession1 = new SleepSession(
                new GregorianCalendar(2021, 4, 18, 12, 34).getTime(),
                (long) (expectedHours1 * 60 * 60 * 1000));
        sleepSession1.setRating(expectedRating1);
        
        List<SleepSession> testSleepSessions = new ArrayList<>();
        testSleepSessions.add(sleepSession0);
        testSleepSessions.add(sleepSession1);
        
        when(mockSleepSessionRepository.getLatestSleepSessionsFromOffset(
                viewModel.DEFAULT_RANGE_OFFSET,
                viewModel.DEFAULT_RANGE_DISTANCE)).thenReturn(new MutableLiveData<>(
                testSleepSessions));
        
        // SUT
        LiveData<List<DurationsChartViewModel.DataPoint>> datasetLive = viewModel.getDataSet();
        TestUtils.activateLocalLiveData(datasetLive);
        
        // verify
        List<DurationsChartViewModel.DataPoint> dataset = datasetLive.getValue();
        assertThat(dataset.size(), is(equalTo(testSleepSessions.size())));
        
        assertThat(dataset.get(0).label, is(equalTo(expectedLabel0)));
        assertThat(dataset.get(0).sleepDurationHours, is(equalTo(expectedHours0)));
        assertThat(dataset.get(0).sleepRating, is(equalTo(expectedRating0)));
        
        assertThat(dataset.get(1).label, is(equalTo(expectedLabel1)));
        assertThat(dataset.get(1).sleepDurationHours, is(equalTo(expectedHours1)));
        assertThat(dataset.get(1).sleepRating, is(equalTo(expectedRating1)));
    }
    
    @Test
    public void stepRangeBack_doesNothingIfThereIsNoMoreData()
    {
        viewModel.stepRangeBack();
        assertThat(viewModel.getRangeOffset(), is(equalTo(viewModel.DEFAULT_RANGE_OFFSET)));
    }
    
    @Test
    public void stepMethodsStepRangeOffset()
    {
        // add more sleep sessions than the range distance, so that stepping the offset will
        // do something.
        List<SleepSession> sleepSessions = new ArrayList<>();
        for (int i = 0; i < viewModel.DEFAULT_RANGE_DISTANCE + 1; i++) {
            sleepSessions.add(TestUtils.ArbitraryData.getSleepSession());
        }
        
        when(mockSleepSessionRepository.getLatestSleepSessionsFromOffset(anyInt(),
                                                                         anyInt())).thenReturn(
                new MutableLiveData<>(sleepSessions));
        
        // activate the data set live data so that it can be accessed inside stepRangeBack()
        LiveData<List<DurationsChartViewModel.DataPoint>> dataset = viewModel.getDataSet();
        TestUtils.activateLocalLiveData(dataset);
        
        // SUT
        viewModel.stepRangeBack();
        
        // the offset is stepped by the current distance
        assertThat(viewModel.getRangeOffset(), is(equalTo(viewModel.DEFAULT_RANGE_DISTANCE)));
        
        // SUT
        viewModel.stepRangeForward();
        
        assertThat(viewModel.getRangeOffset(), is(equalTo(viewModel.DEFAULT_RANGE_OFFSET)));
    }
    
    @Test
    public void stepRangeForward_doesNothingIfOffsetIsZero()
    {
        viewModel.stepRangeForward();
        assertThat(viewModel.getRangeOffset(), is(equalTo(viewModel.DEFAULT_RANGE_OFFSET)));
    }
}
