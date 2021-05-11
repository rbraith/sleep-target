package com.rbraithwaite.sleepapp.ui.session_details.data;

// REFACTOR [21-02-9 10:35PM] -- this duplicates ui.sleep_goals.data.SleepDurationGoalUIData.java
//  -- should I make some ui.common_data package or something? Or should I leave this as is to
//  keep things separated.
public class SessionDataSleepDurationGoal
{
//*********************************************************
// public properties
//*********************************************************

    public int hours;
    public int remainingMinutes;

//*********************************************************
// constructors
//*********************************************************

    public SessionDataSleepDurationGoal(int hours, int remainingMinutes)
    {
        this.hours = hours;
        this.remainingMinutes = remainingMinutes;
    }
}
