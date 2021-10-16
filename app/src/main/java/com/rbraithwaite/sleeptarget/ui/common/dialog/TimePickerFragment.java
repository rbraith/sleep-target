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
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.utils.TaggedLiveEvent;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

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
// private constants
//*********************************************************

    private static final String ARG_TIME_OF_DAY = "ArgTime";
    private static final String ARG_EVENT_TAG = "ArgEventTag";
    
//*********************************************************
// public helpers
//*********************************************************

    // REFACTOR [21-10-16 3:13PM] -- This duplicates DatePickerFragment.ViewModel
    public static class ViewModel
            extends androidx.lifecycle.ViewModel
    {
        private TimeOfDay mTimeOfDay;
        private MutableLiveData<TaggedLiveEvent<TimeOfDay>> mOnTimeSetEvent =
                new MutableLiveData<>();
        
        public static ViewModel getInstance(FragmentActivity activity)
        {
            return new ViewModelProvider(activity).get(ViewModel.class);
        }
        
        public void init(TimeOfDay timeOfDay)
        {
            mTimeOfDay = timeOfDay;
        }
        
        public TimeOfDay getTimeOfDay()
        {
            return mTimeOfDay;
        }
        
        public void setTimeOfDay(String tag, TimeOfDay timeOfDay)
        {
            mTimeOfDay = timeOfDay;
            mOnTimeSetEvent.setValue(new TaggedLiveEvent<>(tag, timeOfDay));
        }
        
        public LiveData<TaggedLiveEvent<TimeOfDay>> onTimeSet()
        {
            return mOnTimeSetEvent;
        }
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TimeOfDay initialTimeOfDay = (TimeOfDay) getArguments().getSerializable(ARG_TIME_OF_DAY);
        getViewModel().init(initialTimeOfDay);
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        TimeOfDay timeOfDay = getViewModel().getTimeOfDay();
        
        return new TimePickerDialog(
                requireContext(),
                this,
                timeOfDay.hourOfDay,
                timeOfDay.minute,
                false);
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
        
        String eventTag = getArguments().getString(ARG_EVENT_TAG);
        getViewModel().setTimeOfDay(eventTag, new TimeOfDay(hourOfDay, minute));
    }
    
//*********************************************************
// api
//*********************************************************

    public static TimePickerFragment createInstance(String eventTag, TimeOfDay initialTimeOfDay)
    {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(createArguments(eventTag, initialTimeOfDay));
        return fragment;
    }
    
    public static Bundle createArguments(String eventTag, TimeOfDay initialTimeOfDay)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME_OF_DAY, initialTimeOfDay);
        args.putString(ARG_EVENT_TAG, eventTag);
        return args;
    }
    
    public ViewModel getViewModel(FragmentActivity activity)
    {
        return ViewModel.getInstance(activity);
    }

//*********************************************************
// private methods
//*********************************************************

    private ViewModel getViewModel()
    {
        return getViewModel(requireActivity());
    }
}
