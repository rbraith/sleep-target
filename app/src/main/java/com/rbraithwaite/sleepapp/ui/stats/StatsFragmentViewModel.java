package com.rbraithwaite.sleepapp.ui.stats;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.stats.chart_durations.DurationsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.SleepIntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import java.util.concurrent.Executor;

public class StatsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private IntervalsChartViewModel mIntervalsChartViewModel;
    private SleepIntervalsDataSet.Generator mSleepIntervalsDataSetGenerator;
    
    private DurationsChartViewModel mDurationsChartViewModel;
    
    private SleepSessionRepository mSleepSessionRepository;
    
    private Executor mExecutor;
    
//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public StatsFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            SleepIntervalsDataSet.Generator sleepIntervalsDataSetGenerator,
            Executor executor)
    {
        mSleepIntervalsDataSetGenerator = sleepIntervalsDataSetGenerator;
        mSleepSessionRepository = sleepSessionRepository;
        mExecutor = executor;
    }
    
//*********************************************************
// api
//*********************************************************

    public IntervalsChartViewModel getIntervalsChartViewModel()
    {
        mIntervalsChartViewModel =
                CommonUtils.lazyInit(mIntervalsChartViewModel, () -> new IntervalsChartViewModel(
                        mSleepSessionRepository,
                        mSleepIntervalsDataSetGenerator,
                        mExecutor));
        return mIntervalsChartViewModel;
    }
    
    public DurationsChartViewModel getDurationsChartViewModel()
    {
        mDurationsChartViewModel =
                CommonUtils.lazyInit(mDurationsChartViewModel, () -> new DurationsChartViewModel(
                        mSleepSessionRepository));
        return mDurationsChartViewModel;
    }
}
