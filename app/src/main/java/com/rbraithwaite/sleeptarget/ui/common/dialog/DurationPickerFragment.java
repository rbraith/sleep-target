/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.utils.LiveDataEvent;

public class DurationPickerFragment
        extends DialogFragment
{
//*********************************************************
// private properties
//*********************************************************

    // HACK [21-10-16 5:32PM] -- This false initialization here is cheating a little bit: exploiting
    //  the fact that the framework uses the default ctor to recreate the fragment on config change.
    //  So when the fragment is created via createInstance() it is instantiating, and when the
    //  framework recreates the fragment it isn't.
    private boolean mInstantiating = false;

//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_HOUR = "hour";
    
    private static final String ARG_MINUTE = "minute";

//*********************************************************
// public helpers
//*********************************************************

    public static class Data
    {
        public int hour;
        public int minute;
        
        public Data(int hour, int minute)
        {
            this.hour = hour;
            this.minute = minute;
        }
    }
    
    public static class ViewModel
            extends androidx.lifecycle.ViewModel
    {
        private int mHour;
        private int mMinute;
        private MutableLiveData<LiveDataEvent<Data>> mDurationSetEvent = new MutableLiveData<>();
        
        public static ViewModel getInstance(FragmentActivity activity)
        {
            return new ViewModelProvider(activity).get(ViewModel.class);
        }
        
        public int getHour()
        {
            return mHour;
        }
        
        public void setHour(int hour)
        {
            mHour = hour;
        }
        
        public int getMinute()
        {
            return mMinute;
        }
        
        public void setMinute(int minute)
        {
            mMinute = minute;
        }
        
        public LiveData<LiveDataEvent<Data>> onDurationSet()
        {
            return mDurationSetEvent;
        }
        
        public void setDuration()
        {
            mDurationSetEvent.setValue(new LiveDataEvent<>(new Data(mHour, mMinute)));
        }
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if (mInstantiating) {
            int hour = getArguments().getInt(ARG_HOUR);
            int minute = getArguments().getInt(ARG_MINUTE);
            
            getViewModel().setHour(hour);
            getViewModel().setMinute(minute);
            
            // just to be safe
            mInstantiating = false;
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        View durationPicker = requireActivity().getLayoutInflater().inflate(
                R.layout.duration_picker, null);
        
        NumberPicker hourPicker = durationPicker.findViewById(R.id.hour_picker);
        hourPicker.setMaxValue(99);
        hourPicker.setValue(getViewModel().getHour());
        hourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> setHour(newVal));
        
        NumberPicker minutePicker = durationPicker.findViewById(R.id.minute_picker);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(getViewModel().getMinute());
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> setMinute(newVal));
        
        builder.setView(durationPicker)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    getViewModel().setDuration();
                });
        return builder.create();
    }

//*********************************************************
// api
//*********************************************************

    // TODO [21-01-29 7:19PM] -- consider a createInstance(OnDurationSetListener) overload, where
    //  the hour and minute are set to some default value.
    public static DurationPickerFragment createInstance(
            int initialHour,
            int initialMinute)
    {
        if (!isValidHour(initialHour)) {
            throw new IllegalArgumentException(String.format("Invalid hour: %d", initialHour));
        }
        if (!isValidMinute(initialMinute)) {
            throw new IllegalArgumentException(String.format("Invalid minute: %d", initialMinute));
        }
        
        DurationPickerFragment fragment = new DurationPickerFragment();
        fragment.setArguments(createArguments(initialHour, initialMinute));
        fragment.mInstantiating = true;
        return fragment;
    }
    
    public static Bundle createArguments(int initialHour, int initialMinute)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, initialHour);
        args.putInt(ARG_MINUTE, initialMinute);
        return args;
    }
    
    public ViewModel getViewModel(FragmentActivity activity)
    {
        return ViewModel.getInstance(activity);
    }

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
    
    private ViewModel getViewModel()
    {
        return getViewModel(requireActivity());
    }
    
    private void setMinute(int minute)
    {
        getViewModel().setMinute(minute);
    }
    
    private void setHour(int hour)
    {
        getViewModel().setHour(hour);
    }
}
