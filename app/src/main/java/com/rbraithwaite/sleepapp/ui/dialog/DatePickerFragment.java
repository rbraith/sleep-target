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
// private constants
//*********************************************************

    private static final String ARG_DATE = "date";
    
//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getArguments().getLong(ARG_DATE));
        
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
        // TO IMPLEMENT
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(long dateMillis)
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, dateMillis);
        return args;
    }
}
