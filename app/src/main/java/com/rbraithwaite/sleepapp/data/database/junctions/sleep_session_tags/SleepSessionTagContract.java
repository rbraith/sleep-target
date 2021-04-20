package com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags;

public class SleepSessionTagContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "sleep_session_tags";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String TAG_ID = "tag_id";
        public static final String SESSION_ID = "session_id";
    }
    
//*********************************************************
// constructors
//*********************************************************

    private SleepSessionTagContract() {/* No instantiation */}
}
