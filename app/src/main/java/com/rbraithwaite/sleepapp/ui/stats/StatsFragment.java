package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.stats.chart_durations.DurationsChartController;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartController;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private IntervalsChartController mIntervalsChart;
    
    private DurationsChartController mDurationsChart;
    
//*********************************************************
// package properties
//*********************************************************

    @Inject
    Executor mExecutor;
    
//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.stats_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        mIntervalsChart = new IntervalsChartController(
                view.findViewById(R.id.stats_intervals),
                getViewModel().getIntervalsChartViewModel(),
                getViewLifecycleOwner());
        
        mDurationsChart = new DurationsChartController(
                view.findViewById(R.id.stats_durations),
                getViewModel().getDurationsChartViewModel(),
                getViewLifecycleOwner(),
                mExecutor);
    }
    
    @Override
    protected boolean getBottomNavVisibility()
    {
        return true;
    }
    
    @Override
    protected Class<StatsFragmentViewModel> getViewModelClass()
    {
        return StatsFragmentViewModel.class;
    }
    
    // REFACTOR [21-03-4 11:43PM] -- maybe just make BaseFragment.getViewModel() public?
    // Using this for tests
    @Override
    public StatsFragmentViewModel getViewModel()
    {
        return super.getViewModel();
    }
}
