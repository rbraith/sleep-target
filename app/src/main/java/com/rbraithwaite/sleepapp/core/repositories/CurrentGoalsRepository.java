package com.rbraithwaite.sleepapp.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;

import java.util.List;

// TODO [21-03-24 11:04PM] document these methods.
public interface CurrentGoalsRepository
{
//*********************************************************
// abstract
//*********************************************************

    LiveData<WakeTimeGoal> getWakeTimeGoal();
    
    void setWakeTimeGoal(final WakeTimeGoal wakeTimeGoal);
    
    void clearWakeTimeGoal();
    
    LiveData<SleepDurationGoal> getSleepDurationGoal();
    
    void setSleepDurationGoal(final SleepDurationGoal sleepDurationGoal);
    
    void clearSleepDurationGoal();
    
    /**
     * Returns the full history of wake-time goal edits.
     */
    LiveData<List<WakeTimeGoal>> getWakeTimeGoalHistory();
    
    LiveData<List<SleepDurationGoal>> getSleepDurationGoalHistory();
}
