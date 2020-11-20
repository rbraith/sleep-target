package com.rbraithwaite.sleepapp.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public abstract class BaseFragment
        extends Fragment
{
//*********************************************************
// abstract
//*********************************************************

    protected abstract boolean getBottomNavVisibility();
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setMainActivityBottomNavVisibility(getBottomNavVisibility());
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void setMainActivityBottomNavVisibility(boolean visibility)
    {
        FragmentActivity activity = requireActivity();
        // its possible this fragment will not be inside a MainActivity (eg it could
        // be inside a test-specific activity)
        // SMELL [20-11-14 5:05PM] -- type check.
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setBottomNavVisibility(visibility);
        }
    }
}
