package com.rbraithwaite.sleepapp.ui.session_archive;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleepapp.R;

// TODO [21-12-29 12:37AM] -- this will need to be SessionDataDeleteDialog (since it will now be
//  appearing in the SessionDataFragment instead of the archive).

public class SessionArchiveDeleteDialog
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private OnPositiveButtonClickListener mOnPositiveButtonClickListener;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchDltDiag";
    private static final String ARG_SESSION_POSITION = "session position";

//*********************************************************
// public helpers
//*********************************************************

    public interface OnPositiveButtonClickListener
    {
        void onPositiveButtonClick(DialogInterface dialog);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.session_archive_delete_dialog_title)
                .setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setMessage(R.string.session_archive_delete_dialog_message)
                .setNegativeButton(R.string.session_archive_delete_dialog_negative, null)
                .setPositiveButton(
                        R.string.session_archive_delete_dialog_positive,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                mOnPositiveButtonClickListener.onPositiveButtonClick(dialog);
                            }
                        });
        return builder.create();
    }

//*********************************************************
// api
//*********************************************************

    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener)
    {
        mOnPositiveButtonClickListener = listener;
    }
}
