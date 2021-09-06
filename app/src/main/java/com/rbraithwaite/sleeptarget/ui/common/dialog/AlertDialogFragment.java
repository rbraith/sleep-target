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
package com.rbraithwaite.sleeptarget.ui.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleeptarget.utils.SerializableWrapper;

public class AlertDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private AlertDialogFactory mAlertDialogFactory;
    
//*********************************************************
// private constants
//*********************************************************

    private static final String STATE_KEY_FACTORY = "factory";

//*********************************************************
// public helpers
//*********************************************************

    // SMELL [21-01-18 5:23PM] -- this factory might not be necessary, but I'm not sure whether
    //  it's good to retain the AlertDialog instance in this fragment (I need to investigate this).
    //  The examples I've seen just create the dialog locally in onCreateDialog then return it,
    //  so no reference is kept.
    public interface AlertDialogFactory
    {
        /**
         * @param context Use this when building the AlertDialog.
         */
        AlertDialog create(Context context);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        if (mAlertDialogFactory == null && savedInstanceState != null) {
            // possible configuration change - try init'ing the factory from the saved state
            SerializableWrapper<AlertDialogFactory> wrapper =
                    (SerializableWrapper<AlertDialogFactory>) savedInstanceState.getSerializable(
                            STATE_KEY_FACTORY);
            if (wrapper != null) {
                mAlertDialogFactory = wrapper.data;
            }
        }
        
        if (mAlertDialogFactory == null) {
            throw new NullPointerException(
                    "The AlertDialogFactory cannot be null. Use createInstance() for new " +
                    "instances of AlertDialogFragment.");
        }
        
        return mAlertDialogFactory.create(requireContext());
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        if (mAlertDialogFactory != null) {
            outState.putSerializable(STATE_KEY_FACTORY,
                                     new SerializableWrapper<>(mAlertDialogFactory));
        }
        super.onSaveInstanceState(outState);
    }
    

//*********************************************************
// api
//*********************************************************

    
    /**
     * Use this static factory instead of using the default empty constructor yourself. The default
     * empty constructor only exists because it is required by the framework.
     */
    // A static factory method needs to be used instead of constructor injection for the factory
    // because the framework requires fragments to have default empty constructors.
    public static AlertDialogFragment createInstance(AlertDialogFactory alertDialogFactory)
    {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.mAlertDialogFactory = alertDialogFactory;
        return fragment;
    }
}
