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
package com.rbraithwaite.sleeptarget.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.rbraithwaite.sleeptarget.R;

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
            //  right now all the view models are behaving like permanent singletons
            //  (https://developer.android.com/guide/fragments/communicate#fragments)
            //  I need to be more careful about how I'm accessing these.
            mViewModel = new ViewModelProvider(requireActivity()).get(mProperties.viewModelClass);
        }
        return mViewModel;
    }
    
    public <T extends ViewModel> T getActivityViewModel(Class<T> viewModelClass)
    {
        return new ViewModelProvider(requireActivity()).get(viewModelClass);
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
