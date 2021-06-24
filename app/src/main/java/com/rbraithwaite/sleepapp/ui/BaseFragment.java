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
    
    private Properties<V> mProperties;
    
//*********************************************************
// protected helpers
//*********************************************************

    protected static class Properties<V extends ViewModel>
    {
        public final boolean isBottomNavVisible;
        public final Class<V> viewModelClass;
        
        public Properties(boolean isBottomNavVisible, Class<V> viewModelClass)
        {
            this.isBottomNavVisible = isBottomNavVisible;
            this.viewModelClass = viewModelClass;
        }
    }
    
//*********************************************************
// abstract
//*********************************************************

    protected abstract Properties<V> initProperties();

    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mProperties = initProperties();
    }
    
    // TODO [21-06-18 1:16AM] -- onActivityCreated is deprecated, find an alternative.
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setMainActivityBottomNavVisibility(mProperties.isBottomNavVisible);
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

    public V getViewModel()
    {
        if (mViewModel == null) {
            // REFACTOR [20-12-23 1:57AM] -- should the ViewModelStoreOwner be the fragment instead?
            mViewModel = new ViewModelProvider(requireActivity()).get(mProperties.viewModelClass);
        }
        return mViewModel;
    }

//*********************************************************
// protected api
//*********************************************************

    
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
