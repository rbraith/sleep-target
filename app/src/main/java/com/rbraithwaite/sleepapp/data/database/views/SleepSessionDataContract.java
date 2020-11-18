package com.rbraithwaite.sleepapp.data.database.views;

public class SleepSessionDataContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String VIEW_NAME = "view_sleep_session_data";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String SESSION_ID = "session_id";
        public static final String START_TIME = "start_time";
        public static final String DURATION = "duration";
    }
    
//*********************************************************
// constructors
//*********************************************************

    private SleepSessionDataContract() {/* No instantiation */}
}
