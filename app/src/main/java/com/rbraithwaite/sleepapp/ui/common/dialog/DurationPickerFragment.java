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

package com.rbraithwaite.sleepapp.ui.common.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.rbraithwaite.sleepapp.R;

public class DurationPickerFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    private OnDurationSetListener mListener;
    private int mHour;
    private int mMinute;

//*********************************************************
// public helpers
//*********************************************************

    public interface OnDurationSetListener
    {
        void onDurationSet(DialogInterface dialog, int which, int hour, int minute);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        View durationPicker = requireActivity().getLayoutInflater().inflate(
                R.layout.duration_picker, null);
        
        NumberPicker hourPicker = durationPicker.findViewById(R.id.hour_picker);
        hourPicker.setMaxValue(99);
        hourPicker.setValue(mHour);
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> setHour(newVal));
        
        NumberPicker minutePicker = durationPicker.findViewById(R.id.minute_picker);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(mMinute);
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> setMinute(newVal));
        
        builder.setView(durationPicker)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (mListener != null) {
                        mListener.onDurationSet(dialog, which, mHour, mMinute);
                    }
                });
        return builder.create();
    }

//*********************************************************
// api
//*********************************************************

    public static DurationPickerFragment createInstance(
            int initialHour,
            int initialMinute,
            OnDurationSetListener onDurationSetListener)
    {
        if (!isValidHour(initialHour)) {
            throw new IllegalArgumentException(String.format("Invalid hour: %d", initialHour));
        }
        if (!isValidMinute(initialMinute)) {
            throw new IllegalArgumentException(String.format("Invalid minute: %d", initialMinute));
        }
        
        DurationPickerFragment fragment = new DurationPickerFragment();
        fragment.mHour = initialHour;
        fragment.mMinute = initialMinute;
        fragment.mListener = onDurationSetListener;
        return fragment;
    }
    
    // TODO [21-01-29 7:19PM] -- consider a createInstance(OnDurationSetListener) overload, where
    //  the hour and minute are set to some default value.

//*********************************************************
// private methods
//*********************************************************

    private static boolean isValidHour(int hour)
    {
        return (hour >= 0);
    }
    
    private static boolean isValidMinute(int minute)
    {
        return ((minute >= 0) && (minute < 60));
    }
    
    private void setMinute(int minute)
    {
        mMinute = minute;
    }
    
    private void setHour(int hour)
    {
        mHour = hour;
    }
}
