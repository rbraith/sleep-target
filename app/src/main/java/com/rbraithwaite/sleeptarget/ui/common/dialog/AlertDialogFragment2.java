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
package com.rbraithwaite.sleeptarget.ui.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.utils.TaggedLiveEvent;

public abstract class AlertDialogFragment2
        extends DialogFragment
{
//*********************************************************
// public helpers
//*********************************************************

    public abstract static class Actions
            extends ViewModel
    {
        private MutableLiveData<TaggedLiveEvent<Void>> mPositiveActionEvent =
                new MutableLiveData<>();
        private MutableLiveData<TaggedLiveEvent<Void>> mNegativeActionEvent =
                new MutableLiveData<>();
        
        public static final String DEFAULT_TAG = "";
        
        public LiveData<TaggedLiveEvent<Void>> onPositiveAction()
        {
            return mPositiveActionEvent;
        }
        
        public LiveData<TaggedLiveEvent<Void>> onNegativeAction()
        {
            return mNegativeActionEvent;
        }
        
        public void positiveAction()
        {
            positiveAction(DEFAULT_TAG);
        }
        
        public void positiveAction(String tag)
        {
            mPositiveActionEvent.setValue(new TaggedLiveEvent<>(tag));
        }
        
        public void negativeAction()
        {
            negativeAction(DEFAULT_TAG);
        }
        
        public void negativeAction(String tag)
        {
            mNegativeActionEvent.setValue(new TaggedLiveEvent<>(tag));
        }
    }
    
//*********************************************************
// abstract
//*********************************************************

    protected abstract AlertDialog createAlertDialog();
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        return createAlertDialog();
    }
    
//*********************************************************
// api
//*********************************************************

    public <ActionsType extends Actions> ActionsType getActions(Class<ActionsType> actionsClass)
    {
        return new ViewModelProvider(requireActivity()).get(actionsClass);
    }
}
