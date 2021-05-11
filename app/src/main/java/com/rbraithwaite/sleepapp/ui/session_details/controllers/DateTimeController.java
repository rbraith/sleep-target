package com.rbraithwaite.sleepapp.ui.session_details.controllers;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.dialog.DatePickerFragment;
import com.rbraithwaite.sleepapp.ui.common.dialog.TimePickerFragment;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import java.util.Calendar;
import java.util.GregorianCalendar;



/**
 * Controls the Date + Time-of-Day displays in the session data screen.
 */
public class DateTimeController
{
//*********************************************************
// private properties
//*********************************************************

    private View mRoot;
    
    private TextView mDateText;
    private TextView mTimeOfDayText;
    private TextView mTitle;
    
    private LifecycleOwner mLifecycleOwner;
    private FragmentManager mFragmentManager;
    
    private DateTimeViewModel mViewModel;
    private Callbacks mCallbacks;

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
            GregorianCalendar initialData,
            View root,
            Formatter formatter,
            LifecycleOwner lifecycleOwner,
            FragmentManager fragmentManager)
    {
        mRoot = root;
        
        mTitle = root.findViewById(R.id.name);
        mTitle.setText(title);
        
        mDateText = root.findViewById(R.id.date);
        mTimeOfDayText = root.findViewById(R.id.time);
        
        mLifecycleOwner = lifecycleOwner;
        mFragmentManager = fragmentManager;
        
        mViewModel = createViewModel();
        
        bindViewModel();
        setFormatter(formatter);
        mViewModel.setDate(
                initialData.get(Calendar.YEAR),
                initialData.get(Calendar.MONTH),
                initialData.get(Calendar.DAY_OF_MONTH));
        mViewModel.setTimeOfDay(
                initialData.get(Calendar.HOUR_OF_DAY),
                initialData.get(Calendar.MINUTE));
        
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
// protected api
//*********************************************************

    protected DateTimeViewModel createViewModel()
    {
        return new DateTimeViewModel();
    }

//*********************************************************
// private methods
//*********************************************************

    private void bindViewModel()
    {
        mViewModel.getDateText().observe(mLifecycleOwner, s -> mDateText.setText(s));
        
        mViewModel.getTimeOfDayText().observe(mLifecycleOwner, s -> mTimeOfDayText.setText(s));
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
                    DatePickerFragment datePicker = new DatePickerFragment();
                    datePicker.setArguments(DatePickerFragment.createArguments(
                            date.year, date.month, date.dayOfMonth));
                    datePicker.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                        if (mCallbacks != null) {
                            if (!mCallbacks.beforeSetDate(year, month, dayOfMonth)) {
                                return;
                            }
                        }
                        mViewModel.setDate(year, month, dayOfMonth);
                    });
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
                    timePicker.setArguments(TimePickerFragment.createArguments(
                            timeOfDay.hourOfDay, timeOfDay.minute));
                    timePicker.setOnTimeSetListener((view, hourOfDay, minute) -> {
                        if (mCallbacks != null) {
                            if (!mCallbacks.beforeSetTimeOfDay(hourOfDay, minute)) {
                                return;
                            }
                        }
                        mViewModel.setTimeOfDay(hourOfDay, minute);
                    });
                    timePicker.show(mFragmentManager, DIALOG_TIME_PICKER);
                });
    }
}
