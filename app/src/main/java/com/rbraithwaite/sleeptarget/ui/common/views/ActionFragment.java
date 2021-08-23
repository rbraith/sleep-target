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

package com.rbraithwaite.sleeptarget.ui.common.views;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;

import java.io.Serializable;

public abstract class ActionFragment<ViewModelType extends ViewModel>
        extends BaseFragment<ViewModelType>
{
//*********************************************************
// private properties
//*********************************************************

    private Params mActionFragmentParams;

//*********************************************************
// public helpers
//*********************************************************
    
    public static class Params implements Serializable
    {
        public static final long serialVersionUID = 20201230L;
        
        public static final int DEFAULT_ICON = -1;
        
        public int positiveIcon = DEFAULT_ICON;
        public int negativeIcon = DEFAULT_ICON;
    }
    
//*********************************************************
// constructors
//*********************************************************

    public ActionFragment()
    {
        setHasOptionsMenu(true);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable Bundle savedInstanceState)
    {
        // init back press behaviour
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true)
                {
                    @Override
                    public void handleOnBackPressed()
                    {
                        onBackPressed();
                    }
                });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        int positiveIcon = _getActionFragmentParams().positiveIcon;
        int negativeIcon = _getActionFragmentParams().negativeIcon;
        
        inflater.inflate(R.menu.session_details_menu, menu);
        if (positiveIcon != Params.DEFAULT_ICON) {
            menu.findItem(R.id.action_positive).setIcon(positiveIcon);
        }
        if (negativeIcon != Params.DEFAULT_ICON) {
            menu.findItem(R.id.action_negative).setIcon(negativeIcon);
        }
    }
    
    /**
     * Children overriding this should pass unhandled (default switch case) item ids on to super.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.action_positive:
            return onPositiveAction();
        case R.id.action_negative:
            return onNegativeAction();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
//*********************************************************
// api
//*********************************************************

    public Params createDefaultActionFragmentParams()
    {
        return new Params();
    }

//*********************************************************
// protected api
//*********************************************************

    
    /**
     * Children can override this to handle positive menu actions in cases where there was no
     * listener. By default this does nothing and returns false.
     */
    protected boolean onPositiveAction()
    {
        return false;
    }
    
    /**
     * Children can override this to handle negative menu actions in cases where there was no
     * listener. By default this returns true and exits the fragment.
     */
    protected boolean onNegativeAction()
    {
        navigateUp();
        return true;
    }
    
    /**
     * Children can override this to change back-button-pressed behaviour. By default this navigates
     * up.
     */
    protected void onBackPressed()
    {
        navigateUp();
    }
    
    /**
     * Children should override this to get their specific params. If this returns null, the default
     * params are used.
     */
    protected Params getActionFragmentParams()
    {
        return null;
    }
    
    // REFACTOR [21-07-23 1:25AM] -- This should be in BaseFragment.
    protected void navigateUp()
    {
        getNavController().navigateUp();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private Params _getActionFragmentParams()
    {
        mActionFragmentParams = CommonUtils.lazyInit(mActionFragmentParams, () -> {
            Params params = getActionFragmentParams();
            return params == null ? createDefaultActionFragmentParams() : params;
        });
        return mActionFragmentParams;
    }
}
