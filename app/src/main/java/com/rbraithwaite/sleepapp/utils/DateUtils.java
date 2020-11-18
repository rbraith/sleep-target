package com.rbraithwaite.sleepapp.utils;

import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils
{
//*********************************************************
// api
//*********************************************************

    // TODO [20-11-15 1:05AM] -- think about how I could test this.
    //  idk if I could? probably not a huge deal
    public static Date getNow()
    {
        return new GregorianCalendar().getTime();
    }
}
