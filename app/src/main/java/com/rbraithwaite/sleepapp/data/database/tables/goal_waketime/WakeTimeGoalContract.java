package com.rbraithwaite.sleepapp.data.database.tables.goal_waketime;

import android.provider.BaseColumns;

public class WakeTimeGoalContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "waketime_goal";

//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String EDIT_TIME = "edit_time";
        public static final String GOAL = "goal";
    }

//*********************************************************
// constructors
//*********************************************************

    private WakeTimeGoalContract() {/* No instantiation */}
}
