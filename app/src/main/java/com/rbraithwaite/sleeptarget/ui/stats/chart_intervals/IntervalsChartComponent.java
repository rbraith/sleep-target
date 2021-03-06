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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.StatsChartIntervalsBinding;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet;
import com.rbraithwaite.sleeptarget.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleeptarget.ui.stats.common.RangeSelectorComponent;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IntervalsChartComponent
        extends ConstraintLayout
{
//*********************************************************
// private properties
//*********************************************************
    
    private StatsChartIntervalsBinding mBinding;
    
    // REFACTOR [21-06-18 12:24AM] -- This init value should derive from the view model's default
    //  resolution.
    private int mCheckedMenuItemId = R.id.stats_intervals_resolution_week;
    private IntervalsChartParamsFactory mChartParamsFactory;
    private CombinedChartViewFactory mChartViewFactory;

//*********************************************************
// package properties
//*********************************************************

    @Inject
    Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    public IntervalsChartComponent(Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public IntervalsChartComponent(
            Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public IntervalsChartComponent(
            Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }

//*********************************************************
// api
//*********************************************************

    public void bindToViewModel(IntervalsChartViewModel viewModel, LifecycleOwner lifecycleOwner)
    {
        // IDEA [21-06-17 1:52AM] -- in the future, I can store references to these observers, so
        //  that I can unbind (to maybe then rebind with a new view model).
        viewModel.hasAnyData().observe(lifecycleOwner, this::observeHasAnyData);
        viewModel.getIntervalsDataSet().observe(lifecycleOwner, this::observeDataSet);
        viewModel.getIntervalsValueText().observe(lifecycleOwner, this::observeValueText);
        mBinding.timePeriodSelector.setCallbacks(createViewModelTimePeriodCallbacks(viewModel));
    }
    
    public void setOnLegendClickListener(OnClickListener listener)
    {
        mBinding.legendClickFrame.setOnClickListener(listener);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void setChartData(IntervalsDataSet dataSet)
    {
        // REFACTOR [21-11-9 5:04PM] -- Should IntervalsChartViewModel itself be directly
        //  generating these AChartEngine params?
        LiveData<CombinedChartViewFactory.Params> chartParams = null;
        switch (dataSet.config.resolution) {
        case WEEK:
            chartParams = mChartParamsFactory.createRangeParams(dataSet);
            break;
        case MONTH:
            // SMELL [21-06-17 3:41AM] I need a better solution here - passing the dataset,
            //  then also some internal config data of that same set makes no sense
            //  - where should the resolution & resolution value be?
            chartParams = mChartParamsFactory.createMonthParams(
                    dataSet,
                    dataSet.config.resolutionValue);
            break;
        case YEAR:
            chartParams = mChartParamsFactory.createYearParams(
                    dataSet,
                    dataSet.config.resolutionValue);
            break;
        }
        // SMELL [21-06-17 3:43AM] the LiveData here is kinda questionable - this would
        //  be a good place for RxJava, or could I use an async callback in the factory?
        LiveDataFuture.getValue(chartParams, null, params -> {
            View chartView = getChartViewFactory().createFrom(getContext(), params);
            mBinding.chartLayout.removeAllViews();
            mBinding.chartLayout.addView(chartView);
        });
    }
    
    private RangeSelectorComponent.Callbacks createViewModelTimePeriodCallbacks(
            IntervalsChartViewModel viewModel)
    {
        return new RangeSelectorComponent.Callbacks()
        {
            @Override
            public int getMenuId()
            {
                return R.menu.stats_intervals_popup_menu;
            }
            
            @Override
            public void onBackPressed()
            {
                viewModel.stepIntervalsRange(IntervalsChartViewModel.Step.BACKWARD);
            }
            
            @Override
            public void onForwardPressed()
            {
                viewModel.stepIntervalsRange(IntervalsChartViewModel.Step.FORWARD);
            }
            
            @Override
            public void onPopupMenuInflated(Menu popupMenu)
            {
                MenuItem previouslyChecked = popupMenu.findItem(mCheckedMenuItemId);
                previouslyChecked.setChecked(true);
            }
            
            @Override
            public boolean onPopupMenuItemClicked(MenuItem item)
            {
                setItemChecked(item);
                
                switch (item.getItemId()) {
                case R.id.stats_intervals_resolution_week:
                    viewModel.setIntervalsResolution(IntervalsDataSet.Resolution.WEEK);
                    return true;
                case R.id.stats_intervals_resolution_month:
                    viewModel.setIntervalsResolution(IntervalsDataSet.Resolution.MONTH);
                    return true;
                case R.id.stats_intervals_resolution_year:
                    viewModel.setIntervalsResolution(IntervalsDataSet.Resolution.YEAR);
                    return true;
                default:
                    return false;
                }
            }
        };
    }
    
    private void observeValueText(String valueText)
    {
        if (valueText != null) {
            mBinding.timePeriodSelector.setRangeValueText(valueText);
        }
    }
    
    private void observeDataSet(IntervalsDataSet dataSet)
    {
        setChartData(dataSet);
    }
    
    private void observeHasAnyData(Boolean hasAnyData)
    {
        toggleChartDisplay(hasAnyData);
    }
    
    private void initComponent(Context context)
    {
        inflate(context, R.layout.stats_chart_intervals, this);
        
        mBinding = StatsChartIntervalsBinding.bind(this);
        
        // REFACTOR [21-06-17 1:20AM] -- I should inject this factory somehow.
        mChartParamsFactory = new IntervalsChartParamsFactory(mExecutor, getContext());
    }
    
    private void setItemChecked(MenuItem item)
    {
        item.setChecked(true);
        mCheckedMenuItemId = item.getItemId();
    }
    
    private CombinedChartViewFactory getChartViewFactory()
    {
        // REFACTOR [21-06-17 1:20AM] -- I should inject this factory somehow.
        mChartViewFactory = CommonUtils.lazyInit(mChartViewFactory, CombinedChartViewFactory::new);
        return mChartViewFactory;
    }
    
    private void toggleChartDisplay(boolean shouldDisplayChart)
    {
        int chartVisibility = View.GONE;
        int noDataVisibility = View.VISIBLE;
        
        if (shouldDisplayChart) {
            chartVisibility = View.VISIBLE;
            noDataVisibility = View.GONE;
        }
        
        mBinding.chartLayout.setVisibility(chartVisibility);
        mBinding.timePeriodSelector.setVisibility(chartVisibility);
        
        mBinding.noDataMessage.setVisibility(noDataVisibility);
    }
}
