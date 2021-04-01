package com.rbraithwaite.sleepapp.ui.session_data.controllers;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

// This is intentionally not a framework ViewModel, as multiple simultaneous instances are required.
public class DateTimeViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private Formatter mFormatter;
    private MutableLiveData<Date> mDate = new MutableLiveData<>();
    private MutableLiveData<TimeOfDay> mTimeOfDay = new MutableLiveData<>();
    
//*********************************************************
// public helpers
//*********************************************************

    public interface Formatter
    {
        String formatTimeOfDay(int hourOfDay, int minute);
        String formatDate(int year, int month, int dayOfMonth);
    }
    
    public static class Date
    {
        int year;
        int month;
        int dayOfMonth;
        
        public Date(int year, int month, int dayOfMonth)
        {
            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;
        }
    }
    
    public static class TimeOfDay
    {
        int hourOfDay;
        int minute;
        
        public TimeOfDay(int hourOfDay, int minute)
        {
            this.hourOfDay = hourOfDay;
            this.minute = minute;
        }
    }
    
//*********************************************************
// api
//*********************************************************

    public void setFormatter(Formatter formatter)
    {
        mFormatter = formatter;
    }
    
    public LiveData<Date> getDate()
    {
        return mDate;
    }
    
    public LiveData<TimeOfDay> getTimeOfDay()
    {
        return mTimeOfDay;
    }
    
    public LiveData<String> getDateText()
    {
        return Transformations.map(
                getDate(),
                new Function<Date, String>()
                {
                    @Override
                    public String apply(Date input)
                    {
                        if (input == null || mFormatter == null) {
                            return null;
                        }
                        return mFormatter.formatDate(input.year, input.month, input.dayOfMonth);
                    }
                });
    }
    
    public LiveData<String> getTimeOfDayText()
    {
        return Transformations.map(
                getTimeOfDay(),
                new Function<TimeOfDay, String>()
                {
                    @Override
                    public String apply(TimeOfDay input)
                    {
                        if (input == null || mFormatter == null) {
                            return null;
                        }
                        return mFormatter.formatTimeOfDay(input.hourOfDay, input.minute);
                    }
                });
    }
    
    public void setDate(int year, int month, int dayOfMonth)
    {
        mDate.setValue(new Date(year, month, dayOfMonth));
    }
    
    public void setTimeOfDay(int hourOfDay, int minute)
    {
        mTimeOfDay.setValue(new TimeOfDay(hourOfDay, minute));
    }
}
