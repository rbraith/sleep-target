package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.data.IntervalsDataSetGenerator;

import org.achartengine.model.XYMultipleSeriesDataset;
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
    public void getIntervalsValueText_updatesFromConfig()
    {
        DateRange range = TestUtils.ArbitraryData.getDateRange();
        viewModel.configureIntervalsDataSet(new IntervalsDataSetGenerator.Config(range, false));
        
        LiveData<String> valueText = viewModel.getIntervalsValueText();
        TestUtils.activateLocalLiveData(valueText);
        
        assertThat(valueText.getValue(), is(equalTo(StatsFormatting.formatIntervalsRange(range))));
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
}
