package com.rbraithwaite.sleepapp.ui.session_data.controllers;

import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.dialog.DatePickerFragment;
import com.rbraithwaite.sleepapp.ui.dialog.TimePickerFragment;
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
        mViewModel.getDateText().observe(mLifecycleOwner, new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                mDateText.setText(s);
            }
        });
        
        mViewModel.getTimeOfDayText().observe(mLifecycleOwner, new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                mTimeOfDayText.setText(s);
            }
        });
    }
    
    private void setupListeners()
    {
        mDateText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onDateClicked();
            }
        });
        
        mTimeOfDayText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onTimeOfDayClicked();
            }
        });
    }
    
    private void onDateClicked()
    {
        LiveDataFuture.getValue(
                mViewModel.getDate(),
                mLifecycleOwner,
                new LiveDataFuture.OnValueListener<DateTimeViewModel.Date>()
                {
                    @Override
                    public void onValue(DateTimeViewModel.Date value)
                    {
                        DatePickerFragment datePicker = new DatePickerFragment();
                        datePicker.setArguments(DatePickerFragment.createArguments(
                                value.year, value.month, value.dayOfMonth));
                        datePicker.setOnDateSetListener(new DatePickerFragment.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(
                                    DatePicker view,
                                    int year,
                                    int month,
                                    int dayOfMonth)
                            {
                                if (mCallbacks != null) {
                                    if (!mCallbacks.beforeSetDate(year, month, dayOfMonth)) {
                                        return;
                                    }
                                }
                                mViewModel.setDate(year, month, dayOfMonth);
                            }
                        });
                        datePicker.show(mFragmentManager, DIALOG_DATE_PICKER);
                    }
                });
    }
    
    private void onTimeOfDayClicked()
    {
        LiveDataFuture.getValue(
                mViewModel.getTimeOfDay(),
                mLifecycleOwner,
                new LiveDataFuture.OnValueListener<DateTimeViewModel.TimeOfDay>()
                {
                    @Override
                    public void onValue(DateTimeViewModel.TimeOfDay value)
                    {
                        TimePickerFragment timePicker = new TimePickerFragment();
                        timePicker.setArguments(TimePickerFragment.createArguments(
                                value.hourOfDay, value.minute));
                        timePicker.setOnTimeSetListener(new TimePickerFragment.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                            {
                                if (mCallbacks != null) {
                                    if (!mCallbacks.beforeSetTimeOfDay(hourOfDay, minute)) {
                                        return;
                                    }
                                }
                                mViewModel.setTimeOfDay(hourOfDay, minute);
                            }
                        });
                        timePicker.show(mFragmentManager, DIALOG_TIME_PICKER);
                    }
                });
    }
}
