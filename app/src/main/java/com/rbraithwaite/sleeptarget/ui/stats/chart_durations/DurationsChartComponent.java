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

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleeptarget.ui.stats.common.RangeSelectorComponent;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

// REFACTOR [21-06-17 11:10PM] -- A lot of stuff in here is duplicated from IntervalsChartComponent
@AndroidEntryPoint
public class DurationsChartComponent
        extends ConstraintLayout
{
//*********************************************************
// private properties
//*********************************************************

    private TextView mTitle;
    private FrameLayout mChartLayout;
    private RangeSelectorComponent mRangeSelector;
    private TextView mNoDataMessage;
    private Group mChartGroup;
    
    // REFACTOR [21-06-18 12:25AM] -- this init value should derive from the view model's default
    //  resolution.
    private int mCheckedMenuItemId = R.id.stats_durations_resolution_10;
    private DurationsChartParamsFactory mChartParamsFactory;

//*********************************************************
// package properties
//*********************************************************

    @Inject
    Executor mExecutor;
    
//*********************************************************
// constructors
//*********************************************************

    public DurationsChartComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public DurationsChartComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public DurationsChartComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
//*********************************************************
// api
//*********************************************************

    public void bindToViewModel(DurationsChartViewModel viewModel, LifecycleOwner lifecycleOwner)
    {
        viewModel.getDataSet()
                .observe(lifecycleOwner, dataSet -> observeDataSet(dataSet, viewModel));
        viewModel.getRangeText().observe(lifecycleOwner, this::observeRangeText);
        mRangeSelector.setCallbacks(createViewModelRangeCallbacks(viewModel));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private RangeSelectorComponent.Callbacks createViewModelRangeCallbacks(DurationsChartViewModel viewModel)
    {
        return new RangeSelectorComponent.Callbacks()
        {
            @Override
            public int getMenuId()
            {
                return R.menu.stats_durations_popup_menu;
            }
            
            @Override
            public void onBackPressed()
            {
                viewModel.stepRangeBack();
            }
            
            @Override
            public void onForwardPressed()
            {
                viewModel.stepRangeForward();
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
                case R.id.stats_durations_resolution_10:
                    viewModel.setRangeDistance(10);
                    return true;
                case R.id.stats_durations_resolution_50:
                    viewModel.setRangeDistance(50);
                    return true;
                case R.id.stats_durations_resolution_100:
                    viewModel.setRangeDistance(100);
                    return true;
                default:
                    return false;
                }
            }
        };
    }
    
    private void setItemChecked(MenuItem item)
    {
        item.setChecked(true);
        mCheckedMenuItemId = item.getItemId();
    }
    
    private void observeRangeText(String rangeText)
    {
        mRangeSelector.setRangeValueText(rangeText);
    }
    
    private void observeDataSet(
            List<DurationsChartViewModel.DataPoint> dataSet,
            DurationsChartViewModel viewModel)
    {
        if (dataSet == null || dataSet.isEmpty()) {
            toggleChartDisplay(false);
        } else {
            updateChartWithData(dataSet, viewModel.getRangeDistance());
        }
    }
    
    /**
     * Assumes the dataset is not null or empty.
     */
    private void updateChartWithData(
            List<DurationsChartViewModel.DataPoint> dataSet,
            int rangeDistance)
    {
        LiveData<CombinedChartViewFactory.Params> chartParams = mChartParamsFactory.createParams(
                dataSet, rangeDistance);
        
        // REFACTOR [21-06-7 3:36PM] -- This should be injected.
        CombinedChartViewFactory chartViewFactory = new CombinedChartViewFactory();
        LiveDataFuture.getValue(chartParams, null, params -> {
            View chartView = chartViewFactory.createFrom(getContext(), params);
            
            mChartLayout.removeAllViews();
            mChartLayout.addView(chartView);
            
            toggleChartDisplay(true);
        });
    }
    
    private void initComponent(Context context)
    {
        inflate(context, R.layout.stats_chart_durations, this);
        
        mTitle = findViewById(R.id.stats_durations_title);
        mChartLayout = findViewById(R.id.stats_durations_layout);
        mChartGroup = findViewById(R.id.stats_durations_chart_group);
        mRangeSelector = findViewById(R.id.stats_durations_range_selector);
        mNoDataMessage = findViewById(R.id.stats_durations_no_data);
        
        // REFACTOR [21-06-17 1:20AM] -- I should inject this factory somehow.
        mChartParamsFactory = new DurationsChartParamsFactory(mExecutor, getContext());
    }
    
    private void toggleChartDisplay(boolean shouldDisplayChart)
    {
        if (shouldDisplayChart) {
            mChartGroup.setVisibility(View.VISIBLE);
            mNoDataMessage.setVisibility(View.GONE);
        } else {
            mChartGroup.setVisibility(View.GONE);
            mNoDataMessage.setVisibility(View.VISIBLE);
        }
    }
}
