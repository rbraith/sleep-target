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

package com.rbraithwaite.sleepapp.ui.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AlertDialogFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private AlertDialogFactory mAlertDialogFactory;

//*********************************************************
// public helpers
//*********************************************************

    // SMELL [21-01-18 5:23PM] -- this factory might not be necessary, but I'm not sure whether
    //  it's good to retain the AlertDialog instance in this fragment (I need to investigate this).
    //  The examples I've seen just create the dialog locally in onCreateDialog then return it,
    //  so no reference is kept.
    public interface AlertDialogFactory
    {
        AlertDialog create();
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        if (mAlertDialogFactory == null) {
            throw new NullPointerException(
                    "The AlertDialogFactory cannot be null. Use createInstance() for new " +
                    "instances of AlertDialogFragment.");
        }
        return mAlertDialogFactory.create();
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
