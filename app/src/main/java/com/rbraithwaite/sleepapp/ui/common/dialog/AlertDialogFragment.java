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
