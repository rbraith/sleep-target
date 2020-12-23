package com.rbraithwaite.sleepapp.ui.session_archive;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleepapp.R;

public class SessionArchiveDeleteDialog
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private OnPositiveButtonClickListener mOnPositiveButtonClickListener;
    private int mSessionPosition;

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
        void onPositiveButtonClick(DialogInterface dialog, int sessionPosition);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mSessionPosition = getArguments().getInt(ARG_SESSION_POSITION);
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.session_archive_delete_dialog_title)
                .setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setMessage(R.string.session_archive_delete_dialog_message)
                .setNegativeButton(R.string.session_archive_delete_dialog_negative, null)
                .setPositiveButton(R.string.session_archive_delete_dialog_positive,
                                   new DialogInterface.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which)
                                       {
                                           mOnPositiveButtonClickListener.onPositiveButtonClick(
                                                   dialog,
                                                   mSessionPosition);
                                       }
                                   });
        
        return builder.create();
    }
    
//*********************************************************
// api
//*********************************************************

    public static SessionArchiveDeleteDialog createInstance(
            Bundle args,
            OnPositiveButtonClickListener listener)
    {
        SessionArchiveDeleteDialog dialog = new SessionArchiveDeleteDialog();
        dialog.setArguments(args);
        dialog.setOnPositiveButtonClickListener(listener);
        return dialog;
    }
    
    public static Bundle createArguments(int sessionPosition)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_SESSION_POSITION, sessionPosition);
        return args;
    }
    
    public void setOnPositiveButtonClickListener(OnPositiveButtonClickListener listener)
    {
        mOnPositiveButtonClickListener = listener;
    }
}