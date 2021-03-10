package com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration;

import android.provider.BaseColumns;

public class SleepDurationGoalContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "sleep_duration_goal";

//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String EDIT_TIME = "edit_time";
        public static final String GOAL_MINUTES = "goal";
    }

//*********************************************************
// constructors
//*********************************************************

    private SleepDurationGoalContract() {/* No instantiation */}
}
