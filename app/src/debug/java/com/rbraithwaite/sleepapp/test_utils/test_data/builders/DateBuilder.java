package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateBuilder
        implements BuilderOf<Date>
{
//*********************************************************
// private properties
//*********************************************************

    private GregorianCalendar mCal;
    
//*********************************************************
// constructors
//*********************************************************

    public DateBuilder()
    {
        mCal = new GregorianCalendar(2021, 8, 19, 12, 34);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Date build()
    {
        return mCal.getTime();
    }
    
//*********************************************************
// api
//*********************************************************

    public DateBuilder addMinutes(int minutes)
    {
        mCal.add(Calendar.MINUTE, minutes);
        return this;
    }
    
    /**
     * month starts from 1, hourOfDay is 24 hrs.
     */
    public DateBuilder withValue(int year, int month, int dayOfMonth, int hourOfDay, int minute)
    {
        mCal.set(year, month - 1, dayOfMonth, hourOfDay, minute);
        return this;
    }
}
