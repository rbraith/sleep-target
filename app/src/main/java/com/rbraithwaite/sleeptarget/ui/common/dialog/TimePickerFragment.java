/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleeptarget.utils.SerializableWrapper;

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
    private OnTimeSetListener mListener;

    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";
    
    private static final String STATE_LISTENER = "listener";

    public interface OnTimeSetListener
    {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        maybeInitFromSavedInstanceState(savedInstanceState);
        
        Bundle args = getArguments();
        
        return new TimePickerDialog(
                requireContext(),
                this,
                args.getInt(ARG_HOUR),
                args.getInt(ARG_MINUTE),
                false);
    }
    
    private void maybeInitFromSavedInstanceState(Bundle savedInstanceState)
    {
        if (savedInstanceState != null) {
            SerializableWrapper<OnTimeSetListener> wrapper =
                    (SerializableWrapper<OnTimeSetListener>) savedInstanceState.getSerializable(STATE_LISTENER);
            if (wrapper != null) {
                mListener = wrapper.data;
            }
        }
    }
    
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        // https://stackoverflow.com/a/26034036
        // see also: DatePickerFragment.onDateSet()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
            !view.isShown()) {
            return;
        }
        
        if (mListener != null) {
            mListener.onTimeSet(view, hourOfDay, minute);
        }
    }
    
//*********************************************************
// overrides
//*********************************************************

@Override
public void onSaveInstanceState(@NonNull Bundle outState)
{
    outState.putSerializable(STATE_LISTENER, new SerializableWrapper<>(mListener));
    super.onSaveInstanceState(outState);
}
    
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
