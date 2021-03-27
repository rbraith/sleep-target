package com.rbraithwaite.sleepapp.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;

import java.util.Date;

// TODO [21-03-24 11:04PM] document these methods.
public interface CurrentSessionRepository
{
//*********************************************************
// abstract
//*********************************************************

    void clearCurrentSession();
    
    LiveData<CurrentSession> getCurrentSession();
    
    void setCurrentSession(Date start);
}
