package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.stats.common.RangeSelectorController;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.components.ApplicationComponent;

public class IntervalsChartController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private FrameLayout mLayout;
    private IntervalsChartViewModel mViewModel;
    
    private RangeSelectorController mIntervalsRangeController;
    private EntryPoint mEntryPoint;

//*********************************************************
// package properties
//*********************************************************

    SleepIntervalsChartFactory mSleepIntervalsChartFactory;
    
//*********************************************************
// package helpers
//*********************************************************

    @dagger.hilt.EntryPoint
    @InstallIn(ApplicationComponent.class)
    interface EntryPoint
    {
        SleepIntervalsChartFactory sleepIntervalsChartFactory();
    }
    
//*********************************************************
// constructors
//*********************************************************

    public IntervalsChartController(
            View root,
            IntervalsChartViewModel viewModel,
            LifecycleOwner lifecycleOwner)
    {
        mRoot = root;
        mLayout = root.findViewById(R.id.stats_intervals_layout);
        
        mViewModel = viewModel;
        
        mSleepIntervalsChartFactory = createSleepIntervalsChartFactory();
        
        viewModel.getIntervalsDataSet().observe(
                lifecycleOwner,
                dataSet -> {
                    LiveData<View> chartLiveData = null;
                    switch (viewModel.getIntervalsResolution()) {
                    case WEEK:
                        chartLiveData = mSleepIntervalsChartFactory.createRangeChartAsync(
                                requireContext(),
                                dataSet);
                        break;
                    case MONTH:
                        chartLiveData = mSleepIntervalsChartFactory.createMonthChartAsync(
                                requireContext(),
                                dataSet,
                                viewModel.getIntervalsResolutionValue());
                        break;
                    case YEAR:
                        chartLiveData = mSleepIntervalsChartFactory.createYearChartAsync(
                                requireContext(),
                                dataSet,
                                viewModel.getIntervalsResolutionValue());
                        break;
                    }
                    LiveDataFuture.getValue(chartLiveData, lifecycleOwner, chart -> {
                        mLayout.removeAllViews();
                        mLayout.addView(chart);
                    });
                });
        
        View intervalsTimePeriodSelector =
                mRoot.findViewById(R.id.stats_intervals_time_period_selector);
        
        mIntervalsRangeController = new RangeSelectorController(intervalsTimePeriodSelector)
        {
            @Override
            public int getMenuId() { return R.menu.stats_intervals_popup_menu; }
            
            @Override
            public void onPopupMenuInflated(Menu popupMenu)
            {
                MenuItem previouslyChecked = popupMenu.findItem(
                        getIntervalsResolutionMenuItemId(mViewModel.getIntervalsResolution()));
                previouslyChecked.setChecked(true);
            }
            
            @Override
            public boolean onPopupMenuItemClicked(MenuItem item)
            {
                switch (item.getItemId()) {
                case R.id.stats_intervals_resolution_week:
                    item.setChecked(true);
                    mViewModel.setIntervalsResolution(IntervalsChartViewModel.Resolution.WEEK);
                    return true;
                case R.id.stats_intervals_resolution_month:
                    item.setChecked(true);
                    mViewModel.setIntervalsResolution(IntervalsChartViewModel.Resolution.MONTH);
                    return true;
                case R.id.stats_intervals_resolution_year:
                    item.setChecked(true);
                    mViewModel.setIntervalsResolution(IntervalsChartViewModel.Resolution.YEAR);
                    return true;
                default:
                    return false;
                }
            }
            
            @Override
            public void onBackPressed()
            {
                mViewModel.stepIntervalsRange(IntervalsChartViewModel.Step.BACKWARD);
            }
            
            @Override
            public void onForwardPressed()
            {
                mViewModel.stepIntervalsRange(IntervalsChartViewModel.Step.FORWARD);
            }
        };
        
        viewModel.getIntervalsValueText().observe(
                lifecycleOwner,
                intervalsValueText -> {
                    if (intervalsValueText != null) {
                        mIntervalsRangeController.setText(intervalsValueText);
                    }
                });
    }
    
//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-05-14 8:33PM] -- This entry point logic is duplicated in TagSelectorViewModel
    //  and elsewhere.
    private EntryPoint getEntryPoint()
    {
        if (mEntryPoint == null) {
            mEntryPoint = EntryPointAccessors.fromApplication(
                    requireContext().getApplicationContext(), EntryPoint.class);
        }
        return mEntryPoint;
    }
    
    private SleepIntervalsChartFactory createSleepIntervalsChartFactory()
    {
        return getEntryPoint().sleepIntervalsChartFactory();
    }
    
    private Context requireContext()
    {
        return mRoot.getContext();
    }
    
    private int getIntervalsResolutionMenuItemId(IntervalsChartViewModel.Resolution intervalsResolution)
    {
        switch (intervalsResolution) {
        case WEEK:
            return R.id.stats_intervals_resolution_week;
        case MONTH:
            return R.id.stats_intervals_resolution_month;
        case YEAR:
            return R.id.stats_intervals_resolution_year;
        default:
            return -1;
        }
    }
}
