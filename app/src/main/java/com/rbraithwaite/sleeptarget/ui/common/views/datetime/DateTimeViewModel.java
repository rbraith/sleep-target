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
package com.rbraithwaite.sleeptarget.ui.common.views.datetime;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

// This is intentionally not a framework ViewModel, as multiple simultaneous instances are required.
public class DateTimeViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private Formatter mFormatter;
    private MutableLiveData<Day> mDate = new MutableLiveData<>();
    private MutableLiveData<TimeOfDay> mTimeOfDay = new MutableLiveData<>();

//*********************************************************
// public helpers
//*********************************************************

    public interface Formatter
    {
        String formatTimeOfDay(int hourOfDay, int minute);
        String formatDate(int year, int month, int dayOfMonth);
    }

//*********************************************************
// api
//*********************************************************

    public void setFormatter(Formatter formatter)
    {
        mFormatter = formatter;
    }
    
    public LiveData<Day> getDate()
    {
        return mDate;
    }
    
    public void setDate(Day date)
    {
        mDate.setValue(date);
    }
    
    public LiveData<TimeOfDay> getTimeOfDay()
    {
        return mTimeOfDay;
    }
    
    public void setTimeOfDay(TimeOfDay timeOfDay)
    {
        mTimeOfDay.setValue(timeOfDay);
    }
    
    public LiveData<String> getDateText()
    {
        return Transformations.map(
                getDate(),
                date -> {
                    if (date == null || mFormatter == null) {
                        return null;
                    }
                    return mFormatter.formatDate(date.year, date.month, date.dayOfMonth);
                });
    }
    
    public LiveData<String> getTimeOfDayText()
    {
        return Transformations.map(
                getTimeOfDay(),
                timeOfDay -> {
                    if (timeOfDay == null || mFormatter == null) {
                        return null;
                    }
                    return mFormatter.formatTimeOfDay(timeOfDay.hourOfDay, timeOfDay.minute);
                });
    }
}
