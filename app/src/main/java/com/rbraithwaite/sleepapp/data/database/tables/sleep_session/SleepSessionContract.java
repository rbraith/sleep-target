package com.rbraithwaite.sleepapp.data.database.tables.sleep_session;

import android.provider.BaseColumns;

public class SleepSessionContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "sleep_sessions";

//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String DURATION = "duration";
    }

//*********************************************************
// constructors
//*********************************************************

    private SleepSessionContract() {/* No instantiation */}
}
