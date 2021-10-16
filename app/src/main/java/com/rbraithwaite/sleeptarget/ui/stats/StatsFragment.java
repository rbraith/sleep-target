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
package com.rbraithwaite.sleeptarget.ui.stats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.stats.chart_durations.DurationsChartComponent;
import com.rbraithwaite.sleeptarget.ui.stats.chart_durations.DurationsChartViewModel;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.IntervalsChartComponent;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.IntervalsChartViewModel;

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
// private constants
//*********************************************************

    private static final String DIALOG_DURATIONS_LEGEND = "durations legend";
    private static final String DIALOG_INTERVALS_LEGEND = "intervals legend";

//*********************************************************
// public helpers
//*********************************************************

    public static class DurationsLegendDialog
            extends DialogFragment
    {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Legend")
                    .setView(getLayoutInflater().inflate(R.layout.stats_legend_durations, null));
            
            return builder.create();
        }
    }
    
    // REFACTOR [21-10-16 7:23PM] -- Duplicates DurationsLegendDialog & the dialogs in the
    //  targets fragment.
    public static class IntervalsLegendDialog
            extends DialogFragment
    {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Legend")
                    .setView(getLayoutInflater().inflate(R.layout.stats_legend_intervals, null));
            
            return builder.create();
        }
    }
    
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
        
        View durationsChartLegendButton =
                view.findViewById(R.id.stats_durations_legend_click_frame);
        durationsChartLegendButton.setOnClickListener(v -> displayDurationsLegendDialog());
        
        View intervalsChartLegendButton =
                view.findViewById(R.id.stats_intervals_legend_click_frame);
        intervalsChartLegendButton.setOnClickListener(v -> displayIntervalsLegendDialog());
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
    
    private void displayDurationsLegendDialog()
    {
        new DurationsLegendDialog().show(getChildFragmentManager(), DIALOG_DURATIONS_LEGEND);
    }
    
    private void displayIntervalsLegendDialog()
    {
        new IntervalsLegendDialog().show(getChildFragmentManager(), DIALOG_INTERVALS_LEGEND);
    }
}
