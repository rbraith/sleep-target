package com.rbraithwaite.sleepapp.ui.stats;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.stats.chart_durations.DurationsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsDataSet;

import java.util.concurrent.Executor;

public class StatsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public StatsFragmentViewModel()
    {
        // This class doesn't do anything at the moment. It only exists to satisfy BaseFragment.
    }
}
