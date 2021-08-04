package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarBuilder
        implements BuilderOf<GregorianCalendar>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mDate;
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public GregorianCalendar build()
    {
        GregorianCalendar cal = new GregorianCalendar();
        if (mDate != null) {
            cal.setTime(mDate);
        }
        return cal;
    }
    
//*********************************************************
// api
//*********************************************************

    public CalendarBuilder with(DateBuilder date)
    {
        mDate = date.build();
        return this;
    }
}
