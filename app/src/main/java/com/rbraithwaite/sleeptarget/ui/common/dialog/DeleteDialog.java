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
import android.os.Bundle;

import com.rbraithwaite.sleeptarget.R;

public class DeleteDialog
        extends AlertDialogFragment2
{
//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_TITLE_ID = "title id";
    private static final String ARG_MESSAGE_ID = "message id";
    private static final String ARG_DIALOG_TAG = "tag";
    private static final int NO_ID = -1;

//*********************************************************
// public constants
//*********************************************************

    public static final String DEFAULT_DIALOG_TAG = "default delete tag";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Actions
            extends AlertDialogFragment2.Actions {}
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected AlertDialog createAlertDialog()
    {
        String dialogTag = getArguments().getString(ARG_DIALOG_TAG);
        int titleId = getArguments().getInt(ARG_TITLE_ID);
        int messageId = getArguments().getInt(ARG_MESSAGE_ID);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        DeleteDialog.Actions actions = getActions(DeleteDialog.Actions.class);
        
        builder.setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete,
                                   (dialog, which) -> actions.positiveAction(dialogTag));
        if (titleId != NO_ID) {
            builder.setTitle(titleId);
        }
        if (messageId != NO_ID) {
            builder.setMessage(messageId);
        }
        
        return builder.create();
    }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(String tag, Integer titleId, Integer messageId)
    {
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_TAG, tag);
        args.putInt(ARG_TITLE_ID, titleId == null ? NO_ID : titleId);
        args.putInt(ARG_MESSAGE_ID, messageId == null ? NO_ID : messageId);
        return args;
    }
    
    public static DeleteDialog createInstance(Integer titleId, Integer messageId)
    {
        return createInstance(DEFAULT_DIALOG_TAG, titleId, messageId);
    }
    
    public static DeleteDialog createInstance(String tag, Integer titleId, Integer messageId)
    {
        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(createArguments(tag, titleId, messageId));
        return dialog;
    }
}
