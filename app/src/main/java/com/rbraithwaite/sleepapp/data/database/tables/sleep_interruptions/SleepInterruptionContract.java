package com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions;

import android.provider.BaseColumns;

public class SleepInterruptionContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "interruptions";

//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String SESSION_ID = "session_id";
        public static final String START_TIME = "start_time";
        public static final String DURATION_MILLIS = "duration";
        public static final String REASON = "reason";
    }

//*********************************************************
// constructors
//*********************************************************

    private SleepInterruptionContract() {/* No instantiation */}
}
