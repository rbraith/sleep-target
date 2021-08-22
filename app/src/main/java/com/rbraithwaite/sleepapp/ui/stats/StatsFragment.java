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

package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.stats.chart_durations.DurationsChartComponent;
import com.rbraithwaite.sleepapp.ui.stats.chart_durations.DurationsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartComponent;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private DurationsChartComponent mDurationsChart;
    private IntervalsChartComponent mIntervalsChart;

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
        mIntervalsChart = view.findViewById(R.id.stats_intervals);
        mIntervalsChart.bindToViewModel(getIntervalsChartViewModel(), getViewLifecycleOwner());
        
        mDurationsChart = view.findViewById(R.id.stats_durations);
        mDurationsChart.bindToViewModel(getDurationsChartViewModel(), getViewLifecycleOwner());
    }
    
    @Override
    protected Properties<StatsFragmentViewModel> initProperties()
    {
        return new Properties<>(true, StatsFragmentViewModel.class);
    }
    
//*********************************************************
// api
//*********************************************************

    public IntervalsChartViewModel getIntervalsChartViewModel()
    {
        return new ViewModelProvider(this).get(IntervalsChartViewModel.class);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private DurationsChartViewModel getDurationsChartViewModel()
    {
        return new ViewModelProvider(this).get(DurationsChartViewModel.class);
    }
}
