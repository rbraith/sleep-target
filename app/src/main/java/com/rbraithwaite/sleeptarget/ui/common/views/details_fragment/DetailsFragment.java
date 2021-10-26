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
package com.rbraithwaite.sleeptarget.ui.common.views.details_fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DeleteDialog;
import com.rbraithwaite.sleeptarget.ui.common.views.ActionFragment;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;

import java.io.Serializable;



/**
 * A base fragment for dealing with the display of some domain model's "details". The common usage
 * is to provide add/update/delete functionality for the model being displayed. This functionality
 * is achieved through the ActionFragment menu actions, overriding this class's protected api.
 */
public abstract class DetailsFragment<DataType,
        ViewModelType extends DetailsFragmentViewModel<DataType>>
        extends ActionFragment<ViewModelType>
{
//*********************************************************
// private properties
//*********************************************************

    private Args<DataType> mArgs;

//*********************************************************
// private constants
//*********************************************************

    private static final int DATA_ICON_DELETE = R.drawable.ic_baseline_delete_forever_24;
    
    private static final String DETAILS_DELETE_TAG = "DetailsFragment";



//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * ADD: Positive action sets a result to ADDED; Negative action does nothing & exits the
     * fragment UPDATE: Positive action sets a result to UPDATED; Negative action sets a result to
     * DELETED
     */
    public enum Mode
    {
        ADD,
        UPDATE
    }
    
    public static class DeleteDialogParams
    {
        public String tag = "DetailsDeleteDialog";
        public int titleId;
        public int messageId;
    }
    
    public static class Args<DataType>
            implements Serializable
    {
        public DataType initialData;
        public Mode mode;
        public static final long serialVersionUID = Params.serialVersionUID;
    }

//*********************************************************
// abstract
//*********************************************************

    protected abstract Args<DataType> getDetailsArgs();
    
    protected abstract Class<? extends DetailsResult<DataType>> getResultClass();

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        
        // handle the result being set post-configuration-change
        handleResultAction();
        
        getViewModel().initData(_getDetailsArgs().initialData);
        
        // assuming the delete dialog is still used, handle results from it
        getActivityViewModel(DeleteDialog.Actions.class).onPositiveAction().observe(
                getViewLifecycleOwner(),
                event -> {
                    if (event.isFreshForTag(DETAILS_DELETE_TAG)) {
                        getViewModel().setResultAction(DetailsResult.Action.DELETED);
                    }
                });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // The view model data is cleared on user actions to distinguish cases where the fragment
        // is destroyed on a user action vs by the system (eg an orientation change, where the data
        // should be preserved and the UI re-initialized)
        if (item.getItemId() == android.R.id.home) { // up button
            return clearDataThenNavigateUp();
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected boolean onPositiveAction()
    {
        switch (_getDetailsArgs().mode) {
        case ADD:
            onAdd();
            return true;
        case UPDATE:
            onUpdate();
            return true;
        default:
            return false;
        }
    }
    
    @Override
    protected boolean onNegativeAction()
    {
        switch (_getDetailsArgs().mode) {
        case ADD:
            return clearDataThenNavigateUp();
        case UPDATE:
            onDelete();
            return true;
        default:
            return false;
        }
    }
    
    @Override
    protected void onBackPressed()
    {
        clearDataThenNavigateUp();
    }
    
    @Override
    protected ActionFragment.Params getActionFragmentParams()
    {
        Args<DataType> args = _getDetailsArgs();
        ActionFragment.Params params = new ActionFragment.Params();
        if (args.mode == Mode.UPDATE) {
            params.negativeIcon = DATA_ICON_DELETE;
        }
        return params;
    }



//*********************************************************
// protected api
//*********************************************************

    
    /**
     * @return Returns true, useful for handling menu actions.
     */
    protected boolean clearDataThenNavigateUp()
    {
        getViewModel().clearData();
        navigateUp();
        return true;
    }
    
    /**
     * Children should override this to add custom functionality. By default this adds then exits
     * the fragment.
     */
    protected void onAdd()
    {
        getViewModel().setResultAction(DetailsResult.Action.ADDED);
    }
    
    /**
     * Children should override this to add custom functionality. By default this updates then exits
     * the fragment.
     */
    protected void onUpdate()
    {
        getViewModel().setResultAction(DetailsResult.Action.UPDATED);
    }
    
    /**
     * Children should only override this if they are using the default onDelete() (i.e. displaying
     * a standard delete dialog)
     */
    protected DeleteDialogParams getDeleteDialogParams()
    {
        return new DeleteDialogParams();
    }
    
    /**
     * Children should override this to add custom functionality. By default this displays a delete
     * dialog, whose OnDeleteListener deletes the item then exits the fragment.
     */
    protected void onDelete()
    {
        DeleteDialogParams dialogParams = getDeleteDialogParams();
        
        DeleteDialog deleteDialog = DeleteDialog.createInstance(
                DETAILS_DELETE_TAG,
                dialogParams.titleId,
                dialogParams.messageId);
        
        deleteDialog.show(getChildFragmentManager(), dialogParams.tag);
    }

//*********************************************************
// private methods
//*********************************************************

    private void handleResultAction()
    {
        getViewModel().getResultAction().observe(getViewLifecycleOwner(), action -> {
            // Transfer the fragment result to the shared DetailsResult model, along with the
            // action used on the fragment.
            _getDetailsResult().setResult(new DetailsResult.Result<>(
                    getViewModel().getResult(),
                    action));
            clearDataThenNavigateUp();
            getViewModel().onResultActionHandled();
        });
    }
    
    private Args<DataType> _getDetailsArgs()
    {
        mArgs = CommonUtils.lazyInit(mArgs, this::getDetailsArgs);
        return mArgs;
    }
    
    private DetailsResult<DataType> _getDetailsResult()
    {
        return new ViewModelProvider(requireActivity()).get(getResultClass());
    }
}
