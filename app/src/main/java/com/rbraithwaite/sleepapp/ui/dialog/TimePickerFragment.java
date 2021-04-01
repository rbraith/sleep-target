package com.rbraithwaite.sleepapp.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

// IDEA [20-12-5 8:36PM] -- consider creating a custom TimePickerDialog which has
//  max/min times (instead of allowing user to pick any time)
//  https://stackoverflow.com/a/16942630
//  I decided not to go with this idea for now since I would need to find a way
//  to grey-out un-selectable times in order to match the behaviour of
//  DatePicker.maxDate(), minDate()
//  --
//  Note: If I were to go with this behaviour, I would need to rework
//  DatePickerFragment
//  to use max/min date values (as it originally did - see DatePickerFragment &
//  SessionEditFragment.initStartTime() in commit [main c3d7e12])
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

    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";

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
        
        return new TimePickerDialog(
                requireContext(),
                this,
                args.getInt(ARG_HOUR),
                args.getInt(ARG_MINUTE),
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

    public static Bundle createArguments(int hourOfDay, int minute)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, hourOfDay);
        args.putInt(ARG_MINUTE, minute);
        return args;
    }
    
    public void setOnTimeSetListener(OnTimeSetListener listener)
    {
        mListener = listener;
    }
}
