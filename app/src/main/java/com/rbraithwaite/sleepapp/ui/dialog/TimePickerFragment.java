package com.rbraithwaite.sleepapp.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimePickerFragment
        extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
//*********************************************************
// private properties
//*********************************************************

    private OnTimeSetListener mListener;

//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_DATETIME = "datetime";

//*********************************************************
// public helpers
//*********************************************************

    public interface OnTimeSetListener
    {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(args.getLong(ARG_DATETIME));
        
        return new TimePickerDialog(
                requireContext(),
                this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        if (mListener != null) {
            mListener.onTimeSet(view, hourOfDay, minute);
        }
    }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(long dateTimeMillis)
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATETIME, dateTimeMillis);
        return args;
    }
    
    public void setOnTimeSetListener(OnTimeSetListener listener)
    {
        mListener = listener;
    }
}
