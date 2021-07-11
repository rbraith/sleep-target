package com.rbraithwaite.sleepapp.utils.interfaces;

public interface OneWayConverter<A, B>
{
//*********************************************************
// abstract
//*********************************************************

    B convert(A a);
}
