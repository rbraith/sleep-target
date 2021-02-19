package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
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
    protected boolean getBottomNavVisibility()
    {
        return true;
    }
    
    @Override
    protected Class<StatsFragmentViewModel> getViewModelClass()
    {
        return null;
    }
}
