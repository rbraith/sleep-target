package com.rbraithwaite.sleepapp.utils;

import com.rbraithwaite.sleepapp.utils.interfaces.Factory;

public class CommonUtils
{
//*********************************************************
// constructors
//*********************************************************

    private CommonUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static <T> T lazyInit(T obj, Factory<T> ifNull)
    {
        return obj == null ? ifNull.create() : obj;
    }
}
