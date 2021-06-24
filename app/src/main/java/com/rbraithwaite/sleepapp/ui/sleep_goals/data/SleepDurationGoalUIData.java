package com.rbraithwaite.sleepapp.ui.sleep_goals.data;

public class SleepDurationGoalUIData
{
//*********************************************************
// public properties
//*********************************************************

    public int hours;
    public int remainingMinutes;

//*********************************************************
// constructors
//*********************************************************

    public SleepDurationGoalUIData(int hours, int remainingMinutes)
    {
        this.hours = hours;
        this.remainingMinutes = remainingMinutes;
    }
}
