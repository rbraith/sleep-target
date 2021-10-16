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
package com.rbraithwaite.sleeptarget.ui.common.views.datetime;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.dialog.DatePickerFragment;
import com.rbraithwaite.sleeptarget.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;



/**
 * Controls the Date + Time-of-Day displays in the session data screen.
 */
public class DateTimeController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    private Fragment mParentFragment;
    
    private TextView mDateText;
    private TextView mTimeOfDayText;
    private TextView mTitle;
    
    private LifecycleOwner mLifecycleOwner;
    private FragmentManager mFragmentManager;
    
    private DateTimeViewModel mViewModel;
    private Callbacks mCallbacks;
    
    /**
     * This is used to distinguish Date/TimePickerFragment view model events. It's assumed that
     * there can be multiple instances of DateTime components on the screen, each of which would
     * be observing the shared Date/TimePickerFragment view models. Events are distinguished
     * between the different DateTime components so that one component doesn't consume an
     * event meant for another.
     */
    private String mEventTag;

//*********************************************************
// public constants
//*********************************************************

    public static final String DIALOG_DATE_PICKER = "DateTimeController_DateDialog";
    public static final String DIALOG_TIME_PICKER = "DateTimeController_TimeDialog";

//*********************************************************
// public helpers
//*********************************************************

    public interface Callbacks
    {
        /**
         * Called before the UI is updated. If false, the UI is not updated.
         */
        boolean beforeSetDate(int year, int month, int dayOfMonth);
        
        /**
         * Called before the UI is updated. If false, the UI is not updated.
         */
        boolean beforeSetTimeOfDay(int hourOfDay, int minute);
    }
    
    public interface Formatter
    {
        String formatTimeOfDay(int hourOfDay, int minute);
        String formatDate(int year, int month, int dayOfMonth);
    }

//*********************************************************
// constructors
//*********************************************************

    public DateTimeController(
            String title,
            String eventTag,
            DateTimeViewModel viewModel,
            View root,
            Formatter formatter,
            Fragment parentFragment)
    {
        mRoot = root;
        mParentFragment = parentFragment;
        
        mTitle = root.findViewById(R.id.name);
        mTitle.setText(title);
        
        mDateText = root.findViewById(R.id.date);
        mTimeOfDayText = root.findViewById(R.id.time);
        
        mLifecycleOwner = mParentFragment.getViewLifecycleOwner();
        mFragmentManager = mParentFragment.getChildFragmentManager();
        
        mViewModel = viewModel;
        
        mEventTag = eventTag;
        
        bindViewModel();
        setFormatter(formatter);
        setupListeners();
    }

//*********************************************************
// api
//*********************************************************

    public void setCallbacks(Callbacks callbacks)
    {
        mCallbacks = callbacks;
    }
    
    public void setFormatter(final Formatter formatter)
    {
        mViewModel.setFormatter(new DateTimeViewModel.Formatter()
        {
            @Override
            public String formatTimeOfDay(int hourOfDay, int minute)
            {
                return formatter.formatTimeOfDay(hourOfDay, minute);
            }
            
            @Override
            public String formatDate(int year, int month, int dayOfMonth)
            {
                return formatter.formatDate(year, month, dayOfMonth);
            }
        });
    }

//*********************************************************
// private methods
//*********************************************************

    private void bindViewModel()
    {
        mViewModel.getDateText().observe(mLifecycleOwner, s -> mDateText.setText(s));
        
        mViewModel.getTimeOfDayText().observe(mLifecycleOwner, s -> mTimeOfDayText.setText(s));
        
        DatePickerFragment.ViewModel.getInstance(mParentFragment.requireActivity())
                .onDateSet()
                .observe(mLifecycleOwner, dateEvent -> {
                    if (!dateEvent.getTag().equals(mEventTag) || dateEvent.isStale()) {
                        return;
                    }
    
                    Day day = dateEvent.getExtra();
                    if (mCallbacks != null &&
                        !mCallbacks.beforeSetDate(day.year, day.month, day.dayOfMonth)) {
                        return;
                    }
                    mViewModel.setDate(day);
                });
    }
    
    private void setupListeners()
    {
        mDateText.setOnClickListener(v -> onDateClicked());
        
        mTimeOfDayText.setOnClickListener(v -> onTimeOfDayClicked());
    }
    
    private void onDateClicked()
    {
        LiveDataFuture.getValue(
                mViewModel.getDate(),
                mLifecycleOwner,
                date -> {
                    DatePickerFragment datePicker = DatePickerFragment.createInstance(mEventTag, date);
                    datePicker.show(mFragmentManager, DIALOG_DATE_PICKER);
                });
    }
    
    private void onTimeOfDayClicked()
    {
        LiveDataFuture.getValue(
                mViewModel.getTimeOfDay(),
                mLifecycleOwner,
                timeOfDay -> {
                    TimePickerFragment timePicker = new TimePickerFragment();
                    // REFACTOR [21-06-16 10:36PM] it would be better to have like a
                    //  setArgs(hour, minute) method, or a createInstance() static method
                    //  setArguments is only relevent when the framework is creating the
                    //  fragment (in these cases I *would* need a createArguments method, but
                    //  not in this case).
                    timePicker.setArguments(TimePickerFragment.createArguments(
                            timeOfDay.hourOfDay, timeOfDay.minute));
                    timePicker.setOnTimeSetListener((view, hourOfDay, minute) -> {
                        if (mCallbacks != null &&
                            !mCallbacks.beforeSetTimeOfDay(hourOfDay, minute)) {
                            return;
                        }
                        mViewModel.setTimeOfDay(new TimeOfDay(hourOfDay, minute));
                    });
                    timePicker.show(mFragmentManager, DIALOG_TIME_PICKER);
                });
    }
}
