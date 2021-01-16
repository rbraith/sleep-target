package com.rbraithwaite.sleepapp.data.current_session;

import java.util.Date;

public class CurrentSessionModel
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    
//*********************************************************
// constructors
//*********************************************************

    public CurrentSessionModel()
    {
        mStart = null;
    }
    
    public CurrentSessionModel(Date start)
    {
        mStart = start;
    }
    
//*********************************************************
// api
//*********************************************************

    public Date getStart()
    {
        return mStart;
    }
    
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public boolean isSet()
    {
        return mStart != null;
    }
}
