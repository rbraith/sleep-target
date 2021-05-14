package com.rbraithwaite.sleepapp.utils.interfaces;

public interface Action<Input>
{
//*********************************************************
// abstract
//*********************************************************

    void performOn(Input input);
}
