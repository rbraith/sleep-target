package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.stats.charts.SleepIntervalsChartFactory;
import com.rbraithwaite.sleepapp.ui.stats.data.SleepIntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
//*********************************************************
// package properties
//*********************************************************

    @Inject
    SleepIntervalsChartFactory mSleepIntervalsChartFactory;

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
        initIntervalsChart(view);
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
    
//*********************************************************
// private methods
//*********************************************************

    private void initIntervalsChart(final View fragmentRoot)
    {
        final StatsFragmentViewModel viewModel = getViewModel();
        
        // bind chart to data
        viewModel.getIntervalsDataSet().observe(
                getViewLifecycleOwner(),
                new Observer<SleepIntervalsDataSet>()
                {
                    @Override
                    public void onChanged(SleepIntervalsDataSet dataSet)
                    {
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
                        LiveDataFuture.getValue(
                                chartLiveData,
                                getViewLifecycleOwner(),
                                new LiveDataFuture.OnValueListener<View>()
                                {
                                    @Override
                                    public void onValue(View chart)
                                    {
                                        FrameLayout intervalsLayout =
                                                fragmentRoot.findViewById(R.id.stats_intervals_layout);
                                        intervalsLayout.removeAllViews();
                                        intervalsLayout.addView(chart);
                                    }
                                });
                    }
                });
        
        View intervalsTimePeriodSelector =
                fragmentRoot.findViewById(R.id.stats_intervals_time_period_selector);
        
        // time period back
        ImageButton intervalsBack =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_back);
        intervalsBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getViewModel().stepIntervalsRange(StatsFragmentViewModel.Step.BACKWARD);
            }
        });
        
        // time period forward
        ImageButton intervalsForward =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_forward);
        intervalsForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getViewModel().stepIntervalsRange(StatsFragmentViewModel.Step.FORWARD);
            }
        });
        
        // time period value text
        final TextView intervalsValueText =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_value);
        viewModel.getIntervalsValueText().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String s)
                    {
                        if (s != null) {
                            intervalsValueText.setText(s);
                        }
                    }
                });
        
        // intervals more options
        ImageButton moreButton =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_more);
        moreButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // REFACTOR [21-03-3 12:54AM] -- extract as a generic popup menu utility.
                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.inflate(R.menu.stats_intervals_popup_menu);
                
                MenuItem previouslyChecked = popup.getMenu().findItem(
                        getIntervalsResolutionMenuItemId(
                                getViewModel().getIntervalsResolution()));
                previouslyChecked.setChecked(true);
                
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId()) {
                        case R.id.stats_intervals_resolution_week:
                            item.setChecked(true);
                            getViewModel().setIntervalsResolution(StatsFragmentViewModel.Resolution.WEEK);
                            return true;
                        case R.id.stats_intervals_resolution_month:
                            item.setChecked(true);
                            getViewModel().setIntervalsResolution(StatsFragmentViewModel.Resolution.MONTH);
                            return true;
                        case R.id.stats_intervals_resolution_year:
                            item.setChecked(true);
                            getViewModel().setIntervalsResolution(StatsFragmentViewModel.Resolution.YEAR);
                            return true;
                        default:
                            return false;
                        }
                    }
                });
                
                popup.show();
            }
        });
    }
    
    
    private int getIntervalsResolutionMenuItemId(StatsFragmentViewModel.Resolution intervalsResolution)
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
