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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.rbraithwaite.sleeptarget.utils.TaggedLiveEvent;
import com.rbraithwaite.sleeptarget.utils.time.Day;

public class DatePickerFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener
{
//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_DAY = "ArgDay";
    private static final String ARG_EVENT_TAG = "ArgEventTag";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class ViewModel
            extends androidx.lifecycle.ViewModel
    {
        private Day mDay;
        private MutableLiveData<TaggedLiveEvent<Day>> mOnDateSetEvent = new MutableLiveData<>();
        
        public static ViewModel getInstance(FragmentActivity activity)
        {
            return new ViewModelProvider(activity).get(ViewModel.class);
        }
        
        public void initDay(Day day)
        {
            mDay = day;
        }
        
        public Day getDay()
        {
            return mDay;
        }
        
        public void setDay(String tag, Day day)
        {
            mDay = day;
            mOnDateSetEvent.setValue(new TaggedLiveEvent<>(tag, day));
        }
        
        public LiveData<TaggedLiveEvent<Day>> onDateSet()
        {
            return mOnDateSetEvent;
        }
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // SMELL [21-10-16 2:06AM] -- It's weird using essentially a singleton view model
        //  between different dialog instances - it basically relies on not more than one
        //  dialog instance existing at a time.
        Day initialDay = (Day) getArguments().getSerializable(ARG_DAY);
        getViewModel().initDay(initialDay);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Day day = getViewModel().getDay();
        
        return new DatePickerDialog(
                requireActivity(),
                this,
                day.year,
                day.month,
                day.dayOfMonth);
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        // https://stackoverflow.com/a/20499834
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
            !view.isShown()) {
            return;
        }
        String eventTag = getArguments().getString(ARG_EVENT_TAG);
        getViewModel().setDay(eventTag, new Day(year, month, dayOfMonth));
    }
    
//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(String eventTag, Day initialDay)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DAY, initialDay);
        args.putString(ARG_EVENT_TAG, eventTag);
        return args;
    }
    
    public static DatePickerFragment createInstance(String eventTag, Day initialDay)
    {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArguments(createArguments(eventTag, initialDay));
        return datePickerFragment;
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
