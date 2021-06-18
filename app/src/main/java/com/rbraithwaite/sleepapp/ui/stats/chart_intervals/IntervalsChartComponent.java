package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartParamsFactory;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsDataSet;
import com.rbraithwaite.sleepapp.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleepapp.ui.stats.common.RangeSelectorComponent;
import com.rbraithwaite.sleepapp.utils.CommonUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IntervalsChartComponent extends LinearLayout
{
    private TextView mTitle;
    private FrameLayout mChartLayout;
    private RangeSelectorComponent mTimePeriodSelector;
    private TextView mNoDataMessage;
    
    // REFACTOR [21-06-18 12:24AM] -- This init value should derive from the view model's default
    //  resolution.
    private int mCheckedMenuItemId = R.id.stats_intervals_resolution_week;
    
    @Inject
    Executor mExecutor;
    private IntervalsChartParamsFactory mChartParamsFactory;
    
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
    
    private void setChartData(IntervalsDataSet dataSet)
    {
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
            mChartLayout.removeAllViews();
            mChartLayout.addView(chartView);
        });
    }
    
    public void bindToViewModel(IntervalsChartViewModel viewModel, LifecycleOwner lifecycleOwner)
    {
        // IDEA [21-06-17 1:52AM] -- in the future, I can store references to these observers, so
        //  that I can unbind (to maybe then rebind with a new view model).
        viewModel.hasAnyData().observe(lifecycleOwner, this::observeHasAnyData);
        viewModel.getIntervalsDataSet().observe(lifecycleOwner, this::observeDataSet);
        viewModel.getIntervalsValueText().observe(lifecycleOwner, this::observeValueText);
        mTimePeriodSelector.setCallbacks(createViewModelTimePeriodCallbacks(viewModel));
    }
    
    private RangeSelectorComponent.Callbacks createViewModelTimePeriodCallbacks(
            IntervalsChartViewModel viewModel)
    {
        return new RangeSelectorComponent.Callbacks() {
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
            mTimePeriodSelector.setRangeValueText(valueText);
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
        setOrientation(VERTICAL);
        inflate(context, R.layout.stats_chart_intervals, this);
        
        mTitle = findViewById(R.id.stats_intervals_title);
        mChartLayout = findViewById(R.id.stats_intervals_layout);
        mTimePeriodSelector = findViewById(R.id.stats_intervals_time_period_selector);
        mNoDataMessage = findViewById(R.id.stats_intervals_no_data);
    
        // REFACTOR [21-06-17 1:20AM] -- I should inject this factory somehow.
        mChartParamsFactory = new IntervalsChartParamsFactory(mExecutor, getContext());
    }
    
    private void setItemChecked(MenuItem item)
    {
        item.setChecked(true);
        mCheckedMenuItemId = item.getItemId();
    }
    
    private CombinedChartViewFactory mChartViewFactory;
    
    private CombinedChartViewFactory getChartViewFactory()
    {
        // REFACTOR [21-06-17 1:20AM] -- I should inject this factory somehow.
        mChartViewFactory = CommonUtils.lazyInit(mChartViewFactory, CombinedChartViewFactory::new);
        return mChartViewFactory;
    }
    
    private void toggleChartDisplay(boolean shouldDisplayChart)
    {
        if (!shouldDisplayChart) {
            mChartLayout.setVisibility(View.GONE);
            mTimePeriodSelector.setVisibility(View.GONE);
            mNoDataMessage.setVisibility(View.VISIBLE);
        } else {
            mChartLayout.setVisibility(View.VISIBLE);
            mTimePeriodSelector.setVisibility(View.VISIBLE);
            mNoDataMessage.setVisibility(View.GONE);
        }
    }
}
