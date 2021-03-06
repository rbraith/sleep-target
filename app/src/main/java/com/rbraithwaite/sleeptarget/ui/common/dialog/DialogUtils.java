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
import android.content.DialogInterface;

import com.rbraithwaite.sleeptarget.R;

public class DialogUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DialogUtils() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    @Deprecated
    public static AlertDialogFragment createDeleteDialog(
            Integer titleId,
            DialogInterface.OnClickListener onDeleteListener)
    {
        return createDeleteDialog(titleId, null, onDeleteListener);
    }
    
    // TODO [21-01-27 11:09PM] -- consider unit tests I could write for this.
    @Deprecated
    public static AlertDialogFragment createDeleteDialog(
            Integer titleId,
            Integer messageId,
            DialogInterface.OnClickListener onDeleteListener)
    {
        return AlertDialogFragment.createInstance((context, inflater) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            
            builder.setIcon(R.drawable.ic_baseline_delete_forever_24)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, onDeleteListener);
            if (titleId != null) {
                builder.setTitle(titleId);
            }
            if (messageId != null) {
                builder.setMessage(messageId);
            }
            
            return builder.create();
        });
    }
}
