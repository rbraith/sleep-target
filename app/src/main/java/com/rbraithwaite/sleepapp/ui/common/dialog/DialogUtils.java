package com.rbraithwaite.sleepapp.ui.common.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.rbraithwaite.sleepapp.R;

public class DialogUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DialogUtils() {/* No instantiation */}


//*********************************************************
// api
//*********************************************************

    public static AlertDialogFragment createDeleteDialog(
            Context context,
            Integer titleId,
            DialogInterface.OnClickListener onDeleteListener)
    {
        return createDeleteDialog(context, titleId, null, onDeleteListener);
    }
    
    // TODO [21-01-27 11:09PM] -- consider unit tests I could write for this.
    public static AlertDialogFragment createDeleteDialog(
            Context context,
            Integer titleId,
            Integer messageId,
            DialogInterface.OnClickListener onDeleteListener)
    {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        
        builder.setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, onDeleteListener);
        if (titleId != null) {
            builder.setTitle(titleId);
        }
        if (messageId != null) {
            builder.setMessage(messageId);
        }
        
        return AlertDialogFragment.createInstance(
                new AlertDialogFragment.AlertDialogFactory()
                {
                    @Override
                    public AlertDialog create() { return builder.create(); }
                });
    }
}
