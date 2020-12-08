package com.rbraithwaite.sleepapp.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

    private static final String ARG_DATE = "date";

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
        GregorianCalendar calendar = new GregorianCalendar();
        Bundle args = getArguments();
        calendar.setTimeInMillis(args.getLong(ARG_DATE));
        
        return new DatePickerDialog(
                requireActivity(),
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
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

    public static Bundle createArguments(Long dateMillis)
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, dateMillis);
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
