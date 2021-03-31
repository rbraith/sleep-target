package com.rbraithwaite.sleepapp.data.prefs;

import java.util.Date;

public class CurrentSessionPrefsData
{
//*********************************************************
// public constants
//*********************************************************

    public final Date start;
    public final String additionalComments;
    
//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionPrefsData(Date start, String additionalComments)
    {
        this.start = start;
        this.additionalComments = additionalComments;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + (start == null ? 0 : start.hashCode());
        hash = prime * hash + (additionalComments == null ? 0 : additionalComments.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CurrentSessionPrefsData that = (CurrentSessionPrefsData) o;
        return ((start == null && that.start == null) || start.equals(that.start)) &&
               ((additionalComments == null && that.additionalComments == null) ||
                additionalComments.equals(that.additionalComments));
    }
}
