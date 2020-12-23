package com.rbraithwaite.sleepapp.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public abstract class BaseFragment<V extends ViewModel>
        extends Fragment
{
//*********************************************************
// private properties
//*********************************************************

    private V mViewModel;

//*********************************************************
// abstract
//*********************************************************

    protected abstract boolean getBottomNavVisibility();
    
    protected abstract Class<V> getViewModelClass();

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
// protected api
//*********************************************************

    //    protected abstract <V extends ViewModel> Class<V> getViewModelType();
//
    protected V getViewModel()
    {
        if (mViewModel == null) {
            // REFACTOR [20-12-23 1:57AM] -- should the ViewModelStoreOwner be the fragment instead?
            mViewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());
        }
        return mViewModel;
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
