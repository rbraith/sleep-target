package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.stats.data.IntervalsDataSetGenerator;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class StatsFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    StatsFragmentViewModel viewModel;
    SleepSessionRepository mockSleepSessionRepository;
    IntervalsDataSetGenerator mockIntervalsDataSetGenerator;
    Executor executor;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        mockIntervalsDataSetGenerator = mock(IntervalsDataSetGenerator.class);
        executor = new TestUtils.SynchronizedExecutor();
        viewModel = new StatsFragmentViewModel(
                mockSleepSessionRepository,
                mockIntervalsDataSetGenerator,
                executor);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        executor = null;
        mockSleepSessionRepository = null;
        mockIntervalsDataSetGenerator = null;
    }
    
    @Test
    public void configureIntervalsDataSet_updates_getIntervalsDataSet()
    {
        // this is just to avoid a NullPointerException
        when(mockSleepSessionRepository.getSleepSessionsInRange(any(Date.class), any(Date.class)))
                .thenReturn(new MutableLiveData<List<SleepSessionModel>>(null));
        
        LiveData<XYMultipleSeriesDataset> intervalsDataSet = viewModel.getIntervalsDataSet();
        TestUtils.activateLocalLiveData(intervalsDataSet);
        
        // SUT
        IntervalsDataSetGenerator.Config testConfig = new IntervalsDataSetGenerator.Config(
                TestUtils.ArbitraryData.getDateRange(),
                false);
        viewModel.configureIntervalsDataSet(testConfig);
        shadowOf(Looper.getMainLooper()).idle();
        
        verify(mockSleepSessionRepository, times(1))
                .getSleepSessionsInRange(
                        testConfig.dateRange.getStart(),
                        testConfig.dateRange.getEnd());
        verify(mockIntervalsDataSetGenerator, times(1))
                .generateFromConfig(anyList(), eq(testConfig));
    }
    
    
    // clarifying AChartEngine behaviour for myself
    @Test
    public void RangeCategorySeries_toXYSeries_createsPairedXPoints()
    {
        RangeCategorySeries testSeries = new RangeCategorySeries("test");
        // data point #1
        testSeries.add(1, 4.5); // this becomes [0](1, 1.0), [1](1, 4.5)
        // data point #2
        testSeries.add(3, 5.4); // this becomes [2](2, 3.0), [3](2, 5.4)
        
        XYSeries testXYSeries = testSeries.toXYSeries();
        
        assertThat(testXYSeries.getItemCount(), is(equalTo(4)));
        
        // range data point #1
        assertThat((int) testXYSeries.getX(0), is(equalTo(1)));
        assertThat(testXYSeries.getY(0), is(equalTo(1.0)));
        assertThat((int) testXYSeries.getX(1), is(equalTo(1)));
        assertThat(testXYSeries.getY(1), is(equalTo(4.5)));
        
        // range data point #2
        assertThat((int) testXYSeries.getX(2), is(equalTo(2)));
        assertThat(testXYSeries.getY(2), is(equalTo(3.0)));
        assertThat((int) testXYSeries.getX(3), is(equalTo(2)));
        assertThat(testXYSeries.getY(3), is(equalTo(5.4)));
    }
}
