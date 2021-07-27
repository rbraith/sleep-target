package com.rbraithwaite.sleepapp.ui.interruption_details;

import com.rbraithwaite.sleepapp.core.models.Interruption;

import java.io.Serializable;

public class InterruptionWrapper
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private Interruption data;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210115;
    
//*********************************************************
// constructors
//*********************************************************

    public InterruptionWrapper(Interruption data)
    {
        this.data = data;
    }
    
//*********************************************************
// api
//*********************************************************

    public Interruption getData()
    {
        return data;
    }
}
