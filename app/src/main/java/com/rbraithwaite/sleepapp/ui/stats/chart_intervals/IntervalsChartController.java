package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.TEMP.IntervalsChartParamsFactory;
import com.rbraithwaite.sleepapp.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleepapp.ui.stats.common.RangeSelectorController;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.util.concurrent.Executor;

import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.components.ApplicationComponent;

public class IntervalsChartController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private Context mContext;
    private IntervalsChartViewModel mViewModel;
    
    private RangeSelectorController mIntervalsRangeController;
    private EntryPoint mEntryPoint;

//*********************************************************
// package helpers
//*********************************************************

    @dagger.hilt.EntryPoint
    @InstallIn(ApplicationComponent.class)
    interface EntryPoint
    {
        Executor provideExecutor();
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
        mContext = mRoot.getContext();
        FrameLayout layout = mRoot.findViewById(R.id.stats_intervals_layout);
        View intervalsTimePeriodSelector =
                mRoot.findViewById(R.id.stats_intervals_time_period_selector);
        View noDataMessage = mRoot.findViewById(R.id.stats_intervals_no_data);
        
        mViewModel = viewModel;
        
        viewModel.hasAnyData().observe(
                lifecycleOwner,
                hasAnyData -> {
                    if (!hasAnyData) {
                        layout.setVisibility(View.GONE);
                        intervalsTimePeriodSelector.setVisibility(View.GONE);
                        noDataMessage.setVisibility(View.VISIBLE);
                        return;
                    }
                    
                    layout.setVisibility(View.VISIBLE);
                    intervalsTimePeriodSelector.setVisibility(View.VISIBLE);
                    noDataMessage.setVisibility(View.GONE);
                });
        
        IntervalsChartParamsFactory paramsFactory = new IntervalsChartParamsFactory(
                getEntryPoint().provideExecutor(), mContext);
        // REFACTOR [21-06-7 3:36PM] -- This should be injected.
        CombinedChartViewFactory chartViewFactory = new CombinedChartViewFactory();
        viewModel.getIntervalsDataSet().observe(
                lifecycleOwner,
                dataSet -> {
                    LiveData<CombinedChartViewFactory.Params> chartParams = null;
                    switch (viewModel.getIntervalsResolution()) {
                    case WEEK:
                        chartParams = paramsFactory.createRangeParams(dataSet);
                        break;
                    case MONTH:
                        chartParams = paramsFactory.createMonthParams(
                                dataSet,
                                viewModel.getIntervalsResolutionValue());
                        break;
                    case YEAR:
                        chartParams = paramsFactory.createYearParams(
                                dataSet,
                                viewModel.getIntervalsResolutionValue());
                        break;
                    }
                    LiveDataFuture.getValue(chartParams, lifecycleOwner, params -> {
                        View chartView = chartViewFactory.createFrom(mContext, params);
                        layout.removeAllViews();
                        layout.addView(chartView);
                    });
                });
        
        mIntervalsRangeController = new RangeSelectorController(intervalsTimePeriodSelector)
        {
            @Override
            public int getMenuId() { return R.menu.stats_intervals_popup_menu; }
            
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

    // REFACTOR [21-06-7 3:32PM] -- I should consider renaming this getDependencies or
    //  getDependenciesProvider, that might be more clear.
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
