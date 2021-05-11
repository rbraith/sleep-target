package com.rbraithwaite.sleepapp.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.rbraithwaite.sleepapp.R;

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
// api
//*********************************************************

    
    /**
     * Gets a currently-displayed DialogFragment by its tag.
     *
     * @param dialogTag The tag to find the fragment by.
     *
     * @return The DialogFragment, or null if that fragment is not currently displayed or is not a
     * DialogFragment.
     */
    public DialogFragment getDialogByTag(String dialogTag)
    {
        try {
            return (DialogFragment) getChildFragmentManager().findFragmentByTag(dialogTag);
        } catch (ClassCastException e) {
            return null;
        }
    }

//*********************************************************
// protected api
//*********************************************************

    protected V getViewModel()
    {
        if (mViewModel == null) {
            // REFACTOR [20-12-23 1:57AM] -- should the ViewModelStoreOwner be the fragment instead?
            mViewModel = new ViewModelProvider(requireActivity()).get(getViewModelClass());
        }
        return mViewModel;
    }
    
    /**
     * This assumes that this fragment is inside MainActivity
     */
    protected NavController getNavController()
    {
        return Navigation.findNavController(requireActivity(), R.id.main_navhost);
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
