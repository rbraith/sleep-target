package com.rbraithwaite.sleepapp.ui.common.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener
{
//*********************************************************
// private properties
//*********************************************************

    private OnDateSetListener mListener;
    private DatePicker mDatePicker;

//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

//*********************************************************
// public helpers
//*********************************************************

    public interface OnDateSetListener
    {
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        
        return new DatePickerDialog(
                requireActivity(),
                this,
                args.getInt(ARG_YEAR),
                args.getInt(ARG_MONTH),
                args.getInt(ARG_DAY));
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        if (mListener != null) {
            mListener.onDateSet(view, year, month, dayOfMonth);
        }
    }

//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(int year, int month, int dayOfMonth)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, dayOfMonth);
        return args;
    }
    
    public void setOnDateSetListener(DatePickerFragment.OnDateSetListener listener)
    {
        mListener = listener;
    }
    
    public DatePicker getDatePicker()
    {
        return mDatePicker;
    }
}
