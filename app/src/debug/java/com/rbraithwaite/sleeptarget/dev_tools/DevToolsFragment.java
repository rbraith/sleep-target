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

package com.rbraithwaite.sleeptarget.dev_tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DevToolsFragment
        extends BaseFragment<DevToolsFragmentViewModel>
{
//*********************************************************
// overrides
//*********************************************************
    
    
    @Override
    protected Properties<DevToolsFragmentViewModel> initProperties()
    {
        return new Properties<>(false, DevToolsFragmentViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.dev_tools_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        // clear database
        Button clearDatabaseButton = view.findViewById(R.id.dev_tool_clear_data);
        final ProgressBar clearDatabaseProgessBar =
                view.findViewById(R.id.dev_tool_clear_data_progress);
        clearDatabaseProgessBar.setVisibility(View.GONE);
        clearDatabaseButton.setOnClickListener(v -> {
            clearDatabaseProgessBar.setVisibility(View.VISIBLE);
            getViewModel().clearData(() -> clearDatabaseProgessBar.setVisibility(View.GONE));
        });
        
        
        // add 100 sleep sessions
        Button add100SleepSessionsButton = view.findViewById(R.id.dev_tool_add_100);
        final ProgressBar add100ProgressBar = view.findViewById(R.id.dev_tool_add_100_progress);
        add100ProgressBar.setVisibility(View.GONE);
        add100SleepSessionsButton.setOnClickListener(v -> {
            add100ProgressBar.setVisibility(View.VISIBLE);
            getViewModel().addArbitrarySleepSessions(
                    100,
                    () -> add100ProgressBar.setVisibility(View.GONE));
        });
        
        
        // maybe add historical goal data
        Button historicalGoalDataButton = view.findViewById(R.id.dev_tool_add_goal_history);
        historicalGoalDataButton.setOnClickListener(v -> {
            getViewModel().maybeInitHistoricalGoalData();
        });
    }
}
