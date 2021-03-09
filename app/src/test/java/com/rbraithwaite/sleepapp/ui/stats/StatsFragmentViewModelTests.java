package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.stats.data.SleepIntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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
    SleepIntervalsDataSet.Generator mMockSleepIntervalsDataSetGenerator;
    Executor executor;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        mMockSleepIntervalsDataSetGenerator = mock(SleepIntervalsDataSet.Generator.class);
        executor = new TestUtils.SynchronizedExecutor();
        viewModel = new StatsFragmentViewModel(
                mockSleepSessionRepository,
                mMockSleepIntervalsDataSetGenerator,
                executor);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        executor = null;
        mockSleepSessionRepository = null;
        mMockSleepIntervalsDataSetGenerator = null;
    }
    
    @Test
    public void getIntervalsResolution_returns_WEEK_byDefault()
    {
        assertThat(viewModel.getIntervalsResolution(),
                   is(equalTo(StatsFragmentViewModel.Resolution.WEEK)));
    }
    
    @Test
    public void getIntervalsResolution_reflects_setIntervalsResolution()
    {
        viewModel.setIntervalsResolution(StatsFragmentViewModel.Resolution.MONTH);
        assertThat(viewModel.getIntervalsResolution(),
                   is(equalTo(StatsFragmentViewModel.Resolution.MONTH)));
    }
    
    @Test
    public void intervalsRangeReflectsResolution()
    {
        avoidIntervalsDataSetNullPointer();
        
        LiveData<SleepIntervalsDataSet> intervalsDataSet = viewModel.getIntervalsDataSet();
        TestUtils.activateLocalLiveData(intervalsDataSet);
        
        viewModel.setIntervalsResolution(StatsFragmentViewModel.Resolution.MONTH);
        
        // These are called twice: first from the activation of the LiveData, then again when
        // the resolution changes.
        verify(mockSleepSessionRepository, times(2))
                .getSleepSessionsInRange(any(Date.class), any(Date.class));
        verify(mMockSleepIntervalsDataSetGenerator, times(2))
                .generateFromConfig(anyList(), any(SleepIntervalsDataSet.Config.class));
    }
    
    @Test
    public void configureIntervalsDataSet_updates_getIntervalsDataSet()
    {
        avoidIntervalsDataSetNullPointer();
        
        LiveData<SleepIntervalsDataSet> intervalsDataSet = viewModel.getIntervalsDataSet();
        TestUtils.activateLocalLiveData(intervalsDataSet);
        
        // SUT
        SleepIntervalsDataSet.Config testConfig = new SleepIntervalsDataSet.Config(
                TestUtils.ArbitraryData.getDateRange(),
                12345,
                false);
        viewModel.configureIntervalsDataSet(testConfig);
        shadowOf(Looper.getMainLooper()).idle();
        
        verify(mockSleepSessionRepository, times(1))
                .getSleepSessionsInRange(
                        testConfig.dateRange.getStart(),
                        testConfig.dateRange.getEnd());
        verify(mMockSleepIntervalsDataSetGenerator, times(1))
                .generateFromConfig(anyList(), eq(testConfig));
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
        
        SleepIntervalsDataSet.Config config1 = viewModel.getIntervalsDataSetConfig();
        long start1 = config1.dateRange.getStart().getTime();
        int diffDays1 = config1.dateRange.getDifferenceInDays();
        
        // SUT forward
        viewModel.stepIntervalsRange(StatsFragmentViewModel.Step.FORWARD);
        
        // verify
        SleepIntervalsDataSet.Config config2 = viewModel.getIntervalsDataSetConfig();
        long start2 = config2.dateRange.getStart().getTime();
        int diffDays2 = config2.dateRange.getDifferenceInDays();
        assertThat(
                // REFACTOR [21-02-28 9:46PM] -- I should be using TimeUnit in TimeUtils.
                TimeUnit.DAYS.convert(start2 - start1, TimeUnit.MILLISECONDS),
                is(equalTo(7L)));
        assertThat(diffDays1, is(equalTo(diffDays2)));
        
        // SUT backward
        viewModel.stepIntervalsRange(StatsFragmentViewModel.Step.BACKWARD);
        
        // verify
        SleepIntervalsDataSet.Config config3 = viewModel.getIntervalsDataSetConfig();
        assertThat(config1, is(equalTo(config3)));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void avoidIntervalsDataSetNullPointer()
    {
        when(mockSleepSessionRepository.getSleepSessionsInRange(any(Date.class), any(Date.class)))
                .thenReturn(new MutableLiveData<List<SleepSessionModel>>(null));
    }
}
