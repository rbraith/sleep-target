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

import android.annotation.SuppressLint;
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
import com.rbraithwaite.sleeptarget.databinding.StatsFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.ui.stats.chart_durations.DurationsChartViewModel;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.IntervalsChartViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private StatsFragmentBinding mBinding;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_DURATIONS_LEGEND = "durations legend";
    private static final String DIALOG_INTERVALS_LEGEND = "intervals legend";

//*********************************************************
// public helpers
//*********************************************************

    // REFACTOR [21-11-5 10:00PM] -- Duplicates the dialogs in the targets fragment?
    public static class LegendDialog
            extends DialogFragment
    {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            @SuppressLint("InflateParams")
            View dialogView = getLayoutInflater().inflate(getArguments().getInt("layout id"), null);
            
            return new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.legend)
                    .setView(dialogView)
                    .create();
        }
        
        public static LegendDialog createInstance(int layoutId)
        {
            Bundle args = new Bundle();
            args.putInt("layout id", layoutId);
            LegendDialog dialog = new LegendDialog();
            dialog.setArguments(args);
            return dialog;
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
        mBinding = StatsFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        mBinding.intervals.bindToViewModel(getIntervalsChartViewModel(), getViewLifecycleOwner());
        // REFACTOR [21-11-5 9:42PM] -- should the legend dialog behaviours be integrated into these
        //  components?
        mBinding.intervals.setOnLegendClickListener(v -> displayIntervalsLegendDialog());
        
        mBinding.durations.bindToViewModel(getDurationsChartViewModel(), getViewLifecycleOwner());
        mBinding.durations.setOnLegendClickListener(v -> displayDurationsLegendDialog());
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
        LegendDialog.createInstance(R.layout.stats_legend_durations)
                .show(getChildFragmentManager(), DIALOG_DURATIONS_LEGEND);
    }
    
    private void displayIntervalsLegendDialog()
    {
        LegendDialog.createInstance(R.layout.stats_legend_intervals)
                .show(getChildFragmentManager(), DIALOG_INTERVALS_LEGEND);
    }
}
